package climatechange.application.rendering;

import climatechange.data.Coordinates;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;

public abstract class Conversions {
    private static final float TEXTURE_LAT_OFFSET = -0.2f;
    private static final float TEXTURE_LON_OFFSET = 2.8f;

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

    public static Coordinates coord3dTogeoCoord(Point3D position) {
        int lat = 90 - (int)(TEXTURE_LAT_OFFSET + Math.toDegrees(Math.acos(-position.getY())));
        int lon = -(int) (TEXTURE_LON_OFFSET + Math.toDegrees(Math.atan2(position.getX(), position.getZ())));//Math.toDegrees(Math.atan(position.getX() / position.getZ()));
        return new Coordinates(lat, lon);
    }

    public static Color temperatureToColor(float temperature, float minTemp, float maxTemp) {
        if (temperature > 0) return new Color(1, 1 - temperature/maxTemp, 0, 0.5);
        else                 return new Color(1 - temperature/minTemp, 1 - temperature/minTemp, 1, 0.5);
    }
}
