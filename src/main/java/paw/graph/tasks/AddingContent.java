package paw.graph.tasks;

import greycat.Node;
import greycat.Task;
import paw.graph.nodes.TokenizeContentNode;
import paw.tokenizer.token.Token;

import java.util.List;

import static greycat.Tasks.newTask;

public class AddingContent {

    public static Task addTokenizeContentToNode(List<Token> tokens,String category, String name){
        return newTask()
                .thenDo(ctx ->{
                    if(ctx.result().size() !=1 || !(ctx.result().get(0) instanceof Node)){
                        ctx.endTask(ctx.result(),new RuntimeException("wrong input to Task"));
                    }
                    Node currentNode = ctx.resultAsNodes().get(0);
                    TokenizeContentNode.getOrCreateTokenizeContentOfNode(currentNode, name, category, result -> {
                        result.setContent(tokens);
                        ctx.continueTask();
                    });
                });
    }

}
