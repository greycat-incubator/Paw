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
    public void buildTokenizer(){
        this.tokenizer = new IdentityTokenizer();
        tokenizer.addPreprocessor(new LowerCasePreprocessor());
    }

    @Test
    public void emptyText(){
        String[] result = tokenizer.tokenize("");
        Assertions.assertEquals(0,result.length);
    }

    @Test
    public void oneWordText(){
        String[] result = tokenizer.tokenize("this");
        Assertions.assertEquals(1,result.length);
        Assertions.assertEquals("this",result[0]);
    }


    @Test
    public void twoWordText(){
        String[] result = tokenizer.tokenize("this is");
        Assertions.assertEquals(1,result.length);
        Assertions.assertEquals("this is",result[0]);
    }

    @Test
    public void twoLinesText(){
        String[] result = tokenizer.tokenize("this is \n me");
        Assertions.assertEquals(1,result.length);
        Assertions.assertEquals("this is \n me",result[0]);
    }

    @Test
    public void capitalLetterText(){
        String[] result = tokenizer.tokenize("THIS is");
        Assertions.assertEquals(1,result.length);
        Assertions.assertEquals("this is",result[0]);
    }

    @Test
    public void emptyTextR() throws IOException {
        String[] result = tokenizer.tokenize(new StringReader(""));
        Assertions.assertEquals(1,result.length);
    }

    @Test
    public void oneWordTextR() throws IOException {
        String[] result = tokenizer.tokenize(new StringReader("this"));
        Assertions.assertEquals(1,result.length);
        Assertions.assertEquals("this",result[0]);
    }


    @Test
    public void twoWordTextR() throws IOException {
        String[] result = tokenizer.tokenize(new StringReader("this is"));
        Assertions.assertEquals(1,result.length);
        Assertions.assertEquals("this is",result[0]);
    }

    @Test
    public void twoLinesTextR() throws IOException {
        String[] result = tokenizer.tokenize(new StringReader("this is \n me"));
        Assertions.assertEquals(1,result.length);
        Assertions.assertEquals("this is \n me",result[0]);
    }

    @Test
    public void capitalLetterTextR() throws IOException {
        String[] result = tokenizer.tokenize(new StringReader("THIS is"));
        Assertions.assertEquals(1,result.length);
        Assertions.assertEquals("this is",result[0]);
    }

}