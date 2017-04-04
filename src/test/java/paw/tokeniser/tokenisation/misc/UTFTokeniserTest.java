package paw.tokeniser.tokenisation.misc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paw.tokeniser.preprocessing.LowerCasePreprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UTFTokeniserTest {
    private UTFTokeniser tokenizer;

    @BeforeEach
    void buildTokenizer(){
        this.tokenizer = new UTFTokeniser();
        tokenizer.addPreprocessor(new LowerCasePreprocessor());

    }

    @Test
    void emptyText(){
        String[] result = tokenizer.tokenize("");
        assertEquals(0, result.length);
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
    void twoLinesTextD(){
        tokenizer.setKeepDelimiter(true);
        String[] result = tokenizer.tokenize("this is \n me");
        Assertions.assertEquals(7, result.length);
        Assertions.assertEquals("\n", result[4]);
    }

    @Test
    void twoLinesText(){
        String[] result = tokenizer.tokenize("this is \n me");
        Assertions.assertEquals(3,result.length);
        Assertions.assertEquals("me",result[2]);
    }

    @Test
    void utfTest(){
        String[] result = tokenizer.tokenize("mais où est-ce-que ça se trouve?");
        Assertions.assertEquals(8,result.length);
        Assertions.assertEquals("où",result[1]);
    }
}