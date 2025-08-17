package com.applydance.service;

import com.applydance.model.TreeNode;
import com.applydance.model.TreeNodeDTO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Service de gestion de la configuration d'arbre générique avec sauvegarde automatique.
 * Remplace l'ancien système spécifique VIE/ville/job.
 */
public class TreeConfigurationService {
    
    private static final Logger logger = LoggerFactory.getLogger(TreeConfigurationService.class);
    private static final String CONFIG_DIR = "data";
    private static final String CONFIG_FILE = "tree_configuration.json";
    
    private final ObjectMapper objectMapper;
    private final Path configPath;
    private TreeNode rootNode;
    private final List<Consumer<TreeNode>> changeListeners;
    
    public TreeConfigurationService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        this.configPath = Paths.get(CONFIG_DIR, CONFIG_FILE);
        this.changeListeners = new ArrayList<>();
        
        // Créer le répertoire data s'il n'existe pas
        try {
            Files.createDirectories(configPath.getParent());
        } catch (IOException e) {
            logger.error("Erreur création répertoire configuration", e);
        }
        
        // Charger la configuration au démarrage
        loadConfiguration();
    }
    
    /**
     * Charge la configuration depuis le fichier ou crée une configuration par défaut
     */
    public void loadConfiguration() {
        try {
            File configFile = configPath.toFile();
            
            if (configFile.exists()) {
                logger.info("Chargement de la configuration depuis {}", configPath);
                
                // Charger le DTO depuis le fichier JSON
                TreeNodeDTO rootDTO = objectMapper.readValue(configFile, TreeNodeDTO.class);
                
                // Convertir le DTO en TreeNode avec reconstruction automatique des références parent
                rootNode = rootDTO.toTreeNode();
                
                logger.info("Configuration chargée avec succès");
            } else {
                logger.info("Aucune configuration trouvée, création de la configuration par défaut");
                createDefaultConfiguration();
                // Sauvegarder immédiatement la configuration par défaut
                saveConfiguration();
            }
            
            // Notifier les listeners
            notifyChangeListeners();
            
        } catch (IOException e) {
            logger.error("Erreur lors du chargement de la configuration", e);
            createDefaultConfiguration();
        }
    }
    
    /**
     * Sauvegarde automatique de la configuration
     */
    public void saveConfiguration() {
        try {
            // Créer le dossier s'il n'existe pas
            Files.createDirectories(configPath.getParent());
            
            // Convertir TreeNode en TreeNodeDTO pour éviter les références circulaires
            TreeNodeDTO rootDTO = TreeNodeDTO.fromTreeNode(rootNode);
            
            // Sauvegarder le DTO (sans références circulaires)
            objectMapper.writeValue(configPath.toFile(), rootDTO);
            logger.info("Configuration sauvegardée dans {}", configPath);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la sauvegarde de la configuration", e);
        }
    }
    
    /**
     * Crée une configuration par défaut générique
     */
    private void createDefaultConfiguration() {
        logger.info("Création de la configuration par défaut");
        
        rootNode = new TreeNode("root", "🌳 Arbre Principal", 100.0, "🌳");
        rootNode.setColor("#2E3440");
        
        // Branche A
        TreeNode brancheA = new TreeNode("brancheA", "📊 Branche A", 60.0, "📊");
        brancheA.setColor("#5E81AC");
        TreeNode subA1 = new TreeNode("subA1", "🔹 Sous-élément A1", 70.0, "🔹");
        subA1.setColor("#81A1C1");
        TreeNode subA2 = new TreeNode("subA2", "🔹 Sous-élément A2", 30.0, "🔹");
        subA2.setColor("#81A1C1");
        brancheA.addChild(subA1);
        brancheA.addChild(subA2);
        
        // Branche B
        TreeNode brancheB = new TreeNode("brancheB", "📈 Branche B", 40.0, "📈");
        brancheB.setColor("#A3BE8C");
        TreeNode subB1 = new TreeNode("subB1", "🔸 Sous-élément B1", 50.0, "🔸");
        subB1.setColor("#88C0D0");
        TreeNode subB2 = new TreeNode("subB2", "🔸 Sous-élément B2", 50.0, "🔸");
        subB2.setColor("#88C0D0");
        brancheB.addChild(subB1);
        brancheB.addChild(subB2);
        
        rootNode.addChild(brancheA);
        rootNode.addChild(brancheB);
        
        logger.info("Configuration par défaut créée avec couleurs");
    }
    
    /**
     * Ajoute un nœud enfant avec redistribution automatique à 100%
     */
    public void addChildNode(TreeNode parent, TreeNode child) {
        if (parent != null && child != null) {
            parent.addChild(child);
            
            // Redistribution automatique pour que la somme = 100%
            redistributeToHundredPercent(parent);
            
            // Sauvegarde automatique
            saveConfiguration();
            notifyChangeListeners();
            
            logger.info("Nœud ajouté : {} sous {} - Redistribution automatique effectuée", 
                       child.getLabel(), parent.getLabel());
        }
    }

    /**
     * Ajoute un nœud enfant SANS redistribution automatique
     * Utilisé pour les tests et situations spéciales où la redistribution manuelle est souhaitée
     */
    public void addChildNodeWithoutRedistribution(TreeNode parent, TreeNode child) {
        if (parent != null && child != null) {
            parent.addChild(child);
            
            // Sauvegarde automatique (sans redistribution)
            saveConfiguration();
            notifyChangeListeners();
            
            logger.info("Nœud ajouté : {} sous {} - SANS redistribution automatique", 
                       child.getLabel(), parent.getLabel());
        }
    }
    
    /**
     * Supprime un nœud avec redistribution automatique à 100%
     */
    public void removeNode(TreeNode nodeToRemove) {
        if (nodeToRemove != null && !nodeToRemove.isRoot()) {
            TreeNode parent = findParent(rootNode, nodeToRemove);
            if (parent != null) {
                parent.removeChild(nodeToRemove);
                
                // Redistribution automatique pour que la somme = 100%
                redistributeToHundredPercent(parent);
                
                // Sauvegarde automatique
                saveConfiguration();
                notifyChangeListeners();
                
                logger.info("Nœud supprimé : {} - Redistribution automatique effectuée", 
                           nodeToRemove.getLabel());
            }
        }
    }
    
    /**
     * Déplace un nœud (pour le glisser-déposer) avec redistribution automatique à 100%
     */
    public void moveNode(TreeNode nodeToMove, TreeNode newParent) {
        if (nodeToMove != null && newParent != null && !nodeToMove.isRoot()) {
            // Supprimer de l'ancien parent
            TreeNode oldParent = findParent(rootNode, nodeToMove);
            if (oldParent != null) {
                oldParent.removeChild(nodeToMove);
                // Redistribuer l'ancien parent
                redistributeToHundredPercent(oldParent);
            }
            
            // Ajouter au nouveau parent
            newParent.addChild(nodeToMove);
            
            // Ajuster le niveau (pas nécessaire car TreeNode n'a pas de level)
            // updateLevels(nodeToMove, newParent.getDepth() + 1);
            
            // Redistribuer le nouveau parent
            redistributeToHundredPercent(newParent);
            
            // Sauvegarde automatique
            saveConfiguration();
            notifyChangeListeners();
            
            logger.info("Nœud déplacé : {} de {} vers {} - Redistribution automatique effectuée", 
                       nodeToMove.getLabel(),
                       oldParent != null ? oldParent.getLabel() : "racine",
                       newParent.getLabel());
        }
    }
    
    /**
     * Met à jour un nœud existant avec redistribution automatique à 100%
     */
    public void updateNode(TreeNode node) {
        if (node != null) {
            // Si le nœud a des frères/sœurs, redistribuer pour que la somme = 100%
            // MAIS en préservant le pourcentage du nœud qui vient d'être modifié
            TreeNode parent = findParent(rootNode, node);
            if (parent != null) {
                redistributeToHundredPercentExcluding(parent, node);
            }
            
            // Sauvegarde automatique
            saveConfiguration();
            notifyChangeListeners();
            
            logger.debug("Nœud mis à jour : {} - Redistribution automatique effectuée (nœud modifié préservé)", node.getLabel());
        }
    }
    
    /**
     * Réinitialise la configuration par défaut
     */
    public void resetToDefault() {
        logger.info("Réinitialisation de la configuration");
        createDefaultConfiguration();
        saveConfiguration();
        notifyChangeListeners();
    }
    
    /**
     * Trouve le parent d'un nœud donné
     */
    private TreeNode findParent(TreeNode root, TreeNode target) {
        if (root == null || !root.hasChildren()) return null;
        
        for (TreeNode child : root.getChildren()) {
            if (child.getId().equals(target.getId())) {
                return root;
            }
            TreeNode found = findParent(child, target);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
    
    /**
     * Met à jour récursivement les niveaux d'un nœud et ses enfants
     * Note: TreeNode calcule automatiquement la profondeur donc cette méthode n'est pas nécessaire
     */
    private void updateLevels(TreeNode node, int newLevel) {
        // TreeNode calcule automatiquement getDepth() donc pas besoin de setLevel()
        // Cette méthode est gardée pour compatibilité mais ne fait rien
    }
    
    /**
     * Ajoute un listener pour les changements de configuration
     */
    public void addChangeListener(Consumer<TreeNode> listener) {
        changeListeners.add(listener);
    }
    
    /**
     * Supprime un listener
     */
    public void removeChangeListener(Consumer<TreeNode> listener) {
        changeListeners.remove(listener);
    }
    
    /**
     * Notifie tous les listeners des changements
     */
    private void notifyChangeListeners() {
        for (Consumer<TreeNode> listener : changeListeners) {
            try {
                // Vérifier si on est sur le thread JavaFX
                if (javafx.application.Platform.isFxApplicationThread()) {
                    // Exécuter directement si on est déjà sur le thread JavaFX
                    listener.accept(rootNode);
                } else {
                    // Sinon, déléguer au thread JavaFX
                    javafx.application.Platform.runLater(() -> {
                        try {
                            listener.accept(rootNode);
                        } catch (Exception e) {
                            logger.error("Erreur lors de la notification du listener (thread JavaFX)", e);
                        }
                    });
                }
            } catch (Exception e) {
                logger.error("Erreur lors de la notification du listener", e);
            }
        }
    }
    
    /**
     * Redistribue automatiquement les pourcentages des enfants pour que leur somme = 100%
     */
    private void redistributeToHundredPercent(TreeNode parent) {
        if (parent == null || !parent.hasChildren()) {
            return;
        }
        
        List<TreeNode> children = parent.getChildren();
        if (children.isEmpty()) {
            return;
        }
        
        // Calculer la somme actuelle
        double currentSum = children.stream()
                .mapToDouble(TreeNode::getPercentage)
                .sum();
        
        logger.debug("Redistribution pour {} : somme actuelle = {}, enfants = {}", 
                    parent.getLabel(), currentSum, children.size());
        
        if (currentSum > 0.01) { // Éviter division par zéro et pourcentages très faibles
            // Redistribuer proportionnellement pour arriver à 100%
            double factor = 100.0 / currentSum;
            for (TreeNode child : children) {
                double oldPercentage = child.getPercentage();
                double newPercentage = oldPercentage * factor;
                child.setPercentage(newPercentage);
                logger.debug("  {} : {}% -> {}%", child.getLabel(), 
                           oldPercentage, newPercentage);
            }
        } else {
            // Si tous sont à 0 ou très proches de 0, répartir équitablement
            double equalPercentage = 100.0 / children.size();
            for (TreeNode child : children) {
                child.setPercentage(equalPercentage);
                logger.debug("  {} : répartition équitable -> {}%", 
                           child.getLabel(), equalPercentage);
            }
        }
        
        // Correction des erreurs d'arrondi pour s'assurer que la somme = exactement 100%
        double finalSum = children.stream()
                .mapToDouble(TreeNode::getPercentage)
                .sum();
                
        if (Math.abs(finalSum - 100.0) > 0.001) {
            // Ajuster le premier enfant pour compenser l'erreur d'arrondi
            TreeNode firstChild = children.get(0);
            double adjustment = 100.0 - finalSum;
            firstChild.setPercentage(firstChild.getPercentage() + adjustment);
            logger.debug("  Correction arrondi: {} ajusté de {}%", 
                        firstChild.getLabel(), adjustment);
        }
        
        // Vérification finale
        double verificationSum = children.stream()
                .mapToDouble(TreeNode::getPercentage)
                .sum();
        
                logger.debug("Redistribution terminée pour {} : somme finale = {}%", 
                    parent.getLabel(), verificationSum);
    }

    /**
     * Redistribue automatiquement les pourcentages des enfants pour que leur somme = 100%
     * MAIS en excluant un nœud spécifique (généralement celui qui vient d'être modifié par l'utilisateur)
     */
    private void redistributeToHundredPercentExcluding(TreeNode parent, TreeNode excludedNode) {
        if (parent == null || !parent.hasChildren()) {
            return;
        }

        List<TreeNode> children = parent.getChildren();
        if (children.isEmpty()) {
            return;
        }

        // Filtrer les enfants pour exclure le nœud spécifié
        List<TreeNode> childrenToRedistribute = children.stream()
                .filter(child -> !child.getId().equals(excludedNode.getId()))
                .collect(Collectors.toList());

        if (childrenToRedistribute.isEmpty()) {
            logger.debug("Aucun enfant à redistribuer pour {} (nœud exclu : {})", 
                        parent.getLabel(), excludedNode.getLabel());
            return;
        }

        // Calculer l'espace disponible pour la redistribution (100% - pourcentage du nœud exclu)
        double excludedPercentage = excludedNode.getPercentage();
        double availablePercentage = 100.0 - excludedPercentage;

        // Calculer la somme actuelle des nœuds à redistribuer
        double currentSumToRedistribute = childrenToRedistribute.stream()
                .mapToDouble(TreeNode::getPercentage)
                .sum();

        logger.debug("Redistribution pour {} (excluant {}) : espace disponible = {}%, somme actuelle des autres = {}%", 
                    parent.getLabel(), excludedNode.getLabel(), availablePercentage, currentSumToRedistribute);

        if (availablePercentage <= 0.001) {
            // Si le nœud exclu prend 100% ou plus, mettre tous les autres à 0
            for (TreeNode child : childrenToRedistribute) {
                child.setPercentage(0.0);
                logger.debug("  {} : mis à 0% (espace insuffisant)", child.getLabel());
            }
        } else if (currentSumToRedistribute > 0.01) {
            // Redistribuer proportionnellement dans l'espace disponible
            double factor = availablePercentage / currentSumToRedistribute;
            for (TreeNode child : childrenToRedistribute) {
                double oldPercentage = child.getPercentage();
                double newPercentage = oldPercentage * factor;
                child.setPercentage(newPercentage);
                logger.debug("  {} : {}% -> {}%", child.getLabel(), 
                           oldPercentage, newPercentage);
            }
        } else {
            // Si tous les autres sont à 0, répartir équitablement l'espace disponible
            double equalPercentage = availablePercentage / childrenToRedistribute.size();
            for (TreeNode child : childrenToRedistribute) {
                child.setPercentage(equalPercentage);
                logger.debug("  {} : répartition équitable -> {}%", 
                           child.getLabel(), equalPercentage);
            }
        }

        // Correction des erreurs d'arrondi pour s'assurer que la somme totale = exactement 100%
        double finalSumOthers = childrenToRedistribute.stream()
                .mapToDouble(TreeNode::getPercentage)
                .sum();
        double totalSum = finalSumOthers + excludedPercentage;
        
        if (Math.abs(totalSum - 100.0) > 0.001 && !childrenToRedistribute.isEmpty()) {
            // Ajuster le premier enfant (non exclu) pour compenser l'erreur d'arrondi
            TreeNode firstChild = childrenToRedistribute.get(0);
            double adjustment = 100.0 - totalSum;
            firstChild.setPercentage(firstChild.getPercentage() + adjustment);
            logger.debug("  Correction arrondi: {} ajusté de {}%", 
                        firstChild.getLabel(), adjustment);
        }

        // Vérification finale
        double verificationSum = children.stream()
                .mapToDouble(TreeNode::getPercentage)
                .sum();
        
        logger.debug("Redistribution terminée pour {} (excluant {}) : somme finale = {}%", 
                    parent.getLabel(), excludedNode.getLabel(), verificationSum);
    }

    /**
     * Valide que la somme des pourcentages des enfants = 100%
     */
    public boolean validatePercentages(TreeNode parent) {
        if (parent == null || !parent.hasChildren()) {
            return true;
        }
        
        double sum = parent.getChildren().stream()
                .mapToDouble(TreeNode::getPercentage)
                .sum();
        
        // Tolérance de 0.1% pour les erreurs d'arrondi
        return Math.abs(sum - 100.0) < 0.1;
    }
    
    /**
     * Valide récursivement tout l'arbre
     */
    public boolean validateEntireTree() {
        return validateNodeRecursively(rootNode);
    }
    
    private boolean validateNodeRecursively(TreeNode node) {
        if (node == null) {
            return true;
        }
        
        // Valider ce nœud
        if (!validatePercentages(node)) {
            logger.warn("Validation échouée pour {} : somme des enfants != 100%", node.getLabel());
            return false;
        }
        
        // Valider récursivement les enfants
        if (node.hasChildren()) {
            for (TreeNode child : node.getChildren()) {
                if (!validateNodeRecursively(child)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    // Getters
    public TreeNode getRootNode() {
        return rootNode;
    }
    
    public void setRootNode(TreeNode rootNode) {
        this.rootNode = rootNode;
        saveConfiguration();
    }
    
    public Path getConfigPath() {
        return configPath;
    }
    

} 