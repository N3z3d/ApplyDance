# 🎯 ApplyDance - Scripts de Lancement Windows

Ce document décrit les scripts batch Windows pour le projet ApplyDance.

## 📁 Scripts Disponibles

### 🚀 `start.bat` - Lancement Standard
Script principal pour lancer l'application en mode production.

**Fonctionnalités :**
- ✅ Vérification automatique de Java 11+
- ✅ Validation du fichier JAR
- ✅ Paramètres JVM optimisés (1GB RAM max)
- ✅ Gestion d'erreur robuste
- ✅ Interface utilisateur professionnelle

**Utilisation :**
```bash
# Double-cliquez sur start.bat
# Ou en ligne de commande :
start.bat
```

### 🐛 `start-debug.bat` - Mode Debug
Script pour le développement avec options de debug avancées.

**Fonctionnalités :**
- ✅ Toutes les fonctionnalités de start.bat
- ✅ Logging DEBUG pour com.applydance
- ✅ Logs GC détaillés
- ✅ Mémoire étendue (2GB max)
- ✅ Création automatique des dossiers manquants
- ✅ Diagnostic d'erreur avancé

**Utilisation :**
```bash
# Double-cliquez sur start-debug.bat
# Ou en ligne de commande :
start-debug.bat
```

### 🔨 `build.bat` - Compilation Maven
Script pour compiler le projet avec Maven.

**Fonctionnalités :**
- ✅ Vérification de Java et Maven
- ✅ Support du wrapper Maven (mvnw)
- ✅ Nettoyage automatique
- ✅ Compilation et packaging
- ✅ Option de lancement automatique

**Utilisation :**
```bash
# Double-cliquez sur build.bat
# Ou en ligne de commande :
build.bat
```

## 🔧 Prérequis

### Java
- **Version minimale :** Java 11
- **Recommandé :** Java 17 LTS
- **Sources :**
  - [Eclipse Temurin](https://adoptium.net/)
  - [BellSoft Liberica](https://bell-sw.com/pages/downloads/) (avec JavaFX)

### Maven (optionnel)
- **Version :** 3.6+
- **Source :** [Apache Maven](https://maven.apache.org/download.cgi)
- **Alternative :** Wrapper Maven (mvnw) inclus dans le projet

## 📋 Workflow Recommandé

### Première Utilisation
1. **Compiler le projet :**
   ```bash
   build.bat
   ```

2. **Lancer l'application :**
   ```bash
   start.bat
   ```

### Développement
1. **Compiler :**
   ```bash
   build.bat
   ```

2. **Lancer en mode debug :**
   ```bash
   start-debug.bat
   ```

### Utilisation Quotidienne
```bash
start.bat
```

## 🚨 Gestion des Erreurs

### Codes d'Erreur
- **1 :** Erreur de configuration ou dépendances
- **2 :** Erreur d'interface graphique JavaFX
- **3 :** Erreur de mémoire insuffisante
- **139 :** Segmentation fault (mémoire native)
- **134 :** Abort (problème JVM)

### Solutions Courantes

#### Java non trouvé
```bash
# Vérifier l'installation
java -version

# Configurer JAVA_HOME si nécessaire
set JAVA_HOME=C:\Program Files\Java\jdk-17
```

#### JAR manquant
```bash
# Compiler le projet
build.bat
```

#### Erreur JavaFX
```bash
# Utiliser une distribution avec JavaFX
# Ou vérifier les dépendances dans le JAR
```

#### Mémoire insuffisante
```bash
# Réduire la mémoire dans le script
set "MAX_HEAP=512m"
```

## 📊 Paramètres de Performance

### Mode Standard (start.bat)
- **Mémoire max :** 1GB
- **Mémoire min :** 256MB
- **GC :** Par défaut
- **Logging :** INFO

### Mode Debug (start-debug.bat)
- **Mémoire max :** 2GB
- **Mémoire min :** 512MB
- **GC :** G1 avec logs
- **Logging :** DEBUG

## 🔍 Diagnostic

### Logs
- **Standard :** `logs/applydance.log`
- **Debug :** Console + logs détaillés
- **GC :** Affiché en mode debug

### Dossiers Importants
- **Données :** `data/`
- **Exports :** `exports/`
- **Logs :** `logs/`
- **JAR :** `target/candidature-slot-generator-1.0.0.jar`

## 🎨 Personnalisation

### Modifier la Mémoire
```batch
# Dans start.bat ou start-debug.bat
set "MAX_HEAP=2048m"  # 2GB
```

### Ajouter des Options JVM
```batch
# Dans la section JVM_OPTS
set "JVM_OPTS=-Xmx%MAX_HEAP% -Xms256m -Dfile.encoding=UTF-8 -XX:+UseG1GC"
```

### Changer la Version Java
```batch
# Dans la section MIN_JAVA_VERSION
set "MIN_JAVA_VERSION=17"
```

## 📞 Support

### Problèmes Courants
1. **Java non trouvé :** Installer Java 11+
2. **JAR manquant :** Exécuter build.bat
3. **Erreur JavaFX :** Utiliser une distribution avec JavaFX
4. **Mémoire insuffisante :** Réduire MAX_HEAP

### Logs de Debug
En cas de problème, utilisez `start-debug.bat` et consultez :
- Les logs dans la console
- Le fichier `logs/applydance.log`
- Les messages d'erreur détaillés

---

**Version :** 1.1.0  
**Dernière mise à jour :** 2024  
**Compatibilité :** Windows 10/11 