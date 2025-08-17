@echo off
setlocal enabledelayedexpansion

:: Configuration
set "APP_NAME=ApplyDance"
set "APP_VERSION=v1.1.0"
set "JAR_NAME=candidature-slot-generator-1.0.0.jar"

:: Titre de la fenêtre
title %APP_NAME% %APP_VERSION% - Compilation Maven

:: Couleurs (si supportées)
if exist "%SystemRoot%\System32\color.exe" (
    color 0B
)

cls
echo.
echo ╔══════════════════════════════════════════════════════════════════════════════╗
echo ║                    🔨 %APP_NAME% %APP_VERSION% - COMPILATION                    ║
echo ║                    Générateur de Slots de Candidature                        ║
echo ║                        Interface Graphique JavaFX                           ║
echo ╚══════════════════════════════════════════════════════════════════════════════╝
echo.

:: =============================================================================
:: VÉRIFICATIONS PRÉALABLES
:: =============================================================================

echo [1/4] 🔍 Vérification de l'environnement...

:: Vérifier que Java est installé
java -version >nul 2>&1
if errorlevel 1 (
    echo    ❌ ERREUR: Java n'est pas installé ou accessible
    echo.
    echo    💡 SOLUTIONS:
    echo       • Installez Java 11+ depuis https://adoptium.net/
    echo       • Assurez-vous que JAVA_HOME est configuré correctement
    echo.
    pause
    exit /b 1
)

echo    ✅ Java détecté

:: Vérifier que Maven est installé
mvn -version >nul 2>&1
if errorlevel 1 (
    echo    ❌ ERREUR: Maven n'est pas installé ou accessible
    echo.
    echo    💡 SOLUTIONS:
    echo       • Installez Maven depuis https://maven.apache.org/download.cgi
    echo       • Ou utilisez le wrapper Maven: mvnw
    echo.
    echo    🔄 Tentative avec le wrapper Maven...
    if exist "mvnw.cmd" (
        set "MAVEN_CMD=mvnw.cmd"
        echo    ✅ Wrapper Maven trouvé
    ) else (
        echo    ❌ Wrapper Maven non trouvé
        pause
        exit /b 1
    )
) else (
    set "MAVEN_CMD=mvn"
    echo    ✅ Maven détecté
)

:: =============================================================================
:: NETTOYAGE
:: =============================================================================

echo [2/4] 🧹 Nettoyage du projet...

echo    📋 Suppression des fichiers de compilation précédents...
%MAVEN_CMD% clean

if errorlevel 1 (
    echo    ❌ ERREUR: Échec du nettoyage
    echo.
    pause
    exit /b 1
)

echo    ✅ Nettoyage terminé

:: =============================================================================
:: COMPILATION
:: =============================================================================

echo [3/4] 🔨 Compilation du projet...

echo    📋 Compilation avec Maven...
echo    ═══════════════════════════════════════════════════════════════════════════════
echo.

%MAVEN_CMD% compile

if errorlevel 1 (
    echo.
    echo    ❌ ERREUR: Échec de la compilation
    echo.
    echo    💡 DIAGNOSTIC:
    echo       • Vérifiez les erreurs de compilation ci-dessus
    echo       • Assurez-vous que toutes les dépendances sont disponibles
    echo       • Vérifiez la syntaxe du code Java
    echo.
    pause
    exit /b 1
)

echo.
echo    ✅ Compilation terminée

:: =============================================================================
:: PACKAGING
:: =============================================================================

echo [4/4] 📦 Création du JAR exécutable...

echo    📋 Packaging avec toutes les dépendances...
echo    ═══════════════════════════════════════════════════════════════════════════════
echo.

%MAVEN_CMD% package -DskipTests

if errorlevel 1 (
    echo.
    echo    ❌ ERREUR: Échec du packaging
    echo.
    echo    💡 DIAGNOSTIC:
    echo       • Vérifiez les erreurs de packaging ci-dessus
    echo       • Assurez-vous que toutes les dépendances sont résolues
    echo       • Vérifiez la configuration du plugin shade
    echo.
    pause
    exit /b 1
)

:: Vérifier que le JAR a été créé
if not exist "target\%JAR_NAME%" (
    echo.
    echo    ❌ ERREUR: Le JAR n'a pas été créé
    echo.
    pause
    exit /b 1
)

:: Afficher les informations du JAR
for %%A in ("target\%JAR_NAME%") do set "jar_size=%%~zA"
echo.
echo    ✅ JAR créé avec succès (!jar_size! octets)

:: =============================================================================
:: RÉSULTAT FINAL
:: =============================================================================

echo.
echo ═══════════════════════════════════════════════════════════════════════════════
echo ✅ COMPILATION RÉUSSIE !
echo.
echo 📋 RÉSUMÉ:
echo    • JAR créé: target\%JAR_NAME%
echo    • Taille: !jar_size! octets
echo    • Prêt pour l'exécution
echo.
echo 🚀 PROCHAINES ÉTAPES:
echo    • Double-cliquez sur start.bat pour lancer l'application
echo    • Ou utilisez start-debug.bat pour le mode debug
echo.

:: Option pour lancer directement
set /p "launch=Lancer l'application maintenant ? (O/n): "
if /i not "!launch!"=="n" (
    echo.
    echo 🎯 Lancement de l'application...
    call start.bat
) else (
    echo.
    echo 📁 Vous pouvez maintenant utiliser start.bat pour lancer l'application
)

pause
exit /b 0 