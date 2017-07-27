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
package paw.graph;

import greycat.DeferCounter;
import greycat.Graph;
import greycat.Node;
import greycat.plugin.NodeFactory;
import greycat.plugin.Plugin;
import greycat.plugin.TypeFactory;
import greycat.struct.EStructArray;
import paw.graph.customTypes.bitset.roaring.CTRoaringBitMap;
import paw.graph.customTypes.radix.struct.RadixTree;
import paw.graph.customTypes.tokenizedContent.CTTCBitset;
import paw.graph.customTypes.tokenizedContent.CTTCRoaring;
import paw.graph.nodes.*;

import static paw.PawConstants.*;

public class PawPlugin implements Plugin {
    @Override
    public void start(Graph graph) {
        graph.nodeRegistry()
                .getOrCreateDeclaration(DictionnaryNode.NAME)
                .setFactory(new NodeFactory() {
                    @Override
                    public Node create(long world, long time, long id, Graph graph) {
                        return new DictionnaryNode(world, time, id, graph);
                    }
                });
        graph.nodeRegistry()
                .getOrCreateDeclaration(DelimiterVocabularyNode.NAME)
                .setFactory(new NodeFactory() {
                    @Override
                    public Node create(long world, long time, long id, Graph graph) {
                        return new DelimiterVocabularyNode(world, time, id, graph);
                    }
                });

        graph.nodeRegistry()
                .getOrCreateDeclaration(TCListNode.NAME)
                .setFactory(new NodeFactory() {
                    @Override
                    public Node create(long world, long time, long id, Graph graph) {
                        return new TCListNode(world, time, id, graph);
                    }
                });

        graph.nodeRegistry()
                .getOrCreateDeclaration(TokenizeContentNode.NAME)
                .setFactory(new NodeFactory() {
                    @Override
                    public Node create(long world, long time, long id, Graph graph) {
                        return new TokenizeContentNode(world, time, id, graph);
                    }
                });
        graph.nodeRegistry()
                .getOrCreateDeclaration(VocabularyNode.NAME)
                .setFactory(new NodeFactory() {
                    @Override
                    public Node create(long world, long time, long id, Graph graph) {
                        return new VocabularyNode(world, time, id, graph);
                    }
                });

        graph.typeRegistry()
                .getOrCreateDeclaration(CTRoaringBitMap.NAME)
                .setFactory(new TypeFactory() {
                    @Override
                    public Object wrap(final EStructArray backend) {
                        return new CTRoaringBitMap(backend);
                    }
                });
        graph.typeRegistry()
                .getOrCreateDeclaration(RadixTree.NAME)
                .setFactory(new TypeFactory() {
                    @Override
                    public Object wrap(final EStructArray backend) {
                        return new RadixTree(backend);
                    }
                });
        graph.typeRegistry()
                .getOrCreateDeclaration(CTTCBitset.NAME)
                .setFactory(new TypeFactory() {
                    @Override
                    public Object wrap(final EStructArray backend) {
                        return new CTTCBitset(backend);
                    }
                });
        graph.typeRegistry()
                .getOrCreateDeclaration(CTTCRoaring.NAME)
                .setFactory(new TypeFactory() {
                    @Override
                    public Object wrap(final EStructArray backend) {
                        return new CTTCRoaring(backend);
                    }
                });


        graph.addConnectHook(result -> {
            DeferCounter counter = graph.newCounter(2);
            graph.declareIndex(0, INDEX_DELIMITER, index -> {
                index.free();
                counter.count();
            }, CATEGORY_OF_TOKENIZE_CONTENT);
            graph.declareIndex(0, INDEX_DICTIONNARY, index -> {
                index.free();
                counter.count();
            }, CATEGORY_OF_TOKENIZE_CONTENT);

            counter.then(() -> result.on(true));
        });
    }

    @Override
    public void stop() {

    }
}
