package paw.greycat.actions.vocabulary;

import greycat.ActionFunction;
import greycat.TaskContext;
import org.junit.jupiter.api.Test;
import paw.greycat.actions.ActionTest;

import static greycat.Tasks.newTask;
import static org.junit.jupiter.api.Assertions.*;
import static paw.PawConstants.ENTRY_POINT_INDEX;
import static paw.PawConstants.ENTRY_POINT_NODE_NAME;
import static paw.PawConstants.VOCABULARY_NODE_NAME;
import static paw.greycat.actions.Pawctions.retrieveVocabularyNode;

class ActionRetrieveVocabularyNodeTest extends ActionTest {

    @Test
    public void inexistingVocabularyNode() {
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
        assertEquals(1, i[0]);
    }

    @Test
    public void checkThatNodeIsCreatedOnce() {
        int counter = 1;
        final int[] i = {0};
        newTask()
                .travelInTime("0")
                .then(retrieveVocabularyNode())
                .thenDo(context -> context.continueWith(context.wrap(context.resultAsNodes().get(0).id())))
                .defineAsVar("idA")
                .readGlobalIndex(ENTRY_POINT_INDEX, ENTRY_POINT_NODE_NAME, VOCABULARY_NODE_NAME)
                .thenDo(context -> context.continueWith(context.wrap(context.resultAsNodes().get(0).id())))
                .defineAsVar("idR")
                .then(retrieveVocabularyNode())
                .thenDo(context -> context.continueWith(context.wrap(context.resultAsNodes().get(0).id())))
                .thenDo(new ActionFunction() {
                            public void eval(TaskContext context) {
                                assertEquals(context.resultAsNodes().size(), 1);
                                assertEquals(context.longVar("idA"), context.longVar("idR"));

                                i[0]++;
                                context.continueTask();
                            }
                        }
                )
                .execute(graph, null);
        assertEquals(1, i[0]);
    }

}