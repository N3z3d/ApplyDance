# 🧪 Séquence de Test Complète - ApplyDance

## 📋 Objectif
Valider toutes les fonctionnalités de l'application ApplyDance après les améliorations UX/UI et corrections de bugs.

---

## 🚀 Préparation du Test

### 1. Compilation et Démarrage
```bash
# Dans PowerShell
cd C:\Users\Thibaut\Desktop\ApplyDance
mvn clean compile
mvn javafx:run
```

**✅ Vérifications initiales :**
- [ ] L'application démarre sans erreur
- [ ] Interface moderne s'affiche correctement
- [ ] Onglets "Génération", "Configuration" et "Statistiques" visibles
- [ ] Aucune erreur dans les logs au démarrage

---

## 🎯 Test 1 : Génération de Slots (Interface Moderne)

### 1.1 Test de l'Interface de Génération
**Actions :**
1. Aller dans l'onglet "Génération"
2. Cliquer sur "Générer un Slot" plusieurs fois (10x)

**✅ Vérifications :**
- [ ] Interface moderne apparaît (gradient violet/bleu, glassmorphism)
- [ ] Affichage centré avec nom du résultat en gros
- [ ] Chemin complet affiché en plus petit
- [ ] Date de génération présente
- [ ] Animation fade-in visible
- [ ] Formats corrects : `🔹 Sous-élément A1` (PAS `GeneratedSlot{id=46...}`)

### 1.2 Test de Distribution Probabiliste
**Actions :**
1. Générer 50 slots de suite
2. Noter les résultats

**✅ Vérifications :**
- [ ] Les 4 éléments apparaissent : A1, A2, B1, B2
- [ ] Distribution cohérente avec les pourcentages :
  - Branche A (60%) : A1 (~42%), A2 (~18%)
  - Branche B (40%) : B1 (~20%), B2 (~20%)
- [ ] Historique mis à jour automatiquement

---

## ⚙️ Test 2 : Configuration d'Arbre (Persistance)

### 2.1 Test d'Ajout de Nœuds
**Actions :**
1. Aller dans l'onglet "Configuration"
2. Clic droit sur "🔹 Sous-élément A1" → "Ajouter un enfant"
3. Nommer : "🎯 Nouveau Élément Test"
4. Valider

**✅ Vérifications :**
- [ ] Nouveau nœud apparaît dans l'arbre
- [ ] Pourcentages redistribués automatiquement
- [ ] Somme des frères/sœurs = 100%
- [ ] Logs confirment la redistribution

### 2.2 Test de Persistance Critique
**Actions :**
1. Après avoir ajouté le nœud, fermer l'application
2. Relancer l'application : `mvn javafx:run`
3. Aller dans "Configuration"

**✅ Vérifications CRITIQUES :**
- [ ] **Le nouveau nœud "🎯 Nouveau Élément Test" est TOUJOURS LÀ**
- [ ] Pourcentages conservés
- [ ] Structure complète restaurée
- [ ] Pas de retour à la configuration par défaut

### 2.3 Test de Génération avec Nouveaux Éléments
**Actions :**
1. Aller dans "Génération"
2. Générer 20 slots
3. Vérifier l'historique

**✅ Vérifications :**
- [ ] **Le nouveau nœud "🎯 Nouveau Élément Test" APPARAÎT lors de la génération**
- [ ] Fréquence cohérente avec son pourcentage
- [ ] Chemin complet correct dans l'historique

---

## 📊 Test 3 : Statistiques (BarChart Moderne)

### 3.1 Test de l'Interface Statistiques
**Actions :**
1. Aller dans l'onglet "Statistiques"
2. Observer le graphique

**✅ Vérifications :**
- [ ] BarChart horizontal (pas vertical)
- [ ] Noms des éléments sur l'axe Y (à gauche)
- [ ] Valeurs numériques sur l'axe X (en bas)
- [ ] Design moderne avec glassmorphism
- [ ] "🌳 Arbre Principal" N'APPARAÎT PAS dans le graphique
- [ ] Seuls les éléments finaux (A1, A2, B1, B2, Nouveau Élément) affichés

### 3.2 Test des Données Statistiques
**Actions :**
1. Vérifier les données du graphique
2. Comparer avec l'historique

**✅ Vérifications :**
- [ ] Données cohérentes avec les générations
- [ ] Mise à jour en temps réel après nouvelles générations
- [ ] Titre du graphique mis à jour avec le nombre total

---

## 🖱️ Test 4 : Glisser-Déposer Avancé

### 4.1 Test de Déplacement de Nœuds
**Actions :**
1. Dans "Configuration", glisser "🔸 Sous-élément B1" vers "📊 Branche A"
2. Observer les changements

**✅ Vérifications :**
- [ ] Nœud déplacé avec succès
- [ ] Pourcentages redistribués automatiquement dans les deux branches
- [ ] Couleurs mises à jour en temps réel
- [ ] Logs de redistribution visibles

### 4.2 Test de Modification de Pourcentages
**Actions :**
1. Double-clic sur un pourcentage
2. Modifier de 30% à 45%
3. Appuyer sur Entrée

**✅ Vérifications :**
- [ ] Nouvelle valeur affichée (45%, pas une valeur bizarre)
- [ ] Frères/sœurs redistribués automatiquement
- [ ] Somme totale = 100%
- [ ] Pas de bug d'affichage avec valeurs incorrectes

---

## 🔄 Test 5 : Test de Persistance Complet

### 5.1 Modifications Multiples
**Actions :**
1. Ajouter 2 nouveaux nœuds avec des noms uniques
2. Déplacer 1 nœud existant
3. Modifier 3 pourcentages différents
4. Générer 10 slots
5. Fermer l'application

### 5.2 Vérification de Restauration
**Actions :**
1. Relancer l'application
2. Vérifier tous les onglets

**✅ Vérifications :**
- [ ] **TOUTES les modifications sont conservées**
- [ ] Arbre identique à avant fermeture
- [ ] Historique preserved (si applicable)
- [ ] Statistiques cohérentes
- [ ] Nouveaux éléments générables

---

## 🎨 Test 6 : Interface Utilisateur Moderne

### 6.1 Test de Responsivité
**Actions :**
1. Redimensionner la fenêtre
2. Tester tous les onglets

**✅ Vérifications :**
- [ ] Interface s'adapte aux tailles
- [ ] BarChart reste lisible
- [ ] Interface de génération moderne centrée
- [ ] Pas de débordements ou coupures

### 6.2 Test des Animations
**Actions :**
1. Générer plusieurs slots rapidement
2. Observer les transitions

**✅ Vérifications :**
- [ ] Animation fade-in fluide
- [ ] Pas de scintillement
- [ ] Transitions smooth entre les slots
- [ ] Performances correctes

---

## 🚨 Test 7 : Gestion d'Erreurs

### 7.1 Test de Validation
**Actions :**
1. Essayer de mettre un pourcentage négatif
2. Essayer de supprimer tous les enfants d'une branche
3. Tester des noms de nœuds très longs

**✅ Vérifications :**
- [ ] Validation appropriée
- [ ] Messages d'erreur clairs
- [ ] Application reste stable
- [ ] Pas de crash

---

## 📝 Rapport de Test

### ✅ Fonctionnalités Validées
- [ ] Interface moderne de génération
- [ ] BarChart horizontal amélioré
- [ ] Persistance complète des modifications
- [ ] Génération incluant nouveaux éléments
- [ ] Glisser-déposer avec redistribution
- [ ] Statistiques en temps réel

### ❌ Problèmes Identifiés
_(À remplir pendant les tests)_

### 🔧 Améliorations Suggérées
_(À remplir pendant les tests)_

---

## 🎯 Critères de Réussite

**Le test est RÉUSSI si :**
1. ✅ **Persistance fonctionne** : Modifications conservées après redémarrage
2. ✅ **Nouveaux éléments générables** : Apparaissent lors de la génération
3. ✅ **Interface moderne** : UX/UI digne de ce nom
4. ✅ **BarChart amélioré** : Horizontal, sans "Arbre Principal"
5. ✅ **Aucun crash** : Application stable

**Le test ÉCHOUE si :**
- ❌ Modifications perdues au redémarrage
- ❌ Nouveaux éléments n'apparaissent jamais en génération
- ❌ Interface horrible type `GeneratedSlot{id=46...}`
- ❌ Crashes ou erreurs bloquantes

---

## 🚀 Instructions d'Exécution

1. **Suivre chaque section dans l'ordre**
2. **Cocher chaque vérification ✅**
3. **Noter tous les problèmes découverts**
4. **Ne pas passer à la section suivante si des tests critiques échouent**
5. **Redémarrer l'application entre les tests de persistance**

**Durée estimée :** 30-45 minutes 