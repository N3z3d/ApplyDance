package com.applydance.service;

import com.applydance.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

/**
 * Service de validation en temps réel pour l'arbre de configuration.
 * Fournit un feedback visuel immédiat sur les erreurs de pourcentage et de structure.
 */
public class ValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);
    private static final double TOLERANCE = 0.1; // Tolérance pour les comparaisons de pourcentages
    
    private final TreeConfigurationService configService;
    private final List<Consumer<ValidationStatus>> listeners;
    private ValidationStatus lastStatus;
    
    public ValidationService(TreeConfigurationService configService) {
        this.configService = configService;
        this.listeners = new ArrayList<>();
        this.lastStatus = new ValidationStatus();
    }
    
    /**
     * Ajoute un listener pour les changements de statut de validation
     */
    public void addValidationListener(Consumer<ValidationStatus> listener) {
        listeners.add(listener);
    }
    
    /**
     * Supprime un listener
     */
    public void removeValidationListener(Consumer<ValidationStatus> listener) {
        listeners.remove(listener);
    }
    
    /**
     * Valide l'arbre complet et notifie les listeners
     */
    public ValidationStatus validateTree() {
        TreeNode rootNode = configService.getRootNode();
        ValidationStatus status = performValidation(rootNode);
        
        // Notifier les listeners si le statut a changé
        if (!status.equals(lastStatus)) {
            lastStatus = status;
            notifyListeners(status);
        }
        
        return status;
    }
    
    /**
     * Valide un nœud spécifique et ses enfants
     */
    public NodeValidationResult validateNode(TreeNode node) {
        if (node == null) {
            return new NodeValidationResult(false, "Nœud manquant", ValidationLevel.ERROR);
        }
        
        NodeValidationResult result = new NodeValidationResult();
        
        // Validation du pourcentage individuel
        if (node.getPercentage() < 0) {
            result.isValid = false;
            result.message = "Pourcentage négatif";
            result.level = ValidationLevel.ERROR;
            return result;
        }
        
        if (node.getPercentage() > 100) {
            result.isValid = false;
            result.message = "Pourcentage > 100%";
            result.level = ValidationLevel.ERROR;
            return result;
        }
        
        // Validation des enfants si présents
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            double totalChildPercentage = node.getChildren().stream()
                    .mapToDouble(TreeNode::getPercentage)
                    .sum();
            
            double difference = Math.abs(totalChildPercentage - 100.0);
            
            if (difference > TOLERANCE) {
                result.isValid = false;
                result.message = String.format("Enfants totalisent %.1f%% au lieu de 100%%", totalChildPercentage);
                result.level = ValidationLevel.ERROR;
                result.suggestedPercentage = calculateSuggestedPercentage(node, totalChildPercentage);
                return result;
            } else if (difference > 0.01) {
                result.isValid = true;
                result.message = String.format("Léger décalage: %.2f%%", difference);
                result.level = ValidationLevel.WARNING;
                return result;
            }
        }
        
        result.isValid = true;
        result.message = "Valide";
        result.level = ValidationLevel.VALID;
        return result;
    }
    
    /**
     * Calcule une suggestion de correction pour un nœud
     */
    public List<PercentageCorrection> getSuggestedCorrections(TreeNode parentNode) {
        List<PercentageCorrection> corrections = new ArrayList<>();
        
        if (parentNode.getChildren() == null || parentNode.getChildren().isEmpty()) {
            return corrections;
        }
        
        double totalCurrent = parentNode.getChildren().stream()
                .mapToDouble(TreeNode::getPercentage)
                .sum();
        
        if (Math.abs(totalCurrent - 100.0) > TOLERANCE) {
            // Calcul proportionnel
            for (TreeNode child : parentNode.getChildren()) {
                double currentPercentage = child.getPercentage();
                double correctedPercentage = (currentPercentage / totalCurrent) * 100.0;
                
                if (Math.abs(correctedPercentage - currentPercentage) > 0.01) {
                    corrections.add(new PercentageCorrection(
                            child.getId(),
                            child.getName(),
                            currentPercentage,
                            correctedPercentage,
                            Math.abs(correctedPercentage - currentPercentage)
                    ));
                }
            }
        }
        
        return corrections;
    }
    
    /**
     * Applique automatiquement les corrections suggérées
     */
    public boolean applyCorrections(TreeNode parentNode) {
        List<PercentageCorrection> corrections = getSuggestedCorrections(parentNode);
        
        if (corrections.isEmpty()) {
            return false;
        }
        
        boolean applied = false;
        for (PercentageCorrection correction : corrections) {
            TreeNode childNode = findNodeById(parentNode.getChildren(), correction.nodeId);
            if (childNode != null) {
                childNode.setPercentage(correction.correctedPercentage);
                applied = true;
                logger.info("Correction appliquée sur {} : {:.1f}% -> {:.1f}%", 
                        childNode.getName(), correction.currentPercentage, correction.correctedPercentage);
            }
        }
        
        if (applied) {
            configService.saveConfiguration();
            validateTree(); // Revalider après correction
        }
        
        return applied;
    }
    
    /**
     * Validation complète de l'arbre
     */
    private ValidationStatus performValidation(TreeNode rootNode) {
        ValidationStatus status = new ValidationStatus();
        
        if (rootNode == null) {
            status.globalLevel = ValidationLevel.ERROR;
            status.globalMessage = "Aucun arbre de configuration";
            status.nodeValidations.put("root", new NodeValidationResult(false, "Nœud racine manquant", ValidationLevel.ERROR));
            return status;
        }
        
        // Validation récursive
        Map<String, NodeValidationResult> validations = new HashMap<>();
        validateNodeRecursively(rootNode, validations, new HashSet<>());
        
        status.nodeValidations = validations;
        
        // Déterminer le niveau global
        boolean hasErrors = validations.values().stream()
                .anyMatch(v -> v.level == ValidationLevel.ERROR);
        boolean hasWarnings = validations.values().stream()
                .anyMatch(v -> v.level == ValidationLevel.WARNING);
        
        if (hasErrors) {
            status.globalLevel = ValidationLevel.ERROR;
            status.globalMessage = "Erreurs détectées dans l'arbre";
        } else if (hasWarnings) {
            status.globalLevel = ValidationLevel.WARNING;
            status.globalMessage = "Avertissements détectés";
        } else {
            status.globalLevel = ValidationLevel.VALID;
            status.globalMessage = "Configuration valide";
        }
        
        return status;
    }
    
    /**
     * Validation récursive des nœuds
     */
    private void validateNodeRecursively(TreeNode node, Map<String, NodeValidationResult> validations, Set<String> seenIds) {
        NodeValidationResult nodeResult = validateNode(node);
        validations.put(node.getId(), nodeResult);
        
        // Vérifier l'unicité des IDs
        if (seenIds.contains(node.getId())) {
            validations.put(node.getId() + "_duplicate", 
                    new NodeValidationResult(false, "ID dupliqué", ValidationLevel.ERROR));
        } else {
            seenIds.add(node.getId());
        }
        
        // Validation récursive des enfants
        if (node.getChildren() != null) {
            for (TreeNode child : node.getChildren()) {
                validateNodeRecursively(child, validations, seenIds);
            }
        }
    }
    
    /**
     * Notifie tous les listeners du changement de statut
     */
    private void notifyListeners(ValidationStatus status) {
        for (Consumer<ValidationStatus> listener : listeners) {
            try {
                listener.accept(status);
            } catch (Exception e) {
                logger.error("Erreur lors de la notification du listener de validation", e);
            }
        }
    }
    
    /**
     * Méthodes utilitaires
     */
    private double calculateSuggestedPercentage(TreeNode parent, double currentTotal) {
        if (parent.getChildren() == null || parent.getChildren().isEmpty()) {
            return 0.0;
        }
        
        return parent.getChildren().get(0).getPercentage() * (100.0 / currentTotal);
    }
    
    private TreeNode findNodeById(List<TreeNode> nodes, String id) {
        return nodes.stream()
                .filter(node -> id.equals(node.getId()))
                .findFirst()
                .orElse(null);
    }
    
    // ========================================
    // CLASSES AUXILIAIRES
    // ========================================
    
    /**
     * Statut global de validation
     */
    public static class ValidationStatus {
        public ValidationLevel globalLevel = ValidationLevel.VALID;
        public String globalMessage = "";
        public Map<String, NodeValidationResult> nodeValidations = new HashMap<>();
        public java.time.LocalDateTime validatedAt = java.time.LocalDateTime.now();
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ValidationStatus)) return false;
            ValidationStatus other = (ValidationStatus) obj;
            return globalLevel == other.globalLevel && 
                   Objects.equals(globalMessage, other.globalMessage) &&
                   Objects.equals(nodeValidations, other.nodeValidations);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(globalLevel, globalMessage, nodeValidations);
        }
    }
    
    /**
     * Résultat de validation pour un nœud spécifique
     */
    public static class NodeValidationResult {
        public boolean isValid = true;
        public String message = "";
        public ValidationLevel level = ValidationLevel.VALID;
        public Double suggestedPercentage;
        
        public NodeValidationResult() {}
        
        public NodeValidationResult(boolean isValid, String message, ValidationLevel level) {
            this.isValid = isValid;
            this.message = message;
            this.level = level;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof NodeValidationResult)) return false;
            NodeValidationResult other = (NodeValidationResult) obj;
            return isValid == other.isValid && 
                   level == other.level &&
                   Objects.equals(message, other.message);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(isValid, message, level);
        }
    }
    
    /**
     * Suggestion de correction de pourcentage
     */
    public static class PercentageCorrection {
        public final String nodeId;
        public final String nodeName;
        public final double currentPercentage;
        public final double correctedPercentage;
        public final double difference;
        
        public PercentageCorrection(String nodeId, String nodeName, 
                                  double currentPercentage, double correctedPercentage, double difference) {
            this.nodeId = nodeId;
            this.nodeName = nodeName;
            this.currentPercentage = currentPercentage;
            this.correctedPercentage = correctedPercentage;
            this.difference = difference;
        }
    }
    
    /**
     * Niveau de validation
     */
    public enum ValidationLevel {
        VALID("#A3BE8C"),      // Vert
        WARNING("#EBCB8B"),    // Jaune
        ERROR("#BF616A");      // Rouge
        
        public final String color;
        
        ValidationLevel(String color) {
            this.color = color;
        }
    }
} 
 
 
 