package climatechange.application.rendering;

import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

public abstract class Quadrilaterals {
    /**
     * Crée un quadrilatère (carré) centré sur des coordonnées géographiques avec une couleur et une taille données.
     * @param latitude Latitude du centre du quadrilatère.
     * @param longitude Longitude du centre du quadrilatère.
     * @param color La couleur du quadrilatère.
     * @param size La taille du quadrilatère.
     * @return Le quadrilatère sous forme de MeshView.
     */
    public static MeshView makeCenteredQuadrilateral(float latitude, float longitude, Color color, float size) {
        return makeQuadrilateral(Conversions.geoCoordTo3dCoord(latitude + size/2, longitude + size/2).multiply(1.01),
                                 Conversions.geoCoordTo3dCoord(latitude - size/2, longitude + size/2).multiply(1.01),
                                 Conversions.geoCoordTo3dCoord(latitude - size/2, longitude - size/2).multiply(1.01),
                                 Conversions.geoCoordTo3dCoord(latitude + size/2, longitude - size/2).multiply(1.01), color);
    }

    /**
     * Crée un quadrilatère ayant une position et une couleur données.
     * @param topRight Le coin en haut à droite du quadrilatère.
     * @param bottomRight Le coin en bas à droite du quadrilatère.
     * @param bottomLeft Le coin en bas à gauche du quadrilatère.
     * @param topLeft Le coin en haut à gauche du quadrilatère.
     * @param color La couleur du quadrilatère.
     * @return Le quadrilatère sous forme de MeshView.
     */
    public static MeshView makeQuadrilateral(Point3D topRight, Point3D bottomRight, Point3D bottomLeft, Point3D topLeft, Color color) {
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
}
