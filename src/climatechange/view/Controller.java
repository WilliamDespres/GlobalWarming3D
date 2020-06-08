package climatechange.view;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private Pane earthPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initEarthPane();


    }

    public void initEarthPane() {
        //Import earth
        ObjModelImporter objModelImporter = new ObjModelImporter();
        try {
            URL modelUrl = this.getClass().getResource("earth/earth.obj");
            objModelImporter.read(modelUrl);
        } catch (ImportException e) {
            e.printStackTrace();
        }
        MeshView[] meshViews = objModelImporter.getImport();
        Group earth = new Group(meshViews);

        //Add camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        new CameraManager(camera, earthPane, earth);

        //Create subscene
        SubScene subScene = new SubScene(earth, 380, 380, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(Color.web("#8eff8a"));

        earthPane.getChildren().addAll(subScene);
    }
}
