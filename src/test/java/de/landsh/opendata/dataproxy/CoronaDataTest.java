package de.landsh.opendata.dataproxy;

import de.landsh.opendata.coronardeck.CoronaDataLexer;
import de.landsh.opendata.coronardeck.CoronaDataParser;
import org.antlr.v4.runtime.*;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Unit test for simple CoronaData.
 */
public class CoronaDataTest {

    @Test
    public void invalidData() {
        final CharStream input = CharStreams.fromString("var data = {\n" +
                "'010580005005: { amount_pt: 2.1037868162693, amount_t: 206, amount_i: 21, amount_d: 3, amount_h: 182 },\n}");

        CoronaDataLexer lexer = new CoronaDataLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CoronaDataParser parser = new CoronaDataParser(tokens);

        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new RuntimeException(msg);
            }
        });

        try {
            parser.data();
            fail();
        } catch (RuntimeException ignore) {
            // ok
        }
    }

    @Test
    public void oneEntry() {
        final CharStream input = CharStreams.fromString("var data = {\n" +
                "'010580005005': { amount_pt: 2.1037868162693, amount_t: 206, amount_i: 21, amount_d: 3, amount_h: 182 },\n}");

        CoronaDataLexer lexer = new CoronaDataLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CoronaDataParser parser = new CoronaDataParser(tokens);

        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new RuntimeException(msg);
            }
        });

        parser.data();


    }

    @Test
    public void test() throws IOException {
        final InputStream inputStream = getClass().getResourceAsStream("/2021-11-12_100007.json");
        assertNotNull(inputStream);
        final CharStream input = CharStreams.fromStream(inputStream);

        CoronaDataLexer lexer = new CoronaDataLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        CoronaDataParser parser = new CoronaDataParser(tokens);

        parser.data();

    }
}
