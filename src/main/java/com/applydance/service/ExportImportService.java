package com.applydance.service;

import com.applydance.model.GeneratedSlot;
import com.applydance.model.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service d'import/export avancé pour les données et configurations.
 * Supporte JSON/CSV pour slots et configurations d'arbre avec validation.
 */
public class ExportImportService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExportImportService.class);
    private static final String EXPORT_DIR = "exports";
    
    private final SlotHistoryService historyService;
    private final TreeConfigurationService configService;
    private final ObjectMapper objectMapper;
    
    public ExportImportService(SlotHistoryService historyService, TreeConfigurationService configService) {
        this.historyService = historyService;
        this.configService = configService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        
        createExportDirectoryIfNotExists();
    }
    
    // ========================================
    // EXPORT DES SLOTS GÉNÉRÉS
    // ========================================
    
    public String exportSlotsToCSV() throws IOException {
        return exportSlotsToCSV(historyService.getAllSlots(), null);
    }
    
    public String exportSlotsToCSV(List<GeneratedSlot> slots, String customFileName) throws IOException {
        String fileName = customFileName != null ? customFileName : generateFileName("slots", "csv");
        String filePath = EXPORT_DIR + File.separator + fileName;
        
        try (FileWriter fileWriter = new FileWriter(filePath);
             CSVWriter csvWriter = new CSVWriter(fileWriter)) {
            
            String[] headers = {
                "ID", "Chemin de Décision", "Nœud Sélectionné", "ID Nœud", 
                "Catégorie Principale", "Date de Génération", "Métadonnées"
            };
            csvWriter.writeNext(headers);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            for (GeneratedSlot slot : slots) {
                String[] data = {
                    slot.getId() != null ? slot.getId().toString() : "",
                    slot.getDecisionPath() != null ? slot.getDecisionPath() : "",
                    slot.getSelectedNodeName() != null ? slot.getSelectedNodeName() : "",
                    slot.getSelectedNodeId() != null ? slot.getSelectedNodeId() : "",
                    slot.getMainCategory() != null ? slot.getMainCategory() : "",
                    slot.getGeneratedAt() != null ? slot.getGeneratedAt().format(formatter) : "",
                    formatMetadataForCSV(slot.getMetadata())
                };
                csvWriter.writeNext(data);
            }
        }
        
        logger.info("Export CSV slots créé : {} ({} slots)", filePath, slots.size());
        return filePath;
    }
    
    public String exportSlotsToJSON() throws IOException {
        return exportSlotsToJSON(historyService.getAllSlots(), null);
    }
    
    public String exportToCSV() throws IOException {
        return exportSlotsToCSV();
    }
    
    public String exportToJSON() throws IOException {
        return exportSlotsToJSON();
    }
    
    public String exportSlotsToJSON(List<GeneratedSlot> slots, String customFileName) throws IOException {
        String fileName = customFileName != null ? customFileName : generateFileName("slots", "json");
        String filePath = EXPORT_DIR + File.separator + fileName;
        
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(filePath), slots);
        
        logger.info("Export JSON slots créé : {} ({} slots)", filePath, slots.size());
        return filePath;
    }
    
    // ========================================
    // EXPORT/IMPORT DES CONFIGURATIONS D'ARBRE
    // ========================================
    
    public String exportTreeConfigToJSON() throws IOException {
        return exportTreeConfigToJSON(null);
    }
    
    public String exportTreeConfigToJSON(String customFileName) throws IOException {
        String fileName = customFileName != null ? customFileName : generateFileName("tree_config", "json");
        String filePath = EXPORT_DIR + File.separator + fileName;
        
        TreeNode rootNode = configService.getRootNode();
        if (rootNode == null) {
            throw new IllegalStateException("Aucune configuration d'arbre disponible à exporter");
        }
        
        TreeConfigExport export = new TreeConfigExport();
        export.exportedAt = java.time.LocalDateTime.now();
        export.version = "1.0";
        export.description = "Configuration d'arbre ApplyDance";
        export.rootNode = rootNode;
        export.validationInfo = validateTreeConfiguration(rootNode);
        
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(filePath), export);
        
        logger.info("Export JSON configuration créé : {}", filePath);
        return filePath;
    }
    
    public ImportResult importTreeConfigFromJSON(String filePath) throws IOException {
        File importFile = new File(filePath);
        if (!importFile.exists()) {
            throw new FileNotFoundException("Fichier d'import non trouvé : " + filePath);
        }
        
        ImportResult result = new ImportResult();
        
        try {
            TreeConfigExport importedConfig = objectMapper.readValue(importFile, TreeConfigExport.class);
            
            if (importedConfig.rootNode == null) {
                result.success = false;
                result.errors.add("Configuration invalide : nœud racine manquant");
                return result;
            }
            
            ValidationResult validation = validateTreeConfiguration(importedConfig.rootNode);
            result.validationResult = validation;
            
            if (!validation.isValid) {
                result.success = false;
                result.errors.addAll(validation.errors);
                return result;
            }
            
            configService.setRootNode(importedConfig.rootNode);
            configService.saveConfiguration();
            
            result.success = true;
            result.importedNodeCount = countNodes(importedConfig.rootNode);
            result.message = String.format("Configuration importée avec succès (%d nœuds)", result.importedNodeCount);
            
            logger.info("Configuration d'arbre importée depuis : {}", filePath);
            
        } catch (Exception e) {
            result.success = false;
            result.errors.add("Erreur lors de l'import : " + e.getMessage());
            logger.error("Erreur import configuration", e);
        }
        
        return result;
    }
    
    // ========================================
    // VALIDATION AVANCÉE
    // ========================================
    
    public ValidationResult validateTreeConfiguration(TreeNode rootNode) {
        ValidationResult result = new ValidationResult();
        
        if (rootNode == null) {
            result.isValid = false;
            result.errors.add("Nœud racine manquant");
            return result;
        }
        
        validateNodeRecursively(rootNode, result, new HashSet<>(), "");
        
        if (Math.abs(rootNode.getPercentage() - 100.0) > 0.1) {
            result.warnings.add("Le nœud racine devrait avoir 100% (actuel: " + rootNode.getPercentage() + "%)");
        }
        
        result.isValid = result.errors.isEmpty();
        return result;
    }
    
    public ValidationResult validateCurrentConfiguration() {
        return validateTreeConfiguration(configService.getRootNode());
    }
    
    private void validateNodeRecursively(TreeNode node, ValidationResult result, Set<String> seenIds, String path) {
        String currentPath = path.isEmpty() ? node.getName() : path + " > " + node.getName();
        
        if (seenIds.contains(node.getId())) {
            result.errors.add("ID dupliqué détecté : " + node.getId() + " (chemin: " + currentPath + ")");
        } else {
            seenIds.add(node.getId());
        }
        
        if (node.getPercentage() < 0 || node.getPercentage() > 100) {
            result.errors.add("Pourcentage invalide pour " + currentPath + " : " + node.getPercentage() + "%");
        }
        
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            double totalChildPercentage = node.getChildren().stream()
                    .mapToDouble(TreeNode::getPercentage)
                    .sum();
            
            if (Math.abs(totalChildPercentage - 100.0) > 0.1) {
                result.errors.add("Somme des pourcentages des enfants de " + currentPath + 
                        " != 100% (actuel: " + String.format("%.1f", totalChildPercentage) + "%)");
            }
            
            for (TreeNode child : node.getChildren()) {
                validateNodeRecursively(child, result, seenIds, currentPath);
            }
        }
    }
    
    // ========================================
    // MÉTHODES UTILITAIRES
    // ========================================
    
    private String formatMetadataForCSV(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        metadata.forEach((key, value) -> {
            if (sb.length() > 0) sb.append("; ");
            sb.append(key).append("=").append(value);
        });
        return sb.toString();
    }
    
    private int countNodes(TreeNode node) {
        if (node == null) return 0;
        
        int count = 1;
        if (node.getChildren() != null) {
            for (TreeNode child : node.getChildren()) {
                count += countNodes(child);
            }
        }
        return count;
    }
    
    private String generateFileName(String baseName, String extension) {
        String timestamp = java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_%s.%s", baseName, timestamp, extension);
    }
    
    private void createExportDirectoryIfNotExists() {
        File exportDir = new File(EXPORT_DIR);
        if (!exportDir.exists()) {
            boolean created = exportDir.mkdirs();
            if (created) {
                logger.info("Dossier exports créé");
            } else {
                logger.warn("Impossible de créer le dossier exports");
            }
        }
    }
    
    // ========================================
    // CLASSES AUXILIAIRES
    // ========================================
    
    public static class TreeConfigExport {
        public java.time.LocalDateTime exportedAt;
        public String version;
        public String description;
        public TreeNode rootNode;
        public ValidationResult validationInfo;
    }
    
    public static class ImportResult {
        public boolean success = false;
        public String message = "";
        public List<String> errors = new ArrayList<>();
        public List<String> warnings = new ArrayList<>();
        public ValidationResult validationResult;
        public int importedNodeCount = 0;
        public boolean backupCreated = false;
    }
    
    public static class ValidationResult {
        public boolean isValid = true;
        public List<String> errors = new ArrayList<>();
        public List<String> warnings = new ArrayList<>();
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Validation ").append(isValid ? "RÉUSSIE" : "ÉCHOUÉE").append("\n");
            
            if (!errors.isEmpty()) {
                sb.append("Erreurs :\n");
                errors.forEach(error -> sb.append("  • ").append(error).append("\n"));
            }
            
            if (!warnings.isEmpty()) {
                sb.append("Avertissements :\n");
                warnings.forEach(warning -> sb.append("  ⚠ ").append(warning).append("\n"));
            }
            
            return sb.toString();
        }
    }
} 