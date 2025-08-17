package com.applydance.service;

import com.applydance.model.GeneratedSlot;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Service de gestion de l'historique des slots générés.
 * Adaptée pour le nouveau système générique avec GeneratedSlot.
 */
public class SlotHistoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(SlotHistoryService.class);
    private static final String HISTORY_DIR = "data";
    private static final String HISTORY_FILE = "slot_history.json";
    
    private final List<GeneratedSlot> slots;
    private final ObjectMapper objectMapper;
    private final Path historyPath;
    private final AtomicLong nextId;
    
    public SlotHistoryService() {
        this.slots = new ArrayList<>();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.historyPath = Paths.get(HISTORY_DIR, HISTORY_FILE);
        this.nextId = new AtomicLong(1);
        
        // Créer le répertoire data s'il n'existe pas
        try {
            Files.createDirectories(historyPath.getParent());
        } catch (IOException e) {
            logger.error("Erreur création répertoire historique", e);
        }
        
        // Charger l'historique existant
        loadHistory();
    }
    
    /**
     * Ajoute un nouveau slot à l'historique
     */
    public void addSlot(GeneratedSlot slot) {
        if (slot == null) {
            logger.warn("Tentative d'ajout d'un slot null");
            return;
        }
        
        // Assigner un ID unique
        slot.setId(nextId.getAndIncrement());
        
        slots.add(slot);
        logger.info("Slot ajouté à l'historique : {}", slot);
        
        // Sauvegarder automatiquement
        saveHistory();
    }
    
    /**
     * Retourne tous les slots
     */
    public List<GeneratedSlot> getAllSlots() {
        return new ArrayList<>(slots);
    }
    
    /**
     * Retourne les N derniers slots
     */
    public List<GeneratedSlot> getLastSlots(int count) {
        if (count <= 0 || slots.isEmpty()) {
            return new ArrayList<>();
        }
        
        int fromIndex = Math.max(0, slots.size() - count);
        return new ArrayList<>(slots.subList(fromIndex, slots.size()));
    }
    
    /**
     * Retourne le dernier slot généré
     */
    public GeneratedSlot getLastSlot() {
        return slots.isEmpty() ? null : slots.get(slots.size() - 1);
    }
    
    /**
     * Retourne le nombre total de slots
     */
    public int getTotalCount() {
        return slots.size();
    }
    
    /**
     * Filtre les slots par catégorie principale
     */
    public List<GeneratedSlot> getSlotsByMainCategory(String category) {
        return slots.stream()
                .filter(slot -> category.equals(slot.getMainCategory()))
                .collect(Collectors.toList());
    }
    
    /**
     * Filtre les slots par nœud sélectionné
     */
    public List<GeneratedSlot> getSlotsBySelectedNode(String nodeName) {
        return slots.stream()
                .filter(slot -> nodeName.equals(slot.getSelectedNodeName()))
                .collect(Collectors.toList());
    }
    
    /**
     * Filtre les slots par période
     */
    public List<GeneratedSlot> getSlotsByDateRange(LocalDateTime from, LocalDateTime to) {
        return slots.stream()
                .filter(slot -> {
                    LocalDateTime slotDate = slot.getGeneratedAt();
                    return slotDate != null && 
                           !slotDate.isBefore(from) && 
                           !slotDate.isAfter(to);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Supprime un slot par ID
     */
    public boolean removeSlot(Long id) {
        boolean removed = slots.removeIf(slot -> id.equals(slot.getId()));
        if (removed) {
            logger.info("Slot supprimé : {}", id);
            saveHistory();
        }
        return removed;
    }
    
    /**
     * Vide complètement l'historique
     */
    public void clearHistory() {
        slots.clear();
        nextId.set(1);
        logger.info("Historique vidé complètement");
        saveHistory();
    }
    
    /**
     * Charge l'historique depuis le fichier
     */
    private void loadHistory() {
        File historyFile = historyPath.toFile();
        
        if (!historyFile.exists()) {
            logger.info("Aucun fichier d'historique trouvé, démarrage avec historique vide");
            return;
        }
        
        try {
            List<GeneratedSlot> loadedSlots = objectMapper.readValue(
                historyFile, 
                new TypeReference<List<GeneratedSlot>>() {}
            );
            
            slots.clear();
            slots.addAll(loadedSlots);
            
            // Mettre à jour le compteur d'ID
            long maxId = slots.stream()
                    .mapToLong(slot -> slot.getId() != null ? slot.getId() : 0)
                    .max()
                    .orElse(0);
            nextId.set(maxId + 1);
            
            logger.info("Historique chargé : {} slots trouvés", slots.size());
            
        } catch (Exception e) {
            logger.error("Erreur lors du chargement de l'historique", e);
            slots.clear();
            nextId.set(1);
        }
    }
    
    /**
     * Sauvegarde l'historique dans le fichier
     */
    private void saveHistory() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(historyPath.toFile(), slots);
            logger.debug("Historique sauvegardé : {} slots", slots.size());
            
        } catch (IOException e) {
            logger.error("Erreur lors de la sauvegarde de l'historique", e);
        }
    }
} 