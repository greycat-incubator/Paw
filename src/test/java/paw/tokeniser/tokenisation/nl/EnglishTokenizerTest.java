package paw.tokeniser.tokenisation.nl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paw.tokeniser.preprocessing.LowerCasePreprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnglishTokenizerTest {


    private EnglishTokenizer tokenizer;

    @BeforeEach
    public void buildTokenizer(){
        this.tokenizer = new EnglishTokenizer();
        tokenizer.addPreprocessor(new LowerCasePreprocessor());

    }

    @Test
    public void emptyText(){
        String[] result = tokenizer.tokenize("");
        assertEquals(1,result.length);
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
        Assertions.assertEquals(2,result.length);
        Assertions.assertEquals("this",result[0]);
    }

    @Test
    public void twoLinesTextD(){
        tokenizer.setKeepDelimiter(true);
        String[] result = tokenizer.tokenize("this is \n me");
        Assertions.assertEquals(4,result.length);
        Assertions.assertEquals("\n",result[2]);
    }

    @Test
    public void twoLinesText(){
        String[] result = tokenizer.tokenize("this is \n me");
        Assertions.assertEquals(3,result.length);
        Assertions.assertEquals("me",result[2]);
    }

    @Test
    public void utfTest(){
        String[] result = tokenizer.tokenize("mais où est-ce-que ça se trouve?");
        Assertions.assertEquals(8,result.length);
        Assertions.assertEquals("o",result[1]);
    }
}