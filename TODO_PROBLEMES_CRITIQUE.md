# 📋 TODO - RÉSOLUTION DES PROBLÈMES APPLYDANCE

## 🚨 ÉTAPE 1 - PROBLÈME LE PLUS CRITIQUE (À FAIRE EN PREMIER)

### ⚠️ **PROBLÈME #1 : SÉRIALISATION JSON DÉFAILLANTE** 
**🔴 PRIORITÉ ABSOLUE** - Tout dépend de ça !

**Symptômes observés dans les logs** :
- `Direct self-reference leading to cycle` dans TOUS les logs
- Erreur répétée 100+ fois par minute : `com.fasterxml.jackson.databind.exc.InvalidDefinitionException`
- Configuration jamais sauvegardée → retour à la config par défaut à chaque redémarrage
- **PREUVE** : Dans les logs récents, l'app charge toujours la config par défaut au lieu de lire un fichier existant

**Cause racine** :
- TreeNode contient des références circulaires parent ↔ enfant
- ObjectMapper ne peut pas sérialiser ces références bidirectionnelles
- TreeNodeMixin ne fonctionne pas du tout

**Impact** : ❌ **BLOQUE TOUT** - Aucune persistance possible, tests impossibles, nœuds parasites persistent

**Solution à implémenter** :
1. Créer TreeNodeDTO sans références parent pour la sérialisation
2. Méthode de conversion TreeNode → TreeNodeDTO → JSON
3. Méthode de restauration JSON → TreeNodeDTO → TreeNode avec rebuild des références parent

### Validation
- ✅ Compilation réussie
- ✅ Aucune erreur de sérialisation dans les logs
- ✅ Configuration chargée et sauvegardée correctement
- ✅ Ajout de nœuds persisté automatiquement
- ✅ 68+ générations de slots sans erreur
- ✅ Fichier JSON propre et valide

### Impact
- **Persistence fonctionnelle** : Les modifications sont maintenant sauvegardées
- **Génération stable** : Plus d'interruptions par des erreurs JSON
- **Base solide** : Permet d'aborder les autres problèmes

---

## 🔴 ÉTAPE 2 - PROBLÈMES CRITIQUES (Après étape 1)

### **PROBLÈME #2 : INTERFACE DÉFAILLANTE (NULLPOINTEREXCEPTION)**
**Symptômes** : 50+ `NullPointerException: Cannot invoke "javafx.scene.control.Label.setText(String)" because "this.totalSlotsLabel" is null`
**Solution** : Corriger @FXML bindings + null-checks

### **PROBLÈME #3 : TESTS AUTOMATISÉS NON FONCTIONNELS**  
**Symptômes** : "❌ === CERTAINS TESTS ONT ÉCHOUÉ ===" - tous les tests échouent
**Solution** : Réécrire service de nettoyage après avoir résolu #1

### **PROBLÈME #4 : NŒUDS PARASITES PERSISTANTS**
**Symptômes** : "ÔÜû´©Å Test Redistribution" apparaît dans les tirages (logs 22:50:59.621)
**Solution** : Nettoyer après avoir résolu #1

---

## 🟠 ÉTAPE 3 - PROBLÈMES MAJEURS (Après étapes 1-2)

### **PROBLÈME #5 : STATISTIQUES ERRONÉES**
**Symptômes** : "6 éléments dans l'arbre mais 4 visibles dans les graphiques"
**Solution** : Utiliser IDs uniques pour différencier les nœuds

### **PROBLÈME #6 : REDISTRIBUTION POURCENTAGES DÉFECTUEUSE**  
**Symptômes** : Pourcentages dispersés incorrectement
**Solution** : Corriger algorithme `redistributeToHundredPercent()`

### **PROBLÈME #7 : ERREURS TABLEVIEW**
**Symptômes** : `Cannot read from unreadable property selectedNodeLabel`
**Solution** : Corriger PropertyValueFactory dans l'historique

---

## 🟡 ÉTAPE 4 - PROBLÈMES MINEURS (Polissage final)

### **PROBLÈME #8 : LOGS VERBEUX**
**Solution** : Réduire verbosité des erreurs JSON répétitives

### **PROBLÈME #9 : INTERFACE NON MODERNE** 
**Solution** : Finaliser suppression boutons redondants

### **PROBLÈME #10 : ARCHITECTURE FRAGILE**
**Solution** : Ajouter gestion d'erreur robuste

---

## 🎯 **PLAN D'EXÉCUTION IMMÉDIAT**

### ✅ **MAINTENANT : Commencer par le PROBLÈME #1**
**Pourquoi en premier ?**
- **Tout le reste dépend de ça** : Pas de persistance = pas de tests possibles
- **Cause de 90% des erreurs** : Les logs sont pollués par les erreurs JSON
- **Facile à valider** : On verra immédiatement si ça marche (plus d'erreurs JSON)

### 🔧 **Actions concrètes pour PROBLÈME #1** :
1. **Créer TreeNodeDTO.java** (Data Transfer Object sans références circulaires)
2. **Modifier TreeConfigurationService.saveConfiguration()** pour utiliser DTO
3. **Modifier TreeConfigurationService.loadConfiguration()** pour rebuild les parents
4. **Tester** : Ajouter un nœud → Redémarrer → Vérifier qu'il est toujours là

### 📊 **Validation du succès** :
- [ ] **Zéro erreur JSON** dans les logs après 5 minutes d'utilisation
- [ ] **Persistance fonctionnelle** : nœud ajouté survit au redémarrage
- [ ] **Logs propres** : Plus de pollution avec des milliers d'erreurs identiques

---

## 📝 **PREUVES DES PROBLÈMES** (Extraits des logs)

### Erreurs JSON répétitives :
```
21:47:58.607 [JavaFX Application Thread] ERROR c.a.service.TreeConfigurationService - Erreur lors de la sauvegarde de la configuration
com.fasterxml.jackson.databind.exc.InvalidDefinitionException: Direct self-reference leading to cycle (through reference chain: com.applydance.model.TreeNode["root"])
```

### Nœuds parasites :
```
22:50:59.621 [JavaFX Application Thread] DEBUG c.a.service.TreeGenerationEngine - Enfant sélectionné : ÔÜû´©Å Test Redistribution (33.33333333333333%)
22:50:59.621 [JavaFX Application Thread] INFO c.a.service.TreeGenerationEngine - Slot généré : GeneratedSlot{id=null, result=ÔÜû´©Å Test Redistribution, generatedAt=2025-06-23T22:50:59.621335600}
```

### NullPointer Interface :
```
21:55:53.409 [JavaFX Application Thread] ERROR com.applydance.gui.MainController - Erreur statistiques
java.lang.NullPointerException: Cannot invoke "javafx.scene.control.Label.setText(String)" because "this.totalSlotsLabel" is null
```

---

## 🚀 **PRÊT À COMMENCER ?**

**Question** : Veux-tu que je commence immédiatement par la **résolution du PROBLÈME #1** (sérialisation JSON) ?

C'est le plus critique car il bloque tout le reste. Une fois résolu, tous les autres problèmes seront plus faciles à corriger.

**Étapes suivantes** :
1. ✅ Créer TreeNodeDTO sans références circulaires
2. ✅ Modifier la sauvegarde pour utiliser DTO  
3. ✅ Modifier le chargement pour rebuild les parents
4. ✅ Tester la persistance
5. ✅ Passer au problème suivant 

## PROBLÈME #1b : Synchronisation Nœuds Ajoutés ⚠️ **NOUVEAU**
**Priorité** : MAJEUR
**Status** : 🔍 **IDENTIFIÉ** - 24/06/2025

### Description du problème
- Ajout de nœud (ex: "vcaca") visible dans l'interface
- **MAIS** impossible de générer ce nœud tant que l'application n'est pas relancée
- Le nœud apparaît seulement après redémarrage complet
- Problème de synchronisation entre interface et moteur de génération

### Impact
- Workflow cassé : modification → test impossible sans redémarrage
- Frustration utilisateur importante
- Fonctionnalité d'ajout à chaud non opérationnelle

---

## PROBLÈME #2 : Interface NullPointerException ✅ **RÉSOLU**
**Priorité** : CRITIQUE (crashs d'interface)
**Status** : ✅ **RÉSOLU** - 24/06/2025

### Description du problème
- NullPointerException fréquents dans MainController
- Variables d'instance non initialisées : totalSlotsLabel, selectedNodeLabel, vieCountLabel, franceCountLabel
- PropertyValueFactory échouait avec des propriétés inexistantes
- Interface instable avec crashs sporadiques

### Cause identifiée
Variables d'instance déclarées mais jamais initialisées, et PropertyValueFactory tentant d'accéder à des propriétés inexistantes dans GeneratedSlot.

### Solution implémentée
1. **Suppression des variables obsolètes** : totalSlotsLabel, vieCountLabel, franceCountLabel 
2. **Refactorisation setupHistoryTable()** : Gestion d'erreur robuste avec try-catch
3. **Correction PropertyValueFactory** : Utilisation de getLastLevel() au lieu de selectedNodeName
4. **Gestion d'erreur** : Try-catch autour de chaque PropertyValueFactory
5. **Messages informatifs** : Logging des erreurs pour le débogage

### Résultats
- ✅ Plus aucune NullPointerException dans les logs
- ✅ Table d'historique configurée avec succès
- ✅ Interface stable et responsive
- ✅ PropertyValueFactory fonctionne correctement
- ✅ Tous les tests automatisés réussis (4/4)

---

## PROBLÈME #3 : Tests Automatisés Non-Fonctionnels ✅ **RÉSOLU**
**Priorité** : CRITIQUE (nœuds parasites persistants)
**Status** : ✅ **RÉSOLU** - 24/06/2025

### Description du problème
- Nœuds parasites créés par les tests automatisés qui persistaient dans l'arbre
- Le mystérieux nœud "ÔÜû´©Å Test Redistribution" apparaissait de manière récurrente
- Tests automatisés laissaient des traces après exécution
- Arbre "pollué" par des éléments de test non nettoyés

### Cause identifiée
Le mystérieux nœud "ÔÜû´©Å Test Redistribution" était en fait un nœud de test créé par Test 4 (redistribution). Le système de détection `isTestNode()` ne détectait pas correctement les nœuds avec caractères corrompus.

### Solution implémentée
1. **Amélioration isTestNode()** : Détection robuste des caractères corrompus (Ô, ÜÇ, û, ´, ©, Å)
2. **Méthode emergencyCleanup()** : Nettoyage d'urgence avec détection étendue
3. **Bouton interface** : Bouton "🧹 Nettoyer" ajouté à l'interface pour nettoyage manuel
4. **Nettoyage automatique** : Le système nettoie automatiquement après chaque série de tests

### Tests de validation
✅ **Tests automatisés 4/4 réussis** (Persistance, Génération, JSON, Redistribution)  
✅ **Nettoyage automatique parfait** : 3 nœuds de test détectés et supprimés  
✅ **Arbre final propre** : 9 nœuds (4 générables : vcaca, oui, Sous-élément A1/A2)  
✅ **Plus aucun nœud parasite** : Validation finale "VALIDE"  
✅ **Interface de nettoyage** : Bouton d'urgence opérationnel  

---

## PROBLÈME #4 : Statistiques Incorrectes ✅ **RÉSOLU**
**Priorité** : MAJEURE  
**Status** : ✅ **RÉSOLU** - 24/06/2025

### Description du problème
- Les branches intermédiaires ("🌿 Branche A", "🌿 Branche B") n'apparaissaient pas dans les statistiques
- Seuls les nœuds finaux (feuilles) étaient comptés dans le graphique
- L'utilisateur voulait voir TOUS les nœuds du chemin de génération, pas seulement les résultats finaux
- Les nœuds avec le même nom n'étaient pas tous affichés dans le graphique

### Cause identifiée
Dans `MainController.refreshStatistics()`, seul le nœud final (`slot.getSelectedNodeName()`) était compté, mais pas les nœuds intermédiaires (branches) traversés pendant la génération.

### Solution implémentée
1. **Analyse du chemin complet** : Utilisation de `slot.getDecisionPath()` au lieu du nœud final uniquement
2. **Parsing du chemin** : Extraction de tous les nœuds via `decisionPath.split(" > ")`
3. **Comptage exhaustif** : Comptage de chaque nœud traversé (branches + feuilles)
4. **Exclusion de la racine** : Seul le nœud racine "🌳 Arbre Principal" est exclu

### Code implémenté
```java
// Extraire tous les nœuds du chemin (ex: "Arbre Principal > Branche A > Sous-élément A1")
String[] pathParts = decisionPath.split(" > ");
// Compter chaque nœud du chemin SAUF le premier (racine)
for (int i = 1; i < pathParts.length; i++) {
    String nodeName = pathParts[i].trim();
    allNodeStats.put(nodeName, allNodeStats.getOrDefault(nodeName, 0) + 1);
}
```

### Résultats
- ✅ **🌿 Branche A : 133 fois** - Branches maintenant visibles !
- ✅ **🌿 Branche B : 86 fois** - Branches maintenant visibles !
- ✅ **📹 Sous-élément A1 : 83 fois** - Feuilles comptées
- ✅ **📹 Sous-élément A2 : 50 fois** - Feuilles comptées
- ✅ **📹 Sous-élément B1 : 43 fois** - Feuilles comptées
- ✅ **📹 Sous-élément B2 : 43 fois** - Feuilles comptées
- ✅ **vcaca : 32 fois** - Nœuds personnalisés comptés
- ✅ **oui : 16 fois** - Nœuds personnalisés comptés

**Validation** : Tests réussis avec 219 slots générés, toutes les branches visibles dans les statistiques

---

## PROBLÈME #5 : Redistribution Automatique des Pourcentages ✅ **RÉSOLU**
**Priorité** : MAJEURE  
**Status** : ✅ **RÉSOLU** - 24/06/2025

### Description du problème
- Problème avec les pourcentages : le reste des pourcentages est distribué aussi à la branche qu'on vient de modifier
- Comportement contre-intuitif : on met un chiffre et quand on valide il vient d'être modifié
- L'utilisateur s'attend à ce que sa valeur reste fixe et que les autres se redistribuent

### Cause identifiée
Deux systèmes de redistribution conflictuels :
- Interface (MainController) : redistributeSiblingPercentages() - redistribue les frères/sœurs
- Service (TreeConfigurationService) : redistributeToHundredPercent() - redistribue TOUS les enfants (y compris celui modifié)

### Solution implémentée
1. **Nouvelle méthode redistributeToHundredPercentExcluding()** : Redistribue en excluant le nœud modifié
2. **Modification updateNode()** : Utilise la nouvelle méthode pour préserver la valeur utilisateur
3. **Logique intelligente** : 
   - Calcule l'espace disponible (100% - valeur du nœud modifié)
   - Redistribue seulement les frères/sœurs dans l'espace restant
   - Préserve absolument la valeur saisie par l'utilisateur
4. **Logs détaillés** : Trace complète de la redistribution pour debugging

### Validation
✅ La valeur saisie par l'utilisateur reste inchangée  
✅ Seuls les frères/sœurs sont redistribués automatiquement  
✅ Somme totale toujours égale à 100% avec correction d'arrondis  
✅ UX fluide et prévisible pour l'utilisateur  
✅ Logs montrent "Redistribution automatique effectuée (nœud modifié préservé)"

**Test confirmé** : Modification de A1 de 70% → 69% → 55%, valeur préservée à chaque fois

---

## PROBLÈME #6 : Génération vcaca nécessite redémarrage 🔄 **NOUVEAU**
**Priorité** : MINEURE
**Status** : 🔄 **NOUVEAU** - 24/06/2025

### Description du problème
- Problème ajout de "vcaca" : après ajout dans l'interface, obligé de relancer l'app pour qu'il soit possible de le générer
- Problème de synchronisation entre interface et moteur de génération

### Actions requises
1. Analyser la synchronisation entre interface et TreeGenerationEngine
2. Vérifier que l'arbre est correctement mis à jour dans le moteur après modification via interface
3. Implémenter une méthode de rafraîchissement automatique

---

## État Global du Projet

### ✅ **PROBLÈMES RÉSOLUS (3/6)**
- ✅ **Problème #1** : Sérialisation JSON (CRITIQUE)
- ✅ **Problème #2** : Interface NullPointerException (CRITIQUE)  
- ✅ **Problème #3** : Tests automatisés/nœuds parasites (CRITIQUE)

### ✅ **PROBLÈMES RÉSOLUS (4/6)**
- ✅ **Problème #1** : Sérialisation JSON (CRITIQUE)
- ✅ **Problème #2** : Interface NullPointerException (CRITIQUE)  
- ✅ **Problème #3** : Tests automatisés/nœuds parasites (CRITIQUE)
- ✅ **Problème #5** : Redistribution pourcentages (MAJEURE)

### 🔄 **PROBLÈMES RESTANTS (2/6)**
- 🔄 **Problème #4** : Statistiques incorrectes (MAJEURE)
- 🔄 **Problème #6** : Génération vcaca après redémarrage (MINEURE - NOUVEAU)

### 📊 **Métriques de Progression**
- **Problèmes critiques résolus** : 3/3 (100%) ✅
- **Problèmes majeurs résolus** : 1/2 (50%) ✅
- **Problèmes mineurs restants** : 1/1 (0% résolus) 🔄
- **Progression globale** : 67% (4/6 problèmes résolus)

### 🏆 **Prochaines Étapes Recommandées**
1. **Problème #4** - Correction statistiques incorrectes (fiabilité données)  
2. **Problème #6** - Synchronisation génération vcaca après redémarrage (polish final)

**🎯 Application maintenant 67% résolue avec toutes les fonctionnalités critiques opérationnelles !** 