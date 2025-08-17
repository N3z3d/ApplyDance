package com.applydance.service;

import com.applydance.model.GeneratedSlot;
import com.applydance.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Moteur de génération générique basé sur l'arbre de configuration.
 * Remplace l'ancien système hardcodé par un système flexible et configurable.
 * 
 * MISE À JOUR : S'abonne maintenant aux changements de configuration pour 
 * une synchronisation en temps réel.
 */
public class TreeGenerationEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(TreeGenerationEngine.class);
    
    private final Random random = new Random();
    private final TreeConfigurationService configService;
    private TreeNode cachedRootNode; // Cache local de l'arbre pour éviter les appels répétés
    
    public TreeGenerationEngine(TreeConfigurationService configService) {
        this.configService = configService;
        
        // Cache initial de l'arbre
        refreshTreeCache(configService.getRootNode());
        
        // S'abonner aux changements de configuration pour la synchronisation en temps réel
        configService.addChangeListener(this::refreshTreeCache);
        
        logger.info("TreeGenerationEngine initialisé avec synchronisation automatique");
    }
    
    /**
     * Met à jour le cache local de l'arbre quand la configuration change.
     * Appelé automatiquement par TreeConfigurationService.
     */
    private void refreshTreeCache(TreeNode newRootNode) {
        this.cachedRootNode = newRootNode;
        logger.info("🔄 Cache de l'arbre de génération mis à jour - {} nœuds disponibles", 
                    newRootNode != null ? countTotalNodes(newRootNode) : 0);
    }
    
    /**
     * Génère un nouveau slot selon l'arbre de configuration actuel.
     */
    public GeneratedSlot generateSlot() {
        logger.info("Génération d'un nouveau slot générique");
        
        if (cachedRootNode == null) {
            logger.error("Aucun arbre de configuration disponible en cache");
            return null;
        }
        
        // Debug : afficher la structure de l'arbre actuel
        logger.debug("Structure de l'arbre au moment de la génération :");
        logTreeStructure(cachedRootNode, "");
        
        // Parcourir l'arbre selon les probabilités jusqu'à un nœud feuille
        List<String> path = new ArrayList<>();
        TreeNode selectedNode = traverseTree(cachedRootNode, path);
        
        if (selectedNode == null) {
            logger.error("Impossible de parcourir l'arbre de configuration");
            return null;
        }
        
        // Construire le chemin de décision
        String decisionPath = String.join(" > ", path);
        
        GeneratedSlot slot = new GeneratedSlot(decisionPath, selectedNode.getId(), selectedNode.getName());
        
        // Ajouter des métadonnées utiles
        slot.addMetadata("nodeLevel", selectedNode.getLevel());
        slot.addMetadata("nodeColor", selectedNode.getColor());
        slot.addMetadata("finalPercentage", selectedNode.getPercentage());
        
        logger.info("Slot généré : {}", slot);
        return slot;
    }
    
    /**
     * Parcourt récursivement l'arbre selon les probabilités.
     */
    private TreeNode traverseTree(TreeNode currentNode, List<String> path) {
        path.add(currentNode.getName());
        
        // Si c'est un nœud feuille (pas d'enfants), on le retourne
        if (currentNode.getChildren() == null || currentNode.getChildren().isEmpty()) {
            logger.debug("Nœud feuille atteint : {}", currentNode.getName());
            return currentNode;
        }
        
        // Sinon, choisir un enfant selon les probabilités
        TreeNode selectedChild = selectChildByProbability(currentNode.getChildren());
        if (selectedChild == null) {
            logger.warn("Aucun enfant sélectionnable pour le nœud : {}", currentNode.getName());
            return currentNode; // Retourner le nœud actuel comme fallback
        }
        
        // Continuer récursivement
        return traverseTree(selectedChild, path);
    }
    
    /**
     * Sélectionne un enfant selon sa probabilité.
     */
    private TreeNode selectChildByProbability(List<TreeNode> children) {
        if (children.isEmpty()) {
            return null;
        }
        
        // Calculer la somme totale des pourcentages (normalisation)
        double totalPercentage = children.stream()
                .mapToDouble(TreeNode::getPercentage)
                .sum();
        
        if (totalPercentage <= 0) {
            // Si aucun pourcentage valide, sélection aléatoire uniforme
            logger.debug("Aucun pourcentage valide, sélection aléatoire uniforme");
            return children.get(random.nextInt(children.size()));
        }
        
        // Générer un nombre aléatoire entre 0 et la somme totale
        double randomValue = random.nextDouble() * totalPercentage;
        double cumulativePercentage = 0.0;
        
        // Sélectionner selon la probabilité cumulative
        for (TreeNode child : children) {
            cumulativePercentage += child.getPercentage();
            if (randomValue <= cumulativePercentage) {
                logger.debug("Enfant sélectionné : {} ({}%)", child.getName(), child.getPercentage());
                return child;
            }
        }
        
        // Fallback : retourner le dernier enfant
        TreeNode fallback = children.get(children.size() - 1);
        logger.debug("Fallback : enfant sélectionné : {}", fallback.getName());
        return fallback;
    }
    
    /**
     * Génère plusieurs slots d'un coup.
     */
    public List<GeneratedSlot> generateMultipleSlots(int count) {
        List<GeneratedSlot> slots = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            GeneratedSlot slot = generateSlot();
            if (slot != null) {
                slots.add(slot);
            }
        }
        
        logger.info("Générés {} slots sur {} demandés", slots.size(), count);
        return slots;
    }
    
    /**
     * Retourne les statistiques de l'arbre de configuration.
     */
    public String getTreeStatistics() {
        TreeNode rootNode = configService.getRootNode();
        if (rootNode == null) {
            return "Aucun arbre de configuration disponible.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("📊 STATISTIQUES DE L'ARBRE DE CONFIGURATION\n");
        sb.append("==========================================\n\n");
        
        int totalNodes = countNodes(rootNode);
        int leafNodes = countLeafNodes(rootNode);
        int maxDepth = calculateMaxDepth(rootNode, 0);
        
        sb.append(String.format("🌳 Nœuds totaux : %d\n", totalNodes));
        sb.append(String.format("🍃 Nœuds feuilles : %d\n", leafNodes));
        sb.append(String.format("📏 Profondeur max : %d\n\n", maxDepth));
        
        sb.append("🔍 STRUCTURE DE L'ARBRE\n");
        appendTreeStructure(rootNode, sb, "", true);
        
        return sb.toString();
    }
    
    /**
     * Log la structure de l'arbre pour debug
     */
    private void logTreeStructure(TreeNode node, String prefix) {
        if (node == null) return;
        
        logger.debug("{}[{}] {} ({}%)", prefix, node.getId(), node.getName(), node.getPercentage());
        
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            for (TreeNode child : node.getChildren()) {
                logTreeStructure(child, prefix + "  ");
            }
        }
    }
    
    /**
     * Compte le nombre total de nœuds dans l'arbre.
     */
    private int countNodes(TreeNode node) {
        if (node == null) return 0;
        
        int count = 1; // Le nœud actuel
        if (node.getChildren() != null) {
            for (TreeNode child : node.getChildren()) {
                count += countNodes(child);
            }
        }
        return count;
    }
    
    /**
     * Compte le nombre de nœuds feuilles dans l'arbre.
     */
    private int countLeafNodes(TreeNode node) {
        if (node == null) return 0;
        
        if (node.getChildren() == null || node.getChildren().isEmpty()) {
            return 1; // C'est une feuille
        }
        
        int count = 0;
        for (TreeNode child : node.getChildren()) {
            count += countLeafNodes(child);
        }
        return count;
    }
    
    /**
     * Calcule la profondeur maximale de l'arbre.
     */
    private int calculateMaxDepth(TreeNode node, int currentDepth) {
        if (node == null) return currentDepth;
        
        if (node.getChildren() == null || node.getChildren().isEmpty()) {
            return currentDepth;
        }
        
        int maxDepth = currentDepth;
        for (TreeNode child : node.getChildren()) {
            int childDepth = calculateMaxDepth(child, currentDepth + 1);
            maxDepth = Math.max(maxDepth, childDepth);
        }
        return maxDepth;
    }
    
    /**
     * Ajoute la structure de l'arbre au StringBuilder de façon formatée.
     */
    private void appendTreeStructure(TreeNode node, StringBuilder sb, String prefix, boolean isLast) {
        if (node == null) return;
        
        sb.append(prefix);
        sb.append(isLast ? "└── " : "├── ");
        sb.append(String.format("%s (%.1f%%)\n", node.getName(), node.getPercentage()));
        
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            for (int i = 0; i < node.getChildren().size(); i++) {
                boolean isLastChild = (i == node.getChildren().size() - 1);
                String newPrefix = prefix + (isLast ? "    " : "│   ");
                appendTreeStructure(node.getChildren().get(i), sb, newPrefix, isLastChild);
            }
        }
    }
    
    /**
     * Compte le nombre total de nœuds dans l'arbre (branches + feuilles).
     */
    private int countTotalNodes(TreeNode node) {
        if (node == null) return 0;
        
        int count = 1; // Compter le nœud actuel
        
        if (node.getChildren() != null) {
            for (TreeNode child : node.getChildren()) {
                count += countTotalNodes(child);
            }
        }
        
        return count;
    }
} 