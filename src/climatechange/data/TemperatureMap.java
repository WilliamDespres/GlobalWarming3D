package climatechange.data;

import climatechange.data.Coordinates;

import java.util.Collections;
import java.util.HashMap;

public class TemperatureMap extends HashMap<Coordinates, Float> {
    public TemperatureMap() {
        super();
    }

    public float getMinAnomaly() {
        return Collections.min(this.values());
    }
}
