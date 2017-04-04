package paw.greycat.actions.tokenizedcontent;

import greycat.Node;
import greycat.struct.IntArray;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paw.greycat.actions.ActionTest;
import paw.tokeniser.tokenisation.TokenizerType;

import java.util.Arrays;

import static greycat.Tasks.newTask;
import static org.junit.jupiter.api.Assertions.*;
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
        int counter = 4;
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
                .defineAsVar("tokenizedContent")
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
                })
                .traverse(RELATION_INDEX_TOKEN_TO_TYPEINDEX)
                .thenDo(ctx -> {
                    assertEquals(13, ctx.resultAsNodes().size());
                    for (int j = 0; j < ctx.resultAsNodes().size(); j++) {
                        assertEquals(TYPE_TOKEN_WITHOUT_TYPE, ctx.resultAsNodes().get(j).get(NODE_NAME_TYPEINDEX));
                    }
                    i[0]++;
                    ctx.continueTask();
                })
                .traverse(RELATION_INDEX_TYPEINDEX_TO_INVERTEDINDEX)
                .thenDo(
                        ctx -> {
                            assertEquals(13, ctx.resultAsNodes().size());
                            long id = ((Node) ctx.variable("tokenizedContent").get(0)).id();
                            for (int j = 0; j < ctx.resultAsNodes().size(); j++) {
                                IntArray array = (IntArray) ctx.resultAsNodes().get(j).get(INVERTEDINDEX_POSITION);
                                int[] position = array.extract();
                                Arrays.sort(position);
                                assertNotEquals(-1, Arrays.binarySearch(position, j));
                                assertEquals(id, ctx.resultAsNodes().get(j).get(INVERTEDINDEX_TOKENIZEDCONTENT));
                            }
                            i[0]++;
                            ctx.continueTask();
                        }
                )
                .execute(graph, null);
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