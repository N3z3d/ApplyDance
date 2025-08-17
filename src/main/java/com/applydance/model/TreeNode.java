package com.applydance.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * Nœud générique d'arbre de décision configurable
 */
public class TreeNode {
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
    private List<TreeNode> children;
    
    @JsonIgnore
    private TreeNode parent;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    // Constructeurs
    public TreeNode() {
        this.children = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    public TreeNode(String id, String label) {
        this();
        this.id = id;
        this.label = label;
        this.percentage = 0.0;
    }

    public TreeNode(String id, String label, double percentage) {
        this(id, label);
        this.percentage = percentage;
    }

    public TreeNode(String id, String label, double percentage, String emoji) {
        this(id, label, percentage);
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

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children != null ? children : new ArrayList<>();
        // Maintenir les références parent
        for (TreeNode child : this.children) {
            child.setParent(this);
        }
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    // Méthodes utilitaires
    public void addChild(TreeNode child) {
        if (child != null) {
            this.children.add(child);
            child.setParent(this);
        }
    }

    public void removeChild(TreeNode child) {
        if (child != null) {
            this.children.remove(child);
            child.setParent(null);
        }
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public boolean isLeaf() {
        return !hasChildren();
    }

    public boolean isRoot() {
        return parent == null;
    }

    public int getDepth() {
        int depth = 0;
        TreeNode current = this.parent;
        while (current != null) {
            depth++;
            current = current.parent;
        }
        return depth;
    }

    @JsonIgnore
    public TreeNode getRoot() {
        TreeNode current = this;
        while (current.parent != null) {
            current = current.parent;
        }
        return current;
    }

    @JsonIgnore
    public List<TreeNode> getPath() {
        List<TreeNode> path = new ArrayList<>();
        TreeNode current = this;
        while (current != null) {
            path.add(0, current);
            current = current.parent;
        }
        return path;
    }

    public String getPathString() {
        return getPath().stream()
                .map(TreeNode::getLabel)
                .reduce((a, b) -> a + " > " + b)
                .orElse("");
    }

    public TreeNode findById(String id) {
        if (this.id != null && this.id.equals(id)) {
            return this;
        }
        
        for (TreeNode child : children) {
            TreeNode found = child.findById(id);
            if (found != null) {
                return found;
            }
        }
        
        return null;
    }

    @JsonIgnore
    public List<TreeNode> getAllNodes() {
        List<TreeNode> allNodes = new ArrayList<>();
        collectAllNodes(allNodes);
        return allNodes;
    }

    private void collectAllNodes(List<TreeNode> nodes) {
        nodes.add(this);
        for (TreeNode child : children) {
            child.collectAllNodes(nodes);
        }
    }

    @JsonIgnore
    public List<TreeNode> getLeafNodes() {
        List<TreeNode> leafNodes = new ArrayList<>();
        collectLeafNodes(leafNodes);
        return leafNodes;
    }

    private void collectLeafNodes(List<TreeNode> leafNodes) {
        if (isLeaf()) {
            leafNodes.add(this);
        } else {
            for (TreeNode child : children) {
                child.collectLeafNodes(leafNodes);
            }
        }
    }

    public TreeNode clone() {
        TreeNode cloned = new TreeNode(this.id, this.label, this.percentage, this.emoji);
        cloned.setDescription(this.description);
        cloned.setMetadata(new HashMap<>(this.metadata));
        
        for (TreeNode child : this.children) {
            cloned.addChild(child.clone());
        }
        
        return cloned;
    }

    // Méthodes de métadonnées
    public void setMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return this.metadata.get(key);
    }

    public String getMetadataAsString(String key) {
        Object value = getMetadata(key);
        return value != null ? value.toString() : null;
    }

    public Integer getMetadataAsInteger(String key) {
        Object value = getMetadata(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public Double getMetadataAsDouble(String key) {
        Object value = getMetadata(key);
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (emoji != null && !emoji.isEmpty()) {
            sb.append(emoji).append(" ");
        }
        sb.append(label);
        if (percentage > 0) {
            sb.append(" (").append(String.format("%.1f", percentage)).append("%)");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TreeNode treeNode = (TreeNode) obj;
        return Objects.equals(id, treeNode.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    // Méthodes de compatibilité pour l'ancien système
    public String getName() {
        return getLabel();
    }
    
    public void setName(String name) {
        setLabel(name);
    }
    
    public String getColor() {
        return getMetadataAsString("color");
    }
    
    public void setColor(String color) {
        // Valider et normaliser la couleur
        String validColor = validateColor(color);
        setMetadata("color", validColor);
    }
    
    /**
     * Valide et normalise une couleur
     */
    private String validateColor(String color) {
        if (color == null || color.trim().isEmpty()) {
            return "#5E81AC"; // Couleur par défaut
        }
        
        String normalizedColor = color.trim();
        if (!normalizedColor.startsWith("#")) {
            normalizedColor = "#" + normalizedColor;
        }
        
        // Vérifier que c'est une couleur valide
        try {
            // Test simple de validation hexadécimale
            if (normalizedColor.length() == 7 && normalizedColor.matches("#[0-9A-Fa-f]{6}")) {
                return normalizedColor;
            } else if (normalizedColor.length() == 4 && normalizedColor.matches("#[0-9A-Fa-f]{3}")) {
                // Convertir #RGB en #RRGGBB
                StringBuilder expanded = new StringBuilder("#");
                for (int i = 1; i < normalizedColor.length(); i++) {
                    char c = normalizedColor.charAt(i);
                    expanded.append(c).append(c);
                }
                return expanded.toString();
            }
        } catch (Exception e) {
            // En cas d'erreur, utiliser la couleur par défaut
        }
        
        return "#5E81AC"; // Couleur par défaut si invalide
    }
    
    public int getLevel() {
        return getDepth();
    }
    
    public void setLevel(int level) {
        // TreeNode calcule automatiquement la profondeur
        // Cette méthode est gardée pour compatibilité
    }
    
    public boolean isExpanded() {
        Object expanded = getMetadata("expanded");
        return expanded == null ? true : (Boolean) expanded;
    }
    
    public void setExpanded(boolean expanded) {
        setMetadata("expanded", expanded);
    }
} 