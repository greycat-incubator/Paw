package paw.greycat.actions.tokenizedcontent;

import greycat.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paw.greycat.actions.ActionTest;
import paw.tokeniser.tokenisation.TokenizerType;

import static greycat.Tasks.newTask;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static paw.PawConstants.*;
import static paw.greycat.actions.Pawctions.*;

class ActionUpdateOrCreateTokenizeRelationFromStringTest extends ActionTest {

    static String text1 = "the apple was looking over the cloud";
    static String text2 = "an orange was riding a skateboard";

    @BeforeEach
    void setUp() {
        initGraph();
    }

    @AfterEach
    void tearDown() {
        removeGraph();
    }

    @Test
    public void oneRelationOneText() {
        int counter = 2;
        final int[] i = {0};
        newTask()
                .travelInTime("0")
                .then(retrieveVocabularyNode())
                .then(createTokenizer("tokenizer", TokenizerType.ENGLISH, true))
                .readGlobalIndex("roots")
                .defineAsVar("nodevar")
                .then(updateOrCreateTokenizeRelationFromString("tokenizer", "nodevar", text1, "text1"))
                .thenDo(ctx ->
                        ctx.continueTask())
                .traverse(RELATION_INDEX_NODE_TO_TOKENIZECONTENT)
                .thenDo(ctx -> {
                    assertEquals(1, ctx.resultAsNodes().size());
                    Node node = ctx.resultAsNodes().get(0);
                    assertEquals(TYPE_TOKEN_WITHOUT_TYPE, node.get(TOKENIZE_CONTENT_TYPE));
                    assertTrue((boolean) node.get(TOKENIZE_CONTENT_DELIMITERS));
                    i[0]++;
                    ctx.continueTask();
                })
                .traverse(RELATION_TOKENIZECONTENT_TO_TOKENS)
                .thenDo(ctx -> {
                    assertEquals(13, ctx.resultAsNodes().size());
                    i[0]++;
                    ctx.continueTask();
                }).execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    public void TwoRelationTwoText() {

    }

    @Test
    public void oneRelationUpdated() {

    }

    @Test
    public void oneRelationUpdatedTwice() {

    }

    @Test
    public void twoRelationUpdated() {

    }

    @Test
    public void twoRelationUpdatedTwice() {

    }


}