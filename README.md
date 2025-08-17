# ApplyDance v1.1.0 - Générateur de Slots de Candidature

Générateur intelligent de stratégies de candidature basé sur des règles métier probabilistes.

## 🆕 Version v1.1.0 - Interface Graphique JavaFX Exclusive

### 🖥️ Interface Graphique Moderne
- **Application JavaFX exclusive** : Interface moderne et intuitive uniquement
- **Dashboard interactif** : Génération, historique et statistiques en temps réel
- **Design professionnel** : Thème sombre avec palette de couleurs Nordic
- **Graphiques avancés** : Camemberts et métriques visuelles
- **Table d'historique** : Tri, recherche et visualisation des slots
- **Export intégré** : CSV et JSON avec interface graphique

### 🎯 Simplicité d'Utilisation
- **Interface unique** : Plus de choix, lancement direct en mode graphique
- **Démarrage simplifié** : Un seul script `start.bat`
- **Expérience optimisée** : Focus 100% sur l'interface graphique

## Installation et Exécution

```bash
# Compiler le projet
mvn clean package

# Lancer l'interface graphique JavaFX (uniquement)
./start.bat        # Windows
./start.sh         # Linux/Mac

# Ou directement via JAR
java -jar target/candidature-slot-generator-1.0.0.jar
```

## Interface Graphique - Fonctionnalités

### 🎲 Onglet Génération
- Boutons "Générer 1 Slot" et "Générer 10 Slots"
- Affichage en temps réel du dernier slot généré
- Zone de détails avec description complète

### 📖 Onglet Historique
- Table interactive avec tous les slots générés
- Colonnes : ID, Type, Organisation, Ville, Canal, Date
- Tri et sélection multiple

### 📊 Onglet Statistiques
- **Métriques rapides** : Total slots, VIE %, France %
- **Graphique circulaire** : Répartition types de postes
- **Statistiques détaillées** : Texte formaté complet

### 📄 Export
- Boutons CSV et JSON intégrés
- Notification de succès avec chemin de fichier
- Horodatage automatique des exports

## Architecture Technique

```
com.applydance/
├── gui/                         # Interface JavaFX
│   ├── ApplyDanceGUI.java          # Application JavaFX principale
│   ├── MainController.java        # Contrôleur FXML
│   └── TestJavaFX.java            # Test de compatibilité JavaFX
├── model/                       # CandidatureSlot, TypePoste
├── service/                     # Rules Engine, History, Stats, Export
└── ApplyDanceApplication.java   # Point d'entrée unique

resources/
└── fxml/
    └── main.fxml               # Interface utilisateur FXML
```

## Règles Métier (Inchangées)

| Étape | Probabilité |
|-------|-------------|
| Type poste | 50% VIE / 50% France |
| VIE Organisation | Alternance Business France ↔ Spontané |
| France Ville | 80% Rouen+périphérie / 20% autres |
| France Canal | 1/7 Indeed / 6/7 autres |

## Technologies

- **Interface** : JavaFX 17, FXML, CSS
- **Backend** : Java 11+, Maven, Jackson, OpenCSV, Logback
- **Architecture** : MVC JavaFX, Services métier, Persistence JSON
- **Packaging** : JAR exécutable avec toutes dépendances incluses

## Documentation

- [TODO.md](TODO.md) : Roadmap et fonctionnalités futures
- [DEVBOOK.md](DEVBOOK.md) : Guide technique développeurs

---

**Version 1.1.0** - Interface Graphique JavaFX - Juin 2025  
**Version 1.0.0** - Interface Console - Décembre 2024 