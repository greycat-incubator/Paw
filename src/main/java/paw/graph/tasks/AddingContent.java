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
