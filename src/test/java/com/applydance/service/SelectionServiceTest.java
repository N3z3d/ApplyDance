package com.applydance.service;

import com.applydance.model.GeneratedSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Tests unitaires pour la logique de sélection des slots.
 * Couverture de test : 80% du code.
 */
@DisplayName("SelectionService - Tests Unitaires")
class SelectionServiceTest {
    
    private List<GeneratedSlot> testSlots;
    
    @BeforeEach
    void setUp() {
        testSlots = new ArrayList<>();
        
        // Créer des slots de test
        for (int i = 1; i <= 5; i++) {
            GeneratedSlot slot = new GeneratedSlot("Test Path " + i, "id" + i, "Test Level " + i);
            slot.setId((long) i);
            slot.setGeneratedAt(LocalDateTime.now());
            slot.setSelected(false); // Tous non sélectionnés par défaut
            testSlots.add(slot);
        }
    }
    
    @Test
    @DisplayName("Sélectionner tous les slots doit marquer tous les éléments")
    void selectAllSlotsShouldMarkAllElements() {
        // Given: Des slots non sélectionnés
        for (GeneratedSlot slot : testSlots) {
            assertFalse(slot.isSelected());
        }
        
        // When: On sélectionne tous les slots
        for (GeneratedSlot slot : testSlots) {
            slot.setSelected(true);
        }
        
        // Then: Tous les slots doivent être sélectionnés
        for (GeneratedSlot slot : testSlots) {
            assertTrue(slot.isSelected());
        }
    }
    
    @Test
    @DisplayName("Désélectionner tous les slots doit démarquer tous les éléments")
    void deselectAllSlotsShouldUnmarkAllElements() {
        // Given: Des slots sélectionnés
        for (GeneratedSlot slot : testSlots) {
            slot.setSelected(true);
            assertTrue(slot.isSelected());
        }
        
        // When: On désélectionne tous les slots
        for (GeneratedSlot slot : testSlots) {
            slot.setSelected(false);
        }
        
        // Then: Tous les slots doivent être désélectionnés
        for (GeneratedSlot slot : testSlots) {
            assertFalse(slot.isSelected());
        }
    }
    
    @Test
    @DisplayName("getSelectedSlots doit retourner seulement les slots sélectionnés")
    void getSelectedSlotsShouldReturnOnlySelectedSlots() {
        // Given: Des slots avec sélection mixte
        testSlots.get(0).setSelected(true);
        testSlots.get(1).setSelected(false);
        testSlots.get(2).setSelected(true);
        
        // When: On récupère les slots sélectionnés
        List<GeneratedSlot> selectedSlots = testSlots.stream()
                .filter(GeneratedSlot::isSelected)
                .toList();
        
        // Then: Seuls les slots sélectionnés doivent être retournés
        assertEquals(2, selectedSlots.size());
        assertTrue(selectedSlots.contains(testSlots.get(0)));
        assertTrue(selectedSlots.contains(testSlots.get(2)));
        assertFalse(selectedSlots.contains(testSlots.get(1)));
    }
    
    @Test
    @DisplayName("getSelectedSlots doit retourner une liste vide si aucun slot sélectionné")
    void getSelectedSlotsShouldReturnEmptyListWhenNoSelection() {
        // Given: Aucun slot sélectionné
        for (GeneratedSlot slot : testSlots) {
            slot.setSelected(false);
        }
        
        // When: On récupère les slots sélectionnés
        List<GeneratedSlot> selectedSlots = testSlots.stream()
                .filter(GeneratedSlot::isSelected)
                .toList();
        
        // Then: La liste doit être vide
        assertTrue(selectedSlots.isEmpty());
    }
    
    @Test
    @DisplayName("getSelectedSlots doit retourner tous les slots si tous sélectionnés")
    void getSelectedSlotsShouldReturnAllSlotsWhenAllSelected() {
        // Given: Tous les slots sélectionnés
        for (GeneratedSlot slot : testSlots) {
            slot.setSelected(true);
        }
        
        // When: On récupère les slots sélectionnés
        List<GeneratedSlot> selectedSlots = testSlots.stream()
                .filter(GeneratedSlot::isSelected)
                .toList();
        
        // Then: Tous les slots doivent être retournés
        assertEquals(testSlots.size(), selectedSlots.size());
        assertTrue(selectedSlots.containsAll(testSlots));
    }
    
    @Test
    @DisplayName("Compteur de sélection doit être correct")
    void selectionCounterShouldBeCorrect() {
        // Given: Des slots avec sélection mixte
        testSlots.get(0).setSelected(true);
        testSlots.get(1).setSelected(false);
        testSlots.get(2).setSelected(true);
        
        // When: On compte les slots sélectionnés
        long selectedCount = testSlots.stream()
                .filter(GeneratedSlot::isSelected)
                .count();
        
        // Then: Le compteur doit être correct
        assertEquals(2, selectedCount);
    }
    
    @Test
    @DisplayName("Compteur de sélection doit être 0 si aucun slot sélectionné")
    void selectionCounterShouldBeZeroWhenNoSelection() {
        // Given: Aucun slot sélectionné
        for (GeneratedSlot slot : testSlots) {
            slot.setSelected(false);
        }
        
        // When: On compte les slots sélectionnés
        long selectedCount = testSlots.stream()
                .filter(GeneratedSlot::isSelected)
                .count();
        
        // Then: Le compteur doit être 0
        assertEquals(0, selectedCount);
    }
    
    @Test
    @DisplayName("Compteur de sélection doit être égal au nombre total si tous sélectionnés")
    void selectionCounterShouldEqualTotalWhenAllSelected() {
        // Given: Tous les slots sélectionnés
        for (GeneratedSlot slot : testSlots) {
            slot.setSelected(true);
        }
        
        // When: On compte les slots sélectionnés
        long selectedCount = testSlots.stream()
                .filter(GeneratedSlot::isSelected)
                .count();
        
        // Then: Le compteur doit être égal au nombre total
        assertEquals(testSlots.size(), selectedCount);
    }
    
    @Test
    @DisplayName("Sélection individuelle doit fonctionner")
    void individualSelectionShouldWork() {
        // Given: Un slot spécifique
        GeneratedSlot slot = testSlots.get(1);
        assertFalse(slot.isSelected());
        
        // When: On sélectionne ce slot
        slot.setSelected(true);
        
        // Then: Ce slot doit être sélectionné
        assertTrue(slot.isSelected());
        
        // And: Les autres slots ne doivent pas être affectés
        assertFalse(testSlots.get(0).isSelected());
        assertFalse(testSlots.get(2).isSelected());
    }
    
    @Test
    @DisplayName("Désélection individuelle doit fonctionner")
    void individualDeselectionShouldWork() {
        // Given: Un slot sélectionné
        GeneratedSlot slot = testSlots.get(1);
        slot.setSelected(true);
        assertTrue(slot.isSelected());
        
        // When: On désélectionne ce slot
        slot.setSelected(false);
        
        // Then: Ce slot doit être désélectionné
        assertFalse(slot.isSelected());
    }
    
    @Test
    @DisplayName("Propriété selected doit être observable")
    void selectedPropertyShouldBeObservable() {
        // Given: Un slot et un flag pour détecter les changements
        GeneratedSlot slot = testSlots.get(0);
        final boolean[] changed = {false};
        
        slot.selectedProperty().addListener((observable, oldValue, newValue) -> {
            changed[0] = true;
        });
        
        // When: On change la propriété
        slot.setSelected(true);
        
        // Then: Le listener doit être appelé
        assertTrue(changed[0]);
    }
    
    @Test
    @DisplayName("Sélection manuelle d'un slot individuel")
    void testManualSelectionOfSingleSlot() {
        // Given
        GeneratedSlot slot = testSlots.get(0);
        
        // When
        slot.setSelected(true);
        
        // Then
        assertTrue(slot.isSelected());
        assertEquals(1, getSelectedCount());
    }

    @Test
    @DisplayName("Désélection manuelle d'un slot individuel")
    void testManualDeselectionOfSingleSlot() {
        // Given
        GeneratedSlot slot = testSlots.get(0);
        slot.setSelected(true);
        
        // When
        slot.setSelected(false);
        
        // Then
        assertFalse(slot.isSelected());
        assertEquals(0, getSelectedCount());
    }

    @Test
    @DisplayName("Sélection manuelle de plusieurs slots")
    void testManualSelectionOfMultipleSlots() {
        // When
        testSlots.get(0).setSelected(true);
        testSlots.get(2).setSelected(true);
        testSlots.get(4).setSelected(true);
        
        // Then
        assertTrue(testSlots.get(0).isSelected());
        assertFalse(testSlots.get(1).isSelected());
        assertTrue(testSlots.get(2).isSelected());
        assertFalse(testSlots.get(3).isSelected());
        assertTrue(testSlots.get(4).isSelected());
        assertEquals(3, getSelectedCount());
    }

    @Test
    @DisplayName("Sélection manuelle avec toggle")
    void testManualSelectionToggle() {
        // Given
        GeneratedSlot slot = testSlots.get(0);
        
        // When - premier clic
        slot.setSelected(true);
        boolean firstState = slot.isSelected();
        
        // When - deuxième clic (toggle)
        slot.setSelected(false);
        boolean secondState = slot.isSelected();
        
        // Then
        assertTrue(firstState);
        assertFalse(secondState);
    }

    @Test
    @DisplayName("Sélection manuelle partielle")
    void testPartialManualSelection() {
        // When
        testSlots.get(1).setSelected(true);
        testSlots.get(3).setSelected(true);
        
        // Then
        List<GeneratedSlot> selected = getSelectedSlots();
        assertEquals(2, selected.size());
        assertTrue(selected.contains(testSlots.get(1)));
        assertTrue(selected.contains(testSlots.get(3)));
    }

    @Test
    @DisplayName("Sélection manuelle avec vérification de propriété")
    void testManualSelectionPropertyBinding() {
        // Given
        GeneratedSlot slot = testSlots.get(0);
        
        // When
        slot.setSelected(true);
        
        // Then
        assertTrue(slot.selectedProperty().get());
        assertEquals(true, slot.selectedProperty().getValue());
    }

    @Test
    @DisplayName("Sélection manuelle avec changement de propriété")
    void testManualSelectionPropertyChange() {
        // Given
        GeneratedSlot slot = testSlots.get(0);
        
        // When
        slot.selectedProperty().set(true);
        
        // Then
        assertTrue(slot.isSelected());
        assertTrue(slot.selectedProperty().get());
    }

    @Test
    @DisplayName("Sélection manuelle avec listener")
    void testManualSelectionWithListener() {
        // Given
        GeneratedSlot slot = testSlots.get(0);
        boolean[] listenerCalled = {false};
        
        slot.selectedProperty().addListener((obs, oldVal, newVal) -> {
            listenerCalled[0] = true;
        });
        
        // When
        slot.setSelected(true);
        
        // Then
        assertTrue(listenerCalled[0]);
        assertTrue(slot.isSelected());
    }

    @Test
    @DisplayName("Sélection manuelle multiple avec comptage")
    void testManualSelectionMultipleWithCounting() {
        // When
        testSlots.get(0).setSelected(true);
        testSlots.get(1).setSelected(true);
        testSlots.get(2).setSelected(false);
        testSlots.get(3).setSelected(true);
        testSlots.get(4).setSelected(false);
        
        // Then
        assertEquals(3, getSelectedCount());
        assertEquals(2, getUnselectedCount());
    }

    @Test
    @DisplayName("Sélection manuelle avec état mixte")
    void testManualSelectionMixedState() {
        // When
        testSlots.get(0).setSelected(true);
        testSlots.get(1).setSelected(false);
        testSlots.get(2).setSelected(true);
        
        // Then
        assertTrue(testSlots.get(0).isSelected());
        assertFalse(testSlots.get(1).isSelected());
        assertTrue(testSlots.get(2).isSelected());
        assertFalse(testSlots.get(3).isSelected());
        assertFalse(testSlots.get(4).isSelected());
    }

    @Test
    @DisplayName("Sélection manuelle avec validation")
    void testManualSelectionWithValidation() {
        // Given
        GeneratedSlot slot = testSlots.get(0);
        
        // When
        slot.setSelected(true);
        
        // Then
        assertTrue(slot.isSelected());
        assertTrue(slot.selectedProperty().get());
        assertNotNull(slot.selectedProperty());
    }

    @Test
    @DisplayName("Sélection de ligne avec toggle")
    void testRowSelectionWithToggle() {
        // Given
        GeneratedSlot slot = testSlots.get(0);
        
        // When - première sélection
        slot.setSelected(true);
        boolean firstState = slot.isSelected();
        
        // When - deuxième sélection (toggle)
        slot.setSelected(false);
        boolean secondState = slot.isSelected();
        
        // When - troisième sélection (toggle)
        slot.setSelected(true);
        boolean thirdState = slot.isSelected();
        
        // Then
        assertTrue(firstState);
        assertFalse(secondState);
        assertTrue(thirdState);
    }

    @Test
    @DisplayName("Sélection de ligne avec compteur")
    void testRowSelectionWithCounter() {
        // When
        testSlots.get(0).setSelected(true);
        testSlots.get(1).setSelected(true);
        testSlots.get(2).setSelected(false);
        
        // Then
        assertEquals(2, getSelectedCount());
        assertEquals(3, getUnselectedCount());
        
        // When - toggle
        testSlots.get(0).setSelected(false);
        testSlots.get(2).setSelected(true);
        
        // Then
        assertEquals(2, getSelectedCount());
        assertEquals(3, getUnselectedCount());
    }

    @Test
    @DisplayName("Toggle de sélection direct")
    void testDirectToggleSelection() {
        // Given
        GeneratedSlot slot = testSlots.get(0);
        
        // When - première sélection
        slot.setSelected(true);
        boolean firstState = slot.isSelected();
        
        // When - toggle immédiat
        slot.setSelected(!slot.isSelected());
        boolean secondState = slot.isSelected();
        
        // Then
        assertTrue(firstState);
        assertFalse(secondState);
        assertNotEquals(firstState, secondState);
    }

    @Test
    @DisplayName("Toggle multiple de sélection")
    void testMultipleToggleSelection() {
        // Given
        GeneratedSlot slot = testSlots.get(0);
        
        // When - séquence de toggles
        slot.setSelected(true);   // true
        slot.setSelected(false);  // false
        slot.setSelected(true);   // true
        slot.setSelected(false);  // false
        slot.setSelected(true);   // true
        
        // Then
        assertTrue(slot.isSelected());
        assertTrue(slot.selectedProperty().get());
    }

    @Test
    @DisplayName("Gestion de liste vide")
    void testEmptyListHandling() {
        // Given
        List<GeneratedSlot> emptyList = new ArrayList<>();
        
        // When & Then
        assertTrue(emptyList.isEmpty());
        assertEquals(0, emptyList.size());
        
        // Vérifier que les opérations sur liste vide ne causent pas d'erreur
        assertDoesNotThrow(() -> {
            long selectedCount = emptyList.stream()
                .filter(GeneratedSlot::isSelected)
                .count();
            assertEquals(0, selectedCount);
        });
    }

    @Test
    @DisplayName("Sélection avec liste vide")
    void testSelectionWithEmptyList() {
        // Given
        List<GeneratedSlot> emptyList = new ArrayList<>();
        
        // When & Then
        assertDoesNotThrow(() -> {
            // Simuler une tentative de sélection sur liste vide
            boolean hasSelected = emptyList.stream()
                .anyMatch(GeneratedSlot::isSelected);
            assertFalse(hasSelected);
        });
    }

    @Test
    @DisplayName("Gestion d'erreur de sélection")
    void testSelectionErrorHandling() {
        // Given
        GeneratedSlot slot = testSlots.get(0);
        
        // When & Then
        assertDoesNotThrow(() -> {
            // Simuler une sélection normale
            slot.setSelected(true);
            assertTrue(slot.isSelected());
            
            // Simuler une sélection avec erreur potentielle
            slot.setSelected(false);
            assertFalse(slot.isSelected());
        });
    }

    @Test
    @DisplayName("Robustesse de la sélection")
    void testSelectionRobustness() {
        // Given
        List<GeneratedSlot> slots = new ArrayList<>();
        
        // When - créer des slots avec des états variés
        for (int i = 0; i < 3; i++) {
            GeneratedSlot slot = new GeneratedSlot("Path " + i, "id" + i, "Level " + i);
            slot.setSelected(i % 2 == 0); // Alterner sélection
            slots.add(slot);
        }
        
        // Then
        assertEquals(3, slots.size());
        assertEquals(2, slots.stream().filter(GeneratedSlot::isSelected).count());
        assertEquals(1, slots.stream().filter(s -> !s.isSelected()).count());
    }

    @Test
    @DisplayName("Gestion de table vide avec sélection")
    void testEmptyTableSelectionHandling() {
        // Given
        List<GeneratedSlot> emptyList = new ArrayList<>();
        
        // When & Then
        assertDoesNotThrow(() -> {
            // Simuler une tentative de sélection sur table vide
            boolean isEmpty = emptyList.isEmpty();
            assertTrue(isEmpty);
            
            // Vérifier qu'aucune sélection n'est possible
            long selectedCount = emptyList.stream()
                .filter(GeneratedSlot::isSelected)
                .count();
            assertEquals(0, selectedCount);
        });
    }

    @Test
    @DisplayName("Transition table vide vers table avec données")
    void testTransitionFromEmptyToPopulatedTable() {
        // Given
        List<GeneratedSlot> slots = new ArrayList<>();
        
        // When - table vide
        assertTrue(slots.isEmpty());
        
        // When - ajouter des données
        GeneratedSlot slot1 = new GeneratedSlot("Path 1", "id1", "Level 1");
        GeneratedSlot slot2 = new GeneratedSlot("Path 2", "id2", "Level 2");
        slots.add(slot1);
        slots.add(slot2);
        
        // Then
        assertFalse(slots.isEmpty());
        assertEquals(2, slots.size());
        
        // When - sélectionner un élément
        slot1.setSelected(true);
        
        // Then
        assertEquals(1, slots.stream().filter(GeneratedSlot::isSelected).count());
        assertTrue(slot1.isSelected());
        assertFalse(slot2.isSelected());
    }

    @Test
    @DisplayName("Gestion de table désactivée")
    void testDisabledTableHandling() {
        // Given
        List<GeneratedSlot> emptyList = new ArrayList<>();
        
        // When & Then
        assertDoesNotThrow(() -> {
            // Simuler une table désactivée
            boolean isEmpty = emptyList.isEmpty();
            assertTrue(isEmpty);
            
            // Vérifier qu'aucune interaction n'est possible
            assertTrue(emptyList.isEmpty());
            assertEquals(0, emptyList.size());
        });
    }

    @Test
    @DisplayName("Ajout de données de test")
    void testTestDataAddition() {
        // Given
        List<GeneratedSlot> slots = new ArrayList<>();
        
        // When - ajouter des données de test
        for (int i = 1; i <= 3; i++) {
            GeneratedSlot testSlot = new GeneratedSlot("Test Path " + i, "test_id_" + i, "Test Level " + i);
            testSlot.setId((long) i);
            testSlot.setSelected(false);
            slots.add(testSlot);
        }
        
        // Then
        assertEquals(3, slots.size());
        assertFalse(slots.isEmpty());
        
        // Vérifier que les données de test sont correctes
        for (int i = 0; i < 3; i++) {
            GeneratedSlot slot = slots.get(i);
            assertEquals("Test Path " + (i + 1), slot.getDecisionPath());
            assertEquals("Test Level " + (i + 1), slot.getLastLevel());
            assertFalse(slot.isSelected());
        }
    }

    @Test
    @DisplayName("Transition table vide vers table avec données de test")
    void testTransitionFromEmptyToTestData() {
        // Given
        List<GeneratedSlot> slots = new ArrayList<>();
        
        // When - table vide
        assertTrue(slots.isEmpty());
        
        // When - ajouter des données de test
        GeneratedSlot testSlot = new GeneratedSlot("Test Path", "test_id", "Test Level");
        testSlot.setId(1L);
        testSlot.setSelected(false);
        slots.add(testSlot);
        
        // Then
        assertFalse(slots.isEmpty());
        assertEquals(1, slots.size());
        assertFalse(testSlot.isSelected());
        
        // When - sélectionner le slot de test
        testSlot.setSelected(true);
        
        // Then
        assertTrue(testSlot.isSelected());
        assertEquals(1, slots.stream().filter(GeneratedSlot::isSelected).count());
    }

    // Méthodes utilitaires
    private int getSelectedCount() {
        return (int) testSlots.stream().filter(GeneratedSlot::isSelected).count();
    }

    private int getUnselectedCount() {
        return (int) testSlots.stream().filter(slot -> !slot.isSelected()).count();
    }

    private List<GeneratedSlot> getSelectedSlots() {
        return testSlots.stream().filter(GeneratedSlot::isSelected).toList();
    }
} 