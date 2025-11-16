package com.applydance.gui;

import com.applydance.model.TreeNode;
import com.applydance.service.TreeConfigurationService;
import java.util.UUID;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Contrôleur JavaFX pour l'interface d'arbre avec glisser-déposer.
 * Remplace l'ancien système par un système totalement générique et configurable.
 */
public class TreeDragDropController {
    
        private static final Logger logger = LoggerFactory.getLogger(TreeDragDropController.class);
    private static final DataFormat TREE_NODE_FORMAT = new DataFormat("application/tree-node-id");
    private static final String DEFAULT_COLOR = "#5E81AC";

    private final TreeConfigurationService configService;
    private VBox treeContainer;
    private TreeNode draggedNode;
    
    // Constructeur qui accepte une instance existante de TreeConfigurationService (recommandé)
    public TreeDragDropController(TreeConfigurationService configService) {
        this.configService = configService;
        
        // Écouter les changements de configuration pour rafraîchir l'affichage
        this.configService.addChangeListener(this::refreshTreeDisplay);
    }
    
    // Constructeur par défaut (pour compatibilité)
    public TreeDragDropController() {
        this.configService = new TreeConfigurationService();
        
        // Écouter les changements de configuration pour rafraîchir l'affichage
        this.configService.addChangeListener(this::refreshTreeDisplay);
    }
    
    /**
     * Crée l'interface principale de configuration d'arbre avec drag & drop
     */
    public ScrollPane createTreeConfigInterface() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #ECEFF4;");
        
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #ECEFF4;");
        
        // Header magnifique
        VBox header = createHeader();
        
        // Conteneur pour l'arbre avec drag & drop
        treeContainer = createTreeContainer();
        
        // Actions globales
        HBox globalActions = createGlobalActions();
        
        mainContainer.getChildren().addAll(header, treeContainer, globalActions);
        scrollPane.setContent(mainContainer);
        
        // Charger l'affichage initial - FORCER le rafraîchissement initial
        Platform.runLater(() -> {
            refreshTreeDisplay(configService.getRootNode());
        });
        
        return scrollPane;
    }
    
    /**
     * Crée le header avec titre et description
     */
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        
        // Titre principal avec style magnifique
        Label titleLabel = new Label("ARBRE CONFIGURABLE AVEC GLISSER-DÉPOSER");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #2E3440; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);");
        
        // Description interactive améliorée
        Label descLabel = new Label("🎯 Clic simple pour éditer • Glisser-déposer pour réorganiser • Menu ⋯ pour plus d'options");
        descLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        descLabel.setStyle("-fx-text-fill: #5E81AC; -fx-background-color: rgba(94, 129, 172, 0.1); " +
                          "-fx-background-radius: 15; -fx-padding: 8;");
        
        header.getChildren().addAll(titleLabel, descLabel);
        return header;
    }
    
    /**
     * Crée le conteneur principal de l'arbre
     */
    private VBox createTreeContainer() {
        VBox container = new VBox(10);
        container.setStyle("-fx-background-color: white; " +
                          "-fx-background-radius: 20; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 8); " +
                          "-fx-padding: 25;");
        
        return container;
    }
    
    /**
     * Crée les actions globales
     */
    private HBox createGlobalActions() {
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(20, 0, 0, 0));
        
        // Bouton Expansion/Contraction
        Button expandButton = new Button("Tout Développer");
        expandButton.setStyle("-fx-background-color: #5E81AC; -fx-text-fill: white; " +
                             "-fx-background-radius: 15; -fx-padding: 12 24; " +
                             "-fx-font-weight: bold; -fx-font-size: 14;");
        expandButton.setOnAction(e -> {
            boolean expand = expandButton.getText().contains("Développer");
            expandCollapseAll(configService.getRootNode(), expand);
            expandButton.setText(expand ? "Tout Réduire" : "Tout Développer");
            refreshTreeDisplay(configService.getRootNode());
        });
        
        // Bouton d'import/rechargement
        Button reloadButton = new Button("🔄 Recharger Config");
        reloadButton.setStyle("-fx-background-color: #88C0D0; -fx-text-fill: white; " +
                             "-fx-background-radius: 15; -fx-padding: 12 24; " +
                             "-fx-font-weight: bold; -fx-font-size: 14;");
        reloadButton.setOnAction(e -> importTreeConfiguration());
        
        actions.getChildren().addAll(expandButton, reloadButton);
        return actions;
    }
    
    /**
     * Rafraîchit l'affichage de l'arbre
     */
    private void refreshTreeDisplay(TreeNode rootNode) {
        // S'assurer que l'opération s'exécute sur le thread JavaFX
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> refreshTreeDisplay(rootNode));
            return;
        }
        
        logger.debug("Rafraîchissement de l'affichage de l'arbre");
        treeContainer.getChildren().clear();
        
        if (rootNode != null) {
            logger.debug("Affichage de la racine : {} (expanded: {}, children: {})", 
                        rootNode.getLabel(), rootNode.isExpanded(), rootNode.hasChildren());
            
            // Afficher le nœud racine
            VBox rootDisplay = createNodeDisplay(rootNode, true);
            treeContainer.getChildren().add(rootDisplay);
            
            // Afficher les enfants - par défaut la racine est expanded
            if (rootNode.hasChildren()) {
                if (!rootNode.isExpanded()) {
                    // Par défaut, expandre la racine
                    rootNode.setExpanded(true);
                }
                
                VBox childrenContainer = new VBox(5);
                childrenContainer.setPadding(new Insets(10, 0, 0, 30));
                
                for (TreeNode child : rootNode.getChildren()) {
                    logger.debug("Affichage enfant : {} ({}%)", child.getLabel(), child.getPercentage());
                    VBox childDisplay = createNodeDisplay(child, false);
                    childrenContainer.getChildren().add(childDisplay);
                    
                    // Récursif pour les petits-enfants
                    if (child.hasChildren() && child.isExpanded()) {
                        VBox grandChildrenContainer = createChildrenContainer(child);
                        childrenContainer.getChildren().add(grandChildrenContainer);
                    }
                }
                
                treeContainer.getChildren().add(childrenContainer);
            }
        } else {
            logger.warn("RootNode est null lors du rafraîchissement");
        }
    }
    
    /**
     * Crée récursivement le conteneur des enfants
     */
    private VBox createChildrenContainer(TreeNode parent) {
        VBox container = new VBox(5);
        container.setPadding(new Insets(5, 0, 0, 30));
        
        if (parent.hasChildren()) {
            for (TreeNode child : parent.getChildren()) {
                VBox childDisplay = createNodeDisplay(child, false);
                container.getChildren().add(childDisplay);
                
                if (child.hasChildren() && child.isExpanded()) {
                    VBox grandChildrenContainer = createChildrenContainer(child);
                    container.getChildren().add(grandChildrenContainer);
                }
            }
        }
        
        return container;
    }
    
    /**
     * Crée l'affichage d'un nœud avec drag & drop
     */
    private VBox createNodeDisplay(TreeNode node, boolean isRoot) {
        VBox nodeContainer = new VBox(5);
        
        // Ligne principale du nœud
        HBox nodeRow = createNodeRow(node, isRoot);
        nodeContainer.getChildren().add(nodeRow);
        
        return nodeContainer;
    }
    
    /**
     * Crée la ligne d'affichage d'un nœud avec édition inline optimisée
     */
    private HBox createNodeRow(TreeNode node, boolean isRoot) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        
        // Style en fonction du niveau
        String rowBackgroundColor = isRoot ? "rgba(46, 52, 64, 0.1)" : "rgba(255, 255, 255, 0.8)";
        String borderColor = node.getColor();
        
        row.setStyle("-fx-background-color: " + rowBackgroundColor + "; " +
                    "-fx-background-radius: 12; " +
                    "-fx-border-color: " + borderColor + "; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 12; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        
        // Bouton d'expansion/contraction
        Button expandButton = new Button();
        if (node.hasChildren()) {
            expandButton.setText(node.isExpanded() ? "▼" : "▶");
            expandButton.setOnAction(e -> {
                node.setExpanded(!node.isExpanded());
                configService.updateNode(node);
            });
        } else {
            expandButton.setText("•");
            expandButton.setDisable(true);
        }
        expandButton.setStyle("-fx-background-color: transparent; " +
                             "-fx-text-fill: " + node.getColor() + "; " +
                             "-fx-font-weight: bold; " +
                             "-fx-font-size: 14;");
        
        // Bouton d'ajout d'enfant à gauche
        Button addChildButton = new Button("+");
        addChildButton.setStyle("-fx-background-color: #A3BE8C; -fx-text-fill: white; " +
                               "-fx-background-radius: 6; -fx-padding: 4; -fx-font-size: 12; " +
                               "-fx-font-weight: bold; -fx-min-width: 25; -fx-max-width: 25;");
        addChildButton.setOnAction(e -> showAddChildDialog(node));
        
        // Label du nœud avec édition inline pour le nom
        Label nameLabel = createInlineEditableLabel(node, "name");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        nameLabel.setStyle("-fx-text-fill: #2E3440; -fx-cursor: hand;");
        
        // Composant de pourcentage avec édition inline optimisée
        Node percentageComponent = createInlineEditablePercentage(node, isRoot);
        
        // Indicateur de validation pour l'ensemble des frères/sœurs
        Label gapIndicator = createGapIndicator(node);
        
        // Bouton d'actions contextuelles (menu trois points)
        Button actionButton = createContextualActionButton(node, isRoot);
        
        // Spacer pour pousser les éléments à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Assemblage des composants avec le nouvel agencement UX optimisé
        if (gapIndicator != null) {
            row.getChildren().addAll(expandButton, addChildButton, nameLabel, spacer, 
                                   percentageComponent, gapIndicator, actionButton);
        } else {
            row.getChildren().addAll(expandButton, addChildButton, nameLabel, spacer, 
                                   percentageComponent, actionButton);
        }
        
        // Hover effect pour améliorer l'interaction
        row.setOnMouseEntered(e -> {
            String currentStyle = row.getStyle();
            if (!currentStyle.contains("-fx-background-color: rgba(94, 129, 172, 0.1)")) {
                row.setStyle(currentStyle + "-fx-background-color: rgba(94, 129, 172, 0.1);");
            }
        });
        
        row.setOnMouseExited(e -> {
            String currentStyle = row.getStyle();
            row.setStyle(currentStyle.replace("-fx-background-color: rgba(94, 129, 172, 0.1);", 
                                           "-fx-background-color: " + rowBackgroundColor + ";"));
        });
        
        // Configuration du drag & drop
        setupDragAndDrop(row, node, isRoot);
        
        return row;
    }
    
    /**
     * Crée un label éditable inline pour les propriétés de nœud
     */
    private Label createInlineEditableLabel(TreeNode node, String property) {
        Label label = new Label();
        
        // Initialiser le contenu selon la propriété
        if ("name".equals(property)) {
            label.setText(node.getName());
        }
        
        // Styles de base
        label.setStyle("-fx-cursor: hand; -fx-padding: 4 8; -fx-background-radius: 6;");
        
        // Tooltip instructif
        Tooltip tooltip = new Tooltip("Cliquez pour éditer");
        tooltip.setStyle("-fx-background-color: #2E3440; -fx-text-fill: white; -fx-background-radius: 6;");
        Tooltip.install(label, tooltip);
        
        // Événement de clic pour l'édition inline
        label.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                activateInlineEdit(label, node, property);
            }
        });
        
        return label;
    }
    
    /**
     * Crée un composant d'édition inline pour les pourcentages
     */
    private Node createInlineEditablePercentage(TreeNode node, boolean isRoot) {
        Label percentageLabel = new Label(String.format("%.1f%%", node.getPercentage()));
        percentageLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        // Validation et style du pourcentage
        TreeNode parent = findParentNode(node);
        boolean isValidPercentage = true;
        if (parent != null && parent.hasChildren()) {
            double sum = parent.getChildren().stream()
                    .mapToDouble(TreeNode::getPercentage)
                    .sum();
            isValidPercentage = Math.abs(sum - 100.0) < 0.1;
        }
        
        String nodeColor = node.getColor() != null ? node.getColor() : DEFAULT_COLOR;
        String percentageColor = isValidPercentage ? nodeColor : "#BF616A";
        String backgroundColor = isValidPercentage ? "rgba(255,255,255,0.8)" : "rgba(191, 97, 106, 0.2)";
        
        percentageLabel.setStyle("-fx-text-fill: " + percentageColor + "; " +
                                "-fx-background-color: " + backgroundColor + "; " +
                                "-fx-background-radius: 8; " +
                                "-fx-padding: 4 8; " +
                                "-fx-cursor: hand;");
        
        // Tooltip informatif amélioré
        String tooltipText = "Cliquez pour éditer le pourcentage";
        if (!isValidPercentage && parent != null) {
            double sum = parent.getChildren().stream()
                    .mapToDouble(TreeNode::getPercentage)
                    .sum();
            tooltipText = sum < 100 ? 
                String.format("⚠️ Somme: %.1f%% (manque %.1f%%) • Cliquez pour éditer", sum, 100.0 - sum) :
                String.format("⚠️ Somme: %.1f%% (excès %.1f%%) • Cliquez pour éditer", sum, sum - 100.0);
        }
        
        Tooltip percentageTooltip = new Tooltip(tooltipText);
        percentageTooltip.setStyle("-fx-background-color: " + percentageColor + "; -fx-text-fill: white; -fx-background-radius: 6;");
        Tooltip.install(percentageLabel, percentageTooltip);
        
        // Clic simple pour éditer
        percentageLabel.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                activateInlinePercentageEdit(percentageLabel, node);
            }
        });
        
        return percentageLabel;
    }
    
    /**
     * Active l'édition inline pour un label de texte
     */
    private void activateInlineEdit(Label label, TreeNode node, String property) {
        // Sauvegarder le contenu original
        String originalText = label.getText();
        
        // Créer le champ d'édition
        TextField textField = new TextField(originalText);
        textField.setStyle("-fx-background-color: white; -fx-border-color: #5E81AC; " +
                          "-fx-border-width: 2; -fx-border-radius: 6; -fx-padding: 4 8;");
        textField.setPrefWidth(label.getWidth());
        textField.selectAll();
        
        // Remplacer le label par le TextField
        HBox parent = (HBox) label.getParent();
        int index = parent.getChildren().indexOf(label);
        parent.getChildren().set(index, textField);
        
        // Focus sur le champ
        Platform.runLater(() -> textField.requestFocus());
        
        // Fonction de validation et sauvegarde
        Runnable saveEdit = () -> {
            String newText = textField.getText().trim();
            if (!newText.isEmpty() && !newText.equals(originalText)) {
                // Appliquer la modification
                if ("name".equals(property)) {
                    node.setName(newText);
                    label.setText(newText);
                }
                
                // Sauvegarder
                configService.updateNode(node);
                
                // Feedback visuel
                showQuickFeedback(textField, "#A3BE8C", "✓");
            } else {
                label.setText(originalText);
            }
            
            // Restaurer le label
            parent.getChildren().set(index, label);
        };
        
        // Annulation
        Runnable cancelEdit = () -> {
            label.setText(originalText);
            parent.getChildren().set(index, label);
        };
        
        // Gestionnaires d'événements
        textField.setOnAction(e -> saveEdit.run());
        textField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                cancelEdit.run();
            }
        });
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Perte de focus
                saveEdit.run();
            }
        });
    }
    
    /**
     * Active l'édition inline pour un pourcentage
     */
    private void activateInlinePercentageEdit(Label percentageLabel, TreeNode node) {
        // Sauvegarder la valeur originale
        double originalValue = node.getPercentage();
        
        // Créer le spinner d'édition
        Spinner<Double> spinner = new Spinner<>(0.0, 100.0, originalValue, 0.5);
        spinner.setEditable(true);
        spinner.setPrefWidth(80);
        spinner.setStyle("-fx-background-color: white; -fx-border-color: #5E81AC; " +
                        "-fx-border-width: 2; -fx-border-radius: 6;");
        
        // Remplacer le label par le spinner
        HBox parent = (HBox) percentageLabel.getParent();
        int index = parent.getChildren().indexOf(percentageLabel);
        parent.getChildren().set(index, spinner);
        
        // Focus sur le spinner
        Platform.runLater(() -> {
            spinner.requestFocus();
            spinner.getEditor().selectAll();
        });
        
        // Validation temps réel pendant la saisie
        spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Mettre à jour temporairement pour le preview
                updatePercentagePreview(percentageLabel, newVal, node);
            }
        });
        
        // Fonction de validation et sauvegarde
        Runnable saveEdit = () -> {
            double newValue = spinner.getValue();
            if (newValue != originalValue) {
                // Appliquer la modification
                node.setPercentage(newValue);
                
                // Mettre à jour le label
                percentageLabel.setText(String.format("%.1f%%", newValue));
                updatePercentageStyle(percentageLabel, node);
                
                // Sauvegarder
                configService.updateNode(node);
                
                // Feedback visuel
                showQuickFeedback(spinner, "#A3BE8C", "✓");
                
                // Rafraîchir l'affichage pour mettre à jour les indicateurs de validation
                Platform.runLater(() -> refreshTreeDisplay(configService.getRootNode()));
            }
            
            // Restaurer le label
            Platform.runLater(() -> parent.getChildren().set(index, percentageLabel));
        };
        
        // Annulation
        Runnable cancelEdit = () -> {
            percentageLabel.setText(String.format("%.1f%%", originalValue));
            updatePercentageStyle(percentageLabel, node);
            parent.getChildren().set(index, percentageLabel);
        };
        
        // Gestionnaires d'événements
        spinner.getEditor().setOnAction(e -> saveEdit.run());
        spinner.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                cancelEdit.run();
            }
        });
        spinner.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Perte de focus
                saveEdit.run();
            }
        });
    }
    
    /**
     * Met à jour le style du label de pourcentage selon la validation
     */
    private void updatePercentageStyle(Label percentageLabel, TreeNode node) {
        TreeNode parent = findParentNode(node);
        boolean isValidPercentage = true;
        if (parent != null && parent.hasChildren()) {
            double sum = parent.getChildren().stream()
                    .mapToDouble(TreeNode::getPercentage)
                    .sum();
            isValidPercentage = Math.abs(sum - 100.0) < 0.1;
        }
        
        String nodeColor = node.getColor() != null ? node.getColor() : DEFAULT_COLOR;
        String percentageColor = isValidPercentage ? nodeColor : "#BF616A";
        String backgroundColor = isValidPercentage ? "rgba(255,255,255,0.8)" : "rgba(191, 97, 106, 0.2)";
        
        percentageLabel.setStyle("-fx-text-fill: " + percentageColor + "; " +
                                "-fx-background-color: " + backgroundColor + "; " +
                                "-fx-background-radius: 8; " +
                                "-fx-padding: 4 8; " +
                                "-fx-cursor: hand;");
    }
    
    /**
     * Aperçu temps réel du pourcentage pendant l'édition
     */
    private void updatePercentagePreview(Label percentageLabel, double newValue, TreeNode node) {
        // Cette méthode peut être étendue pour un feedback temps réel plus sophistiqué
        // Pour l'instant, on utilise updatePercentageStyle après sauvegarde
    }
    
    /**
     * Crée l'indicateur d'écart de pourcentage pour les groupes de frères/sœurs
     */
    private Label createGapIndicator(TreeNode node) {
        TreeNode parent = findParentNode(node);
        if (parent == null || !parent.hasChildren()) {
            return null;
        }
        
        // Afficher uniquement sur le premier enfant pour éviter la duplication
        if (parent.getChildren().indexOf(node) != 0) {
            return null;
        }
        
        double sum = parent.getChildren().stream()
                .mapToDouble(TreeNode::getPercentage)
                .sum();
        boolean isValidPercentage = Math.abs(sum - 100.0) < 0.1;
        
        if (isValidPercentage) {
            return null; // Pas d'indicateur si tout est correct
        }
        
        double gap = 100.0 - sum;
        String gapText;
        String gapColor;
        
        if (gap > 0.1) {
            gapText = String.format("[-%.1f%%]", gap);
            gapColor = "#D08770"; // Orange pour manquant
        } else {
            gapText = String.format("[+%.1f%%]", Math.abs(gap));
            gapColor = "#BF616A"; // Rouge pour excès
        }
        
        Label gapIndicator = new Label(gapText);
        gapIndicator.setFont(Font.font("System", FontWeight.BOLD, 10));
        gapIndicator.setStyle("-fx-text-fill: " + gapColor + "; " +
                            "-fx-background-color: rgba(255,255,255,0.9); " +
                            "-fx-background-radius: 6; " +
                            "-fx-padding: 2 6; " +
                            "-fx-border-color: " + gapColor + "; " +
                            "-fx-border-radius: 6; " +
                            "-fx-border-width: 1;");
        
        // Tooltip explicatif
        String tooltipText = gap > 0.1 ? 
            String.format("Il manque %.1f%% pour atteindre 100%%", gap) :
            String.format("Dépassement de %.1f%% par rapport à 100%%", Math.abs(gap));
        Tooltip gapTooltip = new Tooltip(tooltipText);
        gapTooltip.setStyle("-fx-background-color: " + gapColor + "; -fx-text-fill: white; -fx-background-radius: 6;");
        Tooltip.install(gapIndicator, gapTooltip);
        
        return gapIndicator;
    }
    
    /**
     * Crée le bouton d'actions contextuelles (menu trois points)
     */
    private Button createContextualActionButton(TreeNode node, boolean isRoot) {
        Button actionButton = new Button("⋯");
        actionButton.setStyle("-fx-background-color: transparent; " +
                             "-fx-text-fill: #5E81AC; " +
                             "-fx-font-weight: bold; " +
                             "-fx-font-size: 16; " +
                             "-fx-padding: 4 8; " +
                             "-fx-background-radius: 6; " +
                             "-fx-cursor: hand;");
        
        // Effet hover
        actionButton.setOnMouseEntered(e -> {
            actionButton.setStyle(actionButton.getStyle() + "-fx-background-color: rgba(94, 129, 172, 0.2);");
        });
        actionButton.setOnMouseExited(e -> {
            actionButton.setStyle(actionButton.getStyle().replace("-fx-background-color: rgba(94, 129, 172, 0.2);", ""));
        });
        
        // Menu contextuel
        ContextMenu contextMenu = new ContextMenu();
        
        // Option: Éditer tout
        MenuItem editAllItem = new MenuItem("✏️ Éditer tout");
        editAllItem.setOnAction(e -> showEditDeleteDialog(node));
        
        // Option: Dupliquer (si pas racine)
        if (!isRoot) {
            MenuItem duplicateItem = new MenuItem("📋 Dupliquer");
            duplicateItem.setOnAction(e -> duplicateNode(node));
            contextMenu.getItems().add(duplicateItem);
        }
        
        contextMenu.getItems().add(editAllItem);
        
        // Séparateur et suppression (si pas racine)
        if (!isRoot) {
            contextMenu.getItems().add(new SeparatorMenuItem());
            MenuItem deleteItem = new MenuItem("🗑️ Supprimer");
            deleteItem.setStyle("-fx-text-fill: #BF616A;");
            deleteItem.setOnAction(e -> confirmAndDeleteNode(node));
            contextMenu.getItems().add(deleteItem);
        }
        
        // Afficher le menu au clic
        actionButton.setOnAction(e -> {
            contextMenu.show(actionButton, Side.BOTTOM, 0, 0);
        });
        
        // Tooltip
        Tooltip tooltip = new Tooltip("Actions");
        tooltip.setStyle("-fx-background-color: #2E3440; -fx-text-fill: white; -fx-background-radius: 6;");
        Tooltip.install(actionButton, tooltip);
        
        return actionButton;
    }
    
    /**
     * Duplique un nœud
     */
    private void duplicateNode(TreeNode node) {
        TreeNode parent = findParentNode(node);
        if (parent != null) {
            TreeNode duplicated = node.clone();
            duplicated.setId(UUID.randomUUID().toString());
            duplicated.setName(node.getName() + " (copie)");
            
            configService.addChildNode(parent, duplicated);
            showQuickAlert("✅ Duplication réussie", "#A3BE8C", 
                          "Élément dupliqué : " + duplicated.getName());
        }
    }
    
    /**
     * Confirme et supprime un nœud
     */
    private void confirmAndDeleteNode(TreeNode node) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer : " + node.getName());
        alert.setContentText("Cette action est irréversible. Tous les sous-éléments seront également supprimés.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            configService.removeNode(node);
            showQuickAlert("✅ Suppression effectuée", "#BF616A", 
                          "Élément supprimé : " + node.getName());
        }
    }
    
    /**
     * Affiche un feedback visuel rapide sur un composant
     */
    private void showQuickFeedback(Node component, String color, String symbol) {
        // Animation de feedback rapide
        Platform.runLater(() -> {
            if (component instanceof Labeled) {
                final String originalStyle = ((Labeled) component).getStyle();
                ((Labeled) component).setStyle(originalStyle + "-fx-border-color: " + color + "; -fx-border-width: 2;");
                
                // Restaurer après 1 seconde
                Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                    ((Labeled) component).setStyle(originalStyle);
                }));
                timeline.play();
            }
        });
    }
    
    /**
     * Affiche une alerte rapide et non-invasive
     */
    private void showQuickAlert(String title, String color, String message) {
        // Version simplifiée de showModernAlert pour un feedback plus rapide
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                           "-fx-border-color: " + color + "; -fx-border-width: 2; -fx-border-radius: 15;");
        
        // Auto-fermeture après 2 secondes
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> alert.close()));
        timeline.play();
        
        alert.show();
    }
    
    /**
     * Affiche la boîte de dialogue combinée éditer/supprimer (conservée pour compatibilité)
     */
    private void showEditDeleteDialog(TreeNode node) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Paramétrage de l'élément");
        dialog.setHeaderText("Élément : " + node.getName());
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        // Section Édition
        Label editLabel = new Label("ÉDITER");
        editLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        editLabel.setStyle("-fx-text-fill: #5E81AC;");
        
        GridPane editGrid = new GridPane();
        editGrid.setHgap(10);
        editGrid.setVgap(10);
        editGrid.setPadding(new Insets(10));
        editGrid.setStyle("-fx-background-color: rgba(94, 129, 172, 0.1); -fx-background-radius: 10;");
        
        TextField nameField = new TextField(node.getName());
        nameField.setPromptText("Nom de l'élément");
        
        Spinner<Double> percentageSpinner = new Spinner<>(0.0, 100.0, node.getPercentage(), 0.5);
        percentageSpinner.setEditable(true);
        
        ColorPicker colorPicker = new ColorPicker(getValidColor(node.getColor()));
        
        editGrid.add(new Label("Nom :"), 0, 0);
        editGrid.add(nameField, 1, 0);
        editGrid.add(new Label("Pourcentage :"), 0, 1);
        editGrid.add(percentageSpinner, 1, 1);
        editGrid.add(new Label("Couleur :"), 0, 2);
        editGrid.add(colorPicker, 1, 2);
        
        content.getChildren().addAll(editLabel, editGrid);
        
        // Section Suppression (seulement si ce n'est pas la racine)
        if (!node.isRoot()) {
            Label deleteLabel = new Label("SUPPRIMER");
            deleteLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            deleteLabel.setStyle("-fx-text-fill: #BF616A;");
            
            HBox deleteBox = new HBox(10);
            deleteBox.setPadding(new Insets(10));
            deleteBox.setStyle("-fx-background-color: rgba(191, 97, 106, 0.1); -fx-background-radius: 10;");
            deleteBox.setAlignment(Pos.CENTER_LEFT);
            
            Label deleteDesc = new Label("Supprimer cet élément et tous ses enfants");
            deleteDesc.setStyle("-fx-text-fill: #5E81AC;");
            
            Button deleteButton = new Button("Supprimer");
            deleteButton.setStyle("-fx-background-color: #BF616A; -fx-text-fill: white; " +
                                 "-fx-background-radius: 8; -fx-padding: 8 16;");
            deleteButton.setOnAction(e -> {
                dialog.setResult("DELETE");
                dialog.close();
            });
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            deleteBox.getChildren().addAll(deleteDesc, spacer, deleteButton);
            content.getChildren().addAll(deleteLabel, deleteBox);
        }
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return "EDIT";
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(action -> {
            if ("EDIT".equals(action)) {
                // Appliquer les modifications
                String newName = nameField.getText().trim();
                if (!newName.isEmpty()) {
                    node.setName(newName);
                    node.setPercentage(percentageSpinner.getValue());
                    node.setColor(colorPicker.getValue().toString().replace("0x", "#"));
                    
                    configService.updateNode(node);
                    showModernAlert("Modification enregistrée", "#A3BE8C", "Les modifications ont été sauvegardées");
                }
            } else if ("DELETE".equals(action)) {
                // Confirmer la suppression
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirmer la suppression");
                confirmAlert.setHeaderText("Supprimer : " + node.getName());
                confirmAlert.setContentText("Cette action est irréversible. Tous les sous-éléments seront également supprimés.");
                
                Optional<ButtonType> confirmResult = confirmAlert.showAndWait();
                if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                    configService.removeNode(node);
                    showModernAlert("Suppression effectuée", "#BF616A", "L'élément a été supprimé");
                }
            }
        });
    }
    
    /**
     * Configure le drag & drop pour un nœud
     */
    private void setupDragAndDrop(HBox nodeRow, TreeNode node, boolean isRoot) {
        // Démarrage du drag - la racine ne peut pas être déplacée, mais les autres oui
        if (!isRoot) {
            nodeRow.setOnDragDetected(event -> {
                if (event.isPrimaryButtonDown()) {
                    Dragboard dragboard = nodeRow.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.put(TREE_NODE_FORMAT, node.getId());
                    dragboard.setContent(content);
                    
                    draggedNode = node;
                    
                    // Style de démarrage du drag
                    nodeRow.setStyle(nodeRow.getStyle() + "-fx-opacity: 0.5;");
                    
                    event.consume();
                }
            });
        }
        
        // Gestion du survol pendant le drag - TOUS les nœuds peuvent accepter des drops
        nodeRow.setOnDragOver(event -> {
            if (event.getGestureSource() != nodeRow && 
                event.getDragboard().hasContent(TREE_NODE_FORMAT)) {
                event.acceptTransferModes(TransferMode.MOVE);
                
                // Style de survol spécial pour la racine
                String hoverStyle = isRoot ? 
                    "-fx-border-width: 3; -fx-border-color: #A3BE8C; -fx-background-color: rgba(163, 190, 140, 0.2);" :
                    "-fx-border-width: 3; -fx-border-color: #A3BE8C;";
                    
                nodeRow.setStyle(nodeRow.getStyle().replace("-fx-opacity: 0.5;", "") + hoverStyle);
            }
            event.consume();
        });
        
        // Sortie du survol - TOUS les nœuds
        nodeRow.setOnDragExited(event -> {
            // Restaurer le style normal
            String normalBorderColor = (node.getColor() != null) ? node.getColor() : DEFAULT_COLOR;
            nodeRow.setStyle(nodeRow.getStyle()
                .replace("-fx-border-width: 3; -fx-border-color: #A3BE8C;", 
                         "-fx-border-width: 2; -fx-border-color: " + normalBorderColor + ";")
                .replace("-fx-background-color: rgba(163, 190, 140, 0.2);", ""));
            event.consume();
        });
        
        // Drop - TOUS les nœuds peuvent recevoir des éléments déplacés
        nodeRow.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            
            if (dragboard.hasContent(TREE_NODE_FORMAT)) {
                String draggedNodeId = (String) dragboard.getContent(TREE_NODE_FORMAT);
                
                if (draggedNode != null && !draggedNodeId.equals(node.getId())) {
                    // Vérifier qu'on ne déplace pas un parent vers un de ses enfants
                    if (!isDescendant(node, draggedNode)) {
                        configService.moveNode(draggedNode, node);
                        success = true;
                        
                        String targetName = isRoot ? "la racine (" + node.getName() + ")" : node.getName();
                        showModernAlert("✅ Déplacement réussi", "#A3BE8C", 
                                       draggedNode.getName() + " déplacé sous " + targetName);
                    } else {
                        showModernAlert("❌ Déplacement impossible", "#BF616A", 
                                       "Impossible de déplacer un parent vers un descendant");
                    }
                }
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
        
        // Fin du drag - seulement pour les nœuds non-racine (qui peuvent être déplacés)
        if (!isRoot) {
            nodeRow.setOnDragDone(event -> {
                // Restaurer l'opacité normale
                nodeRow.setStyle(nodeRow.getStyle().replace("-fx-opacity: 0.5;", ""));
                draggedNode = null;
                event.consume();
            });
        }
    }
    
    /**
     * Vérifie si un nœud est descendant d'un autre
     */
    private boolean isDescendant(TreeNode potentialDescendant, TreeNode ancestor) {
        if (ancestor.hasChildren()) {
            for (TreeNode child : ancestor.getChildren()) {
                if (child.getId().equals(potentialDescendant.getId()) || 
                    isDescendant(potentialDescendant, child)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Affiche la boîte de dialogue d'ajout d'enfant
     */
    private void showAddChildDialog(TreeNode parent) {
        Dialog<TreeNode> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un élément");
        dialog.setHeaderText("Ajouter un nouvel élément sous : " + parent.getName());
        
        // Contenu de la boîte de dialogue
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Nom de l'élément");
        
        Spinner<Double> percentageSpinner = new Spinner<>(0.0, 100.0, 50.0, 0.5);
        percentageSpinner.setEditable(true);
        
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(javafx.scene.paint.Color.web("#5E81AC"));
        
        grid.add(new Label("Nom :"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Pourcentage :"), 0, 1);
        grid.add(percentageSpinner, 1, 1);
        grid.add(new Label("Couleur :"), 0, 2);
        grid.add(colorPicker, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String name = nameField.getText().trim();
                if (!name.isEmpty()) {
                    double percentage = percentageSpinner.getValue();
                    String color = colorPicker.getValue().toString().replace("0x", "#");
                    
                    return new TreeNode(UUID.randomUUID().toString(), name, percentage, "");
                }
            }
            return null;
        });
        
        Optional<TreeNode> result = dialog.showAndWait();
        result.ifPresent(newNode -> {
            configService.addChildNode(parent, newNode);
            showModernAlert("✅ Élément ajouté", "#A3BE8C", 
                           "Nouvel élément créé : " + newNode.getName());
        });
    }
    
    /**
     * Affiche la boîte de dialogue d'édition d'un nœud
     */
    private void showEditNodeDialog(TreeNode node) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Modifier l'élément");
        dialog.setHeaderText("Modification de : " + node.getName());
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField nameField = new TextField(node.getName());
        Spinner<Double> percentageSpinner = new Spinner<>(0.0, 100.0, node.getPercentage(), 0.5);
        percentageSpinner.setEditable(true);
        ColorPicker colorPicker = new ColorPicker(getValidColor(node.getColor()));
        
        grid.add(new Label("Nom :"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Pourcentage :"), 0, 1);
        grid.add(percentageSpinner, 1, 1);
        grid.add(new Label("Couleur :"), 0, 2);
        grid.add(colorPicker, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                node.setName(nameField.getText().trim());
                node.setPercentage(percentageSpinner.getValue());
                node.setColor(colorPicker.getValue().toString().replace("0x", "#"));
                return true;
            }
            return false;
        });
        
        Optional<Boolean> result = dialog.showAndWait();
        result.ifPresent(modified -> {
            if (modified) {
                configService.updateNode(node);
                showModernAlert("✅ Modification enregistrée", "#A3BE8C", 
                               "Élément modifié : " + node.getName());
            }
        });
    }
    
    /**
     * Affiche la confirmation de suppression
     */
    private void showDeleteConfirmation(TreeNode node) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer l'élément : " + node.getName());
        alert.setContentText("Cette action est irréversible. Tous les sous-éléments seront également supprimés.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            configService.removeNode(node);
            showModernAlert("✅ Suppression effectuée", "#BF616A", 
                           "Élément supprimé : " + node.getName());
        }
    }
    
    /**
     * Affiche la confirmation de réinitialisation
     */
    private void showResetConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Réinitialiser la configuration");
        alert.setHeaderText("Revenir à la configuration par défaut");
        alert.setContentText("Toute la configuration actuelle sera perdue. Continuer ?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            configService.resetToDefault();
            showModernAlert("✅ Configuration réinitialisée", "#D08770", 
                           "Retour à la configuration par défaut");
        }
    }
    
    /**
     * Développe ou réduit tous les nœuds récursivement
     */
    private void expandCollapseAll(TreeNode node, boolean expand) {
        if (node != null) {
            node.setExpanded(expand);
            if (node.hasChildren()) {
                for (TreeNode child : node.getChildren()) {
                    expandCollapseAll(child, expand);
                }
            }
            // Sauvegarder après chaque modification
            configService.updateNode(node);
        }
    }
    
    /**
     * Affiche une alerte moderne
     */
    private void showModernAlert(String title, String color, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                           "-fx-border-color: " + color + "; -fx-border-width: 2; -fx-border-radius: 15;");
        
        alert.showAndWait();
    }
    
    /**
     * Trouve le parent d'un nœud donné
     */
    private TreeNode findParentNode(TreeNode targetNode) {
        return findParentRecursive(configService.getRootNode(), targetNode);
    }
    
    private TreeNode findParentRecursive(TreeNode current, TreeNode target) {
        if (current == null || !current.hasChildren()) {
            return null;
        }
        
        for (TreeNode child : current.getChildren()) {
            if (child.getId().equals(target.getId())) {
                return current;
            }
            TreeNode found = findParentRecursive(child, target);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
    
    /**
     * Valide et retourne une couleur sûre pour JavaFX
     */
    private javafx.scene.paint.Color getValidColor(String colorString) {
        if (colorString == null || colorString.trim().isEmpty()) {
            return javafx.scene.paint.Color.web(DEFAULT_COLOR);
        }
        
        try {
            // Normaliser la couleur (ajouter # si nécessaire)
            String normalizedColor = colorString.trim();
            if (!normalizedColor.startsWith("#")) {
                normalizedColor = "#" + normalizedColor;
            }
            
            // Vérifier que la couleur est valide
            return javafx.scene.paint.Color.web(normalizedColor);
        } catch (IllegalArgumentException e) {
            logger.warn("Couleur invalide '{}', utilisation de la couleur par défaut", colorString);
            return javafx.scene.paint.Color.web(DEFAULT_COLOR);
        }
    }
    
    /**
     * Export de configuration d'arbre via interface
     */
    private void exportTreeConfiguration() {
        try {
            configService.saveConfiguration();
            showModernAlert("✅ Export réussi", "#A3BE8C", 
                           "Configuration sauvegardée dans data/tree_configuration.json");
        } catch (Exception e) {
            logger.error("Erreur lors de l'export", e);
            showModernAlert("❌ Erreur d'export", "#BF616A", 
                           "Impossible d'exporter : " + e.getMessage());
        }
    }
    
    /**
     * Import de configuration d'arbre via interface
     */
    private void importTreeConfiguration() {
        try {
            configService.loadConfiguration();
            refreshTreeDisplay(configService.getRootNode());
            showModernAlert("✅ Import réussi", "#A3BE8C", 
                           "Configuration rechargée depuis data/tree_configuration.json");
        } catch (Exception e) {
            logger.error("Erreur lors de l'import", e);
            showModernAlert("❌ Erreur d'import", "#BF616A", 
                           "Impossible d'importer : " + e.getMessage());
        }
    }

    // Getter pour le service de configuration
    public TreeConfigurationService getConfigService() {
        return configService;
    }
} 
 