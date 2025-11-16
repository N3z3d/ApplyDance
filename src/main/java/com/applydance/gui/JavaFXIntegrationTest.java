package com.applydance.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test d'intégration complet JavaFX pour vérifier tous les composants UI
 */
public class JavaFXIntegrationTest extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(JavaFXIntegrationTest.class);
    
    @Override
    public void start(Stage primaryStage) {
        logger.info("Début du test d'intégration JavaFX");
        
        try {
            // Test 1: Création du contrôleur principal
            MainController controller = new MainController();
            logger.info("✅ MainController créé avec succès");
            
            // Test 2: Création de l'interface principale
            var mainInterface = controller.createMainInterface();
            logger.info("✅ Interface principale créée avec succès");
            
            // Test 3: Création de la scène
            Scene scene = new Scene(mainInterface, 1200, 800);
            logger.info("✅ Scène JavaFX créée avec succès");
            
            // Test 4: Configuration de la fenêtre
            primaryStage.setTitle("🧪 Test d'Intégration ApplyDance JavaFX");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            logger.info("✅ Fenêtre configurée avec succès");
            
            // Test 5: Affichage
            primaryStage.show();
            logger.info("✅ Fenêtre affichée avec succès");
            
            // Test 6: Test de threading JavaFX
            Platform.runLater(() -> {
                logger.info("✅ Platform.runLater fonctionne correctement");
            });
            
            // Test 7: Fermeture automatique après 3 secondes pour les tests automatisés
            Platform.runLater(() -> {
                try {
                    Thread.sleep(3000);
                    logger.info("✅ Test d'intégration terminé avec succès");
                    Platform.exit();
                } catch (InterruptedException e) {
                    logger.warn("Interruption pendant le test", e);
                    Thread.currentThread().interrupt();
                }
            });
            
        } catch (Exception e) {
            logger.error("❌ Erreur pendant le test d'intégration", e);
            throw new RuntimeException("Test d'intégration échoué", e);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("🚀 Démarrage du test d'intégration JavaFX...");
        launch(args);
    }
}