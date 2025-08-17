# DEVBOOK - ApplyDance

## Guide de Développement Technique v1.2.0

---

## Vue d'Ensemble

ApplyDance est un générateur intelligent de slots de candidature appliquant des règles métier probabilistes pour optimiser la stratégie de recherche d'emploi. Depuis la v1.2.0, l'interface propose une **gestion avancée de la sélection multiple dans l'historique** (cases à cocher, actions groupées, compteur dynamique).

### Technologies
- **Java 11+** avec Maven
- **Jackson** pour JSON
- **OpenCSV** pour export CSV  
- **Logback** pour logging
- **Architecture MVC** adaptée

## Architecture

### Structure des Packages
```
com.applydance/
├── model/                  # Entités métier
│   ├── CandidatureSlot.java
│   └── TypePoste.java
├── service/                # Services applicatifs
│   ├── CandidatureRulesEngine.java
│   ├── SlotHistoryService.java
│   ├── StatisticsService.java
│   └── ExportService.java
└── ApplyDanceApplication.java  # Interface console
```

### Flux de Données
```
User Input → Rules Engine → Slot Generation → Persistence → Statistics
                                        ↓
                                   Export (CSV/JSON)
```

## Classes Principales

### CandidatureSlot
Entité principale représentant un slot de candidature.

```java
public class CandidatureSlot {
    private Long id;
    private LocalDateTime generatedAt;
    private TypePoste typePoste;        // VIE_ETRANGER | POSTE_FRANCE
    private String organisation;        // Business France | Spontané
    private String ville;               // Pour postes France
    private String canal;               // Indeed, LinkedIn, etc.
    private boolean isRouenPeripherie;  // Auto-calculé
}
```

### CandidatureRulesEngine
Cœur applicatif implémentant les règles métier.

**Algorithme :**
1. **Type poste** : 50% VIE / 50% France
2. **VIE** : Alternance Business France ↔ Spontané
3. **France** : 80% Rouen+périphérie / 20% autres villes
4. **Canal** : 1/7 Indeed / 6/7 autres canaux

```java
public CandidatureSlot generateSlot() {
    TypePoste type = random.nextBoolean() ? VIE_ETRANGER : POSTE_FRANCE;
    
    if (type == VIE_ETRANGER) {
        // Alternance stricte Business France / Spontané
        organisation = lastVieWasBusinessFrance ? "Spontané" : "Business France";
        lastVieWasBusinessFrance = !lastVieWasBusinessFrance;
    } else {
        // 80% Rouen+périphérie, 20% autres villes
        ville = random.nextDouble() < 0.8 ? 
            selectFromRouenList() : selectFromAutresVilles();
        // Canal : 1/7 Indeed, 6/7 autres
        canal = random.nextDouble() < (1.0/7.0) ? "Indeed" : selectFromAutres();
    }
}
```

### SlotHistoryService
Gestion de la persistence JSON avec auto-sauvegarde.

**Fichier** : `data/candidature_history.json`

```java
public CandidatureSlot saveSlot(CandidatureSlot slot) {
    slot.setId(nextId.getAndIncrement());
    slots.add(slot);
    saveHistory(); // Auto-save immédiate
    return slot;
}
```

## Règles Métier

### Distribution Probabiliste

| Étape | Condition | Probabilité |
|-------|-----------|-------------|
| 1 | Type poste | 50% VIE / 50% France |
| 2a | VIE → Organisation | Alternance Business France ↔ Spontané |
| 2b | France → Ville | 80% Rouen+périphérie / 20% autres |
| 3 | France → Canal | 1/7 Indeed / 6/7 autres |

### Listes Prédéfinies

**Rouen+Périphérie :**
- Rouen, Le Havre, Dieppe, Fécamp, Elbeuf
- Barentin, Canteleu, Mont-Saint-Aignan, etc.

**Autres Villes :**
- Paris, Lyon, Marseille, Toulouse, Nice
- Nantes, Strasbourg, Montpellier, Bordeaux, etc.

**Canaux :**
- LinkedIn, Indeed, Welcome to the Jungle
- LeBonCoin, Pôle Emploi, JobTeaser, Monster

## Persistence & Storage

### Structure Fichiers
```
applydance/
├── data/candidature_history.json      # Historique principal
├── exports/candidatures_*.csv         # Exports CSV
├── exports/candidatures_*.json        # Exports JSON
└── logs/applydance.log                 # Logs applicatifs
```

### Format JSON
```json
[
  {
    "id": 1,
    "generatedAt": "2024-12-15 14:30:22",
    "typePoste": "VIE_ETRANGER",
    "organisation": "Business France",
    "ville": null,
    "canal": null,
    "rouenPeripherie": false
  }
]
```

## Configuration

### Maven (pom.xml)
```xml
<properties>
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
</properties>

<dependencies>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.15.2</version>
    </dependency>
    <dependency>
        <groupId>com.opencsv</groupId>
        <artifactId>opencsv</artifactId>
        <version>5.8</version>
    </dependency>
</dependencies>
```

### Logging (logback.xml)
```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <pattern>%d{HH:mm:ss.SSS} %-5level %logger{36} - %msg%n</pattern>
    </appender>
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>logs/applydance.log</file>
    </appender>
    <logger name="com.applydance" level="INFO"/>
</configuration>
```

## Build & Déploiement

### Commandes
```bash
# Compilation
mvn clean compile

# Package JAR exécutable
mvn clean package

# Exécution
mvn exec:java
# ou
java -jar target/candidature-slot-generator-1.0.0.jar
```

### Distribution
```
applydance-1.0.0/
├── bin/applydance.jar
├── start.sh / start.bat
├── data/ (vide)
├── exports/ (vide)
└── README.md
```

## Interface Console

### Menu Principal
```
=== MENU ===
1. Générer un slot
2. Voir l'historique  
3. Statistiques
4. Exporter
0. Quitter
```

### Commandes Disponibles
- **Génération** : Nouveau slot selon règles métier
- **Historique** : Liste chronologique des slots
- **Statistiques** : Répartition détaillée avec pourcentages  
- **Export** : CSV/JSON avec horodatage

## Tests & Qualité

### À Implémenter (v1.1.0)
- Tests unitaires des règles métier
- Tests de performance (génération en lot)
- Validation de la distribution probabiliste
- Tests d'intégration persistence

### Couverture Cible
- **Services** : 90%+ couverture
- **Règles métier** : Tests exhaustifs
- **Persistence** : Round-trip JSON

## Troubleshooting

### Problèmes Fréquents

**OutOfMemoryError**
```bash
# Solution : augmenter heap
java -Xmx1g -jar applydance.jar
```

**Fichier JSON Corrompu**
```bash
# Backup et reset
cp data/candidature_history.json data/backup.json
echo "[]" > data/candidature_history.json
```

**Encodage UTF-8**
```bash
# Forcer encoding
java -Dfile.encoding=UTF-8 -jar applydance.jar
```

## Roadmap Technique

### v1.1.0 - Interface Graphique
- Migration vers JavaFX/Swing
- Graphiques statistiques
- Export automatique

### v1.2.0 - Configuration Dynamique  
- Règles configurables via JSON
- Interface de modification
- Profils de génération

### v1.3.0 - Base de Données
- Migration vers H2/PostgreSQL
- API REST
- Interface web

## Métriques Performance

### Benchmarks Actuels
- **Génération 1 slot** : ~2ms
- **Génération 1000 slots** : ~1.2s  
- **Mémoire 1000 slots** : ~52MB
- **Fichier JSON 1000 slots** : ~180KB

### Objectifs v1.1
- Génération < 100ms pour 1000 slots
- Support 50k+ slots historique
- Interface graphique < 2s startup

---

*Version : 1.0.0 | Décembre 2024* 