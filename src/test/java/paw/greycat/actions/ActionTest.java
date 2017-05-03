/**
 * Copyright 2017 Matthieu Jimenez.  All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package paw.greycat.actions;

import greycat.Graph;
import greycat.GraphBuilder;
import greycat.Node;
import greycat.Type;
import greycat.scheduler.TrampolineScheduler;
import mylittleplugin.MyLittleActionPlugin;

import static greycat.Constants.BEGINNING_OF_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class ActionTest {
    protected Graph graph;

    @SuppressWarnings("Duplicates")
    protected void initGraph() {
        graph = new GraphBuilder()
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

