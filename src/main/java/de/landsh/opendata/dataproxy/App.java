package de.landsh.opendata.dataproxy;

import fi.iki.elonen.router.RouterNanoHTTPD;
import fi.iki.elonen.util.ServerRunner;

public class App extends RouterNanoHTTPD {
    public static final int PORT = 8081;

    @Override
    public void addMappings() {
        super.addMappings();
        addRoute("/strassen-sh.geojson", StrassenSH2Geojson.class);
        addRoute("/corona-rd-eck.json", CoronaRdEck.class);

    }

    public App() {
        super(PORT);
        addMappings();
        System.out.println("\nRunning! Point your browser to http://localhost:" + PORT + "/ \n");
    }

    public static void main(String[] args) {
        ServerRunner.run(App.class);
    }
}
