package paw.tokeniser.tokenisation.misc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paw.tokeniser.preprocessing.LowerCasePreprocessor;

import java.io.IOException;
import java.io.StringReader;

class IdentityTokenizerTest {

    private IdentityTokenizer tokenizer;

    @BeforeEach
    void buildTokenizer(){
        this.tokenizer = new IdentityTokenizer();
        tokenizer.addPreprocessor(new LowerCasePreprocessor());
    }

    @Test
    void emptyText(){
        String[] result = tokenizer.tokenize("");
        Assertions.assertEquals(0,result.length);
    }

    @Test
    void oneWordText(){
        String[] result = tokenizer.tokenize("this");
        Assertions.assertEquals(1,result.length);
        Assertions.assertEquals("this",result[0]);
    }


    @Test
    void twoWordText(){
        String[] result = tokenizer.tokenize("this is");
        Assertions.assertEquals(1,result.length);
        Assertions.assertEquals("this is",result[0]);
    }

    @Test
    void twoLinesText(){
        String[] result = tokenizer.tokenize("this is \n me");
        Assertions.assertEquals(1,result.length);
        Assertions.assertEquals("this is \n me",result[0]);
    }

    @Test
    void capitalLetterText(){
        String[] result = tokenizer.tokenize("THIS is");
        Assertions.assertEquals(1,result.length);
        Assertions.assertEquals("this is",result[0]);
    }

    @Test
    void emptyTextR() throws IOException {
        String[] result = tokenizer.tokenize(new StringReader(""));
        Assertions.assertEquals(0, result.length);
    }

    @Test
    void oneWordTextR() throws IOException {
        String[] result = tokenizer.tokenize(new StringReader("this"));
        Assertions.assertEquals(1,result.length);
        Assertions.assertEquals("this",result[0]);
    }


    @Test
    void twoWordTextR() throws IOException {
        String[] result = tokenizer.tokenize(new StringReader("this is"));
        Assertions.assertEquals(1,result.length);
        Assertions.assertEquals("this is",result[0]);
    }

    @Test
    void twoLinesTextR() throws IOException {
        String[] result = tokenizer.tokenize(new StringReader("this is \n me"));
        Assertions.assertEquals(1,result.length);
        Assertions.assertEquals("this is \n me",result[0]);
    }

    @Test
    void capitalLetterTextR() throws IOException {
        String[] result = tokenizer.tokenize(new StringReader("THIS is"));
        Assertions.assertEquals(1,result.length);
        Assertions.assertEquals("this is",result[0]);
    }

}