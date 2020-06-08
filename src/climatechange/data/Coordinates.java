package climatechange.data;

import java.util.Objects;

/**
 * Classe représentant des coordonnées géographiques.
 */
public class Coordinates {
    private final int latitude, longitude;

    /**
     * Constructeur de la classe.
     * Celle-ci ne pourra plus être modifiée par la suite.
     * @param latitude La latitude.
     * @param longitude La longitude.
     */
    public Coordinates(int latitude, int longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Méthode d'accès à la latitude.
     * @return La latitude.
     */
    public int getLatitude() {
        return latitude;
    }

    /**
     * Méthode d'accès à la longitude.
     * @return La longitude.
     */
    public int getLongitude() {
        return longitude;
    }

    /**
     * Méthode d'égalité entre deux coordonnées.
     * @param o La coordonnée à comparer.
     * @return Vrai si les 2 latitudes et les 2 longitudes sont égales, faux sinon.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return getLatitude() == that.getLatitude() &&
                getLongitude() == that.getLongitude();
    }

    /**
     * Méthode de hash d'une coordonnée.
     * @return Le hashcode fait à partir de la latitude et de la longitude.
     */
    @Override
    public int hashCode() {
        return Objects.hash(getLatitude(), getLongitude());
    }

    @Override
    public String toString() {
        return "(" + latitude + "°, " + longitude + "°)";
    }
}
