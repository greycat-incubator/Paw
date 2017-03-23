package paw.tokeniser.tokenisation.misc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paw.tokeniser.preprocessing.LowerCasePreprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleTokenizerTest {

    private SimpleTokenizer tokenizer;

    @BeforeEach
    void buildTokenizer(){
        this.tokenizer = new SimpleTokenizer();
        tokenizer.addPreprocessor(new LowerCasePreprocessor());
    }

    @Test
    void emptyText(){
        String[] result = tokenizer.tokenize("");
        assertEquals(0,result.length);
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
        Assertions.assertEquals(2,result.length);
        Assertions.assertEquals("this",result[0]);
    }

    @Test
    void twoLinesText(){
        String[] result = tokenizer.tokenize("this is \n me");
        Assertions.assertEquals(4,result.length);
        Assertions.assertEquals("\n",result[2]);
    }

}