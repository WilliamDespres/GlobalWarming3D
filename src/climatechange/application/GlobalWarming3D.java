package climatechange.application;

import climatechange.data.Coordinates;
import climatechange.data.ResourceManager;
import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.TriangleMesh;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;

public class GlobalWarming3D implements Initializable {
    private static final float TEXTURE_LAT_OFFSET = -0.2f;
    private static final float TEXTURE_LON_OFFSET = 2.8f;

    @FXML private Pane earthCanvas;
    @FXML private VBox controlsVBox;
    @FXML private CheckBox showTempCheckBox;

    private int year = 2020;
    private float min, max;

    ResourceManager resourceManager = new ResourceManager();
    Group earth;
    ArrayList<MeshView> quadrilaterals = new ArrayList<>();
    ArrayList<Node> key = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            resourceManager.readTemperatureFile(resourceManager.getClass().getResource("tempanomaly_4x4grid.csv").toURI().getPath());
        } catch(Exception e) {
            e.printStackTrace();
        }

        min = resourceManager.getMinAnomaly();
        max = resourceManager.getMaxAnomaly();

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

        initKey();
        enableQuadrilaterals();
    }

    public void enableQuadrilaterals() {
        if (quadrilaterals.isEmpty()) {
            for (Map.Entry<Coordinates, Float> entry : resourceManager.getMap(year).entrySet()) {
                int latitude = entry.getKey().getLatitude();
                int longitude = entry.getKey().getLongitude();
                float temperature = entry.getValue();

                quadrilaterals.add(makeCenteredQuadrilateral(earth, latitude, longitude, getColorFromTemperature(temperature), 4));
            }
        }
        earth.getChildren().addAll(quadrilaterals);
        enableKey();
    }

    public Color getColorFromTemperature(float temperature) {
        if (temperature > 0) return new Color(1, 1-temperature/max, 0, 0.5);
        else                 return new Color(1-temperature/min, 1-temperature/min, 1, 0.5);
    }

    public void initKey() {
        for (float i = 0; i < 10; i++) {
            float temperature = Math.max(Math.min(Math.round(min + i/10 * (max - min)), max), min);

            Rectangle tempRectangle = new Rectangle(320, 280 - i*20, 20, 20);
            tempRectangle.setFill(getColorFromTemperature(temperature));

            Label tempLabel = new Label(new DecimalFormat("#").format(temperature));
            tempLabel.setTranslateX(350);
            tempLabel.setTranslateY(280 - i*20);

            key.add(tempRectangle);
            key.add(tempLabel);
        }
    }

    public void enableKey() {
        earthCanvas.getChildren().addAll(key);
    }

    public void disableKey() {
        earthCanvas.getChildren().removeAll(key);
    }

    public void disableQuadrilaterals() {
        earth.getChildren().removeAll(quadrilaterals);
        disableKey();
        //quadrilaterals.clear();
    }

    public void changeYear(int newYear) {
        // TODO changer année + vider quadrilatères + re-remplir
    }

    public void handleShowTempAction() {
        if (showTempCheckBox.isSelected()) {
            controlsVBox.setDisable(false);
            enableQuadrilaterals();
        }
        else {
            controlsVBox.setDisable(true);
            disableQuadrilaterals();
        }
    }

    private MeshView makeCenteredQuadrilateral(Group parent, float latitude, float longitude, Color color, float size) {
        return makeQuadrilateral(parent, geoCoordTo3dCoord(latitude + size/2, longitude + size/2).multiply(1.01),
                                         geoCoordTo3dCoord(latitude - size/2, longitude + size/2).multiply(1.01),
                                         geoCoordTo3dCoord(latitude - size/2, longitude - size/2).multiply(1.01),
                                         geoCoordTo3dCoord(latitude + size/2, longitude - size/2).multiply(1.01), color);
    }

    private MeshView makeQuadrilateral(Group parent, Point3D topRight, Point3D bottomRight, Point3D bottomLeft, Point3D topLeft, Color color) {
        final TriangleMesh triangleMesh = new TriangleMesh();

        final float[] points = {
                (float)topRight.getX(), (float)topRight.getY(), (float)topRight.getZ(),
                (float)topLeft.getX(), (float)topLeft.getY(), (float)topLeft.getZ(),
                (float)bottomLeft.getX(), (float)bottomLeft.getY(), (float)bottomLeft.getZ(),
                (float)bottomRight.getX(), (float)bottomRight.getY(), (float)bottomRight.getZ()
        };

        final float[] texCoords = {
                1,1,
                1,0,
                0,1,
                0,0
        };

        final int[] faces = {
                0,1,1,0,2,2,
                0,1,2,2,3,3
        };

        triangleMesh.getPoints().setAll(points);
        triangleMesh.getTexCoords().setAll(texCoords);
        triangleMesh.getFaces().setAll(faces);

        final MeshView meshView = new MeshView(triangleMesh);
        meshView.setMaterial(new PhongMaterial(color));
        return meshView;
    }

    public static Point3D geoCoordTo3dCoord(float lat, float lon) {
        float lat_cor = lat + TEXTURE_LAT_OFFSET;
        float lon_cor = lon + TEXTURE_LON_OFFSET;
        return new Point3D(
                -java.lang.Math.sin(java.lang.Math.toRadians(lon_cor))
                        * java.lang.Math.cos(java.lang.Math.toRadians(lat_cor)),
                -java.lang.Math.sin(java.lang.Math.toRadians(lat_cor)),
                java.lang.Math.cos(java.lang.Math.toRadians(lon_cor))
                        * java.lang.Math.cos(java.lang.Math.toRadians(lat_cor)));
    }
}
