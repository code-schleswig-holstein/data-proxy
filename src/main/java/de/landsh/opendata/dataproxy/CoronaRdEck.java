package de.landsh.opendata.dataproxy;

import de.landsh.opendata.coronardeck.CoronaDataLexer;
import de.landsh.opendata.coronardeck.CoronaDataParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;

public class CoronaRdEck extends CachingDataResponser {

    private final String originalURL;

    public CoronaRdEck() {
        originalURL = "https://covid19dashboardrdeck.aco/daten/daten.js";
    }

    CoronaRdEck(String url) {
        originalURL = url;
    }

    @Override
    int getUpdateInterval() {
        return 600000; // 10 minutes
    }

    @Override
    String getMimeType() {
        return "application/json";
    }

    String loadData() throws IOException {
        final InputStream inputStream = new URL(originalURL).openStream();
        final ANTLRInputStream input = new ANTLRInputStream(inputStream);
        final CoronaDataLexer lexer = new CoronaDataLexer(input);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final CoronaDataParser parser = new CoronaDataParser(tokens);
        final ParseTree tree = parser.data();
        final ParseTreeWalker walker = new ParseTreeWalker();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream out = new PrintStream(baos);

        walker.walk(new CoronaRdEckWalker(out), tree);
        out.close();
        inputStream.close();

        return baos.toString();
    }


}
