package paw.greycat.actions.vocabulary;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paw.greycat.actions.ActionTest;

import static greycat.Tasks.newTask;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static paw.PawConstants.*;
import static paw.greycat.actions.Pawctions.retrieveVocabularyNode;

class ActionRetrieveVocabularyNodeTest extends ActionTest {

    @BeforeEach
    void setUp(){
    initGraph();
    }

    @AfterEach
    void tearDown(){
        removeGraph();
    }

    @Test
    void inexistingVocabularyNode() {
        int counter = 1;
        final int[] i = {0};
        newTask()
                .travelInTime("0")
                .then(retrieveVocabularyNode())
                .thenDo(context -> {
                            assertEquals(context.resultAsNodes().size(), 1);
                            i[0]++;
                            context.continueTask();
                        }
                )
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    void checkThatNodeIsCreatedOnce() {
        int counter = 1;
        final int[] i = {0};
        newTask()
                .travelInTime("0")
                .then(retrieveVocabularyNode())
                .thenDo(context -> context.continueWith(context.wrap(context.resultAsNodes().get(0).id())))
                .defineAsVar("idA")
                .readGlobalIndex(RELATION_INDEX_ENTRY_POINT, NODE_TYPE, NODE_TYPE_VOCABULARY)
                .thenDo(context -> context.continueWith(context.wrap(context.resultAsNodes().get(0).id())))
                .defineAsVar("idR")
                .then(retrieveVocabularyNode())
                .thenDo(context -> context.continueWith(context.wrap(context.resultAsNodes().get(0).id())))
                .thenDo(context -> {
                            assertEquals(context.resultAsNodes().size(), 1);
                            assertEquals(context.longVar("idA"), context.longVar("idR"));
                            assertEquals(context.longVar("idA"), context.longResult());
                            i[0]++;
                            context.continueTask();
                        }
                )
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

}