package de.landsh.opendata.dataproxy;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

public class StrassenSH2Geojson implements RouterNanoHTTPD.UriResponder {
    public static final int UPDATE_INTERVAL = 180000; // 3 minutes
    private static String data = "{}";
    private static long lastUpdate = -1;
    private static NanoHTTPD.Response.Status status = NanoHTTPD.Response.Status.INTERNAL_ERROR;

    String loadData() throws IOException {
        System.out.println("Loading data from strassen-sh.de...");

        final JSONObject featureCollection = new JSONObject();
        featureCollection.put("type", "FeatureCollection");
        processQuery(featureCollection, new URL("https://strassen-sh.de/proxy/geojson/los/2/100").openStream(), "frei");
        processQuery(featureCollection, new URL("https://strassen-sh.de/proxy/geojson/los/6/100").openStream(), "gestaut");
        processQuery(featureCollection, new URL("https://strassen-sh.de/proxy/geojson/los/4/100").openStream(), "dicht");
        processQuery(featureCollection, new URL("https://strassen-sh.de/proxy/geojson/los/5/100").openStream(), "zähfließend");
        processQuery(featureCollection, new URL("https://strassen-sh.de/proxy/geojson/los/9/100").openStream(), "Datenausfall");

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

    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        if (System.currentTimeMillis() - UPDATE_INTERVAL > lastUpdate) {
            System.out.println("Loading data for " + getClass().getName());

            try {
                data = loadData();
                status = NanoHTTPD.Response.Status.OK;
            } catch (IOException e) {
                data = "{}";
                status = NanoHTTPD.Response.Status.INTERNAL_ERROR;
            }
            lastUpdate = System.currentTimeMillis();
        }
        return NanoHTTPD.newFixedLengthResponse(status, "application/geo+json", data);
    }


    @Override
    public NanoHTTPD.Response put(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession) {
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED, "text/plain", "method not allowed");
    }

    @Override
    public NanoHTTPD.Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession) {
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED, "text/plain", "method not allowed");
    }

    @Override
    public NanoHTTPD.Response delete(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession) {
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED, "text/plain", "method not allowed");
    }

    @Override
    public NanoHTTPD.Response other(String s, RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession) {
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED, "text/plain", "method not allowed");
    }


}
