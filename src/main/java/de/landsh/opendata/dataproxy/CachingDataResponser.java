package de.landsh.opendata.dataproxy;

import fi.iki.elonen.NanoHTTPD;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public abstract class CachingDataResponser {

    private String data = "{}";
    private long lastUpdate = -1;
    private NanoHTTPD.Response.Status status = NanoHTTPD.Response.Status.INTERNAL_ERROR;

    /**
     * Returns the update interval in milliseconds.
     */
    abstract int getUpdateInterval();

    /**
     * Returns the MIME type for a successful response.
     */
    abstract String getMimeType();

    /**
     * Load data from the original source.
     */
    abstract String loadData() throws IOException;

    private void loadDataIfNecessary() {
        if (System.currentTimeMillis() - getUpdateInterval() > lastUpdate) {
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
    }

    /**
     * Handle GET requests.
     */
    public NanoHTTPD.Response get() {
        loadDataIfNecessary();

        NanoHTTPD.Response response = NanoHTTPD.newFixedLengthResponse(status, getMimeType(), data);
        response.addHeader("Access-Control-Allow-Origin", "*");
        return response;
    }

    /**
     * Handle HEAD requests.
     */
    public NanoHTTPD.Response head() {
        loadDataIfNecessary();
        NanoHTTPD.Response response = new HeadResponse(status, getMimeType(), data.length());
        response.addHeader("Access-Control-Allow-Origin", "*");
        return response;
    }

    static class HeadResponse extends NanoHTTPD.Response {

        protected HeadResponse(IStatus status, String mimeType, long totalBytes) {
            super(status, mimeType, new ByteArrayInputStream(new byte[0]), totalBytes);
        }
    }

}
