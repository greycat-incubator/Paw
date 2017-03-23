package paw.greycat.actions;

import greycat.*;
import greycat.scheduler.TrampolineScheduler;
import mylittleplugin.MyLittleActionPlugin;
import paw.greycat.PawPlugin;

import static greycat.Constants.BEGINNING_OF_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class ActionTest {
    protected Graph graph;

    @SuppressWarnings("Duplicates")
    protected void initGraph() {
        graph = new GraphBuilder()
                .withPlugin(new PawPlugin())
                .withPlugin(new MyLittleActionPlugin())
                .withScheduler(new TrampolineScheduler()).build();
        final ActionTest selfPointer = this;
        graph.connect(result -> {

            //create graph nodes
            final Node n0 = selfPointer.graph.newNode(0, 0);
            n0.set("name", Type.STRING, "n0");
            n0.set("value", Type.INT, 8);

            final Node n1 = selfPointer.graph.newNode(0, 0);
            n1.set("name", Type.STRING, "n1");
            n1.set("value", Type.INT, 3);

            final Node root = selfPointer.graph.newNode(0, 0);
            root.set("name", Type.STRING, "root");
            root.addToRelation("children", n0);
            root.addToRelation("children", n1);

            //create some index
            selfPointer.graph.index(0, BEGINNING_OF_TIME, "roots", rootsIndex -> rootsIndex.addToIndex(root, "name"));
            selfPointer.graph.index(0, BEGINNING_OF_TIME, "nodes", nodesIndex -> {
                nodesIndex.addToIndex(n0, "name");
                nodesIndex.addToIndex(n1, "name");
                nodesIndex.addToIndex(root, "name");
            });
        });
    }

    protected void removeGraph() {
        graph.disconnect(result -> assertEquals(true, result));
    }

}

