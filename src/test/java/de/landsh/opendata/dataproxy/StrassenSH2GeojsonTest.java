package de.landsh.opendata.dataproxy;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class StrassenSH2GeojsonTest {

    StrassenSH strassenSH2Geojson = new StrassenSH();

    @Test
    public void convertFeature() {
        String json = "{\"properties\":{},\"type\":\"LineString\",\"coordinates\":[[11.1348821,54.42609306],[11.12938639,54.42167313],[11.12450702,54.41677863],[11.12069621,54.4118541],[11.11344491,54.40078343]]}";

        JSONObject in = new JSONObject(json);
        JSONObject result = strassenSH2Geojson.convertFeature(in);

        assertTrue(result.has("properties"));
        JSONObject geometry = result.getJSONObject("geometry");
                assertNotNull(geometry);
        assertEquals("LineString", geometry.getString("type"));
        JSONArray coordinates = geometry.getJSONArray("coordinates");
        assertNotNull(coordinates);
        assertEquals(5, coordinates.length());
    }
}
