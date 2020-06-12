package climatechange.application;

import climatechange.application.rendering.Conversions;
import climatechange.application.rendering.Quadrilaterals;
import climatechange.data.Coordinates;
import climatechange.data.ResourceManager;
import climatechange.data.TemperatureMap;
import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
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

    public ResourceManager resourceManager = new ResourceManager();

    Group earth;
    List<MeshView> quadrilaterals = new ArrayList<>();
    List<Node> key = new ArrayList<>();

    private float minTemp, maxTemp;

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

        yearTextField.setText("2020");
        yearSlider.setValue(100);

        initListeners();

        initEarthCanvas();
    }

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
        subScene.setFill(Color.DARKGRAY/*Color.web("#8eff8a")*/);

        earthCanvas.getChildren().addAll(subScene);

        initQuadrilaterals();
        initKey();
        enableQuadrilaterals();
        enableKey();
    }

    public void initListeners() {
        // On n'autorise que les entiers pour le textField
        yearTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.matches("\\d+") && resourceManager.getYears().contains(Integer.parseInt(newValue))) {
                // Mise à jour de la carte
                updateQuadrilaterals(Integer.parseInt(yearTextField.getText()));
            }
            else yearTextField.setText(newValue.replaceAll("[^\\d]", ""));
        });
    }

    public void initQuadrilaterals() {
        TemperatureMap temperatureMap = resourceManager.getMap(2020);

        for (Map.Entry<Coordinates, Float> entry : temperatureMap.entrySet()) {
            int latitude = entry.getKey().getLatitude();
            int longitude = entry.getKey().getLongitude();
            float temperature = entry.getValue();

            quadrilaterals.add(Quadrilaterals.makeCenteredQuadrilateral(earth, latitude, longitude, Conversions.getColorFromTemperature(temperature, minTemp, maxTemp), 4));
        }
    }

    public void updateQuadrilaterals(int year) {
        TemperatureMap temperatureMap = resourceManager.getMap(year);

        for (int i = 0; i < temperatureMap.values().size(); i++) {
            quadrilaterals.get(i).setMaterial(new PhongMaterial(Conversions.getColorFromTemperature((float)temperatureMap.values().toArray()[i], minTemp, maxTemp)));
        }
    }

    public void initKey() {
        for (float i = 0; i < 10; i++) {
            float temperature = Math.max(Math.min(Math.round(minTemp + i/10 * (maxTemp - minTemp)), maxTemp), minTemp);

            Rectangle tempRectangle = new Rectangle(340, 280 - i*20, 20, 20);
            tempRectangle.setFill(Conversions.getColorFromTemperature(temperature, minTemp, maxTemp));

            Label tempLabel = new Label(new DecimalFormat("#").format(temperature));
            tempLabel.setTranslateX(365);
            tempLabel.setTranslateY(280 - i*20);

            key.add(tempRectangle);
            key.add(tempLabel);
        }
    }

    public void enableQuadrilaterals() {
        earth.getChildren().addAll(quadrilaterals);
    }

    public void disableQuadrilaterals() {
        earth.getChildren().removeAll(quadrilaterals);
    }

    public void enableKey() {
        earthCanvas.getChildren().addAll(key);
    }

    public void disableKey() {
        earthCanvas.getChildren().removeAll(key);
    }

    public void changeYear(int newYear) {
        // TODO changer année + vider quadrilatères + re-remplir
    }

    public void handleShowTempAction() {
        if (showTempCheckBox.isSelected()) {
            controlsVBox.setDisable(false);
            enableQuadrilaterals();
            enableKey();
        }
        else {
            controlsVBox.setDisable(true);
            disableQuadrilaterals();
            disableKey();
        }
    }



}
