#!/bin/bash

# ApplyDance - Générateur de Slots de Candidature v1.0.0
# Script de démarrage pour Unix/Linux/macOS

echo "========================================================"
echo "    🎯 ApplyDance v1.1.0 - Interface Graphique JavaFX"
echo "========================================================"
echo

# Vérifier que Java est installé
if ! command -v java &> /dev/null; then
    echo "❌ ERREUR: Java n'est pas installé ou accessible."
    echo "💡 Veuillez installer Java 11+ depuis https://adoptium.net/"
    echo "   ou OpenJDK avec JavaFX depuis https://bell-sw.com/pages/downloads/"
    exit 1
fi

# Vérifier que le JAR existe
if [ ! -f "target/candidature-slot-generator-1.0.0.jar" ]; then
    echo "❌ ERREUR: Le fichier JAR n'existe pas."
    echo "🔧 Veuillez compiler le projet avec: mvn clean package"
    exit 1
fi

echo "🚀 Démarrage de l'interface graphique JavaFX..."
echo

# Démarrer l'interface graphique (JAR contient toutes les dépendances)
java -Xmx512m -Dfile.encoding=UTF-8 -jar target/candidature-slot-generator-1.0.0.jar

echo
echo "✅ Interface graphique fermée." 