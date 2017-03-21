package paw.tokeniser.tokenisation.pl.java;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaToken;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import paw.tokeniser.Tokenizer;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import static com.github.javaparser.ParseStart.COMPILATION_UNIT;
import static com.github.javaparser.Providers.provider;

public class JavaTokenizer extends Tokenizer {
    public final static String ID = "JAVA TOKENIZER";

    @Override
    public String[] tokenize(Reader reader) throws IOException {

        JavaParser jp = new JavaParser(JavaParser.getStaticConfiguration());
        ParseResult<CompilationUnit> result = jp.parse(COMPILATION_UNIT, provider(reader));
        if (result.isSuccessful()) {
            List<JavaToken> l = result.getTokens().get();
            String[] tokens = new String[l.size()];
            for (int i = 0; i < l.size(); i++) {
                JavaToken jt = l.get(i);
                tokens[i] = applyAllTokenPreprocessorTo(jt.getText());
            }
            return tokens;
        }
        return new String[0];
    }

    @Override
    public String toString() {
        return ID + "\n" + super.toString();
    }
}
