package climatechange.application.rendering;

import climatechange.data.Coordinates;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;

public abstract class Conversions {
    private static final float TEXTURE_LAT_OFFSET = -0.2f;
    private static final float TEXTURE_LON_OFFSET = 2.8f;

    /**
     * Renvoie les coordonnées 3D correspondant à des coordonnées géographiques pour une sphère de rayon 1.
     * @param lat Latitude.
     * @param lon Longitude.
     * @return Les coordonnées 3D correspondantes.
     */
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

    /**
     * Renvoie les coordonnées géographiques correspondant à des coordonnées 3D pour une sphère de rayon 1.
     * @param position Les coordonnées 3D.
     * @return Les coordonnées géographiques correspondantes.
     */
    public static Coordinates coord3dTogeoCoord(Point3D position) {
        int lat = 90 - (int)(TEXTURE_LAT_OFFSET + Math.toDegrees(Math.acos(-position.getY())));
        int lon = -(int) (TEXTURE_LON_OFFSET + Math.toDegrees(Math.atan2(position.getX(), position.getZ())));

        //Arrondir lat et lon pour obtenir une coordonnée présente dans le fichier CSV
        if (lat % 4 != 0) {
            if ((lat + 1) % 4 == 0)      lat = lat + 1;
            else if ((lat - 1) % 4 == 0) lat = lat - 1;
            else                         lat = lat + 2;
        }
        if (lat < -88) lat = -88;
        if (lat > 88)  lat = 88;
        if ((lon + 2) % 4 != 0) {
            if ((lon + 3) % 4 == 0)      lon = lon + 1;
            else if ((lon + 1) % 4 == 0) lon = lon - 1;
            else                         lon = lon + 2;
        }
        if (lon < -178) lon = -178;
        if (lon > 178)  lon = 178;

        return new Coordinates(lat, lon);
    }

    /**
     * Renvoie une couleur illustrant une température, sur une échelle du bleu au rouge.
     * @param temperature La température à transformer en couleur.
     * @param minTemp Le minimum de température (= bleu pur).
     * @param maxTemp Le maximum de température (= rouge pur).
     * @return La couleur correspondante.
     */
    public static Color temperatureToColor(float temperature, float minTemp, float maxTemp) {
        if (temperature > 0) return new Color(1, 1 - temperature/maxTemp, 0, 0.5);
        else                 return new Color(1 - temperature/minTemp, 1 - temperature/minTemp, 1, 0.5);
    }
}
