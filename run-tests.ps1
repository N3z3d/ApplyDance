# 🧪 Script de Test ApplyDance
# Automatise la compilation et le lancement pour les tests

Write-Host "🚀 ApplyDance - Script de Test Automatisé" -ForegroundColor Cyan
Write-Host "=======================================" -ForegroundColor Cyan

# Vérifier si on est dans le bon répertoire
if (-not (Test-Path "pom.xml")) {
    Write-Host "❌ Erreur: pom.xml non trouvé. Exécutez ce script depuis le répertoire ApplyDance" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "📂 Répertoire de travail: $(Get-Location)" -ForegroundColor Green

# Nettoyer et compiler
Write-Host ""
Write-Host "🔨 Compilation en cours..." -ForegroundColor Yellow
$compileResult = mvn clean compile 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Compilation réussie" -ForegroundColor Green
} else {
    Write-Host "❌ Erreur de compilation:" -ForegroundColor Red
    Write-Host $compileResult -ForegroundColor Red
    exit 1
}

# Vérifier les fichiers de test
Write-Host ""
Write-Host "📋 Vérification des fichiers de test..." -ForegroundColor Yellow
if (Test-Path "TEST_SEQUENCE.md") {
    Write-Host "✅ TEST_SEQUENCE.md disponible" -ForegroundColor Green
} else {
    Write-Host "⚠️  TEST_SEQUENCE.md non trouvé" -ForegroundColor Yellow
}

# Afficher les logs précédents s'ils existent
if (Test-Path "logs/applydance.log") {
    $logSize = (Get-Item "logs/applydance.log").Length
    Write-Host "📄 Fichier de logs existant: $($logSize) bytes" -ForegroundColor Cyan
}

# Instructions pour l'utilisateur
Write-Host ""
Write-Host "🎯 INSTRUCTIONS DE TEST:" -ForegroundColor Magenta
Write-Host "1. L'application va se lancer automatiquement" -ForegroundColor White
Write-Host "2. Suivez le guide TEST_SEQUENCE.md étape par étape" -ForegroundColor White
Write-Host "3. Testez PARTICULIÈREMENT la persistance (ajout/relance)" -ForegroundColor White
Write-Host "4. Vérifiez l'interface moderne de génération" -ForegroundColor White
Write-Host "5. Confirmez que le BarChart est horizontal et beau" -ForegroundColor White
Write-Host ""
Write-Host "🔑 TESTS CRITIQUES:" -ForegroundColor Red
Write-Host "   - Ajoutez un nœud → Fermez → Relancez → Vérifiez qu'il est toujours là" -ForegroundColor Yellow
Write-Host "   - Générez des slots → Vérifiez que les nouveaux nœuds apparaissent" -ForegroundColor Yellow
Write-Host ""

# Demander confirmation
$response = Read-Host "Prêt à lancer l'application ? (O/n)"
if ($response -eq "n" -or $response -eq "N") {
    Write-Host "❌ Test annulé par l'utilisateur" -ForegroundColor Red
    exit 0
}

# Lancer l'application
Write-Host ""
Write-Host "🚀 Lancement de l'application..." -ForegroundColor Green
Write-Host "   → Surveillez les logs pour détecter d'éventuelles erreurs" -ForegroundColor Cyan
Write-Host "   → Fermez l'application quand vous avez terminé les tests" -ForegroundColor Cyan
Write-Host ""

try {
    mvn javafx:run
} catch {
    Write-Host "❌ Erreur lors du lancement:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "✅ Test terminé. Vérifiez les résultats dans TEST_SEQUENCE.md" -ForegroundColor Green 