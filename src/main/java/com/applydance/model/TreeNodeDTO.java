package com.applydance.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.*;

/**
 * Data Transfer Object pour TreeNode sans références circulaires
 * Utilisé pour la sérialisation/désérialisation JSON
 */
public class TreeNodeDTO {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("label")
    private String label;
    
    @JsonProperty("percentage")
    private double percentage;
    
    @JsonProperty("emoji")
    private String emoji;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("children")
    private List<TreeNodeDTO> children;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    // Constructeurs
    public TreeNodeDTO() {
        this.children = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    public TreeNodeDTO(String id, String label, double percentage, String emoji) {
        this();
        this.id = id;
        this.label = label;
        this.percentage = percentage;
        this.emoji = emoji;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TreeNodeDTO> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNodeDTO> children) {
        this.children = children != null ? children : new ArrayList<>();
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    // Méthodes de conversion
    
    /**
     * Convertit un TreeNode en TreeNodeDTO (sans références parent)
     */
    public static TreeNodeDTO fromTreeNode(TreeNode node) {
        if (node == null) return null;
        
        TreeNodeDTO dto = new TreeNodeDTO();
        dto.setId(node.getId());
        dto.setLabel(node.getLabel());
        dto.setPercentage(node.getPercentage());
        dto.setEmoji(node.getEmoji());
        dto.setDescription(node.getDescription());
        dto.setMetadata(node.getMetadata() != null ? new HashMap<>(node.getMetadata()) : new HashMap<>());
        
        // Convertir récursivement les enfants
        List<TreeNodeDTO> childrenDTO = new ArrayList<>();
        if (node.getChildren() != null) {
            for (TreeNode child : node.getChildren()) {
                TreeNodeDTO childDTO = fromTreeNode(child);
                if (childDTO != null) {
                    childrenDTO.add(childDTO);
                }
            }
        }
        dto.setChildren(childrenDTO);
        
        return dto;
    }
    
    /**
     * Convertit un TreeNodeDTO en TreeNode (avec reconstruction des références parent)
     */
    public TreeNode toTreeNode() {
        return toTreeNode(null);
    }
    
    /**
     * Convertit un TreeNodeDTO en TreeNode avec un parent spécifique
     */
    private TreeNode toTreeNode(TreeNode parent) {
        TreeNode node = new TreeNode();
        node.setId(this.getId());
        node.setLabel(this.getLabel());
        node.setPercentage(this.getPercentage());
        node.setEmoji(this.getEmoji());
        node.setDescription(this.getDescription());
        node.setMetadata(this.getMetadata() != null ? new HashMap<>(this.getMetadata()) : new HashMap<>());
        node.setParent(parent);
        
        // Convertir récursivement les enfants et établir les références parent
        List<TreeNode> children = new ArrayList<>();
        if (this.getChildren() != null) {
            for (TreeNodeDTO childDTO : this.getChildren()) {
                TreeNode child = childDTO.toTreeNode(node);
                children.add(child);
            }
        }
        node.setChildren(children);
        
        return node;
    }
} 