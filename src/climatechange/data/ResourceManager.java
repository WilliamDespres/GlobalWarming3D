package climatechange.data;

import climatechange.data.Coordinates;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class ResourceManager {

    private HashMap<Integer, TemperatureMap> allMaps;
    protected int sampleNumber = 0;

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

            sampleNumber = allYears.size();
            System.out.println(sampleNumber);

            System.out.println("[End of file " + path + "]");
        } catch (IOException e) {
            System.out.println("[Could not read file " + path + "]");
            e.printStackTrace();
        }

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

    public TemperatureMap getMap(Integer year) {
        return allMaps.get(year);
    }
}
