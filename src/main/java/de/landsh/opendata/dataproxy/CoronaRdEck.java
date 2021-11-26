package de.landsh.opendata.dataproxy;

import de.landsh.opendata.coronardeck.CoronaDataLexer;
import de.landsh.opendata.coronardeck.CoronaDataParser;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Map;

public class CoronaRdEck implements RouterNanoHTTPD.UriResponder {
    public static final int UPDATE_INTERVAL = 600000; // 10 minutes
    private static String data = "{}";
    private static long lastUpdate = -1;
    private static NanoHTTPD.Response.Status status = NanoHTTPD.Response.Status.INTERNAL_ERROR;

    String loadData() throws IOException {
        InputStream inputStream = new URL("https://covid19dashboardrdeck.aco/daten/daten.js").openStream();
        final ANTLRInputStream input = new ANTLRInputStream(inputStream);

        final CoronaDataLexer lexer = new CoronaDataLexer(input);

        final CommonTokenStream tokens = new CommonTokenStream(lexer);

        final CoronaDataParser parser = new CoronaDataParser(tokens);

        final ParseTree tree = parser.data();

        final ParseTreeWalker walker = new ParseTreeWalker();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);

        walker.walk(new CoronaRdEckWalker(out), tree);
        out.close();
        inputStream.close();

        return baos.toString();
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
        return NanoHTTPD.newFixedLengthResponse(status, "application/json", data);
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
