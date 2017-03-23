package paw.greycat.actions.tokenization;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paw.greycat.actions.ActionTest;
import paw.tokeniser.Tokenizer;
import paw.tokeniser.preprocessing.LowerCasePreprocessor;
import paw.tokeniser.preprocessing.PreprocessorType;
import paw.tokeniser.preprocessing.UpperCasePreprocessor;
import paw.tokeniser.tokenisation.TokenizerType;
import paw.tokeniser.tokenisation.nl.EnglishTokenizer;

import static greycat.Tasks.newTask;
import static org.junit.jupiter.api.Assertions.*;
import static paw.greycat.actions.Pawctions.addPreprocessors;
import static paw.greycat.actions.Pawctions.createTokenizer;

class ActionAddPreprocessorsTest extends ActionTest{
    @BeforeEach
    public void setUp() {
        initGraph();
    }

    @AfterEach
    public void tearDown() {
        removeGraph();
    }

    @Test
    public void addOnePreprocessor(){
        int counter = 1;
        final int[] i = {0};
        newTask()
                .then(createTokenizer("tokenizer", TokenizerType.ENGLISH,true))
                .then(addPreprocessors("tokenizer", PreprocessorType.LOWER_CASE))
                .thenDo(ctx -> {
                    Tokenizer tok = (Tokenizer) ctx.variable("tokenizer").get(0);
                    assertEquals(1,tok.getListOfPreprocessor().size());
                    assertTrue(tok.getListOfPreprocessor().get(0) instanceof LowerCasePreprocessor);
                    i[0]++;
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    public void addSeveralPreprocessor(){
        int counter = 1;
        final int[] i = {0};
        newTask()
                .then(createTokenizer("tokenizer", TokenizerType.ENGLISH,true))
                .then(addPreprocessors("tokenizer", PreprocessorType.LOWER_CASE,PreprocessorType.UPPER_CASE))
                .thenDo(ctx -> {
                    Tokenizer tok = (Tokenizer) ctx.variable("tokenizer").get(0);
                    assertEquals(2,tok.getListOfPreprocessor().size());
                    assertTrue(tok.getListOfPreprocessor().get(1) instanceof UpperCasePreprocessor);
                    i[0]++;
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    public void addNotExistingPreprocessor(){
        int counter = 0;
        final int[] i = {0};
        assertThrows(AssertionError.class,() -> {
            newTask()
                    .then(createTokenizer("tokenizer", (byte) 22,true))
                    .thenDo(ctx -> {
                        i[0]++;
                        ctx.continueTask();
                    })
                    .execute(graph, null);
        });
        assertEquals(counter, i[0]);
    }
}