package com.applydance.gui;

import com.applydance.model.GeneratedSlot;
import com.applydance.model.TreeNode;
import com.applydance.service.*;
import com.applydance.service.StatisticsService.StatisticsReport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.chart.*;
import javafx.scene.text.TextAlignment;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.shape.Line;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.util.Duration;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.collections.ListChangeListener;
import javafx.scene.control.SelectionMode;

public class MainController {
    
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    private TreeGenerationEngine generationEngine;
    private SlotHistoryService historyService;
    private StatisticsService statisticsService;
    private ExportImportService exportService;
    private TreeConfigurationService configService;
    private AutomatedTestService testService;
    
    private Button generateButton;
    private Button generate10Button;
    private TextArea lastSlotDetails;
    private TableView<GeneratedSlot> historyTable;
    private TableColumn<GeneratedSlot, Long> idColumn;
    private TableColumn<GeneratedSlot, Boolean> selectionColumn;
    private TableColumn<GeneratedSlot, String> pathColumn;
    private TableColumn<GeneratedSlot, String> nodeColumn;
    private TableColumn<GeneratedSlot, String> categoryColumn;
    private TableColumn<GeneratedSlot, String> dateColumn;
    // Variables d'interface obsolètes supprimées - elles causaient des NullPointerException
    private PieChart typeDistributionChart;
    private BarChart<Number, String> statisticsBarChart;
    private TextArea statisticsTextArea;
    private Button exportCsvButton;
    private Button exportJsonButton;
    private Label exportStatusLabel;
    
    // Nouveau système de barres personnalisées
    private VBox customBarsContainer;
    private List<CustomBarItem> customBarItems = new ArrayList<>();
    
    private ObservableList<GeneratedSlot> historyData = FXCollections.observableArrayList();
    
    // Variables pour la barre d'actions de suppression
    private Button selectAllButton;
    private Button deselectAllButton;
    private Label selectionCounterLabel;
    private Button deleteSelectedButton;
    
    private Pane currentTreePane; // Référence pour la mise à jour
    private VBox currentSlotContainer; // Container moderne pour l'affichage des slots
    
    public BorderPane createMainInterface() {
        logger.info("Création de l'interface principale");
        
        // Initialiser les services
        this.configService = new TreeConfigurationService();
        this.generationEngine = new TreeGenerationEngine(configService);
        this.historyService = new SlotHistoryService();
        this.statisticsService = new StatisticsService(historyService);
        this.exportService = new ExportImportService(historyService, configService);
        this.testService = new AutomatedTestService(configService, generationEngine, historyService);
        
        // Créer l'interface
        BorderPane root = new BorderPane();
        
        // Header
        VBox header = createHeader();
        root.setTop(header);
        
        // Centre avec les onglets
        TabPane tabPane = createTabPane();
        root.setCenter(tabPane);
        
        // Footer
        HBox footer = createFooter();
        root.setBottom(footer);
        
        // Configurer la table et charger les données
        setupHistoryTable();
        loadInitialData();
        
        logger.info("Interface principale créée avec succès");
        return root;
    }
    
    private VBox createHeader() {
        VBox header = new VBox();
        
        // Titre principal
        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setStyle("-fx-background-color: #2E3440; -fx-padding: 15;");
        
        Label titleLabel = new Label("🎯 ApplyDance v1.1.0 - Générateur de Slots de Candidature");
        titleLabel.setStyle("-fx-text-fill: white;");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleBox.getChildren().add(titleLabel);
        
        // Toolbar
        ToolBar toolBar = new ToolBar();
        toolBar.setStyle("-fx-background-color: #434C5E;");
        
        generateButton = new Button("🎲 Générer 1 Slot");
        generateButton.setStyle("-fx-background-color: #5E81AC; -fx-text-fill: white;");
        generateButton.setFont(Font.font("System", FontWeight.BOLD, 12));
        generateButton.setOnAction(e -> generateSlot());
        
        generate10Button = new Button("🔄 Réinitialiser");
        generate10Button.setStyle("-fx-background-color: #D08770; -fx-text-fill: white;");
        generate10Button.setFont(Font.font("System", FontWeight.BOLD, 12));
        generate10Button.setOnAction(e -> resetAlternance());
        
        exportCsvButton = new Button("📊 Export CSV");
        exportCsvButton.setStyle("-fx-background-color: #A3BE8C; -fx-text-fill: white;");
        exportCsvButton.setFont(Font.font("System", FontWeight.BOLD, 12));
        exportCsvButton.setOnAction(e -> exportToCsv());
        
        exportJsonButton = new Button("📄 Export JSON");
        exportJsonButton.setStyle("-fx-background-color: #EBCB8B; -fx-text-fill: black;");
        exportJsonButton.setFont(Font.font("System", FontWeight.BOLD, 12));
        exportJsonButton.setOnAction(e -> exportToJson());
        
        Button testButton = new Button("🧪 Tests Auto");
        testButton.setStyle("-fx-background-color: #B48EAD; -fx-text-fill: white;");
        testButton.setFont(Font.font("System", FontWeight.BOLD, 12));
        testButton.setOnAction(e -> runAutomatedTests());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        exportStatusLabel = new Label("📤 Prêt à exporter");
        exportStatusLabel.setStyle("-fx-text-fill: #D8DEE9;");
        
        toolBar.getItems().addAll(generateButton, generate10Button, new Separator(), 
                                 exportCsvButton, exportJsonButton, testButton, spacer, exportStatusLabel);
        
        header.getChildren().addAll(titleBox, toolBar);
        return header;
    }
    
    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Onglet Génération
        Tab generationTab = new Tab("🎲 Génération");
        generationTab.setContent(createGenerationTab());
        
        // Onglet Historique
        Tab historyTab = new Tab("📖 Historique");
        historyTab.setContent(createHistoryTab());
        
        // Onglet Statistiques
        Tab statsTab = new Tab("📊 Statistiques");
        statsTab.setContent(createStatsTab());
        
        // Onglet Paramétrage
        Tab configTab = new Tab("⚙️ Paramétrage");
        configTab.setContent(createConfigTab());
        
        tabPane.getTabs().addAll(generationTab, historyTab, statsTab, configTab);
        return tabPane;
    }
    
    private VBox createGenerationTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        // Zone dernier slot - Interface moderne
        VBox slotBox = createModernSlotDisplay();
        currentSlotContainer = slotBox; // Stocker la référence pour les mises à jour
        
        // Boutons d'action
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button genButton = new Button("🎲 Générer un Slot");
        genButton.setPrefSize(200, 50);
        genButton.setStyle("-fx-background-color: #5E81AC; -fx-text-fill: white; -fx-background-radius: 10;");
        genButton.setFont(Font.font("System", FontWeight.BOLD, 14));
        genButton.setOnAction(e -> generateSlot());
        
        Button resetButton = new Button("🔄 Réinitialiser l'alternance");
        resetButton.setPrefSize(200, 50);
        resetButton.setStyle("-fx-background-color: #D08770; -fx-text-fill: white; -fx-background-radius: 10;");
        resetButton.setFont(Font.font("System", FontWeight.BOLD, 14));
        resetButton.setOnAction(e -> resetAlternance());
        
        buttonBox.getChildren().addAll(genButton, resetButton);
        
        content.getChildren().addAll(slotBox, buttonBox);
        return content;
    }
    
    /**
     * Crée un affichage moderne et attrayant pour les slots générés
     */
    private VBox createModernSlotDisplay() {
        VBox slotBox = new VBox(20);
        slotBox.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #667eea, #764ba2);" +
            "-fx-padding: 30;" +
            "-fx-background-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 25, 0, 0, 8);"
        );
        slotBox.setAlignment(Pos.CENTER);
        
        // Titre moderne
        Label titleLabel = new Label("🎯 Résultat de la Génération");
        titleLabel.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-alignment: center;"
        );
        
        // Zone de résultat principale - initialement vide
        VBox resultBox = new VBox(15);
        resultBox.setStyle(
            "-fx-background-color: white;" +
            "-fx-padding: 25;" +
            "-fx-background-radius: 15;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);"
        );
        resultBox.setAlignment(Pos.CENTER);
        resultBox.setPrefHeight(150);
        
        // Message par défaut
        Label defaultMessage = new Label("🎲 Cliquez sur \"Générer un Slot\" pour commencer !");
        defaultMessage.setStyle(
            "-fx-text-fill: #6c757d;" +
            "-fx-font-size: 16px;" +
            "-fx-font-style: italic;"
        );
        resultBox.getChildren().add(defaultMessage);
        
        slotBox.getChildren().addAll(titleLabel, resultBox);
        return slotBox;
    }
    
    private VBox createHistoryTab() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label titleLabel = new Label("📖 Historique des Slots Générés");
        titleLabel.setStyle("-fx-text-fill: #2E3440;");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        // Barre d'actions de sélection
        HBox actionBar = createSelectionActionBar();
        
        historyTable = new TableView<>();
        historyTable.setPrefHeight(500);
        historyTable.setStyle(
            "-fx-background-color: #ECEFF4; " +
            "-fx-selection-bar: transparent; " +           // Couleur fond sélectionné (focus)
            "-fx-selection-bar-non-focused: transparent; " + // Couleur fond sélectionné (non focus)
            "-fx-focus-color: transparent; " +
            "-fx-faint-focus-color: transparent;"
        );
        // Supprimer la surbrillance bleue tout en conservant la sélection interne nécessaire aux cases à cocher
        
        idColumn = new TableColumn<>("ID");
        idColumn.setPrefWidth(50);
        selectionColumn = new TableColumn<>("☑️");
        selectionColumn.setPrefWidth(40);
        selectionColumn.setResizable(false);
        selectionColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectionColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectionColumn));
        pathColumn = new TableColumn<>("Chemin de Décision");
        pathColumn.setPrefWidth(200);
        nodeColumn = new TableColumn<>("Nœud Sélectionné");
        nodeColumn.setPrefWidth(150);
        categoryColumn = new TableColumn<>("Catégorie");
        categoryColumn.setPrefWidth(120);
        dateColumn = new TableColumn<>("Date de Génération");
        dateColumn.setPrefWidth(180);
        
        historyTable.getColumns().addAll(idColumn, selectionColumn, pathColumn, nodeColumn, 
                                        categoryColumn, dateColumn);
        
        // Permettre l'édition pour que le CheckBoxTableCell mette à jour la propriété et affiche le logo validé
        historyTable.setEditable(true);
        selectionColumn.setEditable(true);
        
        // Laisser la sélection par défaut (SINGLE ou MULTIPLE), ne pas toucher au modèle de sélection
        // Plus de listener sur la sélection de ligne
        
        content.getChildren().addAll(titleLabel, actionBar, historyTable);
        return content;
    }
    
    private VBox createStatsTab() {
        VBox content = new VBox(30);
        content.setPadding(new Insets(40));
        content.setStyle("-fx-background-color: linear-gradient(to bottom, #FAF7F0, #F5F1EA);"); // Fond crème inspiré du projet ELO
        content.setAlignment(Pos.TOP_CENTER);
        
        // Titre principal moderne
        Label title = new Label("📊 Statistiques de Génération");
        title.setStyle("-fx-text-fill: #2E3440; -fx-font-size: 28px; -fx-font-weight: bold;");
        title.setAlignment(Pos.CENTER);
        
        // Container pour le bar chart personnalisé
        VBox customBarChart = createCustomBarChart();
        
        content.getChildren().addAll(title, customBarChart);
        return content;
    }
    
    /**
     * Crée un bar chart personnalisé inspiré du design du projet ELO
     */
    private VBox createCustomBarChart() {
        VBox chartContainer = new VBox();
        chartContainer.setSpacing(0); // Pas d'espacement entre les barres comme dans le projet ELO
        chartContainer.setPadding(new Insets(30));
        chartContainer.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #FAF7F0, #F5F1EA); " +
            "-fx-background-radius: 20; " +
            "-fx-border-color: rgba(150,140,120,0.4); " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 20; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);"
        );
        chartContainer.setPrefHeight(600);
        chartContainer.setPrefWidth(1000);
        
        // ScrollPane pour navigation fluide
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(chartContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle(
            "-fx-background: transparent; " +
            "-fx-background-color: transparent; " +
            "-fx-focus-color: transparent; " +
            "-fx-faint-focus-color: transparent;"
        );
        
        // Stocker la référence pour la mise à jour
        this.customBarsContainer = chartContainer;
        
        VBox wrapper = new VBox();
        wrapper.getChildren().add(scrollPane);
        wrapper.setPrefHeight(600);
        wrapper.setPrefWidth(1000);
        
        return wrapper;
    }
    
    /**
     * Classe pour représenter une barre du graphique personnalisé
     */
    private static class CustomBarItem {
        final String label;
        final Rectangle bar;
        final Label nameLabel;
        final Label valueLabel;
        final HBox container;
        int value;
        javafx.scene.paint.Color barColor;
        String internalKey; // Clé unique pour distinguer les barres
        
        // Couleurs distinctes inspirées du projet ELO
        private static final javafx.scene.paint.Color[] DISTINCT_COLORS = {
            javafx.scene.paint.Color.web("#D32F2F"), // Rouge foncé
            javafx.scene.paint.Color.web("#1976D2"), // Bleu foncé  
            javafx.scene.paint.Color.web("#388E3C"), // Vert foncé
            javafx.scene.paint.Color.web("#F57C00"), // Orange foncé
            javafx.scene.paint.Color.web("#7B1FA2"), // Violet foncé
            javafx.scene.paint.Color.web("#0097A7"), // Cyan foncé
            javafx.scene.paint.Color.web("#5D4037"), // Marron
            javafx.scene.paint.Color.web("#616161"), // Gris foncé
            javafx.scene.paint.Color.web("#C2185B"), // Rose foncé
            javafx.scene.paint.Color.web("#303F9F"), // Indigo foncé
            javafx.scene.paint.Color.web("#689F38"), // Vert olive
            javafx.scene.paint.Color.web("#E64A19"), // Rouge-orange foncé
            javafx.scene.paint.Color.web("#512DA8"), // Violet profond
            javafx.scene.paint.Color.web("#00796B"), // Teal foncé
            javafx.scene.paint.Color.web("#455A64"), // Bleu-gris foncé
            javafx.scene.paint.Color.web("#8BC34A")  // Vert pomme
        };
        
        CustomBarItem(String label, int value, int colorIndex) {
            this.label = label;
            this.value = value;
            this.barColor = DISTINCT_COLORS[colorIndex % DISTINCT_COLORS.length];
            this.bar = new Rectangle();
            this.nameLabel = new Label(label);
            this.valueLabel = new Label(String.valueOf(value));
            this.container = new HBox();
            
            setupBar();
        }
        
        private void setupBar() {
            // Configuration de la barre inspirée du projet ELO
            bar.setHeight(20); // Hauteur fixe
            bar.setArcWidth(8);
            bar.setArcHeight(8);
            bar.setFill(barColor);
            bar.setStroke(javafx.scene.paint.Color.web("#333333", 0.4)); // Bordure pour contraste
            bar.setStrokeWidth(1.5);
            
            // Configuration du label de nom avec contraste optimal pour fond crème
            nameLabel.setStyle(
                "-fx-text-fill: #2C2C2C; " + // Gris très foncé pour lisibilité maximale
                "-fx-font-weight: bold; " +
                "-fx-font-size: 12px; " +
                "-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.9), 1, 0, 0, 1);" +
                "-fx-background-color: rgba(255,255,255,0.95); " +
                "-fx-background-radius: 3; " +
                "-fx-padding: 2 8 2 8;"
            );
            nameLabel.setPrefWidth(200); // Largeur fixe pour alignement
            
            // Configuration du label de valeur
            valueLabel.setStyle(
                "-fx-text-fill: #333333; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 11px; " +
                "-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.8), 2, 0, 1, 1);" +
                "-fx-background-color: rgba(255,255,255,0.8); " +
                "-fx-background-radius: 3; " +
                "-fx-padding: 2 6 2 6;"
            );
            valueLabel.setPrefWidth(80);
            valueLabel.setAlignment(Pos.CENTER);
            
            // Configuration du container
            container.setAlignment(Pos.CENTER_LEFT);
            container.setSpacing(15);
            container.setPadding(new Insets(8));
            container.setStyle(
                "-fx-background-color: rgba(255,255,255,0.02); " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: rgba(255,255,255,0.1); " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8;"
            );
            
            container.getChildren().addAll(nameLabel, bar, valueLabel);
        }
        
        void updateValue(int newValue, int maxValue) {
            this.value = newValue;
            valueLabel.setText(String.valueOf(newValue));
            
            // Calculer la largeur proportionnelle (max 80% de la largeur disponible)
            double maxWidth = 600; // Largeur max de la barre
            double minWidth = 30; // Largeur minimale
            
            double width;
            if (maxValue == 0) {
                width = minWidth;
            } else {
                double ratio = (double) newValue / maxValue;
                width = minWidth + (maxWidth - minWidth) * ratio;
            }
            
            bar.setWidth(width);
        }
        
        void setInternalKey(String key) {
            this.internalKey = key;
        }
    }

    
    private ScrollPane createConfigTab() {
        // Utiliser le nouveau système générique avec glisser-déposer
        // CORRECTION : Passer l'instance partagée de TreeConfigurationService
        com.applydance.gui.TreeDragDropController dragDropController = new com.applydance.gui.TreeDragDropController(configService);
        return dragDropController.createTreeConfigInterface();
    }
    
    private VBox createDynamicTree() {
        VBox treeContainer = new VBox(5);
        treeContainer.setStyle("-fx-background-color: white; " +
                              "-fx-background-radius: 15; " +
                              "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5); " +
                              "-fx-padding: 20;");
        
        // Créer la structure d'arbre hiérarchique avec volets extensibles
        VBox treeStructure = createExpandableTreeStructure();
        
        treeContainer.getChildren().add(treeStructure);
        return treeContainer;
    }
    
    private VBox createExpandableTreeStructure() {
        VBox treeStructure = new VBox(2);
        
        // Nœud racine - avec pourcentage 100%
        TreeNodeData rootNode = new TreeNodeData("Génération de Slots", 0, "#2E3440", true, true);
        rootNode.percentage = 100.0;
        VBox rootSection = createExpandableSection(rootNode, null);
        
        // Section VIE
        TreeNodeData vieNode = new TreeNodeData("VIE à l'étranger", 1, "#5E81AC", true, true);
        vieNode.percentage = 50.0;
        VBox vieSection = createExpandableSection(vieNode, rootSection);
        
        // Sous-éléments VIE
        TreeNodeData businessNode = new TreeNodeData("Business France", 2, "#81A1C1", true, false);
        businessNode.percentage = 70.0;
        TreeNodeData spontaneNode = new TreeNodeData("Spontané", 2, "#81A1C1", true, false);
        spontaneNode.percentage = 30.0;
        VBox businessSection = createExpandableSection(businessNode, vieSection);
        VBox spontaneSection = createExpandableSection(spontaneNode, vieSection);
        
        // Section France
        TreeNodeData franceNode = new TreeNodeData("Postes en France", 1, "#A3BE8C", true, true);
        franceNode.percentage = 50.0;
        VBox franceSection = createExpandableSection(franceNode, rootSection);
        
        // Sous-section Rouen
        TreeNodeData rouenNode = new TreeNodeData("Région Rouen", 2, "#88C0D0", true, true);
        rouenNode.percentage = 80.0;
        VBox rouenSection = createExpandableSection(rouenNode, franceSection);
        
        // Villes Rouen
        TreeNodeData rouenVille = new TreeNodeData("Rouen", 3, "#8FBCBB", true, false);
        rouenVille.percentage = 60.0;
        TreeNodeData leHavreVille = new TreeNodeData("Le Havre", 3, "#8FBCBB", true, false);
        leHavreVille.percentage = 25.0;
        TreeNodeData dieppeVille = new TreeNodeData("Dieppe", 3, "#8FBCBB", true, false);
        dieppeVille.percentage = 15.0;
        
        VBox rouenVilleSection = createExpandableSection(rouenVille, rouenSection);
        VBox leHavreSection = createExpandableSection(leHavreVille, rouenSection);
        VBox dieppeSection = createExpandableSection(dieppeVille, rouenSection);
        
        // Sous-section Canaux
        TreeNodeData canauxNode = new TreeNodeData("Canaux de recherche", 2, "#EBCB8B", true, true);
        canauxNode.percentage = 100.0;
        VBox canauxSection = createExpandableSection(canauxNode, franceSection);
        
        // Canaux individuels
        TreeNodeData linkedinNode = new TreeNodeData("LinkedIn", 3, "#D08770", true, false);
        linkedinNode.percentage = 35.0;
        TreeNodeData indeedNode = new TreeNodeData("Indeed", 3, "#D08770", true, false);
        indeedNode.percentage = 40.0;
        TreeNodeData jungleNode = new TreeNodeData("Welcome to the Jungle", 3, "#D08770", true, false);
        jungleNode.percentage = 25.0;
        
        VBox linkedinSection = createExpandableSection(linkedinNode, canauxSection);
        VBox indeedSection = createExpandableSection(indeedNode, canauxSection);
        VBox jungleSection = createExpandableSection(jungleNode, canauxSection);
        
        // Autres villes
        TreeNodeData autresVillesNode = new TreeNodeData("Autres villes", 2, "#BF616A", true, true);
        autresVillesNode.percentage = 20.0;
        VBox autresVillesSection = createExpandableSection(autresVillesNode, franceSection);
        
        TreeNodeData parisNode = new TreeNodeData("Paris", 3, "#B48EAD", true, false);
        parisNode.percentage = 70.0;
        TreeNodeData lyonNode = new TreeNodeData("Lyon", 3, "#B48EAD", true, false);
        lyonNode.percentage = 30.0;
        
        VBox parisSection = createExpandableSection(parisNode, autresVillesSection);
        VBox lyonSection = createExpandableSection(lyonNode, autresVillesSection);
        
        // Assemblage de l'arbre (sections racines seulement - les enfants sont ajoutés automatiquement)
        treeStructure.getChildren().addAll(rootSection);
        
        return treeStructure;
    }
    
    private VBox createExpandableSection(TreeNodeData node, VBox parentSection) {
        VBox section = new VBox(2);
        section.setUserData(node); // Stocker les données du nœud
        
        // Ligne du nœud avec bouton d'expansion
        HBox nodeRow = createExpandableNodeRow(node, section);
        
        // Conteneur pour les enfants (initialement caché si c'est un nœud extensible)
        VBox childrenContainer = new VBox(2);
        childrenContainer.setStyle("-fx-padding: 0 0 0 " + (20 + node.level * 15) + ";");
        
        if (node.hasChildren) {
            childrenContainer.setVisible(false);
            childrenContainer.setManaged(false);
        }
        
        section.getChildren().addAll(nodeRow, childrenContainer);
        
        // Ajouter cette section au parent si spécifié
        if (parentSection != null) {
            // Trouver le conteneur d'enfants du parent
            VBox parentChildrenContainer = findChildrenContainer(parentSection);
            if (parentChildrenContainer != null) {
                parentChildrenContainer.getChildren().add(section);
            }
        }
        
        return section;
    }
    
    private HBox createExpandableNodeRow(TreeNodeData node, VBox section) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 15, 12, 15 + (node.level * 20)));
        String backgroundColor = node.level % 2 == 0 ? "#FAFAFA" : "#F5F5F5";
        row.setStyle("-fx-background-color: " + backgroundColor + "; " +
                    "-fx-border-color: #E8E8E8; " +
                    "-fx-border-width: 0 0 0.5 0; " +
                    "-fx-background-radius: 5;");
        
        // Hover effect
        row.setOnMouseEntered(e -> {
            row.setStyle("-fx-background-color: " + node.color + "15; " +
                        "-fx-border-color: " + node.color + "40; " +
                        "-fx-border-width: 0 0 0.5 0; " +
                        "-fx-background-radius: 5; " +
                        "-fx-padding: 12 15 12 " + (15 + (node.level * 20)) + "; " +
                        "-fx-cursor: hand;");
        });
        row.setOnMouseExited(e -> {
            row.setStyle("-fx-background-color: " + backgroundColor + "; " +
                        "-fx-border-color: #E8E8E8; " +
                        "-fx-border-width: 0 0 0.5 0; " +
                        "-fx-background-radius: 5; " +
                        "-fx-padding: 12 15 12 " + (15 + (node.level * 20)) + ";");
        });
        
        // Bouton d'expansion/contraction
        Button expandButton = new Button();
        if (node.hasChildren) {
            expandButton.setText("▶");
            expandButton.setStyle("-fx-background-color: transparent; " +
                                "-fx-text-fill: " + node.color + "; " +
                                "-fx-font-weight: bold; " +
                                "-fx-padding: 0; " +
                                "-fx-cursor: hand; " +
                                "-fx-font-size: 12;");
            expandButton.setPrefSize(20, 20);
            
            // Action d'expansion/contraction
            expandButton.setOnAction(e -> toggleExpansion(expandButton, section));
        } else {
            expandButton.setText("•");
            expandButton.setStyle("-fx-background-color: transparent; " +
                                "-fx-text-fill: #D8DEE9; " +
                                "-fx-padding: 0; " +
                                "-fx-font-size: 8;");
            expandButton.setPrefSize(20, 20);
            expandButton.setDisable(true);
        }
        
        // Nom du nœud avec pourcentage (éviter double affichage)
        String displayText = node.name;
        if (node.hasPercentage && !node.name.contains("%")) {
            displayText += " (" + String.format("%.1f%%", node.percentage) + ")";
        }
        Label nameLabel = new Label(displayText);
        nameLabel.setStyle("-fx-text-fill: " + node.color + "; " +
                          "-fx-font-weight: bold; " +
                          "-fx-font-size: " + Math.max(12, 16 - node.level) + ";");
        nameLabel.setPrefWidth(350);
        
        // Double-clic pour éditer
        nameLabel.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                showEditNodeDialog(node, section);
            }
        });
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Actions pour chaque nœud
        HBox actions = createNodeActions(node, section);
        
        row.getChildren().addAll(expandButton, nameLabel, spacer, actions);
        return row;
    }
    
    private void toggleExpansion(Button expandButton, VBox section) {
        VBox childrenContainer = findChildrenContainer(section);
        if (childrenContainer != null) {
            boolean isExpanded = childrenContainer.isVisible();
            
            if (isExpanded) {
                // Fermer
                expandButton.setText("▶");
                // Animation de fermeture
                FadeTransition fade = new FadeTransition(Duration.millis(200), childrenContainer);
                fade.setFromValue(1.0);
                fade.setToValue(0.0);
                fade.setOnFinished(e -> {
                    childrenContainer.setVisible(false);
                    childrenContainer.setManaged(false);
                });
                fade.play();
            } else {
                // Ouvrir
                expandButton.setText("▼");
                childrenContainer.setVisible(true);
                childrenContainer.setManaged(true);
                
                // Animation d'ouverture
                FadeTransition fade = new FadeTransition(Duration.millis(200), childrenContainer);
                fade.setFromValue(0.0);
                fade.setToValue(1.0);
                fade.play();
            }
        }
    }
    
    private VBox findChildrenContainer(VBox section) {
        // Le conteneur d'enfants est le deuxième élément de la section
        if (section.getChildren().size() > 1) {
            return (VBox) section.getChildren().get(1);
        }
        return null;
    }
    
    private HBox createNodeActions(TreeNodeData node, VBox section) {
        HBox actions = new HBox(5);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        // Bouton + pour ajouter un sous-élément uniquement
        Button addButton = new Button("➕");
        addButton.setStyle("-fx-background-color: #A3BE8C; " +
                          "-fx-text-fill: white; " +
                          "-fx-background-radius: 15; " +
                          "-fx-padding: 4 6; " +
                          "-fx-font-size: 10; " +
                          "-fx-cursor: hand;");
        addButton.setOnAction(e -> showAddChildDialog(node, section));
        
        actions.getChildren().add(addButton);
        return actions;
    }
    
    private HBox createSeparator() {
        HBox separator = new HBox();
        separator.setPrefHeight(2);
        separator.setStyle("-fx-background-color: #E5E9F0; -fx-opacity: 0.5;");
        return separator;
    }
    
    private void showAddChildDialog(TreeNodeData parentNode, VBox parentSection) {
        Stage dialog = new Stage();
        dialog.setTitle("Ajouter un élément à: " + parentNode.name);
        dialog.initModality(Modality.APPLICATION_MODAL);
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
        
        Label titleLabel = new Label("➕ Ajouter un nouvel élément");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: #2E3440;");
        
        // Nom du nouvel élément
        Label nameLabel = new Label("Nom de l'élément:");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #4C566A;");
        TextField nameField = new TextField();
        nameField.setPromptText("Ex: Paris, Indeed, Business France...");
        nameField.setStyle("-fx-padding: 12; -fx-font-size: 14; -fx-background-radius: 8;");
        
        // Pourcentage avec input précis
        Label percentageLabel = new Label("Pourcentage:");
        percentageLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #4C566A;");
        
        HBox percentageInputBox = new HBox(8);
        percentageInputBox.setAlignment(Pos.CENTER_LEFT);
        
        TextField percentageField = new TextField("50.0");
        percentageField.setStyle("-fx-padding: 10; -fx-font-size: 14; -fx-background-radius: 8; " +
                               "-fx-border-radius: 8; -fx-border-color: #D0D0D0; -fx-border-width: 1; " +
                               "-fx-pref-width: 80;");
        percentageField.setPromptText("0.0");
        
        Label percentSymbol = new Label("%");
        percentSymbol.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #5E81AC;");
        
        Label infoLabel = new Label("(Les autres éléments s'ajusteront automatiquement)");
        infoLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #8A8A8A; -fx-font-style: italic;");
        
        percentageInputBox.getChildren().addAll(percentageField, percentSymbol);
        
        VBox percentageBox = new VBox(8);
        percentageBox.getChildren().addAll(percentageLabel, percentageInputBox, infoLabel);
        
        // Boutons
        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);
        
        Button saveButton = new Button("💾 Ajouter");
        saveButton.setStyle("-fx-background-color: #A3BE8C; -fx-text-fill: white; " +
                           "-fx-background-radius: 10; -fx-padding: 12 25; -fx-font-size: 14; -fx-font-weight: bold;");
        saveButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                try {
                    // Récupérer le pourcentage du TextField
                    String percentageText = percentageField.getText().trim();
                    double percentage = Double.parseDouble(percentageText);
                    
                    if (percentage < 0 || percentage > 100) {
                        showModernAlert("❌ Le pourcentage doit être entre 0 et 100", "#BF616A");
                        return;
                    }
                    
                    TreeNodeData newNode = new TreeNodeData(
                        name, 
                        parentNode.level + 1, 
                        "#5E81AC", 
                        true,  // Tous les éléments ont un pourcentage
                        false  // Par défaut, les nouveaux éléments n'ont pas d'enfants
                    );
                    newNode.percentage = percentage;
                    
                    // Ajouter le nouveau nœud à la section parent
                    VBox childrenContainer = findChildrenContainer(parentSection);
                    if (childrenContainer != null) {
                        VBox newSection = createExpandableSection(newNode, null);
                        childrenContainer.getChildren().add(newSection);
                        
                        // Mettre à jour le parent pour qu'il ait une flèche d'expansion s'il n'en avait pas
                        updateParentExpandability(parentSection);
                        
                        // Redistribuer les pourcentages des éléments du même niveau
                        redistributeSiblingPercentages(newSection, newNode, 0, percentage);
                    }
                    
                    showModernAlert("✅ Élément ajouté: " + newNode.name + " (" + 
                                   String.format("%.1f%%", newNode.percentage) + ")", "#A3BE8C");
                    dialog.close();
                } catch (NumberFormatException ex) {
                    showModernAlert("❌ Veuillez saisir un nombre valide pour le pourcentage", "#BF616A");
                }
            } else {
                showModernAlert("❌ Veuillez saisir un nom", "#BF616A");
            }
        });
        
        Button cancelButton = new Button("❌ Annuler");
        cancelButton.setStyle("-fx-background-color: #BF616A; -fx-text-fill: white; " +
                             "-fx-background-radius: 10; -fx-padding: 12 25; -fx-font-size: 14; -fx-font-weight: bold;");
        cancelButton.setOnAction(e -> dialog.close());
        
        buttons.getChildren().addAll(saveButton, cancelButton);
        
        content.getChildren().addAll(titleLabel, nameLabel, nameField, percentageBox, buttons);
        
        Scene scene = new Scene(content, 450, 350);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private void showEditNodeDialog(TreeNodeData node, VBox section) {
        Stage dialog = new Stage();
        dialog.setTitle("Éditer l'élément");
        dialog.initModality(Modality.APPLICATION_MODAL);
        
        VBox content = new VBox(25);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: linear-gradient(to bottom, #FAFAFA, #F5F5F5); " +
                        "-fx-background-radius: 15; -fx-border-radius: 15; " +
                        "-fx-border-color: #E0E0E0; -fx-border-width: 1;");
        
        Label titleLabel = new Label("Modifier l'élément");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20; -fx-text-fill: #2E3440;");
        
        // Section Nom
        VBox nameSection = new VBox(8);
        Label nameLabel = new Label("Nom :");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #4C566A;");
        TextField nameField = new TextField(node.name);
        nameField.setStyle("-fx-padding: 12; -fx-font-size: 14; -fx-background-radius: 8; " +
                          "-fx-border-radius: 8; -fx-border-color: #D0D0D0; -fx-border-width: 1;");
        nameField.setPromptText("Nom de l'élément");
        nameSection.getChildren().addAll(nameLabel, nameField);
        
        // Section Pourcentage avec boutons + et - 
        VBox percentageSection = new VBox(10);
        Label percentageLabel = new Label("Pourcentage :");
        percentageLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #4C566A;");
        
        HBox percentageContainer = new HBox(5);
        percentageContainer.setAlignment(Pos.CENTER_LEFT);
        
        // Bouton -
        Button minusButton = new Button("−");
        minusButton.setStyle("-fx-background-color: #BF616A; -fx-text-fill: white; " +
                           "-fx-font-size: 18; -fx-font-weight: bold; -fx-padding: 8 12; " +
                           "-fx-background-radius: 5; -fx-cursor: hand;");
        
        // Affichage de la valeur
        Label valueDisplay = new Label(String.format("%.1f%%", node.percentage));
        valueDisplay.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: " + node.color + "; " +
                            "-fx-min-width: 80; -fx-alignment: center; -fx-border-color: " + node.color + "; " +
                            "-fx-border-width: 2; -fx-border-radius: 5; -fx-padding: 8;");
        
        // Bouton +
        Button plusButton = new Button("+");
        plusButton.setStyle("-fx-background-color: #A3BE8C; -fx-text-fill: white; " +
                          "-fx-font-size: 18; -fx-font-weight: bold; -fx-padding: 8 12; " +
                          "-fx-background-radius: 5; -fx-cursor: hand;");
        
        // Variables pour l'accélération
        final double[] currentValue = {node.percentage};
        final long[] pressStartTime = {0};
        final Timeline[] repeatTimeline = {null};
        
        // Clic simple pour bouton +
        plusButton.setOnAction(e -> {
            currentValue[0] = Math.min(100.0, currentValue[0] + 0.5);
            valueDisplay.setText(String.format("%.1f%%", currentValue[0]));
            node.percentage = currentValue[0];
        });
        
        // Clic simple pour bouton -
        minusButton.setOnAction(e -> {
            currentValue[0] = Math.max(0.0, currentValue[0] - 0.5);
            valueDisplay.setText(String.format("%.1f%%", currentValue[0]));
            node.percentage = currentValue[0];
        });
        
        // Configuration de l'accélération pour le bouton + (maintien appuyé)
        plusButton.setOnMousePressed(e -> {
            pressStartTime[0] = System.currentTimeMillis();
            if (repeatTimeline[0] != null) {
                repeatTimeline[0].stop();
            }
            
            repeatTimeline[0] = new Timeline(new KeyFrame(Duration.millis(100), event -> {
                long pressDuration = System.currentTimeMillis() - pressStartTime[0];
                double increment = calculateIncrement(pressDuration);
                
                currentValue[0] = Math.min(100.0, currentValue[0] + increment);
                valueDisplay.setText(String.format("%.1f%%", currentValue[0]));
                node.percentage = currentValue[0];
            }));
            repeatTimeline[0].setCycleCount(Timeline.INDEFINITE);
            repeatTimeline[0].play();
        });
        
        plusButton.setOnMouseReleased(e -> {
            if (repeatTimeline[0] != null) {
                repeatTimeline[0].stop();
            }
        });
        
        // Configuration de l'accélération pour le bouton - (maintien appuyé)
        minusButton.setOnMousePressed(e -> {
            pressStartTime[0] = System.currentTimeMillis();
            if (repeatTimeline[0] != null) {
                repeatTimeline[0].stop();
            }
            
            repeatTimeline[0] = new Timeline(new KeyFrame(Duration.millis(100), event -> {
                long pressDuration = System.currentTimeMillis() - pressStartTime[0];
                double decrement = calculateIncrement(pressDuration);
                
                currentValue[0] = Math.max(0.0, currentValue[0] - decrement);
                valueDisplay.setText(String.format("%.1f%%", currentValue[0]));
                node.percentage = currentValue[0];
            }));
            repeatTimeline[0].setCycleCount(Timeline.INDEFINITE);
            repeatTimeline[0].play();
        });
        
        minusButton.setOnMouseReleased(e -> {
            if (repeatTimeline[0] != null) {
                repeatTimeline[0].stop();
            }
        });
        
        Label infoLabel = new Label("(Clic simple : +0.5% | Maintien appuyé : accélération exponentielle)");
        infoLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #8A8A8A; -fx-font-style: italic;");
        
        percentageContainer.getChildren().addAll(minusButton, valueDisplay, plusButton);
        percentageSection.getChildren().addAll(percentageLabel, percentageContainer, infoLabel);
        
        // Boutons
        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(20, 0, 0, 0));
        
        Button validateButton = new Button("✅ Valider");
        validateButton.setStyle("-fx-background-color: linear-gradient(to bottom, #A3BE8C, #8FA68E); " +
                           "-fx-text-fill: white; -fx-background-radius: 10; -fx-border-radius: 10; " +
                           "-fx-padding: 12 25; -fx-font-size: 14; -fx-font-weight: bold; " +
                           "-fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);");
        
        validateButton.setOnAction(e -> {
            String newName = nameField.getText().trim();
            if (!newName.isEmpty()) {
                // Le pourcentage est déjà mis à jour en temps réel dans node.percentage
                double newPercentage = node.percentage;
                
                if (newPercentage < 0 || newPercentage > 100) {
                    showModernAlert("Le pourcentage doit être entre 0 et 100", "#BF616A");
                    return;
                }
                
                node.name = newName;
                double oldPercentage = newPercentage; // L'ancien pourcentage est maintenant égal au nouveau
                
                // Redistribuer les pourcentages des éléments du même niveau
                redistributeSiblingPercentages(section, node, oldPercentage, newPercentage);
                
                showModernAlert("Élément modifié avec succès - Auto-sauvegardé", "#A3BE8C");
                refreshConfigDisplay();
                dialog.close();
            } else {
                showModernAlert("Le nom ne peut pas être vide", "#BF616A");
            }
        });
        
        Button cancelButton = new Button("Annuler");
        cancelButton.setStyle("-fx-background-color: linear-gradient(to bottom, #E5E5E5, #D0D0D0); " +
                             "-fx-text-fill: #666666; -fx-background-radius: 10; -fx-border-radius: 10; " +
                             "-fx-padding: 12 25; -fx-font-size: 14; -fx-font-weight: bold; " +
                             "-fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);");
        cancelButton.setOnAction(e -> dialog.close());
        
        // Bouton supprimer (seulement pour les éléments non-racine)
        if (node.level > 0) {
            Button deleteButton = new Button("Supprimer");
            deleteButton.setStyle("-fx-background-color: linear-gradient(to bottom, #BF616A, #A54A58); " +
                                 "-fx-text-fill: white; -fx-background-radius: 10; -fx-border-radius: 10; " +
                                 "-fx-padding: 12 25; -fx-font-size: 14; -fx-font-weight: bold; " +
                                 "-fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);");
            deleteButton.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Supprimer l'élément");
                alert.setHeaderText("Êtes-vous sûr de vouloir supprimer cet élément ?");
                alert.setContentText("Élément: " + node.name + "\n\nCette action est irréversible.");
                
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // Récupérer le parent avant suppression pour vérifier s'il faut mettre à jour son bouton
                    VBox parentContainer = null;
                    if (section.getParent() instanceof VBox) {
                        parentContainer = (VBox) section.getParent();
                    }
                    
                    // Supprimer la section de son parent
                    if (section.getParent() != null) {
                        ((VBox) section.getParent()).getChildren().remove(section);
                    }
                    
                    // Mettre à jour l'expandabilité du parent si nécessaire
                    if (parentContainer != null) {
                        updateParentExpandabilityAfterDeletion(parentContainer);
                    }
                    
                    showModernAlert("Élément supprimé: " + node.name, "#BF616A");
                    dialog.close();
                }
            });
            
            buttons.getChildren().addAll(validateButton, deleteButton, cancelButton);
        } else {
            buttons.getChildren().addAll(validateButton, cancelButton);
        }
        
        content.getChildren().addAll(titleLabel, nameSection, percentageSection, buttons);
        
        Scene scene = new Scene(content, 450, 380);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private void showDeleteConfirmation(TreeNodeData node, VBox section) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer l'élément");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer cet élément ?");
        alert.setContentText("Élément: " + node.name + "\n\nCette action est irréversible.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Récupérer le parent avant suppression pour vérifier s'il faut mettre à jour son bouton
            VBox parentContainer = null;
            if (section.getParent() instanceof VBox) {
                parentContainer = (VBox) section.getParent();
            }
            
            // Supprimer la section de son parent
            if (section.getParent() != null) {
                ((VBox) section.getParent()).getChildren().remove(section);
            }
            
            // Mettre à jour l'expandabilité du parent si nécessaire
            if (parentContainer != null) {
                updateParentExpandabilityAfterDeletion(parentContainer);
            }
            
            showModernAlert("🗑️ Élément supprimé: " + node.name, "#BF616A");
        }
    }
    
    private void showPercentageDialog(TreeNodeData node) {
        Stage dialog = new Stage();
        dialog.setTitle("Pourcentage: " + node.name);
        dialog.initModality(Modality.APPLICATION_MODAL);
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
        
        Label titleLabel = new Label("📊 Configurer le pourcentage");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: #2E3440;");
        
        Label elementLabel = new Label("Élément: " + node.name);
        elementLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #4C566A;");
        
        // Slider avec valeur actuelle
        Label valueLabel = new Label(String.format("%.1f%%", node.percentage));
        valueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20; -fx-text-fill: " + node.color + ";");
        
        Slider slider = new Slider(0, 100, node.percentage);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(25);
        slider.setPrefWidth(350);
        slider.setStyle("-fx-control-inner-background: #E5E9F0;");
        
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            valueLabel.setText(String.format("%.1f%%", newVal.doubleValue()));
        });
        
        VBox sliderBox = new VBox(10);
        sliderBox.setAlignment(Pos.CENTER);
        sliderBox.getChildren().addAll(valueLabel, slider);
        
        // Boutons
        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);
        
        Button applyButton = new Button("✅ Appliquer");
        applyButton.setStyle("-fx-background-color: #A3BE8C; -fx-text-fill: white; " +
                           "-fx-background-radius: 10; -fx-padding: 12 25; -fx-font-size: 14; -fx-font-weight: bold;");
        applyButton.setOnAction(e -> {
            node.percentage = slider.getValue();
            showModernAlert("📊 Pourcentage mis à jour: " + node.name + " (" + 
                           String.format("%.1f%%", node.percentage) + ") - Auto-sauvegardé", "#A3BE8C");
            refreshConfigDisplay(); // Rafraîchir l'affichage pour montrer le nouveau pourcentage
            dialog.close();
        });
        
        Button cancelButton = new Button("❌ Annuler");
        cancelButton.setStyle("-fx-background-color: #BF616A; -fx-text-fill: white; " +
                             "-fx-background-radius: 10; -fx-padding: 12 25; -fx-font-size: 14; -fx-font-weight: bold;");
        cancelButton.setOnAction(e -> dialog.close());
        
        buttons.getChildren().addAll(applyButton, cancelButton);
        
        content.getChildren().addAll(titleLabel, elementLabel, sliderBox, buttons);
        
        Scene scene = new Scene(content, 450, 300);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private HBox createGlobalActions() {
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(15));
        
        Button expandAllButton = new Button("📂 Étendre Tout");
        expandAllButton.setStyle("-fx-background-color: #5E81AC; -fx-text-fill: white; " +
                                "-fx-background-radius: 10; -fx-padding: 12 20; -fx-font-size: 14;");
        expandAllButton.setOnAction(e -> expandCollapseAll(true));
        
        Button collapseAllButton = new Button("📁 Fermer Tout");
        collapseAllButton.setStyle("-fx-background-color: #81A1C1; -fx-text-fill: white; " +
                                  "-fx-background-radius: 10; -fx-padding: 12 20; -fx-font-size: 14;");
        collapseAllButton.setOnAction(e -> expandCollapseAll(false));
        
        Button reloadConfigButton = new Button("🔄 Recharger");
        reloadConfigButton.setStyle("-fx-background-color: #88C0D0; -fx-text-fill: white; " +
                                 "-fx-background-radius: 10; -fx-padding: 12 20; -fx-font-size: 14;");
        reloadConfigButton.setOnAction(e -> reloadConfiguration());
        
        Button cleanupButton = new Button("🧹 Nettoyer");
        cleanupButton.setStyle("-fx-background-color: #D08770; -fx-text-fill: white; " +
                                  "-fx-background-radius: 10; -fx-padding: 12 20; -fx-font-size: 14;");
        cleanupButton.setOnAction(e -> runEmergencyCleanup());
        
        actions.getChildren().addAll(expandAllButton, collapseAllButton, reloadConfigButton, cleanupButton);
        return actions;
    }
    
    private void expandCollapseAll(boolean expand) {
        // Trouver toutes les sections expandables dans l'arbre
        ScrollPane configTab = (ScrollPane) ((TabPane) ((BorderPane) generateButton.getScene().getRoot())
                .getCenter()).getTabs().get(3).getContent();
        VBox mainContainer = (VBox) configTab.getContent();
        VBox treeContainer = (VBox) mainContainer.getChildren().get(2); // Le conteneur de l'arbre
        VBox treeStructure = (VBox) treeContainer.getChildren().get(0);
        
        expandCollapseAllSections(treeStructure, expand);
        
        String action = expand ? "étendues" : "fermées";
        showModernAlert("📂 Toutes les sections ont été " + action, "#5E81AC");
    }
    
    private void expandCollapseAllSections(VBox container, boolean expand) {
        for (javafx.scene.Node node : container.getChildren()) {
            if (node instanceof VBox) {
                VBox section = (VBox) node;
                
                // Chercher le bouton d'expansion dans la première ligne
                if (section.getChildren().size() > 0 && section.getChildren().get(0) instanceof HBox) {
                    HBox nodeRow = (HBox) section.getChildren().get(0);
                    if (nodeRow.getChildren().size() > 0 && nodeRow.getChildren().get(0) instanceof Button) {
                        Button expandButton = (Button) nodeRow.getChildren().get(0);
                        
                        // Si c'est un bouton d'expansion (pas un bullet point)
                        if (expandButton.getText().equals("▶") || expandButton.getText().equals("▼")) {
                            VBox childrenContainer = findChildrenContainer(section);
                            if (childrenContainer != null) {
                                boolean isCurrentlyExpanded = childrenContainer.isVisible();
                                
                                if (expand && !isCurrentlyExpanded) {
                                    // Étendre
                                    expandButton.setText("▼");
                                    childrenContainer.setVisible(true);
                                    childrenContainer.setManaged(true);
                                    childrenContainer.setOpacity(1.0);
                                } else if (!expand && isCurrentlyExpanded) {
                                    // Fermer
                                    expandButton.setText("▶");
                                    childrenContainer.setVisible(false);
                                    childrenContainer.setManaged(false);
                                }
                                
                                // Appliquer récursivement aux enfants
                                expandCollapseAllSections(childrenContainer, expand);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void reloadConfiguration() {
        try {
            // Recharger la configuration depuis le fichier
            configService.loadConfiguration();
            
            // Rafraîchir l'affichage
            refreshConfigDisplay();
            
            // Afficher les statistiques de la dernière sauvegarde
            TreeNode root = configService.getRootNode();
            int nodeCount = countAllNodesInTree(root);
            
            showModernAlert("🔄 Configuration rechargée avec succès !\n" +
                           "📊 " + nodeCount + " nœuds trouvés\n" +
                           "💾 Dernière sauvegarde restaurée", "#88C0D0");
            
        } catch (Exception e) {
            logger.error("Erreur lors du rechargement de la configuration", e);
            showModernAlert("❌ Erreur lors du rechargement de la configuration", "#BF616A");
        }
    }
    
    /**
     * Compte récursivement tous les nœuds de l'arbre
     */
    private int countAllNodesInTree(TreeNode node) {
        if (node == null) return 0;
        
        int count = 1; // Le nœud actuel
        for (TreeNode child : node.getChildren()) {
            count += countAllNodesInTree(child);
        }
        return count;
    }
    
    private void refreshConfigDisplay() {
        // Méthode pour rafraîchir l'affichage de l'arbre de configuration
        // Pour l'instant, on affiche juste un message
        showModernAlert("🔄 Configuration mise à jour", "#5E81AC");
    }
    
    private void redistributeSiblingPercentages(VBox currentSection, TreeNodeData modifiedNode, double oldPercentage, double newPercentage) {
        // Trouver le parent de la section courante pour identifier les siblings
        if (currentSection.getParent() instanceof VBox) {
            VBox parentContainer = (VBox) currentSection.getParent();
            
            // Collecter tous les siblings (éléments du même niveau)
            List<TreeNodeData> siblings = new ArrayList<>();
            List<VBox> siblingsSections = new ArrayList<>();
            
            for (javafx.scene.Node child : parentContainer.getChildren()) {
                if (child instanceof VBox && child != currentSection) {
                    VBox siblingSection = (VBox) child;
                    Object userData = siblingSection.getUserData();
                    if (userData instanceof TreeNodeData) {
                        TreeNodeData siblingNode = (TreeNodeData) userData;
                        if (siblingNode.level == modifiedNode.level) {
                            siblings.add(siblingNode);
                            siblingsSections.add(siblingSection);
                        }
                    }
                }
            }
            
            // Si il y a des siblings, redistribuer les pourcentages
            if (!siblings.isEmpty()) {
                // Calculer le total actuel des siblings (avant redistribution)
                double totalSiblingsPercentage = siblings.stream()
                    .mapToDouble(node -> node.percentage)
                    .sum();
                
                // Calculer ce qui reste à redistribuer (100% - nouveau pourcentage du nœud modifié)
                double availablePercentage = 100.0 - newPercentage;
                
                if (availablePercentage > 0.001 && totalSiblingsPercentage > 0.001) {
                    // Redistribuer proportionnellement
                    for (TreeNodeData sibling : siblings) {
                        double proportionOfSibling = sibling.percentage / totalSiblingsPercentage;
                        sibling.percentage = Math.round((availablePercentage * proportionOfSibling) * 10.0) / 10.0;
                    }
                } else if (availablePercentage > 0.001) {
                    // Si tous les siblings sont à 0%, redistribuer équitablement
                    double equalShare = Math.round((availablePercentage / siblings.size()) * 10.0) / 10.0;
                    for (TreeNodeData sibling : siblings) {
                        sibling.percentage = equalShare;
                    }
                } else {
                    // Si pas assez de place, mettre tout à 0
                    for (TreeNodeData sibling : siblings) {
                        sibling.percentage = 0.0;
                    }
                }
                
                // Mettre à jour l'affichage des siblings
                updateSectionDisplay(currentSection, modifiedNode);
                for (int i = 0; i < siblings.size(); i++) {
                    updateSectionDisplay(siblingsSections.get(i), siblings.get(i));
                }
            }
        }
    }
    
    private void updateParentExpandability(VBox parentSection) {
        // Récupérer les données du nœud parent
        TreeNodeData parentNode = (TreeNodeData) parentSection.getUserData();
        if (parentNode != null) {
            // Vérifier si le parent avait déjà des enfants
            if (!parentNode.hasChildren) {
                // Marquer le parent comme ayant des enfants
                parentNode.hasChildren = true;
                
                // Mettre à jour le bouton d'expansion du parent
                if (parentSection.getChildren().size() > 0 && parentSection.getChildren().get(0) instanceof HBox) {
                    HBox nodeRow = (HBox) parentSection.getChildren().get(0);
                    if (nodeRow.getChildren().size() > 0 && nodeRow.getChildren().get(0) instanceof Button) {
                        Button expandButton = (Button) nodeRow.getChildren().get(0);
                        
                        // Transformer le bullet point en flèche d'expansion
                        expandButton.setText("▶");
                        expandButton.setStyle("-fx-background-color: transparent; " +
                                            "-fx-text-fill: " + parentNode.color + "; " +
                                            "-fx-font-weight: bold; " +
                                            "-fx-padding: 0; " +
                                            "-fx-cursor: hand; " +
                                            "-fx-font-size: 12;");
                        expandButton.setDisable(false);
                        
                        // Ajouter l'action d'expansion/contraction
                        expandButton.setOnAction(e -> toggleExpansion(expandButton, parentSection));
                    }
                }
            }
        }
    }

    private void updateParentExpandabilityAfterDeletion(VBox parentContainer) {
        // Trouver la section parent qui contient le conteneur d'enfants
        VBox parentSection = null;
        
        // Remonter dans la hiérarchie pour trouver la section parent
        javafx.scene.Node currentNode = parentContainer;
        while (currentNode != null && currentNode.getParent() != null) {
            if (currentNode instanceof VBox && ((VBox) currentNode).getUserData() instanceof TreeNodeData) {
                parentSection = (VBox) currentNode;
                break;
            }
            currentNode = currentNode.getParent();
        }
        
        if (parentSection != null) {
            TreeNodeData parentNode = (TreeNodeData) parentSection.getUserData();
            
            // Compter le nombre d'enfants restants dans le conteneur
            int remainingChildren = 0;
            for (javafx.scene.Node child : parentContainer.getChildren()) {
                if (child instanceof VBox && ((VBox) child).getUserData() instanceof TreeNodeData) {
                    remainingChildren++;
                }
            }
            
            // Si plus aucun enfant, transformer la flèche en bullet point
            if (remainingChildren == 0 && parentNode.hasChildren) {
                parentNode.hasChildren = false;
                
                // Mettre à jour le bouton d'expansion du parent
                if (parentSection.getChildren().size() > 0 && parentSection.getChildren().get(0) instanceof HBox) {
                    HBox nodeRow = (HBox) parentSection.getChildren().get(0);
                    if (nodeRow.getChildren().size() > 0 && nodeRow.getChildren().get(0) instanceof Button) {
                        Button expandButton = (Button) nodeRow.getChildren().get(0);
                        
                        // Transformer la flèche en bullet point
                        expandButton.setText("•");
                        expandButton.setStyle("-fx-background-color: transparent; " +
                                            "-fx-text-fill: #8A8A8A; " +
                                            "-fx-font-weight: bold; " +
                                            "-fx-padding: 0; " +
                                            "-fx-cursor: default; " +
                                            "-fx-font-size: 16;");
                        expandButton.setDisable(true);
                        expandButton.setOnAction(null); // Supprimer l'action d'expansion
                    }
                }
            }
        }
    }

    private void updateSectionDisplay(VBox section, TreeNodeData node) {
        // Mettre à jour l'affichage du nom avec le nouveau pourcentage
        if (section.getChildren().size() > 0 && section.getChildren().get(0) instanceof HBox) {
            HBox nodeRow = (HBox) section.getChildren().get(0);
            
            // Trouver le label du nom (deuxième élément après le bouton d'expansion)
            for (javafx.scene.Node child : nodeRow.getChildren()) {
                if (child instanceof Label) {
                    Label nameLabel = (Label) child;
                    
                    // Mettre à jour le texte avec le nouveau pourcentage
                    String displayText = node.name;
                    if (!node.name.contains("%")) {
                        displayText += " (" + String.format("%.1f%%", node.percentage) + ")";
                    }
                    nameLabel.setText(displayText);
                    break;
                }
            }
        }
    }
    
    private double calculateIncrement(long pressDuration) {
        // Calcul de l'incrément avec accélération exponentielle
        if (pressDuration < 500) {
            return 0.1; // Incrément de base très précis
        } else if (pressDuration < 1000) {
            return 0.5; // Accélération modérée
        } else if (pressDuration < 2000) {
            return 1.0; // Accélération normale
        } else if (pressDuration < 3000) {
            return 2.5; // Accélération rapide
        } else {
            return 5.0; // Accélération maximale
        }
    }
    
    // Classe pour représenter un nœud d'arbre avec support d'expansion
    private static class TreeNodeData {
        String name;
        int level;
        String color;
        boolean hasPercentage;
        boolean hasChildren;
        double percentage; // Pourcentage de cet élément
        
        TreeNodeData(String name, int level, String color, boolean hasPercentage, boolean hasChildren) {
            this.name = name;
            this.level = level;
            this.color = color;
            this.hasPercentage = hasPercentage;
            this.hasChildren = hasChildren;
            this.percentage = hasPercentage ? 50.0 : 0.0; // Valeur par défaut seulement si hasPercentage
        }
    }
    
    private void setupHistoryTable() {
        try {
            // Configuration des colonnes avec gestion d'erreur robuste
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            selectionColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
            selectionColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectionColumn));
            pathColumn.setCellValueFactory(new PropertyValueFactory<>("decisionPath"));
            
            // Utiliser getLastLevel() pour afficher le résultat final
            nodeColumn.setCellValueFactory(cellData -> {
                try {
                    String result = cellData.getValue().getLastLevel();
                    return new javafx.beans.property.SimpleStringProperty(result != null ? result : "Non défini");
                } catch (Exception e) {
                    logger.warn("Erreur lors de l'obtention du nœud résultat pour le slot {}", cellData.getValue().getId());
                    return new javafx.beans.property.SimpleStringProperty("Erreur");
                }
            });
            
            categoryColumn.setCellValueFactory(cellData -> {
                try {
                    Object category = cellData.getValue().getMetadata("category");
                    return new javafx.beans.property.SimpleStringProperty(
                        category != null ? category.toString() : "Non définie");
                } catch (Exception e) {
                    logger.warn("Erreur lors de l'obtention de la catégorie pour le slot {}", cellData.getValue().getId());
                    return new javafx.beans.property.SimpleStringProperty("Erreur");
                }
            });
            
            dateColumn.setCellValueFactory(cellData -> {
                try {
                    return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getGeneratedAt().format(DATE_FORMATTER));
                } catch (Exception e) {
                    logger.warn("Erreur lors du formatage de la date pour le slot {}", cellData.getValue().getId());
                    return new javafx.beans.property.SimpleStringProperty("Date invalide");
                }
            });
        
        // Configurer la table avec les données déjà initialisées
        historyTable.setItems(historyData);
        
        // Mettre à jour dynamiquement le compteur quand l'utilisateur coche/décoche une ligne
        attachSelectionListeners();
        
        // Listener pour mettre à jour le compteur de sélection automatiquement
        historyData.addListener((ListChangeListener<GeneratedSlot>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    updateSelectionCounter();
                    // Attacher les listeners aux nouveaux éléments ajoutés
                    if (change.wasAdded()) {
                        change.getAddedSubList().forEach(slot ->
                            slot.selectedProperty().addListener((obs, oldVal, newVal) -> updateSelectionCounter()));
                    }
                }
            }
        });
            
            logger.info("Table d'historique configurée avec succès");
        } catch (Exception e) {
            logger.error("Erreur lors de la configuration de la table d'historique", e);
        }
    }
    
    /**
     * Crée la barre d'actions pour la sélection et suppression des slots
     */
    private HBox createSelectionActionBar() {
        HBox actionBar = new HBox(10);
        actionBar.setPadding(new Insets(10));
        actionBar.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #DEE2E6; -fx-border-width: 0 0 1 0;");
        actionBar.setAlignment(Pos.CENTER_LEFT);
        
        // Bouton "Tout sélectionner"
        selectAllButton = new Button("☑️ Tout sélectionner");
        selectAllButton.setStyle("-fx-background-color: #28A745; -fx-text-fill: white; -fx-font-weight: bold;");
        selectAllButton.setOnAction(e -> selectAllSlots());
        
        // Bouton "Tout désélectionner"
        deselectAllButton = new Button("☐ Tout désélectionner");
        deselectAllButton.setStyle("-fx-background-color: #6C757D; -fx-text-fill: white; -fx-font-weight: bold;");
        deselectAllButton.setOnAction(e -> deselectAllSlots());
        
        // Compteur de sélection
        selectionCounterLabel = new Label("0 élément(s) sélectionné(s)");
        selectionCounterLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        
        // Espaceur
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Bouton "Supprimer sélectionnés"
        deleteSelectedButton = new Button("🗑️ Supprimer sélectionnés");
        deleteSelectedButton.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteSelectedButton.setOnAction(e -> deleteSelectedSlots());
        deleteSelectedButton.setDisable(true); // Désactivé par défaut
        
        actionBar.getChildren().addAll(selectAllButton, deselectAllButton, selectionCounterLabel, spacer, deleteSelectedButton);
        
        return actionBar;
    }
    
    /**
     * Sélectionne tous les slots
     */
    private void selectAllSlots() {
        for (GeneratedSlot slot : historyData) {
            slot.setSelected(true);
        }
        updateSelectionCounter();
        logger.info("Tous les slots ont été sélectionnés");
    }
    
    /**
     * Désélectionne tous les slots
     */
    private void deselectAllSlots() {
        for (GeneratedSlot slot : historyData) {
            slot.setSelected(false);
        }
        updateSelectionCounter();
        logger.info("Tous les slots ont été désélectionnés");
    }
    
    /**
     * Retourne la liste des slots sélectionnés
     */
    private List<GeneratedSlot> getSelectedSlots() {
        return historyData.stream()
                .filter(GeneratedSlot::isSelected)
                .collect(Collectors.toList());
    }
    
    /**
     * Met à jour le compteur de sélection et l'état des boutons
     */
    private void updateSelectionCounter() {
        int selectedCount = getSelectedSlots().size();
        selectionCounterLabel.setText(selectedCount + " élément(s) sélectionné(s)");
        deleteSelectedButton.setDisable(selectedCount == 0);
        
        // Mettre à jour le style du compteur selon le nombre
        if (selectedCount > 0) {
            selectionCounterLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #DC3545;");
        } else {
            selectionCounterLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        }
    }
    
    /**
     * Attache un listener sur la propriété selected de chaque slot afin de mettre à jour
     * le compteur en temps réel lorsque l'utilisateur coche ou décoche manuellement.
     */
    private void attachSelectionListeners() {
        for (GeneratedSlot slot : historyData) {
            slot.selectedProperty().addListener((obs, oldVal, newVal) -> updateSelectionCounter());
        }
    }
    
    /**
     * Supprime les slots sélectionnés après confirmation
     */
    private void deleteSelectedSlots() {
        List<GeneratedSlot> selectedSlots = getSelectedSlots();
        if (selectedSlots.isEmpty()) {
            showInfoAlert("Information", "Aucun élément sélectionné");
            return;
        }
        
        // Dialog de confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer les éléments sélectionnés ?");
        alert.setContentText("Vous êtes sur le point de supprimer " + selectedSlots.size() + " élément(s).\nCette action est irréversible.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Supprimer les slots sélectionnés
            for (GeneratedSlot slot : selectedSlots) {
                historyService.removeSlot(slot.getId());
            }
            
            // Rafraîchir l'affichage
            refreshHistory();
            updateSelectionCounter();
            refreshStatistics();
            
            showInfoAlert("Suppression réussie", selectedSlots.size() + " élément(s) supprimé(s) avec succès");
            logger.info("{} slots supprimés", selectedSlots.size());
        }
    }
    
    private void loadInitialData() {
        try {
            // Charger les données existantes
            refreshHistory();
            
            // Ajouter des données de test si la table est vide
            if (historyData.isEmpty()) {
                logger.info("Table vide, ajout de données de test");
                addTestData();
            }
            
            updateSelectionCounter();
            refreshStatistics();
            logger.info("Données initiales chargées : {} slots", historyData.size());
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des données initiales", e);
            showErrorAlert("Erreur", "Impossible de charger les données : " + e.getMessage());
        }
    }
    
    private void addTestData() {
        try {
            // Créer des slots de test
            for (int i = 1; i <= 3; i++) {
                GeneratedSlot testSlot = new GeneratedSlot(
                    "Test Path " + i, 
                    "test_id_" + i, 
                    "Test Level " + i
                );
                testSlot.setId((long) i);
                testSlot.setGeneratedAt(java.time.LocalDateTime.now().minusHours(i));
                testSlot.setSelected(false);
                historyData.add(testSlot);
            }
            logger.info("3 slots de test ajoutés");
        } catch (Exception e) {
            logger.error("Erreur lors de l'ajout des données de test", e);
        }
    }
    
    private void generateSlot() {
        try {
            GeneratedSlot slot = generationEngine.generateSlot();
            historyService.addSlot(slot);
            
            // Mettre à jour l'affichage moderne du slot
            updateModernSlotDisplay(slot);
            refreshHistory();
            refreshStatistics();
            
        } catch (Exception e) {
            logger.error("Erreur generation", e);
            showErrorAlert("Erreur", "Impossible de generer : " + e.getMessage());
        }
    }
    
    /**
     * Met à jour l'affichage moderne du slot généré
     */
    private void updateModernSlotDisplay(GeneratedSlot slot) {
        if (currentSlotContainer != null && currentSlotContainer.getChildren().size() >= 2) {
            // Récupérer la zone de résultat (2ème enfant)
            VBox resultBox = (VBox) currentSlotContainer.getChildren().get(1);
            resultBox.getChildren().clear();
            
            // Icône du résultat
            Label iconLabel = new Label("🎯");
            iconLabel.setStyle(
                "-fx-font-size: 48px;" +
                "-fx-text-alignment: center;"
            );
            
            // Nom du résultat principal
            Label resultLabel = new Label(slot.getLastLevel());
            resultLabel.setStyle(
                "-fx-text-fill: #2E3440;" +
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-alignment: center;" +
                "-fx-wrap-text: true;"
            );
            
            // Chemin complet (plus discret)
            Label pathLabel = new Label("Chemin : " + slot.getDecisionPath());
            pathLabel.setStyle(
                "-fx-text-fill: #6c757d;" +
                "-fx-font-size: 12px;" +
                "-fx-text-alignment: center;" +
                "-fx-wrap-text: true;"
            );
            
            // Date de génération
            Label dateLabel = new Label("Généré le " + slot.getGeneratedAt().format(DATE_FORMATTER));
            dateLabel.setStyle(
                "-fx-text-fill: #28a745;" +
                "-fx-font-size: 11px;" +
                "-fx-font-style: italic;" +
                "-fx-text-alignment: center;"
            );
            
            // Animation d'apparition
            resultBox.setOpacity(0);
            resultBox.getChildren().addAll(iconLabel, resultLabel, pathLabel, dateLabel);
            
            // Fade in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), resultBox);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        }
    }
    
    private void resetAlternance() {
        try {
            // Dans le nouveau système générique, nous n'avons plus d'alternance VIE spécifique
            // Cette fonction peut vider l'historique ou réinitialiser les configurations
            historyService.clearHistory();
            showInfoAlert("Réinitialisation", "L'historique des slots a été vidé !");
            refreshHistory();
            refreshStatistics();
            
        } catch (Exception e) {
            logger.error("Erreur réinitialisation", e);
            showErrorAlert("Erreur", "Erreur lors de la réinitialisation : " + e.getMessage());
        }
    }
    
    private void refreshHistory() {
        List<GeneratedSlot> allSlots = historyService.getAllSlots();
        historyData.clear();
        historyData.addAll(allSlots);
    }
    
    private void refreshStatistics() {
        try {
            StatisticsService.StatisticsReport report = statisticsService.getGeneralStatistics();
            
            // Mise à jour du système de barres personnalisées
            if (customBarsContainer != null) {
                // Collecter TOUS les nœuds traversés dans les chemins de décision
                Map<String, Integer> uniqueNodeStats = new HashMap<>();
                
                List<GeneratedSlot> allSlots = historyService.getAllSlots();
                for (GeneratedSlot slot : allSlots) {
                    String decisionPath = slot.getDecisionPath();
                    
                    // Traiter TOUS les nœuds du chemin de décision, pas seulement le final
                    if (decisionPath != null && decisionPath.contains(" > ")) {
                        String[] pathParts = decisionPath.split(" > ");
                        
                        // Parcourir tous les nœuds du chemin (sauf la racine "Arbre Principal")
                        for (int i = 1; i < pathParts.length; i++) {
                            String nodeName = pathParts[i].trim();
                            
                            if (!nodeName.isEmpty() && !nodeName.equals("Arbre Principal")) {
                                // Nettoyer le nom (supprimer emojis/caractères spéciaux) MAIS GARDER L'ID pour la distinction
                                String cleanNodeName = nodeName.replaceAll("[\\p{So}\\p{Cn}]", "").trim();
                                
                                // Extraire le nom pur et construire une clé unique avec le chemin parent
                                String displayName = cleanNodeName;
                                String idPart = "";
                                
                                // Si le nom contient [ID:...], extraire le nom pur et l'ID
                                if (cleanNodeName.contains("[ID:") && cleanNodeName.contains("]")) {
                                    int idStart = cleanNodeName.indexOf("[ID:");
                                    displayName = cleanNodeName.substring(0, idStart).trim();
                                    idPart = cleanNodeName.substring(idStart);
                                }
                                
                                // TOUJOURS créer une clé unique basée sur le nom + chemin parent
                                // Cela distingue "Linkedin sous Rouen" de "Linkedin sous Autre que Rouen"
                                StringBuilder pathContext = new StringBuilder();
                                for (int j = 1; j < i; j++) { // Construire le chemin parent
                                    if (pathContext.length() > 0) pathContext.append(" > ");
                                    String parentName = pathParts[j].replaceAll("[\\p{So}\\p{Cn}]", "").trim();
                                    if (parentName.contains("[ID:")) {
                                        parentName = parentName.substring(0, parentName.indexOf("[ID:")).trim();
                                    }
                                    pathContext.append(parentName);
                                }
                                
                                // Utiliser nom + contexte parent comme clé unique, afficher seulement le nom
                                String nodeKey = displayName + "|" + pathContext.toString();
                                if (!idPart.isEmpty()) {
                                    nodeKey += "|" + idPart;
                                }
                                
                                if (!displayName.isEmpty()) {
                                    uniqueNodeStats.merge(nodeKey, 1, Integer::sum);
                                }
                            }
                        }
                    }
                    
                    // SUPPRESSION de cette section car le nœud final est déjà compté dans la boucle ci-dessus
                    // Le dernier élément du chemin "Arbre Principal > VIE > Spontanée" est "Spontanée"
                    // qui est déjà traité dans la boucle for, donc pas besoin de le recompter
                }
                
                // Convertir les clés en labels d'affichage (sans ID, sans contexte interne)
                // MAIS garder les entrées distinctes pour que les Linkedin apparaissent séparément
                Map<String, Integer> displayStats = new LinkedHashMap<>();
                for (Map.Entry<String, Integer> entry : uniqueNodeStats.entrySet()) {
                    String key = entry.getKey();
                    String displayLabel;
                    
                    if (key.contains("|")) {
                        // Extraire seulement le nom pur pour l'affichage
                        displayLabel = key.split("\\|")[0];
                    } else {
                        displayLabel = key;
                    }
                    
                    // Utiliser la clé interne unique mais afficher le nom pur
                    // Cela permet d'avoir deux barres "Linkedin" distinctes
                    displayStats.put(key, entry.getValue());
                }
                
                // Afficher les statistiques complètes des nœuds
                logger.info("🎯 Statistiques complètes des nœuds générés (avec IDs uniques) :");
                displayStats.entrySet().stream()
                        .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                        .limit(20)
                        .forEach(entry -> {
                            String displayName = entry.getKey().contains("|") ? 
                                entry.getKey().split("\\|")[0] : entry.getKey();
                            logger.info("   • {} : {} fois", displayName, entry.getValue());
                        });
                
                // Créer les barres avec les nœuds les plus fréquents
                List<Map.Entry<String, Integer>> sortedStats = displayStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(15)
                    .collect(Collectors.toList());
                
                // Trouver la valeur maximale pour le calcul des proportions
                int maxValue = sortedStats.isEmpty() ? 1 : sortedStats.get(0).getValue();
                
                // Vider le container et recréer les barres
                customBarsContainer.getChildren().clear();
                customBarItems.clear();
                
                // Créer les nouvelles barres personnalisées
                for (int i = 0; i < sortedStats.size(); i++) {
                    Map.Entry<String, Integer> entry = sortedStats.get(i);
                    String internalKey = entry.getKey();
                    String displayLabel = internalKey.contains("|") ? 
                        internalKey.split("\\|")[0] : internalKey;
                    
                    // Utiliser la clé interne complète comme identifiant unique pour les barres
                    // Mais afficher seulement le nom pur à l'utilisateur
                    CustomBarItem barItem = new CustomBarItem(displayLabel, entry.getValue(), i);
                    barItem.setInternalKey(internalKey); // Stocker la clé unique
                    barItem.updateValue(entry.getValue(), maxValue);
                    customBarItems.add(barItem);
                    customBarsContainer.getChildren().add(barItem.container);
                }
            }
            
        } catch (Exception e) {
            logger.error("Erreur statistiques", e);
        }
    }
    
    private void exportToCsv() {
        try {
            String exportPath = exportService.exportToCSV();
            exportStatusLabel.setText("✅ Export CSV réussi !");
            exportStatusLabel.setStyle("-fx-text-fill: #A3BE8C;");
            
            // Notification visible
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("📊 Export CSV Réussi !");
            alert.setHeaderText("Votre fichier CSV a été créé avec succès");
            alert.setContentText("📁 Fichier créé :\n" + exportPath + "\n\n✅ L'export contient tous vos slots générés.");
            alert.showAndWait();
            
        } catch (Exception e) {
            logger.error("Erreur export CSV", e);
            exportStatusLabel.setText("❌ Erreur export CSV");
            exportStatusLabel.setStyle("-fx-text-fill: #BF616A;");
            showErrorAlert("Erreur", "Impossible d'exporter CSV : " + e.getMessage());
        }
    }
    
    private void exportToJson() {
        try {
            String exportPath = exportService.exportToJSON();
            exportStatusLabel.setText("✅ Export JSON réussi !");
            exportStatusLabel.setStyle("-fx-text-fill: #EBCB8B;");
            
            // Notification visible
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("📄 Export JSON Réussi !");
            alert.setHeaderText("Votre fichier JSON a été créé avec succès");
            alert.setContentText("📁 Fichier créé :\n" + exportPath + "\n\n✅ L'export contient tous vos slots générés au format JSON.");
            alert.showAndWait();
            
        } catch (Exception e) {
            logger.error("Erreur export JSON", e);
            exportStatusLabel.setText("❌ Erreur export JSON");
            exportStatusLabel.setStyle("-fx-text-fill: #BF616A;");
            showErrorAlert("Erreur", "Impossible d'exporter JSON : " + e.getMessage());
        }
    }
    
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private HBox createFooter() {
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(10));
        footer.setStyle("-fx-background-color: #4C566A;");
        
        Label appLabel = new Label("ApplyDance v1.1.0 - Interface Graphique JavaFX");
        appLabel.setStyle("-fx-text-fill: #D8DEE9;");
        appLabel.setFont(Font.font(11));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label statusLabel = new Label("Prêt");
        statusLabel.setStyle("-fx-text-fill: #88C0D0;");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
        
        footer.getChildren().addAll(appLabel, spacer, statusLabel);
        return footer;
    }
    
    private void showModernAlert(String message, String color) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Configuration");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style moderne pour l'alerte
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
        
        alert.showAndWait();
    }
    
    /**
     * Lance le nettoyage d'urgence des nœuds parasites
     */
    private void runEmergencyCleanup() {
        logger.info("🧹 Lancement du nettoyage d'urgence depuis l'interface");
        
        // Afficher un message de confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("🧹 Nettoyage d'urgence");
        confirmAlert.setHeaderText("Nettoyer les nœuds parasites/test ?");
        confirmAlert.setContentText("Cette action va supprimer tous les nœuds de test détectés dans l'arbre.\n\n" +
                                   "Les nœuds suivants seront supprimés :\n" +
                                   "• Nœuds avec 'Test' dans le nom\n" +
                                   "• Nœuds avec caractères corrompus\n" +
                                   "• Nœuds créés par les tests automatisés\n\n" +
                                   "⚠️ Cette action est irréversible !");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Afficher un loading message
            exportStatusLabel.setText("🧹 Nettoyage en cours...");
            exportStatusLabel.setStyle("-fx-text-fill: #D08770;");
            
            // Lancer le nettoyage dans un thread séparé
            new Thread(() -> {
                try {
                    boolean success = testService.emergencyCleanup();
                    
                    // Mettre à jour l'UI dans le thread JavaFX
                    Platform.runLater(() -> {
                        if (success) {
                            exportStatusLabel.setText("✅ Nettoyage réussi");
                            exportStatusLabel.setStyle("-fx-text-fill: #A3BE8C;");
                            showInfoAlert("🧹 Nettoyage Réussi", 
                                        "✅ Tous les nœuds parasites ont été supprimés !\n\n" +
                                        "• Configuration sauvegardée\n" +
                                        "• Arbre nettoyé\n" +
                                        "• Génération optimisée\n\n" +
                                        "Consultez les logs pour plus de détails.");
                            
                            // Rafraîchir la configuration si nécessaire
                            reloadConfiguration();
                        } else {
                            exportStatusLabel.setText("⚠️ Nettoyage partiel");
                            exportStatusLabel.setStyle("-fx-text-fill: #EBCB8B;");
                            showErrorAlert("🧹 Nettoyage Partiel", 
                                         "⚠️ Le nettoyage n'a pas pu supprimer tous les nœuds parasites.\n\n" +
                                         "Consultez les logs pour identifier les problèmes restants.\n" +
                                         "Vous pouvez relancer le nettoyage.");
                        }
                    });
                    
                } catch (Exception e) {
                    logger.error("Erreur lors du nettoyage d'urgence", e);
                    Platform.runLater(() -> {
                        exportStatusLabel.setText("❌ Erreur nettoyage");
                        exportStatusLabel.setStyle("-fx-text-fill: #BF616A;");
                        showErrorAlert("Erreur Nettoyage", "Une erreur est survenue lors du nettoyage:\n" + e.getMessage());
                    });
                }
            }).start();
        }
    }
    
    /**
     * Lance les tests automatisés et affiche les résultats
     */
    private void runAutomatedTests() {
        logger.info("🧪 Lancement des tests automatisés depuis l'interface");
        
        // Afficher un loading message
        exportStatusLabel.setText("🧪 Tests en cours...");
        exportStatusLabel.setStyle("-fx-text-fill: #EBCB8B;");
        
        // Lancer les tests dans un thread séparé pour ne pas bloquer l'UI
        new Thread(() -> {
            try {
                // Générer le rapport de l'état actuel
                testService.generateTreeReport();
                
                // Lancer tous les tests
                boolean allTestsPassed = testService.runAllTests();
                
                // Mettre à jour l'UI dans le thread JavaFX
                Platform.runLater(() -> {
                    if (allTestsPassed) {
                        exportStatusLabel.setText("✅ Tous les tests sont passés");
                        exportStatusLabel.setStyle("-fx-text-fill: #A3BE8C;");
                        showInfoAlert("Tests Automatisés", 
                                    "✅ Tous les tests sont passés avec succès!\n\n" +
                                    "• Persistance: ✅\n" +
                                    "• Génération: ✅\n" +
                                    "• Sérialisation JSON: ✅\n" +
                                    "• Redistribution: ✅\n\n" +
                                    "Consultez les logs pour plus de détails.");
                    } else {
                        exportStatusLabel.setText("❌ Certains tests ont échoué");
                        exportStatusLabel.setStyle("-fx-text-fill: #BF616A;");
                        showErrorAlert("Tests Automatisés", 
                                     "❌ Certains tests ont échoué!\n\n" +
                                     "Consultez les logs pour identifier les problèmes.\n" +
                                     "Les tests permettent de détecter:\n" +
                                     "• Problèmes de persistance\n" +
                                     "• Problèmes de génération\n" +
                                     "• Erreurs de sérialisation JSON");
                    }
                });
                
            } catch (Exception e) {
                logger.error("Erreur lors de l'exécution des tests automatisés", e);
                Platform.runLater(() -> {
                    exportStatusLabel.setText("❌ Erreur lors des tests");
                    exportStatusLabel.setStyle("-fx-text-fill: #BF616A;");
                    showErrorAlert("Erreur Tests", "Une erreur est survenue lors des tests:\n" + e.getMessage());
                });
            }
        }).start();
    }
} 