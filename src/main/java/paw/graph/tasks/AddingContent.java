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
package paw.graph.tasks;

import greycat.Node;
import greycat.Task;
import paw.graph.nodes.TokenizeContentNode;
import paw.tokenizer.token.Token;

import java.util.List;

import static greycat.Tasks.newTask;

public class AddingContent {

    public static Task addTokenizeContentToNode(List<Token> tokens, String category, String name) {
        return newTask()
                .thenDo(ctx -> {
                    Node currentNode = ctx.resultAsNodes().get(0);
                    TokenizeContentNode.getOrCreateTokenizeContentOfNode(currentNode, name, category, result -> {
                        result.setContent(tokens);
                        result.free();
                        ctx.continueTask();
                    });
                });
    }

}
