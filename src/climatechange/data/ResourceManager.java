package climatechange.data;

import climatechange.data.Coordinates;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * Classe permettant de charger les données sur les anomalies de températures d'un fichier CSV
 * et d'interagir avec celles-ci.
 */
public class ResourceManager {

    private HashMap<Integer, TemperatureMap> allMaps;

    /**
     * Constructeur de la classe.
     * Initialise la HashMap des cartes de températures.
     */
    public ResourceManager() {
        allMaps = new HashMap<>();
    }

    /**
     * Charge les données depuis un fichier CSV.
     * @param path Le chemin du fichier CSV.
     */
    public void readTemperatureFile(String path) {
        // Liste des années
        ArrayList<Integer> allYears = new ArrayList<>();

        // Ouverture du fichier
        try {
            System.out.println("[Reading file " + path + "...]");

            FileReader file = new FileReader(path);
            BufferedReader bufRead = new BufferedReader(file);

            String line = bufRead.readLine().replaceAll("\"", "");  // Suppression des guillemets

            // Lecture de chaque ligne
            while (line != null) {
                String[] array = line.split(",");

                // Si 1ère ligne : Initialisation (ajout des années)
                if (array[0].equals("lat")) {
                    for (int i = 2; i < array.length; i++) {
                        try {
                            Integer year = Integer.parseInt(array[i]);
                            allYears.add(year);
                            allMaps.put(year, new TemperatureMap());
                        } catch (NumberFormatException nfe) {
                            nfe.printStackTrace();
                        }
                    }
                }
                // Sinon : Parcours de la ligne
                else {
                    // Pour une coordonnée
                    Coordinates coord = new Coordinates(Integer.parseInt(array[0]), Integer.parseInt(array[1]));
                    // Ajout des anomalies à chaque année
                    for (int i = 2; i < array.length; i++) {
                        try {
                            allMaps.get(allYears.get(i - 2)).put(coord, Float.parseFloat(array[i]));
                        } catch (NumberFormatException nfe) {
                            allMaps.get(allYears.get(i - 2)).put(coord, Float.NaN); // Si non renseignée, on assigne la valeur NaN
                        }
                    }
                }
                // Passage ligne suivante
                line = bufRead.readLine();
            }

            bufRead.close();
            file.close();

            System.out.println("[End of file " + path + "]");
        } catch (IOException e) {
            System.out.println("[Could not read file " + path + "]");
            e.printStackTrace();
        }

    }

    /**
     * Méthode d'accès à la carte des anomalies de température d'une année.
     * @param year L'année demandée.
     * @return La carte des anomalies de température de cette année.
     */
    public TemperatureMap getMap(Integer year) {
        return allMaps.getOrDefault(year, null);
    }

    public int getSampleNumber() {
        return allMaps.keySet().size();
    }

    /**
     * Renvoie l'ensemble des années connues.
     * @return Un Set contenant l'ensemble des années connues.
     */
    public Set<Integer> getYears() {
        return allMaps.keySet();
    }

    /**
     * Renvoie l'ensemble des zones connues.
     * @return Un Set contenant l'ensemble des zones connues.
     */
    public Set<Coordinates> getAreas() {
        return (allMaps.containsKey(1880) ? allMaps.get(1880).keySet() : Collections.emptySet());
    }

    /**
     * Renvoie la valeur minimale des anomalies de température contenues dans le fichier CSV.
     * @return La valeur minimale des anomalies.
     */
    public Float getMinAnomaly() {
        float minAnomaly = allMaps.get(1880).getMinAnomaly();
        for (TemperatureMap temperatureMap : allMaps.values()) {
            if (temperatureMap.getMinAnomaly() < minAnomaly)
                minAnomaly = temperatureMap.getMinAnomaly();
        }
        return minAnomaly;
    }

    /**
     * Renvoie la valeur maximale des anomalies de température contenues dans le fichier CSV.
     * @return La valeur maximale des anomalies.
     */
    public Float getMaxAnomaly() {
        float maxAnomaly = allMaps.get(1880).getMaxAnomaly();
        for (TemperatureMap temperatureMap : allMaps.values()) {
            if (temperatureMap.getMaxAnomaly() > maxAnomaly)
                maxAnomaly = temperatureMap.getMaxAnomaly();
        }
        return maxAnomaly;
    }

    /**
     * Renvoie l'anomalie de température pour une zone et une années données.
     * @param latitude La latitude de la zone recherchée.
     * @param longitude La longitude de la zone recherchée.
     * @param year L'année recherchée.
     * @return L'anomalie de température de la zone et de l'année données.
     */
    public Float getAnomaly(Integer year, int latitude, int longitude) {
        return allMaps.containsKey(year) ? allMaps.get(year).get(latitude, longitude) : null;
    }

    /**
     * Renvoie un tableau contenant toutes les anomalies de températures pour une année,
     * dans l'ordre de lecture du fichier CSV.
     * @param year L'année dont les anomalies sont à renvoyer.
     * @return Toutes les anomalies de températures pour cette année.
     */
    public Float[] getAnomalies(Integer year) {
        return allMaps.containsKey(year) ? allMaps.get(year).values().toArray(new Float[0]) : null;
    }

    /**
     * Renvoie un tableau contenant toutes les anomalies de températures pour une zone,
     * dans l'ordre croissant des années.
     * @param latitude La latitude de la zone recherchée.
     * @param longitude La longitude de la zone recherchée.
     * @return Toutes les anomalies de températures pour cette zone.
     */
    public Float[] getAnomalies(int latitude, int longitude) {
        Float[] anomalies = new Float[getSampleNumber()];
        int i = 0;
        for (TemperatureMap temperatureMap : allMaps.values()) {
            anomalies[i++] = temperatureMap.get(latitude, longitude);
        }
        return anomalies;
    }
}
