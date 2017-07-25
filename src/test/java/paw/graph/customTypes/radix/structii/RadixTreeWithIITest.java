package paw.graph.customTypes.radix.structii;

import greycat.Graph;
import greycat.GraphBuilder;
import greycat.Node;
import greycat.scheduler.NoopScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paw.graph.PawPlugin;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RadixTreeWithIITest {
    Graph g;

    @BeforeEach
    public void init() {
        g = GraphBuilder.newBuilder().withPlugin(new PawPlugin()).withScheduler(new NoopScheduler()).build();
        g.connect(null);
    }

    @Test
    public void test() {
        Node node = g.newNode(0, 0);
        RadixTreeWithII r = (RadixTreeWithII) node.getOrCreateCustom("radix", RadixTreeWithII.NAME);
        int k = r.getOrCreateWithID("there", 1);
        r.addIDToNode(k, 2);
        assertTrue(true);
    }

    @AfterEach
    public void close() {
        g.disconnect(null);
    }

}