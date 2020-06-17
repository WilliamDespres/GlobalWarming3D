package climatechange.application.rendering;

import javafx.geometry.Point3D;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public abstract class Histograms {
    /**
     * Crée un histogramme de taille et couleur proportionnelles à la température, centré sur une position géographique donnée.
     * @param latitude La latitude de la position géographique.
     * @param longitude La longitude de la position géographique.
     * @param temperature La température de la position géographique.
     * @param maxTemp L'anomalie de température la plus haute.
     * @param minTemp L'anomalie de température la plus basse.
     * @return L'histogramme sous forme de cylindre.
     */
    public static Box makeHistogram(int latitude, int longitude, float temperature, float maxTemp, float minTemp) {
        Box box = new Box(0.01, 0.6, 0.01);//temperature > 0 ? 0.6 * temperature/maxTemp : 0.01, 0.01);
        Point3D position = Conversions.geoCoordTo3dCoord(latitude, longitude);

        //Place le cylindre au bon endroit
        Translate translate = new Translate(position.getX(), position.getY(), position.getZ());

        // Oriente le cylindre selon l'axe origine-position
        double angle = position.angle(0,-1,0);
        Point3D axis = new Point3D(-position.getZ(), 0, position.getX());
        Rotate rotate = new Rotate(angle, axis);

        // Ajout des transformations
        box.getTransforms().addAll(translate, rotate);

        // Couleur du cylindre
        box.setMaterial(new PhongMaterial(Conversions.temperatureToColor(temperature, minTemp, maxTemp)));

        return box;
    }
}
