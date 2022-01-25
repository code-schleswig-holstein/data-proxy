package de.landsh.opendata.dataproxy;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;

public class App extends NanoHTTPD {
    public static int port = 8080;

    private final StrassenSH strassenSH = new StrassenSH();
    private final CoronaRdEck coronaRdEck = new CoronaRdEck();

    public App() {
        super(port);
        System.out.println("\nRunning! Point your browser to http://localhost:" + port + "/ \n");
    }

    public static void main(String[] args) throws IOException {

        if (args.length > 0) {
            try {
                App.port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignore) {
                System.err.println("Usage: java -jar data-proxy.jar [PORT]");
                System.exit(1);
            }
        }

        new App().start(5000, false);
    }

    @Override
    public Response serve(IHTTPSession session) {

        final CachingDataResponser responder;
        if ("/strassen-sh.geojson".equals(session.getUri())) {
            responder = strassenSH;
        } else if ("/corona-rd-eck.json".equals(session.getUri())) {
            responder = coronaRdEck;
        } else {
            responder = null;
        }

        if (responder == null) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found");
        } else {
            if (session.getMethod() == Method.GET) {
                return responder.get();
            } else if (session.getMethod() == Method.HEAD) {
                return responder.head();
            } else {
                return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, "text/plain", "Method not allowed");

            }
        }

    }
}
