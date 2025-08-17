package com.applydance.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Application JavaFX principale pour ApplyDance.
 * Interface graphique moderne remplaçant la console.
 */
public class ApplyDanceGUI extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(ApplyDanceGUI.class);
    
    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Démarrage de l'interface graphique ApplyDance");
            
            // Créer l'interface directement en Java (sans FXML)
            MainController controller = new MainController();
            Scene scene = new Scene(controller.createMainInterface(), 1200, 800);
            
            // Configuration de la fenêtre principale
            primaryStage.setTitle("🎯 ApplyDance v1.1.0 - Générateur de Slots de Candidature");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            
            // Gestion de la fermeture
            primaryStage.setOnCloseRequest(event -> {
                logger.info("Fermeture de l'application ApplyDance");
                System.exit(0);
            });
            
            primaryStage.show();
            
            logger.info("Interface graphique démarrée avec succès");
            
        } catch (Exception e) {
            logger.error("Erreur lors du chargement de l'interface graphique", e);
            throw new RuntimeException("Impossible de charger l'interface graphique", e);
        }
    }
    
    /**
     * Point d'entrée principal pour l'interface graphique
     */
    public static void main(String[] args) {
        // Lancer l'application JavaFX
        launch(args);
    }
    
    /**
     * Méthode utilitaire pour lancer l'interface graphique depuis d'autres classes
     */
    public static void launchGUI(String[] args) {
        main(args);
    }
} 