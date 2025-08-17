# 🔄 PROBLÈME #6 : Synchronisation Nœuds - EN COURS

## 🎯 Diagnostic du Problème

**Problème identifié** : Les nœuds nouvellement ajoutés ne sont pas immédiatement disponibles pour la génération sans redémarrage de l'application.

### Symptômes observés dans les logs :
1. **Erreurs de sérialisation JSON persistantes** :
   ```
   InvalidDefinitionException: Direct self-reference leading to cycle 
   (through reference chain: com.applydance.model.TreeNode["root"])
   ```

2. **Problèmes de PropertyValueFactory** :
   ```
   WARNING: Can not retrieve property 'selectedNodeLabel' in PropertyValueFactory
   ```

3. **TreeGenerationEngine ne détecte pas les nouveaux nœuds immédiatement**

## 🔍 Analyse des Logs

D'après les logs fournis, je vois plusieurs problèmes :

1. **Ligne 944-1040** : Erreurs de références circulaires JSON lors de la sauvegarde
2. **Ligne 50-165** : `TreeGenerationEngine` n'a pas le système de cache/synchronisation que j'ai implémenté
3. **Ligne 628-724** : Les nouvelles modifications d'arbre ne sont pas propagées au moteur de génération

## 🔧 Solution Requise

Le problème vient du fait que `TreeGenerationEngine` utilise encore l'ancienne approche :
- Il récupère `configService.getRootNode()` à chaque génération
- Mais ne s'abonne pas aux changements de configuration
- Le cache que j'ai ajouté n'est pas fonctionnel

### Actions à effectuer :
1. ✅ Vérifier que TreeGenerationEngine utilise bien le système de listeners
2. ❌ Corriger les références circulaires JSON 
3. ❌ Tester la synchronisation en temps réel
4. ❌ Valider que les nouveaux nœuds sont générables immédiatement

## 📊 Statut Actuel
**❌ NON RÉSOLU** - Le problème persiste selon les logs utilisateur 