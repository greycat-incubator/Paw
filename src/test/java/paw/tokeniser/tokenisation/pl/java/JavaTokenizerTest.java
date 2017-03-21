package paw.tokeniser.tokenisation.pl.java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class JavaTokenizerTest {
public final static String hello = "public class HelloWorld {\n" +
        "   public static void main(String[] args) {\n" +
        "      // Prints \"Hello, World\" in the terminal window.\n" +
        "      System.out.println(\"Hello, World\");\n" +
        "   }\n" +
        "}";

    public final static String not_java_prog = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";

    private JavaTokenizer tokenizer;

    @BeforeEach
    public void buildTokenizer() {
        this.tokenizer = new JavaTokenizer();
    }

    @Test
    public void jProg() {
        String[] result = tokenizer.tokenize(hello);
        System.out.println(Arrays.toString(result));
    }

    @Test
    public void notJProg() {
        String[] result = tokenizer.tokenize(not_java_prog);
        System.out.println(Arrays.toString(result));
    }
}