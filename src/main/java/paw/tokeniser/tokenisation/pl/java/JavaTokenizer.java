package paw.tokeniser.tokenisation.pl.java;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaToken;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import paw.tokeniser.Tokenizer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static com.github.javaparser.ParseStart.COMPILATION_UNIT;
import static com.github.javaparser.Providers.provider;

/**
 * A Java tokenizer
 * relying on javaParser library
 *
 * Not working on anything else than java file!
 */
public class JavaTokenizer extends Tokenizer {
    public final static String ID = "JAVA TOKENIZER";

    private boolean removeComments = false;

    @Override
    public String[] tokenize(Reader reader) throws IOException {

        JavaParser jp = new JavaParser(JavaParser.getStaticConfiguration());
        ParseResult<CompilationUnit> result = jp.parse(COMPILATION_UNIT, provider(reader));
        if (result.isSuccessful()) {
            List<JavaToken> l = result.getTokens().get();
            List<String> tokens = new ArrayList<>(l.size());
            for (int i = 0; i < l.size(); i++) {
                JavaToken jt = l.get(i);

                String s = applyAllTokenPreprocessorTo(jt.getText());
                if (removeComments && (s.contains("/**") || s.contains("//")))
                    s = "";
                if (!s.contains("\n"))
                    s = s.trim();
                if (!s.isEmpty()) {
                    tokens.add(s);
                }
            }
            return tokens.toArray(new String[tokens.size()]);
        }
        return new String[0];
    }

    public boolean isRemoveComments() {
        return removeComments;
    }

    public void setRemoveComments(boolean removeComments) {
        this.removeComments = removeComments;
    }

    @Override
    public String toString() {
        return ID + "\n" + super.toString();
    }
}
