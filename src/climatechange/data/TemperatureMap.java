package climatechange.data;

import climatechange.data.Coordinates;

import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * Classe représentant une carte des anomalies de températures pour une année,
 * associée à leurs coordonnées géographiques.
 */
public class TemperatureMap extends LinkedHashMap<Coordinates, Float> {
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
    public Float getMinAnomaly() {
        return Collections.min(this.values());
    }

    /**
     * Renvoie la valeur maximale des anomalies de température pour cette année.
     * @return La valeur maximale des anomalies.
     */
    public Float getMaxAnomaly() {
        return Collections.max(this.values().stream().filter(d -> !Double.isNaN(d)).collect(Collectors.toList())); // pour ignorer les valeurs NaN qui sont considérées plus grandes que les autres
    }

    /**
     * Renvoie l'anomalie de température pour une zone donnée, sans fournir d'objet Coordinates  en paramètre.
     * @param latitude La latitude de la zone recherchée.
     * @param longitude La longitude de la zone recherchée.
     * @return L'anomalie de température de la zone recherchée.
     */
    public Float get(int latitude, int longitude) {
        return this.getOrDefault(new Coordinates(latitude, longitude), null);
    }
}
