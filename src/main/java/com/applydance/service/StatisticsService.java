package com.applydance.service;

import com.applydance.model.GeneratedSlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de calcul des statistiques génériques sur les slots générés.
 * Adapté pour le nouveau système d'arbre configurable.
 */
public class StatisticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);
    
    private final SlotHistoryService historyService;
    
    public StatisticsService(SlotHistoryService historyService) {
        this.historyService = historyService;
    }
    
    /**
     * Calcule et retourne les statistiques générales
     */
    public StatisticsReport getGeneralStatistics() {
        List<GeneratedSlot> allSlots = historyService.getAllSlots();
        
        if (allSlots.isEmpty()) {
            return new StatisticsReport();
        }
        
        StatisticsReport report = new StatisticsReport();
        report.totalSlots = allSlots.size();
        
        // Calculer les statistiques par catégorie principale
        report.mainCategoryStats = calculateMainCategoryStats(allSlots);
        
        // Calculer les statistiques par nœud final sélectionné
        report.selectedNodeStats = calculateSelectedNodeStats(allSlots);
        
        // Calculer les statistiques par chemin de décision
        report.decisionPathStats = calculateDecisionPathStats(allSlots);
        
        // Calculer les statistiques temporelles
        report.temporalStats = calculateTemporalStats(allSlots);
        
        // Calculer les métadonnées statistiques
        report.metadataStats = calculateMetadataStats(allSlots);
        
        logger.info("Statistiques générales calculées pour {} slots", allSlots.size());
        return report;
    }
    
    /**
     * Calcule les statistiques par catégorie principale
     */
    private Map<String, Integer> calculateMainCategoryStats(List<GeneratedSlot> slots) {
        return slots.stream()
                .filter(slot -> slot.getMainCategory() != null)
                .collect(Collectors.groupingBy(
                        GeneratedSlot::getMainCategory,
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));
    }
    
    /**
     * Calcule les statistiques par nœud final sélectionné (avec différenciation par ID unique)
     */
    private Map<String, Integer> calculateSelectedNodeStats(List<GeneratedSlot> slots) {
        return slots.stream()
                .filter(slot -> slot.getSelectedNodeName() != null)
                .collect(Collectors.groupingBy(
                        slot -> {
                            // Créer une clé INTERNE unique basée sur l'ID et le chemin complet
                            String path = slot.getDecisionPath();
                            String nodeName = slot.getSelectedNodeName();
                            String nodeId = slot.getSelectedNodeId();
                            
                            // Créer la clé interne unique avec l'ID
                            String internalKey = nodeId != null ? nodeId : nodeName;
                            
                            // Créer le LABEL d'affichage sans l'ID (seulement nom + chemin parent)
                            String displayLabel;
                            if (path != null && path.contains(" > ")) {
                                String[] parts = path.split(" > ");
                                if (parts.length >= 3) {
                                    // Créer un label descriptif avec le chemin parent
                                    StringBuilder pathBuilder = new StringBuilder();
                                    for (int i = 1; i < parts.length - 1; i++) { // Skip racine et inclure jusqu'à l'avant-dernier
                                        if (pathBuilder.length() > 0) pathBuilder.append(" > ");
                                        pathBuilder.append(parts[i].replaceAll("[\\p{So}\\p{Cn}]", "").trim());
                                    }
                                    
                                    String cleanNodeName = nodeName.replaceAll("[\\p{So}\\p{Cn}]", "").trim();
                                    
                                    if (pathBuilder.length() > 0) {
                                        // Format: "NomNœud [Chemin Parent]" - SANS l'ID
                                        displayLabel = cleanNodeName + " [" + pathBuilder.toString() + "]";
                                    } else {
                                        displayLabel = cleanNodeName;
                                    }
                                } else {
                                    displayLabel = nodeName.replaceAll("[\\p{So}\\p{Cn}]", "").trim();
                                }
                            } else {
                                displayLabel = nodeName.replaceAll("[\\p{So}\\p{Cn}]", "").trim();
                            }
                            
                            // Retourner la combinaison clé interne + label d'affichage
                            return internalKey + "|" + displayLabel;
                        },
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));
    }
    
    /**
     * Calcule les statistiques par chemin de décision
     */
    private Map<String, Integer> calculateDecisionPathStats(List<GeneratedSlot> slots) {
        return slots.stream()
                .filter(slot -> slot.getDecisionPath() != null)
                .collect(Collectors.groupingBy(
                        GeneratedSlot::getDecisionPath,
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));
    }
    
    /**
     * Calcule les statistiques temporelles
     */
    private TemporalStatistics calculateTemporalStats(List<GeneratedSlot> slots) {
        TemporalStatistics stats = new TemporalStatistics();
        
        if (slots.isEmpty()) {
            return stats;
        }
        
        // Trouver les dates min et max
        List<LocalDateTime> dates = slots.stream()
                .map(GeneratedSlot::getGeneratedAt)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
        
        if (!dates.isEmpty()) {
            stats.firstSlotDate = dates.get(0);
            stats.lastSlotDate = dates.get(dates.size() - 1);
            
            // Calculer la répartition par jour
            stats.dailyDistribution = slots.stream()
                    .filter(slot -> slot.getGeneratedAt() != null)
                    .collect(Collectors.groupingBy(
                            slot -> slot.getGeneratedAt().toLocalDate().toString(),
                            Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                    ));
            
            // Calculer la répartition par heure
            stats.hourlyDistribution = slots.stream()
                    .filter(slot -> slot.getGeneratedAt() != null)
                    .collect(Collectors.groupingBy(
                            slot -> String.valueOf(slot.getGeneratedAt().getHour()),
                            Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                    ));
        }
        
        return stats;
    }
    
    /**
     * Calcule les statistiques des métadonnées
     */
    private Map<String, Map<String, Integer>> calculateMetadataStats(List<GeneratedSlot> slots) {
        Map<String, Map<String, Integer>> metadataStats = new HashMap<>();
        
        for (GeneratedSlot slot : slots) {
            if (slot.getMetadata() != null) {
                for (Map.Entry<String, Object> entry : slot.getMetadata().entrySet()) {
                    String key = entry.getKey();
                    String value = String.valueOf(entry.getValue());
                    
                    metadataStats.computeIfAbsent(key, k -> new HashMap<>())
                            .merge(value, 1, Integer::sum);
                }
            }
        }
        
        return metadataStats;
    }
    
    /**
     * Calcule un pourcentage avec 1 décimale
     */
    private double calculatePercentage(long count, int total) {
        if (total == 0) return 0.0;
        return Math.round((count * 100.0 / total) * 10.0) / 10.0;
    }
    
    /**
     * Retourne un rapport de statistiques formaté pour l'affichage
     */
    public String getFormattedStatistics() {
        StatisticsReport report = getGeneralStatistics();
        
        if (report.totalSlots == 0) {
            return "📊 Aucune statistique disponible - aucun slot généré.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("📊 STATISTIQUES DES SLOTS GÉNÉRÉS\n");
        sb.append("===================================\n\n");
        
        // Général
        sb.append("🎯 GÉNÉRAL\n");
        sb.append(String.format("   Total des slots générés : %d\n", report.totalSlots));
        
        if (report.temporalStats.firstSlotDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            sb.append(String.format("   Premier slot : %s\n", 
                    report.temporalStats.firstSlotDate.format(formatter)));
            sb.append(String.format("   Dernier slot : %s\n", 
                    report.temporalStats.lastSlotDate.format(formatter)));
        }
        sb.append("\n");
        
        // Catégories principales
        if (!report.mainCategoryStats.isEmpty()) {
            sb.append("📋 CATÉGORIES PRINCIPALES\n");
            report.mainCategoryStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> {
                        double percentage = calculatePercentage(entry.getValue(), report.totalSlots);
                        sb.append(String.format("   %s : %d (%.1f%%)\n", 
                                entry.getKey(), entry.getValue(), percentage));
                    });
            sb.append("\n");
        }
        
        // Nœuds finaux les plus fréquents
        if (!report.selectedNodeStats.isEmpty()) {
            sb.append("🎲 RÉSULTATS LES PLUS FRÉQUENTS (Top 10)\n");
            report.selectedNodeStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> {
                        double percentage = calculatePercentage(entry.getValue(), report.totalSlots);
                        sb.append(String.format("   %s : %d (%.1f%%)\n", 
                                entry.getKey(), entry.getValue(), percentage));
                    });
            sb.append("\n");
        }
        
        // Chemins de décision les plus fréquents
        if (!report.decisionPathStats.isEmpty()) {
            sb.append("🛤️ CHEMINS DE DÉCISION LES PLUS FRÉQUENTS (Top 5)\n");
            report.decisionPathStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(5)
                    .forEach(entry -> {
                        double percentage = calculatePercentage(entry.getValue(), report.totalSlots);
                        sb.append(String.format("   %s : %d (%.1f%%)\n", 
                                entry.getKey(), entry.getValue(), percentage));
                    });
            sb.append("\n");
        }
        
        // Répartition temporelle
        if (!report.temporalStats.dailyDistribution.isEmpty()) {
            sb.append("📅 RÉPARTITION PAR JOUR (5 derniers jours)\n");
            report.temporalStats.dailyDistribution.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByKey().reversed())
                    .limit(5)
                    .forEach(entry -> 
                            sb.append(String.format("   %s : %d slots\n", 
                                    entry.getKey(), entry.getValue())));
            sb.append("\n");
        }
        
        // Métadonnées intéressantes
        if (!report.metadataStats.isEmpty()) {
            sb.append("📊 MÉTADONNÉES\n");
            report.metadataStats.entrySet().stream()
                    .limit(3) // Limiter à 3 métadonnées pour ne pas surcharger
                    .forEach(metaEntry -> {
                        sb.append(String.format("   %s :\n", metaEntry.getKey()));
                        metaEntry.getValue().entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                                .limit(3)
                                .forEach(valueEntry -> 
                                        sb.append(String.format("     • %s : %d\n", 
                                                valueEntry.getKey(), valueEntry.getValue())));
                    });
        }
        
        return sb.toString();
    }
    
    /**
     * Classe pour encapsuler les statistiques générales
     */
    public static class StatisticsReport {
        public int totalSlots = 0;
        public Map<String, Integer> mainCategoryStats = new HashMap<>();
        public Map<String, Integer> selectedNodeStats = new HashMap<>();
        public Map<String, Integer> decisionPathStats = new HashMap<>();
        public TemporalStatistics temporalStats = new TemporalStatistics();
        public Map<String, Map<String, Integer>> metadataStats = new HashMap<>();
    }
    
    /**
     * Classe pour encapsuler les statistiques temporelles
     */
    public static class TemporalStatistics {
        public LocalDateTime firstSlotDate;
        public LocalDateTime lastSlotDate;
        public Map<String, Integer> dailyDistribution = new HashMap<>();
        public Map<String, Integer> hourlyDistribution = new HashMap<>();
    }
} 