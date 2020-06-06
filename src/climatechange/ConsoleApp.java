package climatechange;

import climatechange.data.Coordinates;
import climatechange.data.ResourceManager;

import java.net.URISyntaxException;

public class ConsoleApp {
    public static void main(String[] args) {
        ResourceManager rm = new ResourceManager();
        try {
            rm.readTemperatureFile(rm.getClass().getResource("tempanomaly_4x4grid.csv").toURI().getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        System.out.println("");
    }
}
