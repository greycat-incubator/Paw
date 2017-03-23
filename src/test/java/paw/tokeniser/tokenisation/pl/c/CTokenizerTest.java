package paw.tokeniser.tokenisation.pl.c;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class CTokenizerTest {

    private final static String c_prog = "" +
            "#include <stdio.h>\n" +
            "int main()\n" +
            "{\n" +
            "   // printf() displays the string inside quotation\n" +
            "   printf(\"Hello, World!\");\n" +
            "   return 0;\n" +
            "}";
    private final static String not_c_prog = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
    private final static String h_file = "#ifndef FOO_H_   /* Include guard */\n" +
            "#define FOO_H_\n" +
            "\n" +
            "int foo(int x);  /* An example function declaration */\n" +
            "\n" +
            "#endif // FOO_H_";
    private final static String empty = "";
    private CTokenizer tokenizer;

    @BeforeEach
    void buildTokenizer(){
        this.tokenizer = new CTokenizer();
    }

    @Test
    void cProg(){
        String[] result = tokenizer.tokenize(c_prog);
        System.out.println(Arrays.toString(result));
    }



    @Test
    void notCProg(){
        String[] result = tokenizer.tokenize(not_c_prog);
        System.out.println(Arrays.toString(result));
    }

    @Test
    void hFile(){
        String[] result = tokenizer.tokenize(h_file);
        System.out.println(Arrays.toString(result));
    }

    @Test
    void empty(){
        String[] result = tokenizer.tokenize(empty);
        System.out.println(Arrays.toString(result));
    }

}