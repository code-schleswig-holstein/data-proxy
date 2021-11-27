package de.landsh.opendata.dataproxy;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class StrassenSH extends CachingDataResponser {

    private final String baseURL;

    public StrassenSH() {
        baseURL = "https://strassen-sh.de/proxy/geojson/los/";
    }

    StrassenSH(String baseURL) {
        this.baseURL = baseURL;
    }

    @Override
    int getUpdateInterval() {
        return 180000; // 3 minutes
    }

    @Override
    String getMimeType() {
        return "application/geo+json";
    }

    String loadData() throws IOException {
        System.out.println("Loading data from strassen-sh.de...");

        final JSONObject featureCollection = new JSONObject();
        featureCollection.put("type", "FeatureCollection");
        processQuery(featureCollection, new URL(baseURL + "2/100").openStream(), "frei");
        processQuery(featureCollection, new URL(baseURL + "6/100").openStream(), "gestaut");
        processQuery(featureCollection, new URL(baseURL + "4/100").openStream(), "dicht");
        processQuery(featureCollection, new URL(baseURL + "5/100").openStream(), "zähfließend");
        processQuery(featureCollection, new URL(baseURL + "9/100").openStream(), "Datenausfall");

        return featureCollection.toString();
    }

    private void processQuery(JSONObject featureCollection, InputStream in, String status) {
        if (!featureCollection.has("features")) {
            featureCollection.put("features", new JSONArray());
        }

        final JSONObject json = new JSONObject(new JSONTokener(in));
        final JSONArray featuresOut = featureCollection.getJSONArray("features");
        final JSONArray featuresIn = json.getJSONArray("features");
        for (int i = 0; i < featuresIn.length(); i++) {
            final JSONObject featureIn = featuresIn.getJSONObject(i);
            final JSONObject featureOut = convertFeature(featureIn);
            featureOut.getJSONObject("properties").put("status", status);
            featuresOut.put(featureOut);
        }

        if (json.has("properties")) {
            featureCollection.put("properties", json.getJSONObject("properties"));
        }
    }

    JSONObject convertFeature(JSONObject feature) {
        final JSONObject result = new JSONObject();
        result.put("type", "Feature");

        final JSONObject geometry = new JSONObject();
        final JSONObject properties = feature.getJSONObject("properties");
        final String type = feature.getString("type");
        final JSONArray coordinates = feature.getJSONArray("coordinates");

        result.put("properties", properties);
        // result.put("id", coordinates.hashCode());
        geometry.put("type", type);
        geometry.put("coordinates", coordinates);
        result.put("geometry", geometry);

        return result;
    }


}
