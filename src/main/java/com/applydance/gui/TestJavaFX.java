package com.applydance.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Test simple de JavaFX pour diagnostiquer les problèmes
 */
public class TestJavaFX extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("✅ JavaFX fonctionne !");
        Button button = new Button("🎯 Test ApplyDance");
        
        button.setOnAction(e -> {
            label.setText("✅ Bouton cliqué ! ApplyDance prêt.");
        });
        
        VBox root = new VBox(10, label, button);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");
        
        Scene scene = new Scene(root, 300, 150);
        
        primaryStage.setTitle("Test JavaFX - ApplyDance");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        System.out.println("🚀 Test JavaFX en cours...");
        launch(args);
    }
} 