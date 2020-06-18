package climatechange.application;

import climatechange.application.rendering.Conversions;
import climatechange.application.rendering.Histograms;
import climatechange.application.rendering.Quadrilaterals;
import climatechange.data.Coordinates;
import climatechange.data.ResourceManager;
import climatechange.data.TemperatureMap;
import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

public class GlobalWarming3D implements Initializable {

    // Composants GUI
    @FXML private Pane earthCanvas;
    @FXML private VBox controlsVBox;
    @FXML private CheckBox showTempCheckBox;
    @FXML private TextField yearTextField;
    @FXML private Button playPauseButton;
    @FXML private ImageView playPauseImageView;
    @FXML private Button stopButton;
    @FXML private Spinner<Integer> speedSpinner;
    @FXML private Slider yearSlider;
    @FXML private RadioButton colorsRadioButton;
    @FXML private RadioButton histogramsRadioButton;
    @FXML private LineChart<Number, Number> lineChart;
    @FXML private NumberAxis xAxis;
    Label coordinatesLabel = new Label();

    // Gestion des ressources
    public ResourceManager resourceManager = new ResourceManager();
    private float minTemp, maxTemp;
    XYChart.Series<Number, Number> series;

    // Composants 3D
    Group earth;
    List<MeshView> quadrilaterals = new ArrayList<>();
    List<Box> histograms = new ArrayList<>();
    List<Node> key = new ArrayList<>();

    // Animation
    boolean animated = false;
    Timeline animation;

    /**
     * Méthode d'initialisation de l'application.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Charger les données
        try {
            resourceManager.readTemperatureFile(resourceManager.getClass().getResource("tempanomaly_4x4grid.csv").toURI().getPath());
        } catch(Exception e) {
            e.printStackTrace();
        }

        // Stocker min et max anomalies
        minTemp = resourceManager.getMinAnomaly();
        maxTemp = resourceManager.getMaxAnomaly();

        // Grouper les radio buttons
        ToggleGroup radioButtonsGroup = new ToggleGroup();
        colorsRadioButton.setToggleGroup(radioButtonsGroup);
        histogramsRadioButton.setToggleGroup(radioButtonsGroup);

        // Ajouter les vitesses possibles
        ObservableList<Integer> speeds = FXCollections.observableArrayList(1, 2, 4, 8, 16, 32, 64);
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(speeds);
        valueFactory.setValue(8);
        speedSpinner.setValueFactory(valueFactory);

        // Initialiser Canvas, Listeners et Graphique
        initEarthCanvas();
        initListeners();
        initTemperatures();
        initChart();
        updateChart(null);
        coordinatesLabel.getTransforms().add(new Translate(10,10));
        yearTextField.setText("2020");

        // Ajouter infobulles
        showTempCheckBox.setTooltip(new Tooltip("Display the temperature anomalies on the globe."));
        yearTextField.setTooltip(new Tooltip("Set a year to show its temperature anomalies."));
        yearSlider.setTooltip(new Tooltip("Set a year to show its temperature anomalies."));
        playPauseButton.setTooltip(new Tooltip("Animate the temperature evolution."));
        stopButton.setTooltip(new Tooltip("Stop the animation."));
        speedSpinner.setTooltip(new Tooltip("Set the animation speed."));
        colorsRadioButton.setTooltip(new Tooltip("Show temperatures with colored quadrilaterals on the surface of the Earth."));
        histogramsRadioButton.setTooltip(new Tooltip("Show temperatures with colored histograms on the surface of the Earth."));
        Tooltip.install(earthCanvas, new Tooltip("Drag and drop to rotate \nScroll to zoom \nPress CTRL for precision \nPress ALT to reset"));
        Tooltip.install(lineChart, new Tooltip("Click on an area on Earth to show its temperature evolution. \nClick on the background to show the average evolution."));
    }

    /**
     * Initialise le canevas 3D représentant la Terre.
     */
    public void initEarthCanvas() {
        //Import earth
        ObjModelImporter objModelImporter = new ObjModelImporter();
        try {
            URL modelUrl = this.getClass().getResource("earth/earth.obj");
            objModelImporter.read(modelUrl);
        } catch (ImportException e) {
            e.printStackTrace();
        }
        MeshView[] meshViews = objModelImporter.getImport();
        earth = new Group(meshViews);

        //Add camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        new CameraManager(camera, earthCanvas, earth);

        //Create subscene
        SubScene subScene = new SubScene(earth, 380, 380, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(Color.DARKGRAY);

        // Add subscene to canvas
        earthCanvas.getChildren().addAll(subScene, coordinatesLabel);
    }

    /**
     * Initialise l'affichage des températures.
     */
    public void initTemperatures() {
        initQuadrilaterals();
        initHistograms();
        initKey();

        if (showTempCheckBox.isSelected()) {
            enableKey();
            if (colorsRadioButton.isSelected()) enableQuadrilaterals();
            else enableHistograms();
        }
    }

    /**
     * Initialise tous les listeners des composants JavaFX de l'interface 2D et 3D.
     */
    public void initListeners() {
        // Listener pour le textField
        yearTextField.textProperty().addListener((observableValue, oldValue, newValue) -> {

            // On n'autorise que les entiers
            if (newValue.matches("\\d+")) {
                int year = Integer.parseInt(newValue);

                //Que les entiers entre 1880 et 2020
                if (year >= 1000) {
                    if (year < 1880) {
                        year = 1880;
                        yearTextField.setText(Integer.toString(year));
                    }
                    if (year > 2020) {
                        year = 2020;
                        yearTextField.setText(Integer.toString(year));
                    }

                    // Mise à jour du slider
                    yearSlider.setValue(year);
                    // Mise à jour de la carte
                    updateQuadrilaterals(year);
                    updateHistograms(year);
                }
            }
            // Suppression des caractères non numériques
            else yearTextField.setText(newValue.replaceAll("[^\\d]", ""));
        });


        // Listener pour le slider
        yearSlider.valueProperty().addListener(((observableValue, oldValue, newValue) -> {
            // Mise à jour du textField
            yearTextField.setText(Long.toString(Math.round((double)newValue)));
        }));


        // Listener pour la choice box
        speedSpinner.valueProperty().addListener(((observableValue, oldValue, newValue) -> {
            if (animated) {
                // On change la vitesse de l'animation
                animation.stop();
                animation = new Timeline(new KeyFrame(Duration.millis((float) 1000/newValue), event -> {
                    if (yearSlider.getValue() < 2020) yearSlider.increment();
                    else stopButton.fire();
                }));
                animation.setCycleCount(Timeline.INDEFINITE);
                animation.play();
            }
        }));


        // Listeners pour la souris sur la Terre
        // Affichage des coordonnées
        earth.setOnMouseMoved(mouseEvent -> {
            Point3D click = mouseEvent.getPickResult().getIntersectedPoint();
            coordinatesLabel.setText(Conversions.coord3dTogeoCoord(click).toString());
            mouseEvent.consume();
        });
        // Création du graphique
        earth.setOnMouseClicked(mouseEvent -> {
            Point3D click = mouseEvent.getPickResult().getIntersectedPoint();
            Coordinates coordinates = Conversions.coord3dTogeoCoord(click);

            updateChart(coordinates);
            mouseEvent.consume();
        });

        // Listeners pour la souris hors de la Terre
        earthCanvas.setOnMouseMoved(mouseEvent -> {
            coordinatesLabel.setText("");
        });
        earthCanvas.setOnMouseClicked(mouseEvent -> {
            updateChart(null);
        });
    }

    /**
     * Initialise la liste de quadrilatères représentant les anomalies de température pour l'année 2020.
     */
    public void initQuadrilaterals() {
        TemperatureMap temperatureMap = resourceManager.getMap(2020);

        for (Map.Entry<Coordinates, Float> entry : temperatureMap.entrySet()) {
            int latitude = entry.getKey().getLatitude();
            int longitude = entry.getKey().getLongitude();
            float temperature = entry.getValue();

            quadrilaterals.add(Quadrilaterals.makeCenteredQuadrilateral(latitude, longitude, Conversions.temperatureToColor(temperature, minTemp, maxTemp), 4));
        }
    }

    /**
     * Initialise la liste d'histogrammes représentant les anomalies de température pour l'année 2020.
     */
    public void initHistograms() {
        TemperatureMap temperatureMap = resourceManager.getMap(2020);

        for (Map.Entry<Coordinates, Float> entry : temperatureMap.entrySet()) {
            int latitude = entry.getKey().getLatitude();
            int longitude = entry.getKey().getLongitude();
            float temperature = entry.getValue();

            histograms.add(Histograms.makeHistogram(latitude, longitude, temperature, maxTemp, minTemp));
        }
    }

    /**
     * Initialise la liste de composants 2D (Rectangles et Labels) de la légende.
     * Cette légende est commune aux 2 modes de visualisation (histogrammes et quadrilatères).
     */
    public void initKey() {
        Label unit = new Label("°C");
        unit.setTranslateX(345);
        unit.setTranslateY(80);
        key.add(unit);
        for (float i = 0; i < 10; i++) {
            float temperature = Math.max(Math.min(Math.round(minTemp + i/10 * (maxTemp - minTemp)), maxTemp), minTemp);

            Rectangle tempRectangle = new Rectangle(340, 280 - i*20, 20, 20);
            tempRectangle.setFill(Conversions.temperatureToColor(temperature, minTemp, maxTemp));

            Label tempLabel = new Label(new DecimalFormat("#").format(temperature));
            tempLabel.setTranslateX(365);
            tempLabel.setTranslateY(280 - i*20);

            key.add(tempRectangle);
            key.add(tempLabel);
        }
    }

    /**
     * Met à jour la liste de quadrilatères représentant les anomalies de température pour une année donnée.
     * @param year L'année à afficher.
     */
    public void updateQuadrilaterals(int year) {
        Float[] temperatures = resourceManager.getAnomalies(year);

        for (int i = 0; i < temperatures.length; i++) {
            quadrilaterals.get(i).setMaterial(new PhongMaterial(Conversions.temperatureToColor(temperatures[i], minTemp, maxTemp)));
        }
    }

    /**
     * Met à jour la liste d'histogrammes représentant les anomalies de température pour une année donnée.
     * @param year L'année à afficher.
     */
    public void updateHistograms(int year) {
        Float[] temperatures = resourceManager.getAnomalies(year);

        for (int i = 0; i < temperatures.length; i++) {
            Box histogram = histograms.get(i);
            float temperature = temperatures[i];

            histogram.setMaterial(new PhongMaterial(Conversions.temperatureToColor(temperature, minTemp, maxTemp)));
            histogram.setHeight(temperature > 0 ? (float)Math.round(60 * temperature/maxTemp) / 100 : 0.01);
        }
    }

    /**
     * Affiche les quadrilatères.
     */
    public void enableQuadrilaterals() {
        earth.getChildren().addAll(quadrilaterals);
    }

    /**
     * Arrête l'affichage des quadrilatères.
     */
    public void disableQuadrilaterals() {
        earth.getChildren().removeAll(quadrilaterals);
    }

    /**
     * Affiche les histogrammes.
     */
    public void enableHistograms() {
        earth.getChildren().addAll(histograms);
    }

    /**
     * Arrête l'affichage des histogrammes.
     */
    public void disableHistograms() {
        earth.getChildren().removeAll(histograms);
    }

    /**
     * Affiche la légende.
     */
    public void enableKey() {
        earthCanvas.getChildren().addAll(key);
    }

    /**
     * Arrête l'affichage de la légende.
     */
    public void disableKey() {
        earthCanvas.getChildren().removeAll(key);
    }

    /**
     * Initialise le graphe 2D de l'évolution des températures.
     */
    public void initChart() {
        lineChart.setCreateSymbols(false);
        lineChart.setAnimated(false);

        //xAxis.setAnimated(false);
        //yAxis.setAnimated(false);
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(1880);
        xAxis.setUpperBound(2020);
        series = new XYChart.Series<>();
        lineChart.getData().add(series);
    }

    /**
     * Affiche le graphe 2D de l'évolution des températures pour une zone donnée, ou pour l'ensemble du globe.
     * @param coordinates Coordonnées de la zone pour lesquelles les températures sont à afficher,
     *                    ou null pour afficher l'évolution de la moyenne mondiale.
     */
    public void updateChart(Coordinates coordinates) {
        series.getData().clear();
        // Afficher pour une année
        if (coordinates != null && resourceManager.getAreas().contains(coordinates)) {
            lineChart.setTitle(coordinates + " temperature evolution");
            for (Integer year : resourceManager.getYears()) {
                if (!Float.isNaN(resourceManager.getMap(year).get(coordinates)))
                    series.getData().add(new XYChart.Data<>(year, resourceManager.getMap(year).get(coordinates)));
            }
        }
        // Afficher la moyenne mondiale
        else {
            lineChart.setTitle("World temperature evolution");
            for (Integer year : resourceManager.getYears()) {
                TemperatureMap temperatureMap = resourceManager.getMap(year);
                float average = 0;
                int sampleSize = 0;
                for (Coordinates coord : resourceManager.getAreas()) {
                    if (!Float.isNaN(temperatureMap.get(coord))) {
                        average += temperatureMap.get(coord);
                        sampleSize ++;
                    }
                }
                average /= sampleSize;

                series.getData().add(new XYChart.Data<>(year, average));
            }
        }
    }

    /**
     * Active ou désactive l'affichage des températures.
     * Méthode appelée quand l'utilisateur clique sur la checkBox "Show temperatures".
     */
    public void handleShowTempCheckBoxAction() {
        //Activation
        if (showTempCheckBox.isSelected()) {
            controlsVBox.setDisable(false);
            enableKey();
            if (colorsRadioButton.isSelected()) enableQuadrilaterals();
            else enableHistograms();
        }
        //Désactivation
        else {
            controlsVBox.setDisable(true);
            disableQuadrilaterals();
            disableHistograms();
            disableKey();
        }
    }

    /**
     * Passe l'affichage en mode quadrilatères.
     */
    public void handleColorsRadioButtonAction() {
        disableHistograms();
        enableQuadrilaterals();
    }

    /**
     * Passe l'affichage en mode histogrammes.
     */
    public void handleHistogramsRadioButtonAction() {
        disableQuadrilaterals();
        enableHistograms();
    }

    /**
     * Lance ou met en pause l'animation.
     */
    public void handlePlayPauseButtonAction() {
        if (!animated) {
            if (yearSlider.getValue() == 2020) yearSlider.setValue(1880);
            animation = new Timeline(new KeyFrame(Duration.millis((float) 1000/speedSpinner.getValue()), event -> {
                if (yearSlider.getValue() < 2020) yearSlider.increment();
                else stopButton.fire();
            }));
            animation.setCycleCount(Timeline.INDEFINITE);
            animation.play();
            playPauseImageView.setImage(new Image("climatechange/application/icons/pause.png"));
            animated = true;
        }
        else {
            animation.stop();
            playPauseImageView.setImage(new Image("climatechange/application/icons/play.png"));
            animated = false;
        }
    }

    /**
     * Arrête l'animation.
     */
    public void handleStopButtonAction() {
        if (animated) {
            playPauseImageView.setImage(new Image("climatechange/application/icons/play.png"));
            animation.stop();
            animated = false;
        }
        yearSlider.setValue(2020);
    }

}
