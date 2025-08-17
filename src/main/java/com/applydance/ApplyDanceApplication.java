package com.applydance;

import com.applydance.gui.ApplyDanceGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application principale ApplyDance - Générateur de slots de candidature.
 * Interface graphique JavaFX moderne pour gérer la génération et le suivi des candidatures.
 */
public class ApplyDanceApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(ApplyDanceApplication.class);
    
    /**
     * Point d'entrée principal de l'application - INTERFACE GRAPHIQUE UNIQUEMENT
     */
    public static void main(String[] args) {
        logger.info("🚀 Démarrage ApplyDance - Interface Graphique JavaFX");
        System.out.println("🎯 ApplyDance v1.1.0 - Lancement de l'interface graphique...");
        
        try {
            // Lancer directement l'interface graphique JavaFX
            ApplyDanceGUI.main(args);
        } catch (Exception e) {
            logger.error("❌ Erreur fatale lors du démarrage de l'interface graphique", e);
            System.err.println("❌ ERREUR : Impossible de démarrer l'interface graphique JavaFX");
            System.err.println("💡 Vérifiez que Java avec JavaFX est installé correctement");
            System.err.println("🔗 Plus d'infos : https://openjfx.io/");
            System.exit(1);
        }
    }


} 