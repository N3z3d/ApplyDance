@echo off
title ApplyDance v1.1.0 - Interface Graphique JavaFX
cls

echo ========================================================
echo    🎯 ApplyDance v1.1.0 - Interface Graphique JavaFX
echo ========================================================
echo.

:: Vérifier que Java est installé
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ ERREUR: Java n'est pas installé ou accessible.
    echo 💡 Veuillez installer Java 11+ depuis https://adoptium.net/
    echo    ou OpenJDK avec JavaFX depuis https://bell-sw.com/pages/downloads/
    echo.
    pause
    exit /b 1
)

:: Vérifier que le JAR existe
if not exist "target\candidature-slot-generator-1.0.0.jar" (
    echo ❌ ERREUR: Le fichier JAR n'existe pas.
    echo 🔧 Veuillez compiler le projet avec: mvn clean package
    echo.
    pause
    exit /b 1
)

echo 🚀 Démarrage de l'interface graphique JavaFX...
echo.

:: Démarrer l'interface graphique (JAR contient toutes les dépendances)
java -Xmx512m -Dfile.encoding=UTF-8 -jar target\candidature-slot-generator-1.0.0.jar

echo.
echo ✅ Interface graphique fermée.
pause 