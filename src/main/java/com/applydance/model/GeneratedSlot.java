package com.applydance.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Représente un slot généré selon l'arbre de configuration générique.
 * Remplace l'ancien système hardcodé VIE/ville/job par un système flexible.
 */
public class GeneratedSlot {
    
    private Long id;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generatedAt;
    
    // Chemin dans l'arbre de décision (ex: "Arbre Principal > Branche A > Sous-élément A1")
    private String decisionPath;
    
    // Nœud final sélectionné
    private String selectedNodeId;
    private String selectedNodeName;
    
    // Métadonnées additionnelles (flexibles pour extensions futures)
    private Map<String, Object> metadata;
    
    // Propriété de sélection pour l'interface utilisateur
    private final BooleanProperty selected;
    
    // Constructeurs
    public GeneratedSlot() {
        this.generatedAt = LocalDateTime.now();
        this.metadata = new HashMap<>();
        this.selected = new SimpleBooleanProperty(false);
    }
    
    public GeneratedSlot(String decisionPath, String selectedNodeId, String selectedNodeName) {
        this();
        this.decisionPath = decisionPath;
        this.selectedNodeId = selectedNodeId;
        this.selectedNodeName = selectedNodeName;
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    
    public String getDecisionPath() { return decisionPath; }
    public void setDecisionPath(String decisionPath) { this.decisionPath = decisionPath; }
    
    public String getSelectedNodeId() { return selectedNodeId; }
    public void setSelectedNodeId(String selectedNodeId) { this.selectedNodeId = selectedNodeId; }
    
    public String getSelectedNodeName() { return selectedNodeName; }
    public void setSelectedNodeName(String selectedNodeName) { this.selectedNodeName = selectedNodeName; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    // Getters et Setters pour la propriété selected
    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean selected) { this.selected.set(selected); }
    public BooleanProperty selectedProperty() { return selected; }
    
    // Méthodes utilitaires pour les métadonnées
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
    
    public Object getMetadata(String key) {
        return this.metadata.get(key);
    }
    
    @Override
    public String toString() {
        return String.format("GeneratedSlot{id=%d, result=%s, generatedAt=%s, selected=%s}",
                id, getLastLevel(), generatedAt, isSelected());
    }
    
    /**
     * Retourne une description textuelle du slot pour l'utilisateur
     */
    @JsonIgnore
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("🎯 Slot Généré\n");
        sb.append("📋 Chemin de décision : ").append(decisionPath).append("\n");
        sb.append("🎲 Résultat : ").append(selectedNodeName).append("\n");
        sb.append("☑️ Sélectionné : ").append(isSelected() ? "Oui" : "Non").append("\n");
        
        // Ajouter les métadonnées si présentes
        if (!metadata.isEmpty()) {
            sb.append("📊 Informations additionnelles :\n");
            metadata.forEach((key, value) -> 
                sb.append("   • ").append(key).append(" : ").append(value).append("\n"));
        }
        
        sb.append("⏰ Généré le ").append(generatedAt.format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
        
        return sb.toString();
    }
    
    /**
     * Méthode pour extraire le nom de la catégorie principale du chemin
     */
    @JsonIgnore
    public String getMainCategory() {
        if (decisionPath == null || !decisionPath.contains(" > ")) {
            return selectedNodeName;
        }
        
        String[] parts = decisionPath.split(" > ");
        return parts.length > 1 ? parts[1] : selectedNodeName;
    }
    
    /**
     * Méthode pour extraire le dernier niveau du chemin
     */
    @JsonIgnore
    public String getLastLevel() {
        if (decisionPath == null || !decisionPath.contains(" > ")) {
            return selectedNodeName;
        }
        
        String[] parts = decisionPath.split(" > ");
        return parts[parts.length - 1];
    }
} 