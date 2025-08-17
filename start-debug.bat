@echo off
setlocal enabledelayedexpansion

:: Configuration Debug
set "APP_NAME=ApplyDance"
set "APP_VERSION=v1.1.0"
set "JAR_NAME=candidature-slot-generator-1.0.0.jar"
set "JAR_PATH=target\%JAR_NAME%"
set "MAIN_CLASS=com.applydance.ApplyDanceApplication"
set "MIN_JAVA_VERSION=11"
set "MAX_HEAP=2048m"

:: Titre de la fenêtre
title %APP_NAME% %APP_VERSION% - MODE DEBUG

:: Couleurs (si supportées)
if exist "%SystemRoot%\System32\color.exe" (
    color 0E
)

cls
echo.
echo ╔══════════════════════════════════════════════════════════════════════════════╗
echo ║                    🐛 %APP_NAME% %APP_VERSION% - MODE DEBUG                    ║
echo ║                    Générateur de Slots de Candidature                        ║
echo ║                        Interface Graphique JavaFX                           ║
echo ╚══════════════════════════════════════════════════════════════════════════════╝
echo.

:: =============================================================================
:: VÉRIFICATIONS PRÉALABLES
:: =============================================================================

echo [1/5] 🔍 Vérification de l'environnement Java...

:: Vérifier que Java est installé et accessible
java -version >nul 2>&1
if errorlevel 1 (
    echo    ❌ ERREUR: Java n'est pas installé ou accessible dans le PATH
    echo.
    echo    💡 SOLUTIONS:
    echo       • Installez Java %MIN_JAVA_VERSION%+ depuis https://adoptium.net/
    echo       • Ou OpenJDK avec JavaFX depuis https://bell-sw.com/pages/downloads/
    echo       • Assurez-vous que JAVA_HOME est configuré correctement
    echo.
    pause
    exit /b 1
)

:: Vérifier la version de Java
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set "java_version=%%g"
    set "java_version=!java_version:"=!"
    goto :check_version
)
:check_version

:: Extraire le numéro de version principal
for /f "tokens=1 delims=." %%a in ("!java_version!") do set "major_version=%%a"
for /f "tokens=1 delims=_" %%a in ("!major_version!") do set "major_version=%%a"

if !major_version! lss %MIN_JAVA_VERSION% (
    echo    ❌ ERREUR: Version Java insuffisante (détectée: !java_version!)
    echo       Version minimale requise: Java %MIN_JAVA_VERSION%+
    echo.
    pause
    exit /b 1
)

echo    ✅ Java !java_version! détecté (version compatible)

:: =============================================================================
:: VÉRIFICATION DU JAR
:: =============================================================================

echo [2/5] 📦 Vérification du fichier JAR...

if not exist "%JAR_PATH%" (
    echo    ❌ ERREUR: Le fichier JAR n'existe pas: %JAR_PATH%
    echo.
    echo    🔧 COMPILATION REQUISE:
    echo       Exécutez la commande suivante pour compiler le projet:
    echo.
    echo       mvn clean package
    echo.
    pause
    exit /b 1
)

:: Vérifier la taille du JAR
for %%A in ("%JAR_PATH%") do set "jar_size=%%~zA"
echo    ✅ JAR trouvé (!jar_size! octets)

:: =============================================================================
:: VÉRIFICATION DES DOSSERS DE DONNÉES
:: =============================================================================

echo [3/5] 📁 Vérification des dossiers de données...

if not exist "data\" (
    echo    ⚠️  Dossier 'data/' manquant - création...
    mkdir "data"
)

if not exist "logs\" (
    echo    ⚠️  Dossier 'logs/' manquant - création...
    mkdir "logs"
)

if not exist "exports\" (
    echo    ⚠️  Dossier 'exports/' manquant - création...
    mkdir "exports"
)

echo    ✅ Dossiers de données vérifiés

:: =============================================================================
:: CONFIGURATION DU MODE DEBUG
:: =============================================================================

echo [4/5] 🐛 Configuration du mode debug...

:: Paramètres JVM pour le debug
set "JVM_OPTS=-Xmx%MAX_HEAP% -Xms512m -Dfile.encoding=UTF-8 -Djava.awt.headless=false"

:: Paramètres de debug
set "DEBUG_OPTS=-Dlogback.configurationFile=logback.xml -Dlogging.level.com.applydance=DEBUG -Dlogging.level.root=INFO"

:: Paramètres JavaFX pour le debug
set "JAVAFX_OPTS=--add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED --add-opens=javafx.controls/javafx.scene.control=ALL-UNNAMED"

:: Paramètres de performance pour le debug
set "PERF_OPTS=-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+PrintGC -XX:+PrintGCTimeStamps"

echo    ✅ Mode debug configuré

:: =============================================================================
:: LANCEMENT EN MODE DEBUG
:: =============================================================================

echo [5/5] 🚀 Lancement en mode debug...
echo.

echo    📋 Paramètres de debug:
echo       • Mémoire max: %MAX_HEAP%
echo       • Logging: DEBUG pour com.applydance
echo       • GC: G1 avec logs détaillés
echo       • Encodage: UTF-8
echo       • Classe principale: %MAIN_CLASS%
echo.

echo    🎯 Démarrage en cours...
echo    ═══════════════════════════════════════════════════════════════════════════════
echo.

:: Lancement avec tous les paramètres de debug
java %JVM_OPTS% %DEBUG_OPTS% %JAVAFX_OPTS% %PERF_OPTS% -jar "%JAR_PATH%"

:: =============================================================================
:: GESTION DE LA FIN D'EXÉCUTION
:: =============================================================================

set "exit_code=%errorlevel%"

echo.
echo ═══════════════════════════════════════════════════════════════════════════════

if %exit_code% equ 0 (
    echo ✅ Application fermée normalement
) else (
    echo ❌ Application fermée avec le code d'erreur: %exit_code%
    echo.
    echo 💡 DIAGNOSTIC DEBUG:
    if %exit_code% equ 1 (
        echo    • Erreur de configuration ou de dépendances
    ) else if %exit_code% equ 2 (
        echo    • Erreur d'interface graphique JavaFX
    ) else if %exit_code% equ 3 (
        echo    • Erreur de mémoire insuffisante
    ) else if %exit_code% equ 139 (
        echo    • Segmentation fault - problème de mémoire native
    ) else if %exit_code% equ 134 (
        echo    • Abort - problème de JVM
    ) else (
        echo    • Erreur inconnue (code %exit_code%)
    )
    echo.
    echo 🔧 SOLUTIONS DEBUG:
    echo    • Vérifiez les logs détaillés dans 'logs/applydance.log'
    echo    • Consultez les logs GC dans la console
    echo    • Vérifiez la mémoire disponible
    echo    • Redémarrez avec moins de mémoire si nécessaire
)

echo.
echo 📁 Fichiers de données: data/
echo 📊 Exports: exports/
echo 📝 Logs détaillés: logs/applydance.log
echo 🐛 Mode debug activé
echo.

pause
exit /b %exit_code% 