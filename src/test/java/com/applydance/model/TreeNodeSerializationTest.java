package com.applydance.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de sérialisation/désérialisation pour vérifier l'absence de références circulaires
 */
public class TreeNodeSerializationTest {
    
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    @Test
    void testTreeNodeDirectSerialization_ShouldHandleCircularReferences() {
        // Créer un arbre avec des références parent/enfant
        TreeNode root = new TreeNode("root", "Root Node", 100.0);
        TreeNode child1 = new TreeNode("child1", "Child 1", 50.0);
        TreeNode child2 = new TreeNode("child2", "Child 2", 50.0);
        
        root.addChild(child1);
        root.addChild(child2);
        
        // Vérifier que les références parent sont bien établies
        assertNotNull(child1.getParent());
        assertNotNull(child2.getParent());
        assertEquals(root, child1.getParent());
        assertEquals(root, child2.getParent());
        
        // Test de sérialisation directe (devrait fonctionner grâce à @JsonIgnore sur parent)
        assertDoesNotThrow(() -> {
            String json = objectMapper.writeValueAsString(root);
            assertNotNull(json);
            assertFalse(json.contains("\"parent\""));
            System.out.println("TreeNode serialization successful:");
            System.out.println(json);
        });
    }
    
    @Test
    void testTreeNodeDTO_SerializationAndDeserialization() {
        // Créer un arbre complexe
        TreeNode root = new TreeNode("root", "Root Node", 100.0);
        TreeNode branch1 = new TreeNode("branch1", "Branch 1", 60.0);
        TreeNode branch2 = new TreeNode("branch2", "Branch 2", 40.0);
        TreeNode leaf1 = new TreeNode("leaf1", "Leaf 1", 30.0);
        TreeNode leaf2 = new TreeNode("leaf2", "Leaf 2", 30.0);
        
        root.addChild(branch1);
        root.addChild(branch2);
        branch1.addChild(leaf1);
        branch1.addChild(leaf2);
        
        // Convertir en DTO
        TreeNodeDTO rootDTO = TreeNodeDTO.fromTreeNode(root);
        
        // Sérialiser le DTO
        assertDoesNotThrow(() -> {
            String json = objectMapper.writeValueAsString(rootDTO);
            assertNotNull(json);
            System.out.println("TreeNodeDTO serialization successful:");
            System.out.println(json);
            
            // Désérialiser le DTO
            TreeNodeDTO deserializedDTO = objectMapper.readValue(json, TreeNodeDTO.class);
            assertNotNull(deserializedDTO);
            assertEquals(rootDTO.getId(), deserializedDTO.getId());
            assertEquals(rootDTO.getLabel(), deserializedDTO.getLabel());
            assertEquals(rootDTO.getChildren().size(), deserializedDTO.getChildren().size());
            
            // Convertir le DTO désérialisé en TreeNode
            TreeNode deserializedRoot = deserializedDTO.toTreeNode();
            assertNotNull(deserializedRoot);
            
            // Vérifier que les références parent sont correctement reconstruites
            assertEquals(2, deserializedRoot.getChildren().size());
            TreeNode deserializedBranch1 = deserializedRoot.getChildren().get(0);
            assertNotNull(deserializedBranch1.getParent());
            assertEquals(deserializedRoot, deserializedBranch1.getParent());
            
            if (deserializedBranch1.hasChildren()) {
                TreeNode deserializedLeaf1 = deserializedBranch1.getChildren().get(0);
                assertNotNull(deserializedLeaf1.getParent());
                assertEquals(deserializedBranch1, deserializedLeaf1.getParent());
            }
        });
    }
    
    @Test
    void testDeepNesting_NoStackOverflow() {
        // Créer un arbre profondément imbriqué pour tester les références circulaires
        TreeNode root = new TreeNode("root", "Root", 100.0);
        TreeNode current = root;
        
        // Créer 10 niveaux de profondeur
        for (int i = 1; i <= 10; i++) {
            TreeNode child = new TreeNode("node" + i, "Node " + i, 100.0);
            current.addChild(child);
            current = child;
        }
        
        // Convertir en DTO et sérialiser (ne devrait pas causer de stack overflow)
        assertDoesNotThrow(() -> {
            TreeNodeDTO rootDTO = TreeNodeDTO.fromTreeNode(root);
            String json = objectMapper.writeValueAsString(rootDTO);
            assertNotNull(json);
            
            // Désérialiser et reconvertir
            TreeNodeDTO deserializedDTO = objectMapper.readValue(json, TreeNodeDTO.class);
            TreeNode deserializedRoot = deserializedDTO.toTreeNode();
            
            // Vérifier la profondeur
            assertEquals(10, getMaxDepth(deserializedRoot));
        });
    }
    
    @Test
    void testComplexTreeStructure_WithMetadata() {
        // Créer un arbre complexe avec métadonnées
        TreeNode root = new TreeNode("root", "Root", 100.0, "🌳");
        root.setDescription("Root description");
        root.setColor("#FF0000");
        root.setMetadata("custom", "value");
        
        TreeNode child1 = new TreeNode("child1", "Child 1", 70.0, "📁");
        child1.setColor("#00FF00");
        child1.setMetadata("type", "folder");
        
        TreeNode child2 = new TreeNode("child2", "Child 2", 30.0, "📄");
        child2.setColor("#0000FF");
        child2.setMetadata("type", "file");
        
        root.addChild(child1);
        root.addChild(child2);
        
        // Cycle complet: TreeNode -> DTO -> JSON -> DTO -> TreeNode
        assertDoesNotThrow(() -> {
            // TreeNode vers DTO
            TreeNodeDTO rootDTO = TreeNodeDTO.fromTreeNode(root);
            
            // DTO vers JSON
            String json = objectMapper.writeValueAsString(rootDTO);
            System.out.println("Complex tree JSON:");
            System.out.println(json);
            
            // JSON vers DTO
            TreeNodeDTO deserializedDTO = objectMapper.readValue(json, TreeNodeDTO.class);
            
            // DTO vers TreeNode
            TreeNode deserializedRoot = deserializedDTO.toTreeNode();
            
            // Vérifications
            assertEquals(root.getId(), deserializedRoot.getId());
            assertEquals(root.getLabel(), deserializedRoot.getLabel());
            assertEquals(root.getEmoji(), deserializedRoot.getEmoji());
            assertEquals(root.getDescription(), deserializedRoot.getDescription());
            assertEquals(root.getColor(), deserializedRoot.getColor());
            assertEquals(root.getMetadata("custom"), deserializedRoot.getMetadata("custom"));
            
            assertEquals(2, deserializedRoot.getChildren().size());
            
            // Vérifier les enfants
            TreeNode deserializedChild1 = deserializedRoot.findById("child1");
            assertNotNull(deserializedChild1);
            assertEquals(child1.getLabel(), deserializedChild1.getLabel());
            assertEquals(child1.getColor(), deserializedChild1.getColor());
            assertEquals(child1.getMetadata("type"), deserializedChild1.getMetadata("type"));
            assertEquals(deserializedRoot, deserializedChild1.getParent());
        });
    }
    
    @Test
    void testEmptyTree_Serialization() {
        TreeNode emptyRoot = new TreeNode("empty", "Empty Root", 0.0);
        
        assertDoesNotThrow(() -> {
            TreeNodeDTO dto = TreeNodeDTO.fromTreeNode(emptyRoot);
            String json = objectMapper.writeValueAsString(dto);
            
            TreeNodeDTO deserializedDTO = objectMapper.readValue(json, TreeNodeDTO.class);
            TreeNode deserializedRoot = deserializedDTO.toTreeNode();
            
            assertEquals(emptyRoot.getId(), deserializedRoot.getId());
            assertEquals(emptyRoot.getLabel(), deserializedRoot.getLabel());
            assertEquals(0, deserializedRoot.getChildren().size());
            assertTrue(deserializedRoot.isRoot());
            assertTrue(deserializedRoot.isLeaf());
        });
    }
    
    /**
     * Calcule la profondeur maximale d'un arbre
     */
    private int getMaxDepth(TreeNode node) {
        if (node == null || !node.hasChildren()) {
            return 0;
        }
        
        int maxChildDepth = 0;
        for (TreeNode child : node.getChildren()) {
            maxChildDepth = Math.max(maxChildDepth, getMaxDepth(child));
        }
        
        return 1 + maxChildDepth;
    }
}