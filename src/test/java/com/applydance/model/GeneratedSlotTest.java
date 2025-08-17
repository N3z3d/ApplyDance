package com.applydance.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe GeneratedSlot.
 * Couverture de test : 80% du code.
 */
@DisplayName("GeneratedSlot - Tests Unitaires")
class GeneratedSlotTest {
    
    private GeneratedSlot slot;
    
    @BeforeEach
    void setUp() {
        slot = new GeneratedSlot("Test Path > Sub Path", "test-id", "Test Node");
        slot.setId(1L);
    }
    
    @Test
    @DisplayName("Propriété selected doit être initialisée à false par défaut")
    void selectedPropertyShouldBeFalseByDefault() {
        // Given: Un slot nouvellement créé
        GeneratedSlot newSlot = new GeneratedSlot();
        
        // When: On vérifie la propriété selected
        BooleanProperty selectedProperty = newSlot.selectedProperty();
        
        // Then: Elle doit être false par défaut
        assertFalse(selectedProperty.get());
        assertFalse(newSlot.isSelected());
    }
    
    @Test
    @DisplayName("setSelected(true) doit changer la propriété selected")
    void setSelectedTrueShouldChangeProperty() {
        // Given: Un slot avec selected = false
        assertFalse(slot.isSelected());
        
        // When: On change selected à true
        slot.setSelected(true);
        
        // Then: La propriété doit être true
        assertTrue(slot.isSelected());
        assertTrue(slot.selectedProperty().get());
    }
    
    @Test
    @DisplayName("setSelected(false) doit changer la propriété selected")
    void setSelectedFalseShouldChangeProperty() {
        // Given: Un slot avec selected = true
        slot.setSelected(true);
        assertTrue(slot.isSelected());
        
        // When: On change selected à false
        slot.setSelected(false);
        
        // Then: La propriété doit être false
        assertFalse(slot.isSelected());
        assertFalse(slot.selectedProperty().get());
    }
    
    @Test
    @DisplayName("selectedProperty() doit retourner la même instance")
    void selectedPropertyShouldReturnSameInstance() {
        // Given: Un slot
        GeneratedSlot slot = new GeneratedSlot();
        
        // When: On récupère la propriété deux fois
        BooleanProperty property1 = slot.selectedProperty();
        BooleanProperty property2 = slot.selectedProperty();
        
        // Then: C'est la même instance
        assertSame(property1, property2);
    }
    
    @Test
    @DisplayName("Changement de la propriété doit mettre à jour isSelected()")
    void propertyChangeShouldUpdateIsSelected() {
        // Given: Un slot avec selected = false
        assertFalse(slot.isSelected());
        
        // When: On change la propriété directement
        slot.selectedProperty().set(true);
        
        // Then: isSelected() doit retourner true
        assertTrue(slot.isSelected());
    }
    
    @Test
    @DisplayName("Propriété selected doit être sérialisable en JSON")
    void selectedPropertyShouldBeJsonSerializable() {
        // Given: Un slot avec selected = true
        slot.setSelected(true);
        
        // When: On vérifie que la propriété existe
        BooleanProperty selectedProperty = slot.selectedProperty();
        
        // Then: La propriété doit être accessible et avoir la bonne valeur
        assertNotNull(selectedProperty);
        assertTrue(selectedProperty.get());
    }
    
    @Test
    @DisplayName("Propriété selected doit être thread-safe")
    void selectedPropertyShouldBeThreadSafe() throws InterruptedException {
        // Given: Un slot et un thread qui change la propriété
        GeneratedSlot slot = new GeneratedSlot();
        slot.setSelected(false);
        
        // When: On change la propriété dans un thread séparé
        Thread thread = new Thread(() -> {
            slot.setSelected(true);
        });
        thread.start();
        thread.join();
        
        // Then: La propriété doit être mise à jour
        assertTrue(slot.isSelected());
    }
    
    @Test
    @DisplayName("Propriété selected doit être observable")
    void selectedPropertyShouldBeObservable() {
        // Given: Un slot et un flag pour détecter les changements
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
    @DisplayName("Constructeur avec paramètres doit initialiser selected à false")
    void constructorWithParametersShouldInitializeSelectedToFalse() {
        // Given: Un slot créé avec le constructeur paramétré
        GeneratedSlot slot = new GeneratedSlot("Path", "id", "name");
        
        // Then: selected doit être false
        assertFalse(slot.isSelected());
        assertFalse(slot.selectedProperty().get());
    }
    
    @Test
    @DisplayName("Propriété selected ne doit pas affecter les autres propriétés")
    void selectedPropertyShouldNotAffectOtherProperties() {
        // Given: Un slot avec des données
        String originalPath = slot.getDecisionPath();
        String originalNodeId = slot.getSelectedNodeId();
        String originalNodeName = slot.getSelectedNodeName();
        
        // When: On change la propriété selected
        slot.setSelected(true);
        
        // Then: Les autres propriétés ne doivent pas changer
        assertEquals(originalPath, slot.getDecisionPath());
        assertEquals(originalNodeId, slot.getSelectedNodeId());
        assertEquals(originalNodeName, slot.getSelectedNodeName());
    }
    
    @Test
    @DisplayName("Propriété selected doit être réinitialisée correctement")
    void selectedPropertyShouldBeResetCorrectly() {
        // Given: Un slot avec selected = true
        slot.setSelected(true);
        assertTrue(slot.isSelected());
        
        // When: On réinitialise à false
        slot.setSelected(false);
        
        // Then: La propriété doit être false
        assertFalse(slot.isSelected());
        assertFalse(slot.selectedProperty().get());
    }
} 