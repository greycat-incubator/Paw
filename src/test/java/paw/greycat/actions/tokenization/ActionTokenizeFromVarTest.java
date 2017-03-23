package paw.greycat.actions.tokenization;

import greycat.TaskResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paw.greycat.actions.ActionTest;
import paw.tokeniser.tokenisation.TokenizerType;

import static greycat.Tasks.newTask;
import static org.junit.jupiter.api.Assertions.*;
import static paw.greycat.actions.Pawctions.createTokenizer;
import static paw.greycat.actions.Pawctions.tokenizeFromVar;

@SuppressWarnings("Duplicates")
class ActionTokenizeFromVarTest extends ActionTest {

    @BeforeEach
    void setUp() {
        initGraph();
    }

    @AfterEach
    void tearDown() {
        removeGraph();
    }

    @Test
    void tokenizeFromSingleStringInVar() {
        int counter = 1;
        final int[] i = {0};
        newTask()
                .then(createTokenizer("tokenizer", TokenizerType.ENGLISH, true))
                .thenDo(ctx -> {
                    ctx.setVariable("myvar", new String[]{"this is me, and you."});
                    ctx.continueTask();
                })
                .then(tokenizeFromVar("tokenizer", "myvar"))
                .thenDo(ctx -> {
                    assertEquals(1, ctx.result().size());
                    TaskResult<String> result = (TaskResult<String>) ctx.result().get(0);
                    assertEquals(11, result.size());
                    assertEquals(",", result.get(5));
                    i[0]++;
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    void tokenizeFromSeveralStringInVar() {
        int counter = 1;
        final int[] i = {0};
        newTask()
                .then(createTokenizer("tokenizer", TokenizerType.ENGLISH, true))
                .thenDo(ctx -> {
                    ctx.setVariable("myvar", new String[]{"this is me, and you.", "and this is another sentence."});
                    ctx.continueTask();
                })
                .then(tokenizeFromVar("tokenizer", "myvar"))
                .thenDo(ctx -> {
                    assertEquals(2, ctx.result().size());
                    TaskResult<String> result = (TaskResult<String>) ctx.result().get(1);
                    assertEquals(10, result.size());
                    assertEquals(".", result.get(9));
                    i[0]++;
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    void tokenizeFromNotExistingVar() {
        int counter = 1;
        final int[] i = {0};
        newTask()
                .then(createTokenizer("tokenizer", TokenizerType.ENGLISH, true))
                .then(tokenizeFromVar("tokenizer", "myvar"))
                .thenDo(ctx -> {
                    assertEquals(0, ctx.result().size());
                    i[0]++;
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    void tokenizeFromVarStoringSomethingElse() {
        int counter = 0;
        final int[] i = {0};
        assertThrows(AssertionError.class, () ->
                newTask()
                        .then(createTokenizer("tokenizer", TokenizerType.ENGLISH, true))
                        .thenDo(ctx -> {
                            ctx.setVariable("myvar", new int[]{1, 2, 3});
                            ctx.continueTask();
                        })
                        .then(tokenizeFromVar("tokenizer", "myvar"))
                        .thenDo(ctx -> {
                            i[0]++;
                            ctx.continueTask();
                        })
                        .execute(graph, null));
        assertEquals(counter, i[0]);
    }
}