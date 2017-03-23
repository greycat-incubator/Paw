package paw.greycat.actions.tokenization;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paw.PawConstants;
import paw.greycat.actions.ActionTest;
import paw.tokeniser.Tokenizer;
import paw.tokeniser.preprocessing.LowerCasePreprocessor;
import paw.tokeniser.preprocessing.PreprocessorType;
import paw.tokeniser.tokenisation.TokenizerType;

import static greycat.Tasks.newTask;
import static org.junit.jupiter.api.Assertions.*;
import static paw.greycat.actions.Pawctions.addPreprocessors;
import static paw.greycat.actions.Pawctions.createTokenizer;
import static paw.greycat.actions.Pawctions.setTypOfToken;

class ActionSetTypeOfTokenTest extends ActionTest {
    @BeforeEach
    public void setUp() {
        initGraph();
    }

    @AfterEach
    public void tearDown() {
        removeGraph();
    }

    @Test
    public void setTypeOfTokenOnce(){
        int counter = 1;
        final int[] i = {0};
        newTask()
                .then(createTokenizer("tokenizer", TokenizerType.ENGLISH,true))
                .then(setTypOfToken("tokenizer", "myTok"))
                .thenDo(ctx -> {
                    Tokenizer tok = (Tokenizer) ctx.variable("tokenizer").get(0);
                    assertEquals("myTok",tok.getTypeOfToken());
                    i[0]++;
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    public void setTypeOfTokenTwice(){
        int counter = 1;
        final int[] i = {0};
        newTask()
                .then(createTokenizer("tokenizer", TokenizerType.ENGLISH,true))
                .then(setTypOfToken("tokenizer", "myTok"))
                .then(setTypOfToken("tokenizer", "myTok2"))
                .thenDo(ctx -> {
                    Tokenizer tok = (Tokenizer) ctx.variable("tokenizer").get(0);
                    assertEquals("myTok2",tok.getTypeOfToken());
                    i[0]++;
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    public void dontSetTypeOfToken(){
        int counter = 1;
        final int[] i = {0};
        newTask()
                .then(createTokenizer("tokenizer", TokenizerType.ENGLISH,true))
                .thenDo(ctx -> {
                    Tokenizer tok = (Tokenizer) ctx.variable("tokenizer").get(0);
                    assertEquals(PawConstants.NO_TYPE_TOKEN,tok.getTypeOfToken());
                    i[0]++;
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }
}