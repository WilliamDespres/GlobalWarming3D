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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

public class GlobalWarming3D implements Initializable {

    @FXML private Pane earthCanvas;
    @FXML private VBox controlsVBox;
    @FXML private CheckBox showTempCheckBox;
    @FXML private TextField yearTextField;
    @FXML private Slider yearSlider;
    @FXML private RadioButton colorsRadioButton;
    @FXML private RadioButton histogramsRadioButton;
    @FXML private ChoiceBox<Integer> speedChoiceBox;
    @FXML private Button playPauseButton;
    @FXML private Button stopButton;
    @FXML private ImageView playPauseImage;

    public ResourceManager resourceManager = new ResourceManager();

    Group earth;
    List<MeshView> quadrilaterals = new ArrayList<>();
    List<Cylinder> histograms = new ArrayList<>();
    List<Node> key = new ArrayList<>();

    private float minTemp, maxTemp;

    boolean animated = false;
    AnimationTimer animationTimer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Charger les données
        try {
            resourceManager.readTemperatureFile(resourceManager.getClass().getResource("tempanomaly_4x4grid.csv").toURI().getPath());
        } catch(Exception e) {
            e.printStackTrace();
        }

        minTemp = resourceManager.getMinAnomaly();
        maxTemp = resourceManager.getMaxAnomaly();

        ToggleGroup radioButtonsGroup = new ToggleGroup();
        colorsRadioButton.setToggleGroup(radioButtonsGroup);
        histogramsRadioButton.setToggleGroup(radioButtonsGroup);

        speedChoiceBox.getItems().addAll(1, 2, 4, 8, 16, 32);
        speedChoiceBox.setValue(8);

        initListeners();
        initEarthCanvas();

        yearTextField.setText("2020");
    }

    /**
     * Initialise le canevas 3D représentant la Terre et ses anomalies de température (si option activée).
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
        earthCanvas.getChildren().addAll(subScene);

        // Initialisation des températures (activé par défaut)
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
     * Initialise tous les listeners des composants JavaFX de l'interface 2D.
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
        Object[] areas = resourceManager.getAreas().toArray();

        for (int i = 0; i < temperatures.length; i++) {
            Cylinder histogram = histograms.get(i);
            float temperature = temperatures[i];

            histogram.setMaterial(new PhongMaterial(Conversions.temperatureToColor(temperature, minTemp, maxTemp)));
            histogram.setHeight(temperature > 0 ? 0.6 * temperature/maxTemp : 0.01);
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
            yearSlider.setValue(1880);
            final long startNanoTime = System.nanoTime();
            animationTimer = new AnimationTimer() {
                @Override
                public void handle(long currentNanoTime) {
                    if ((currentNanoTime - startNanoTime) % (1000000000 / speedChoiceBox.getValue()) == 0) {//TODO marche pas
                        yearSlider.increment();
                    }
                    if (yearSlider.getValue() == 2020)
                        stopButton.fire();
                }
            };
            animationTimer.start();
            /*try {
                playPauseImage.setImage(new Image(getClass().getResource("\\icons\\pause.png").toURI().getPath()));
            } catch (Exception ignored) {ignored.printStackTrace();}
            */animated = true;
        }
        else {
            animationTimer.stop();
            /*try {
                playPauseImage.setImage(new Image(getClass().getResource("\\icons\\play.png").toURI().getPath()));
            } catch (Exception ignored) {ignored.printStackTrace();}
            */animated = false;
        }
    }

    /**
     * Arrête l'animation.
     */
    public void handleStopButtonAction() {
        if (animated) {
            /*try {
                playPauseImage.setImage(new Image(getClass().getResource("\\icons\\play.png").toURI().getPath()));
            } catch (Exception ignored) {ignored.printStackTrace();}
            */animationTimer.stop();
            yearSlider.setValue(2020);
            animated = false;
        }
    }

}
