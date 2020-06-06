package climatechange.data;

import climatechange.data.Coordinates;

import java.util.Collections;
import java.util.HashMap;

/**
 * Classe représentant une carte des anomalies de températures pour une année,
 * associée à leurs coordonnées géographiques.
 */
public class TemperatureMap extends HashMap<Coordinates, Float> {
    /**
     * Constructeur par défaut de la classe. Crée une HashMap vide.
     */
    public TemperatureMap() {
        super();
    }

    /**
     * Renvoie la valeur minimale des anomalies de température pour cette année.
     * @return La valeur minimale des anomalies.
     */
    public float getMinAnomaly() {
        return Collections.min(this.values());
    }

    /**
     * Renvoie la valeur maximale des anomalies de température pour cette année.
     * @return La valeur maximale des anomalies.
     */
    public float getMaxAnomaly() {
        return Collections.max(this.values());
    }
}
