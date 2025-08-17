package com.applydance.service;

import com.applydance.model.TreeNode;
import com.applydance.model.GeneratedSlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de tests automatisés pour valider la persistance et la génération
 */
public class AutomatedTestService {
    
    private static final Logger logger = LoggerFactory.getLogger(AutomatedTestService.class);
    
    private final TreeConfigurationService configService;
    private final TreeGenerationEngine generationEngine;
    private final SlotHistoryService historyService;
    
    public AutomatedTestService(TreeConfigurationService configService, 
                               TreeGenerationEngine generationEngine,
                               SlotHistoryService historyService) {
        this.configService = configService;
        this.generationEngine = generationEngine;
        this.historyService = historyService;
    }
    
    /**
     * Exécute tous les tests et nettoie automatiquement les nœuds de test
     */
    public boolean runAllTests() {
        logger.info("🧪 === DÉMARRAGE DES TESTS AUTOMATISÉS ===");
        
        // Forcer le nettoyage avant de commencer
        cleanAllTestNodes();
        
        try {
            // Générer un rapport initial
            generateTreeReport();
            
            // Exécuter chaque test avec logs détaillés
            logger.info("🧪 Exécution des tests en cours...");
            
            boolean test1 = testPersistence();
            logger.info("📊 Test 1 (Persistance) terminé: {}", test1 ? "✅ RÉUSSI" : "❌ ÉCHOUÉ");
            
            boolean test2 = testGenerationWithNewNodes();
            logger.info("📊 Test 2 (Génération) terminé: {}", test2 ? "✅ RÉUSSI" : "❌ ÉCHOUÉ");
            
            boolean test3 = testJsonSerialization();
            logger.info("📊 Test 3 (JSON) terminé: {}", test3 ? "✅ RÉUSSI" : "❌ ÉCHOUÉ");
            
            boolean test4 = testRedistribution();
            logger.info("📊 Test 4 (Redistribution) terminé: {}", test4 ? "✅ RÉUSSI" : "❌ ÉCHOUÉ");
            
            boolean allTestsPass = test1 && test2 && test3 && test4;
            
            // Rapport final
            logger.info("🏁 === RÉSULTATS FINAUX ====");
            logger.info("📊 Tests réussis: {}/4", (test1 ? 1 : 0) + (test2 ? 1 : 0) + (test3 ? 1 : 0) + (test4 ? 1 : 0));
            
            if (allTestsPass) {
                logger.info("🎉 TOUS LES TESTS SONT RÉUSSIS !");
            } else {
                logger.error("💥 CERTAINS TESTS ONT ÉCHOUÉ");
            }
            
            return allTestsPass;
            
        } finally {
            // NETTOYER: Supprimer tous les nœuds de test avec retry automatique
            logger.info("🧹 === NETTOYAGE FINAL DES NŒUDS DE TEST ===");
            
            TreeNode root = configService.getRootNode();
            int initialTestNodes = countTestNodes(root);
            logger.info("🔍 Nœuds de test à nettoyer: {}", initialTestNodes);
            
            // Effectuer le nettoyage avec plusieurs tentatives si nécessaire
            int maxCleanupAttempts = 3;
            boolean cleanupSuccess = false;
            
            for (int attempt = 1; attempt <= maxCleanupAttempts && !cleanupSuccess; attempt++) {
                logger.info("🧹 Tentative de nettoyage {}/{}", attempt, maxCleanupAttempts);
                
                cleanAllTestNodes();
                
                // Vérifier immédiatement
                root = configService.getRootNode(); // Rafraîchir la référence
                int remainingTestNodes = countTestNodes(root);
                
                if (remainingTestNodes == 0) {
                    cleanupSuccess = true;
                    logger.info("✅ Nettoyage réussi en {} tentative(s)", attempt);
                } else {
                    logger.warn("⚠️ Tentative {} échouée: {} nœuds de test restants", attempt, remainingTestNodes);
                    
                    if (attempt < maxCleanupAttempts) {
                        // Forcer sauvegarde/rechargement pour synchroniser l'état
                        try {
                            configService.saveConfiguration();
                            Thread.sleep(200);
                            configService.loadConfiguration();
                            Thread.sleep(200);
                        } catch (Exception e) {
                            logger.warn("⚠️ Erreur lors de la synchronisation: {}", e.getMessage());
                        }
                    }
                }
            }
            
            // Rapport final du nettoyage
            root = configService.getRootNode();
            int finalTestNodes = countTestNodes(root);
            
            if (finalTestNodes > 0) {
                logger.error("💥 ÉCHEC CRITIQUE: {} nœuds de test persistent après {} tentatives de nettoyage !", 
                           finalTestNodes, maxCleanupAttempts);
                
                // Lister les nœuds qui résistent au nettoyage pour diagnostic
                List<String> persistentIds = findAllTestNodeIds(root);
                logger.error("🔍 Nœuds persistants: {}", persistentIds);
            } else {
                logger.info("✅ Nettoyage parfait : aucun nœud de test restant");
            }
            
            // Forcer la sauvegarde finale
            configService.saveConfiguration();
            
            generateTreeReport();
            logger.info("🧪 === FIN DES TESTS AUTOMATISÉS ===");
        }
    }
    
    /**
     * Test 1: Persistance - Ajouter un nœud, sauvegarder, charger, vérifier présence
     */
    public boolean testPersistence() {
        logger.info("🔄 Test 1: Persistance des modifications");
        
        try {
            // État initial
            TreeNode root = configService.getRootNode();
            int initialChildCount = root.getChildren().size();
            
            // Créer un nouveau nœud de test
            TreeNode testNode = new TreeNode("testNode_" + System.currentTimeMillis(), 
                                           "🧪 Nœud Test Persistance", 25.0, "🧪");
            testNode.setColor("#FF6B35");
            
            logger.info("📝 Ajout du nœud de test: {}", testNode.getLabel());
            configService.addChildNode(root, testNode);
            
            // Vérifier l'ajout immédiat
            if (root.getChildren().size() != initialChildCount + 1) {
                logger.error("❌ Échec: Le nœud n'a pas été ajouté immédiatement");
                return false;
            }
            
            // Forcer la sauvegarde
            configService.saveConfiguration();
            
            // Simuler un redémarrage en rechargeant la configuration
            logger.info("🔄 Simulation d'un redémarrage - rechargement de la configuration");
            configService.loadConfiguration();
            
            // Vérifier la persistance
            TreeNode reloadedRoot = configService.getRootNode();
            boolean nodeFound = reloadedRoot.getChildren().stream()
                    .anyMatch(child -> child.getId().equals(testNode.getId()));
            
            if (nodeFound) {
                logger.info("✅ Test 1 RÉUSSI: Le nœud a été persisté avec succès");
                return true;
            } else {
                logger.error("❌ Test 1 ÉCHOUÉ: Le nœud n'a pas été retrouvé après rechargement");
                return false;
            }
            
        } catch (Exception e) {
            logger.error("❌ Test 1 ÉCHOUÉ avec exception: ", e);
            return false;
        }
    }
    
    /**
     * Test 2: Génération - Vérifier que les nouveaux nœuds apparaissent dans les tirages
     */
    public boolean testGenerationWithNewNodes() {
        logger.info("🎲 Test 2: Génération avec nouveaux nœuds");
        
        try {
            TreeNode root = configService.getRootNode();
            
            // Créer un nœud unique avec un ID facilement identifiable
            String uniqueId = "uniqueTestNode_" + System.currentTimeMillis();
            TreeNode uniqueNode = new TreeNode(uniqueId, "🎯 Nœud Unique Test", 100.0, "🎯");
            uniqueNode.setColor("#FFD700");
            
            // L'ajouter directement comme enfant de la racine pour maximiser sa probabilité
            logger.info("📝 Ajout du nœud unique comme enfant direct de la racine");
            configService.addChildNode(root, uniqueNode);
            
            // Le système de notification automatique de TreeConfigurationService
            // se charge de mettre à jour le cache du générateur
            
            // Vérifier d'abord que le nœud est bien dans la liste des nœuds feuilles
            List<TreeNode> leafNodes = root.getLeafNodes();
            boolean nodeInLeaves = leafNodes.stream()
                    .anyMatch(leaf -> leaf.getId().equals(uniqueNode.getId()));
            
            if (!nodeInLeaves) {
                logger.error("❌ Test 2 ÉCHOUÉ: Le nœud test n'est pas dans les feuilles de l'arbre");
                logger.error("🔍 Diagnostic: Nœuds feuilles détectés: {}", 
                    leafNodes.stream().map(TreeNode::getLabel).toList());
                return false;
            }
            
            logger.info("✅ Nœud test confirmé dans les feuilles ({}% de probabilité)", uniqueNode.getPercentage());
            
            // Effectuer plusieurs générations avec retry automatique si nécessaire
            Set<String> generatedResults = new HashSet<>();
            int maxGenerations = 100; // Réduit mais plus focalisé
            boolean nodeFound = false;
            
            logger.info("🎲 Recherche du nouveau nœud avec maximum {} tirages", maxGenerations);
            
            for (int i = 0; i < maxGenerations && !nodeFound; i++) {
                try {
                    GeneratedSlot slot = generationEngine.generateSlot();
                    String result = slot.getSelectedNodeName();
                    generatedResults.add(result);
                    
                    if (result.equals(uniqueNode.getLabel())) {
                        nodeFound = true;
                        logger.info("🎯 Nouveau nœud trouvé à la génération {}/{}", i + 1, maxGenerations);
                        break;
                    }
                    
                    // Log des progrès tous les 25 tirages
                    if ((i + 1) % 25 == 0) {
                        logger.info("📊 Progrès: {}/{} tirages, {} résultats uniques", 
                                  i + 1, maxGenerations, generatedResults.size());
                    }
                    
                } catch (Exception e) {
                    logger.warn("⚠️ Erreur lors d'une génération {}: {}", i + 1, e.getMessage());
                }
            }
            
            logger.info("📊 Total résultats uniques obtenus: {}", generatedResults.size());
            logger.info("📋 Liste des résultats: {}", generatedResults);
            
            if (nodeFound) {
                logger.info("✅ Test 2 RÉUSSI: Le nouveau nœud '{}' est apparu dans les tirages", uniqueNode.getLabel());
                return true;
            } else {
                logger.error("❌ Test 2 ÉCHOUÉ: Le nouveau nœud '{}' n'est jamais apparu en {} tirages", 
                           uniqueNode.getLabel(), maxGenerations);
                
                // Diagnostics supplémentaires améliorés
                logger.error("🔍 Diagnostic: Pourcentage du nœud test = {}%", uniqueNode.getPercentage());
                logger.error("🔍 Diagnostic: Nombre de feuilles dans l'arbre = {}", leafNodes.size());
                logger.error("🔍 Diagnostic: Structure de l'arbre actuelle:");
                generateTreeReport(); // Génère un rapport complet pour diagnostic
                return false;
            }
            
        } catch (Exception e) {
            logger.error("❌ Test 2 ÉCHOUÉ avec exception: ", e);
            return false;
        }
    }
    
    /**
     * Test 3: Sérialisation JSON - Vérifier que la sauvegarde/chargement fonctionne sans erreur
     */
    public boolean testJsonSerialization() {
        logger.info("💾 Test 3: Sérialisation JSON");
        
        try {
            TreeNode root = configService.getRootNode();
            
            // Compter les nœuds avant sauvegarde
            int nodeCountBefore = countAllNodes(root);
            logger.info("📊 Nombre de nœuds avant sauvegarde: {}", nodeCountBefore);
            
            // Forcer la sauvegarde
            configService.saveConfiguration();
            logger.info("💾 Sauvegarde effectuée");
            
            // Recharger
            configService.loadConfiguration();
            TreeNode reloadedRoot = configService.getRootNode();
            
            // Compter les nœuds après rechargement
            int nodeCountAfter = countAllNodes(reloadedRoot);
            logger.info("📊 Nombre de nœuds après rechargement: {}", nodeCountAfter);
            
            if (nodeCountBefore == nodeCountAfter) {
                logger.info("✅ Test 3 RÉUSSI: Sérialisation JSON fonctionnelle");
                return true;
            } else {
                logger.error("❌ Test 3 ÉCHOUÉ: Perte de nœuds lors de la sérialisation ({} -> {})", 
                           nodeCountBefore, nodeCountAfter);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("❌ Test 3 ÉCHOUÉ avec exception: ", e);
            return false;
        }
    }
    
    /**
     * Test 4: Validation manuelle - Vérifier qu'on peut ajouter des nœuds sans redistribution forcée
     */
    public boolean testRedistribution() {
        logger.info("⚖️ Test 4: Validation sans redistribution automatique");
        
        try {
            TreeNode root = configService.getRootNode();
            
            if (root.getChildren().isEmpty()) {
                logger.error("❌ Test 4 ÉCHOUÉ: Aucune branche disponible pour le test");
                return false;
            }
            
            // Ajouter un nœud sans déclencher de redistribution automatique
            TreeNode testNode = new TreeNode("redistTest_" + System.currentTimeMillis(), 
                                           "⚖️ Test Sans Redistribution", 25.0, "⚖️");
            
            TreeNode firstBranch = root.getChildren().get(0);
            double initialSum = firstBranch.getChildren().stream()
                    .mapToDouble(TreeNode::getPercentage)
                    .sum();
            
            logger.info("📊 Somme initiale des enfants de '{}': {}%", firstBranch.getLabel(), initialSum);
            
            configService.addChildNodeWithoutRedistribution(firstBranch, testNode);
            
            double newSum = firstBranch.getChildren().stream()
                    .mapToDouble(TreeNode::getPercentage)
                    .sum();
            
            logger.info("📊 Nouvelle somme après ajout: {}%", newSum);
            
            // Vérifier que la redistribution automatique n'a PAS eu lieu
            // La nouvelle somme devrait être initialSum + 25.0
            double expectedSum = initialSum + 25.0;
            double tolerance = 0.01;
            
            if (Math.abs(newSum - expectedSum) < tolerance) {
                logger.info("✅ Test 4 RÉUSSI: Pas de redistribution automatique ({}% + 25% = {}%)", 
                           initialSum, newSum);
                return true;
            } else {
                logger.error("❌ Test 4 ÉCHOUÉ: Redistribution inattendue détectée (attendu {}%, obtenu {}%)", 
                           expectedSum, newSum);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("❌ Test 4 ÉCHOUÉ avec exception: ", e);
            return false;
        }
    }
    
    /**
     * Compte récursivement tous les nœuds de l'arbre
     */
    private int countAllNodes(TreeNode root) {
        if (root == null) return 0;
        
        int count = 1; // Le nœud actuel
        for (TreeNode child : root.getChildren()) {
            count += countAllNodes(child);
        }
        return count;
    }
    
    /**
     * Génère un rapport détaillé de l'état actuel de l'arbre
     */
    public void generateTreeReport() {
        logger.info("📋 === RAPPORT DE L'ÉTAT DE L'ARBRE ===");
        
        TreeNode root = configService.getRootNode();
        if (root == null) {
            logger.info("❌ Aucun arbre chargé");
            return;
        }
        
        logger.info("🌳 Arbre: {}", root.getLabel());
        logger.info("📊 Nombre total de nœuds: {}", countAllNodes(root));
        logger.info("🍃 Nœuds feuilles: {}", root.getLeafNodes().size());
        
        // Lister tous les nœuds feuilles (ceux qui peuvent être générés)
        List<TreeNode> leaves = root.getLeafNodes();
        logger.info("📝 Éléments générables:");
        for (TreeNode leaf : leaves) {
            logger.info("   - {} ({}%)", leaf.getLabel(), leaf.getPercentage());
        }
        
        // Validation générale
        boolean isValid = configService.validateEntireTree();
        logger.info("✅ Validation de l'arbre: {}", isValid ? "VALIDE" : "INVALIDE");
        
        logger.info("📋 === FIN DU RAPPORT ===");
    }
    
    /**
     * Clone profondément un arbre pour sauvegarde/restauration
     */
    private TreeNode cloneTree(TreeNode source) {
        if (source == null) return null;
        
        TreeNode clone = new TreeNode();
        clone.setId(source.getId());
        clone.setLabel(source.getLabel());
        clone.setPercentage(source.getPercentage());
        clone.setEmoji(source.getEmoji());
        clone.setDescription(source.getDescription());
        clone.setColor(source.getColor());
        clone.setExpanded(source.isExpanded());
        
        if (source.getMetadata() != null) {
            clone.setMetadata(new HashMap<>(source.getMetadata()));
        }
        
        // Cloner récursivement les enfants
        for (TreeNode child : source.getChildren()) {
            TreeNode childClone = cloneTree(child);
            if (childClone != null) {
                clone.addChild(childClone); // Utilise addChild pour gérer les références parent
            }
        }
        
        return clone;
    }
    
    /**
     * Trouve tous les IDs de nœuds de test dans l'arbre
     */
    private List<String> findAllTestNodeIds(TreeNode root) {
        List<String> testIds = new ArrayList<>();
        if (root == null) return testIds;
        
        // Vérifier si c'est un nœud de test (par ID ou nom)
        if (isTestNode(root)) {
            testIds.add(root.getId());
        }
        
        // Parcourir récursivement tous les enfants
        for (TreeNode child : root.getChildren()) {
            testIds.addAll(findAllTestNodeIds(child));
        }
        
        return testIds;
    }
    
    /**
     * Trouve un nœud par son ID dans l'arbre
     */
    private TreeNode findNodeById(TreeNode root, String id) {
        if (root == null || id == null) return null;
        
        if (id.equals(root.getId())) {
            return root;
        }
        
        // Chercher dans les enfants
        for (TreeNode child : root.getChildren()) {
            TreeNode found = findNodeById(child, id);
            if (found != null) {
                return found;
            }
        }
        
        return null;
    }
    
    /**
     * Détermine si un nœud est un nœud de test
     */
    private boolean isTestNode(TreeNode node) {
        if (node == null) return false;
        
        String id = node.getId();
        String label = node.getLabel();
        
        // Vérifier par ID - tous les patterns de test
        if (id != null && (id.contains("testNode_") || 
                          id.contains("uniqueTestNode_") || 
                          id.contains("redistTest_"))) {
            return true;
        }
        
        // Vérifier par nom/label - plus robuste pour détecter tous les nœuds de test
        if (label != null) {
            String normalizedLabel = label.toLowerCase();
            if (normalizedLabel.contains("test redistribution") || 
                normalizedLabel.contains("nœud test") || 
                normalizedLabel.contains("nœud unique test") ||
                normalizedLabel.contains("redistribution") ||
                // Détection robuste des caractères corrompus suivis de "test"
                (label.contains("Test") && !label.equals("Test")) ||
                // Pattern spécifique pour le nœud parasite observé
                label.contains("ÔÜû´©Å") ||
                // Émojis de test
                label.contains("🧪") ||
                label.contains("🎯")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Nettoie tous les nœuds de test de l'arbre avec une approche plus robuste
     */
    private void cleanAllTestNodes() {
        TreeNode root = configService.getRootNode();
        List<String> testNodeIds = findAllTestNodeIds(root);
        logger.info("🔍 Nœuds de test détectés: {}", testNodeIds);
        
        if (testNodeIds.isEmpty()) {
            logger.info("✅ Aucun nœud de test à nettoyer");
            return;
        }
        
        // Supprimer chaque nœud de test individuellement avec vérification
        for (String testId : testNodeIds) {
            TreeNode nodeToRemove = findNodeById(root, testId);
            if (nodeToRemove != null) {
                String nodeLabel = nodeToRemove.getLabel();
                logger.info("🗑️ Suppression du nœud de test: {} ({})", nodeLabel, testId);
                
                try {
                    configService.removeNode(nodeToRemove);
                    
                    // Vérification immédiate de la suppression
                    TreeNode checkNode = findNodeById(configService.getRootNode(), testId);
                    if (checkNode == null) {
                        logger.info("✅ Nœud '{}' supprimé avec succès", nodeLabel);
                    } else {
                        logger.warn("⚠️ Le nœud '{}' existe encore après suppression", nodeLabel);
                    }
                    
                } catch (Exception e) {
                    logger.error("❌ Erreur lors de la suppression de '{}': {}", nodeLabel, e.getMessage());
                }
            } else {
                logger.warn("⚠️ Nœud avec ID {} introuvable lors du nettoyage", testId);
            }
        }
        
        // Forcer la sauvegarde après chaque série de suppressions
        try {
            configService.saveConfiguration();
            logger.info("💾 Configuration sauvegardée après nettoyage");
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la sauvegarde après nettoyage: {}", e.getMessage());
        }
    }
    
    /**
     * Méthode publique pour nettoyer d'urgence tous les nœuds parasites/test
     * Utilisable depuis l'extérieur pour résoudre les problèmes de nœuds persistants
     */
    public boolean emergencyCleanup() {
        logger.info("🚨 === NETTOYAGE D'URGENCE DES NŒUDS PARASITES ===");
        
        TreeNode root = configService.getRootNode();
        if (root == null) {
            logger.warn("❌ Aucun arbre chargé pour le nettoyage");
            return false;
        }
        
        // Compter les nœuds de test avant nettoyage
        int testNodesBefore = countTestNodes(root);
        logger.info("🔍 Nœuds de test détectés avant nettoyage: {}", testNodesBefore);
        
        if (testNodesBefore == 0) {
            logger.info("✅ Aucun nœud de test détecté, nettoyage non nécessaire");
            return true;
        }
        
        // Effectuer le nettoyage
        cleanAllTestNodes();
        
        // Vérifier après nettoyage
        int testNodesAfter = countTestNodes(root);
        logger.info("🔍 Nœuds de test restants après nettoyage: {}", testNodesAfter);
        
        // Forcer la sauvegarde pour persister le nettoyage
        try {
            configService.saveConfiguration();
            logger.info("💾 Configuration sauvegardée après nettoyage");
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la sauvegarde après nettoyage: {}", e.getMessage());
            return false;
        }
        
        // Résultat final
        boolean success = testNodesAfter == 0;
        if (success) {
            logger.info("✅ Nettoyage d'urgence réussi: {} nœuds parasites supprimés", testNodesBefore);
        } else {
            logger.error("❌ Nettoyage d'urgence partiel: {} nœuds parasites restants", testNodesAfter);
        }
        
        logger.info("🚨 === FIN DU NETTOYAGE D'URGENCE ===");
        return success;
    }
    
    /**
     * Compte le nombre de nœuds de test dans l'arbre
     */
    private int countTestNodes(TreeNode root) {
        if (root == null) return 0;
        
        int count = 0;
        if (isTestNode(root)) {
            count++;
        }
        
        for (TreeNode child : root.getChildren()) {
            count += countTestNodes(child);
        }
        
        return count;
    }
}