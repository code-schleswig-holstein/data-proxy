package de.landsh.opendata.dataproxy;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import java.io.IOException;

public abstract class CachingDataConverter extends RouterNanoHTTPD.DefaultHandler {


    /**
     * Return the update interval in milliseconds;
     */
    abstract int getUpdateInterval();

    abstract String loadData() throws IOException;


}
