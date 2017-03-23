package paw.greycat.actions.vocabulary;

import greycat.ActionFunction;
import greycat.Node;
import greycat.TaskContext;
import greycat.TaskResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paw.greycat.actions.ActionTest;

import static greycat.Tasks.newTask;
import static mylittleplugin.MyLittleActions.injectAsVar;
import static org.junit.jupiter.api.Assertions.*;
import static paw.PawConstants.TOKEN_NAME;
import static paw.greycat.actions.Pawctions.getOrCreateTokensFromStrings;
import static paw.greycat.actions.Pawctions.getOrCreateTokensFromVar;

@SuppressWarnings("Duplicates")
class ActionGetOrCreateTokensFromVarTest extends ActionTest {
    @BeforeEach
    public void setUp() {
        initGraph();
    }

    @AfterEach
    public void tearDown() {
        removeGraph();
    }


    @Test
    public void createOneToken() {
        int counter = 1;
        final int[] i = {0};
        newTask()
                .then(injectAsVar("mytok", new String[]{"Token7"}))
                .then(getOrCreateTokensFromVar("mytok"))
                .thenDo(ctx -> {
                    TaskResult<Node> tok = ctx.resultAsNodes();
                    assertEquals(1, tok.size());
                    Node n = tok.get(0);
                    assertEquals("Token7", n.get(TOKEN_NAME));
                    i[0]++;
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    public void createSeveralTokens() {
        int counter = 1;
        final int[] i = {0};
        newTask()
                .then(injectAsVar("mytok", new String[]{"Token", "Token2", "Token3", "Token4"}))
                .then(getOrCreateTokensFromVar("mytok"))
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext ctx) {
                        TaskResult<Node> tok = ctx.resultAsNodes();
                        assertEquals(4, tok.size());
                        Node n = tok.get(0);
                        assertEquals("Token", n.get(TOKEN_NAME));
                        Node n1 = tok.get(1);
                        assertEquals("Token2", n1.get(TOKEN_NAME));
                        Node n2 = tok.get(2);
                        assertEquals("Token3", n2.get(TOKEN_NAME));
                        Node n3 = tok.get(3);
                        assertEquals("Token4", n3.get(TOKEN_NAME));
                        i[0]++;
                        ctx.continueTask();
                    }
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    public void retrieveOneAlreadyExistingToken() {
        int counter = 2;
        final int[] i = {0};
        newTask()
                .then(injectAsVar("mytok", new String[]{"Token4"}))
                .then(getOrCreateTokensFromVar("mytok"))
                .thenDo(ctx -> {
                    i[0]++;
                    ctx.continueWith(ctx.wrap(ctx.resultAsNodes().get(0).id()));
                })
                .defineAsVar("id")
                .then(getOrCreateTokensFromVar("mytok"))
                .thenDo(context -> context.continueWith(context.wrap(context.resultAsNodes().get(0).id())))
                .thenDo(ctx -> {
                    i[0]++;
                    assertEquals(ctx.longVar("id"), ctx.longResult());
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    public void retrieveSeveralAlreadyExistingToken() {
        int counter = 2;
        final int[] i = {0};
        newTask()
                .then(injectAsVar("mytok", new String[]{"Token", "Token2", "Token3", "Token4"}))
                .then(getOrCreateTokensFromVar("mytok"))
                .thenDo(ctx -> {
                    Long[] ids = new Long[ctx.resultAsNodes().size()];
                    i[0]++;
                    TaskResult<Node> nodes = ctx.resultAsNodes();
                    int size = nodes.size();
                    for (int i1 = 0; i1 < size; i1++) {
                        ids[i1] = nodes.get(i1).id();
                    }
                    ctx.continueWith(ctx.wrap(ids));
                })
                .defineAsVar("ids")
                .then(getOrCreateTokensFromVar("mytok"))
                .thenDo(ctx -> {
                    assertEquals(ctx.variable("ids").get(0), ctx.resultAsNodes().get(0).id());
                    assertEquals(ctx.variable("ids").get(1), ctx.resultAsNodes().get(1).id());
                    assertEquals(ctx.variable("ids").get(2), ctx.resultAsNodes().get(2).id());
                    assertEquals(ctx.variable("ids").get(3), ctx.resultAsNodes().get(3).id());
                    i[0]++;
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    public void mix() {
        int counter = 2;
        final int[] i = {0};
        newTask()
                .then(injectAsVar("mytok", new String[]{"Token", "Token2", "Token3", "Token4"}))
                .then(getOrCreateTokensFromVar("mytok"))
                .thenDo(ctx -> {
                    Long[] ids = new Long[ctx.resultAsNodes().size()];

                    TaskResult<Node> nodes = ctx.resultAsNodes();
                    int size = nodes.size();
                    for (int i1 = 0; i1 < size; i1++) {
                        ids[i1] = nodes.get(i1).id();
                    }
                    i[0]++;
                    ctx.continueWith(ctx.wrap(ids));
                })
                .defineAsVar("ids")
                .then(injectAsVar("mytok", new String[]{"Token", "Token5", "Token3", "Token7"}))
                .then(getOrCreateTokensFromVar("mytok"))
                .thenDo(ctx -> {
                    i[0]++;
                    assertEquals(ctx.variable("ids").get(0), ctx.resultAsNodes().get(0).id());
                    assertNotEquals(ctx.variable("ids").get(1), ctx.resultAsNodes().get(1).id());
                    assertEquals(ctx.variable("ids").get(2), ctx.resultAsNodes().get(2).id());
                    assertNotEquals(ctx.variable("ids").get(3), ctx.resultAsNodes().get(3).id());
                    ctx.continueTask();
                })
                //.addHook(new VerboseHook())
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

}