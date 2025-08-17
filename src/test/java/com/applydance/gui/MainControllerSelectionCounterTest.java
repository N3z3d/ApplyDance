package com.applydance.gui;

import com.applydance.model.GeneratedSlot;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires ciblant la logique de sélection dans {@link MainController} :
 *  • selectAllSlots()
 *  • deselectAllSlots()
 *  • getSelectedSlots()
 *  • Mise à jour du compteur visuel
 */
@DisplayName("MainController - Sélection & Compteur")
class MainControllerSelectionCounterTest {

    private MainController controller;

    @BeforeAll
    static void initJfx() throws Exception {
        // Initialise le toolkit JavaFX une seule fois pour l'ensemble de la classe
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            // Toolkit déjà démarré
            latch.countDown();
        }
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Impossible d'initialiser JavaFX");
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new MainController();

        // Injecter les champs privés via réflexion
        setPrivateField(controller, "selectionCounterLabel", new Label());
        setPrivateField(controller, "deleteSelectedButton", new Button());

        // Ajouter des données dans historyData
        @SuppressWarnings("unchecked")
        var historyData = (javafx.collections.ObservableList<GeneratedSlot>) getPrivateField(controller, "historyData");

        GeneratedSlot s1 = new GeneratedSlot("Path1", "id1", "Node1");
        s1.setId(1L);
        s1.setGeneratedAt(LocalDateTime.now());
        GeneratedSlot s2 = new GeneratedSlot("Path2", "id2", "Node2");
        s2.setId(2L);
        s2.setGeneratedAt(LocalDateTime.now());
        GeneratedSlot s3 = new GeneratedSlot("Path3", "id3", "Node3");
        s3.setId(3L);
        s3.setGeneratedAt(LocalDateTime.now());

        historyData.addAll(s1, s2, s3);

        // Appeler attachSelectionListeners() en réflexion
        invokePrivateMethod(controller, "attachSelectionListeners");
    }

    @Test
    @DisplayName("selectAllSlots doit sélectionner tous les éléments et mettre à jour le compteur")
    void selectAllSlotsUpdatesCounter() throws Exception {
        Platform.runLater(() -> {
            invokePrivateMethod(controller, "selectAllSlots");
        });
        waitFx();
        assertEquals(3, invokePrivateMethodReturning(controller, "getSelectedSlots", java.util.List.class).size());
        Label counter = (Label) getPrivateField(controller, "selectionCounterLabel");
        assertTrue(counter.getText().startsWith("3 "));
    }

    @Test
    @DisplayName("deselectAllSlots doit tout désélectionner et remettre le compteur à 0")
    void deselectAllSlotsUpdatesCounter() throws Exception {
        // Pré-sélectionner
        Platform.runLater(() -> invokePrivateMethod(controller, "selectAllSlots"));
        waitFx();
        // Maintenant désélectionner
        Platform.runLater(() -> invokePrivateMethod(controller, "deselectAllSlots"));
        waitFx();
        assertEquals(0, invokePrivateMethodReturning(controller, "getSelectedSlots", java.util.List.class).size());
        Label counter2 = (Label) getPrivateField(controller, "selectionCounterLabel");
        assertTrue(counter2.getText().startsWith("0 "));
    }

    @Test
    @DisplayName("Sélection individuelle met à jour le compteur dynamiquement")
    void individualSelectionUpdatesCounter() throws Exception {
        @SuppressWarnings("unchecked")
        var data = (javafx.collections.ObservableList<GeneratedSlot>) getPrivateField(controller, "historyData");
        Platform.runLater(() -> data.get(0).setSelected(true));
        waitFx();
        Label counter3 = (Label) getPrivateField(controller, "selectionCounterLabel");
        assertTrue(counter3.getText().startsWith("1 "));
    }

    // Utilitaire pour attendre la fin des tâches JavaFX
    private void waitFx() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        if (!latch.await(5, TimeUnit.SECONDS)) {
            fail("Timeout d'attente JavaFX");
        }
    }

    // Méthodes utilitaires de réflexion
    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Object getPrivateField(Object target, String fieldName) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void invokePrivateMethod(Object target, String methodName, Object... args) {
        try {
            var method = target.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokePrivateMethodReturning(Object target, String methodName, Class<T> returnType) {
        try {
            var method = target.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            return (T) method.invoke(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
} 