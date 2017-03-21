package paw.greycat;

import greycat.Graph;
import greycat.Type;
import greycat.plugin.Plugin;
import paw.greycat.actions.PawctionNames;
import paw.greycat.actions.Pawctions;

public class PawPlugin implements Plugin {
    @Override
    public void start(Graph graph) {
        graph.actionRegistry()
                .getOrCreateDeclaration(PawctionNames.RETRIEVE_VOCABULARY_NODE)
                .setParams()
                .setDescription("retrieve the Vocabulary Node")
                .setFactory(params -> Pawctions.retrieveVocabularyNode());

        graph.actionRegistry()
                .getOrCreateDeclaration(PawctionNames.GET_OR_CREATE_TOKENS_FROM_STRINGS)
                .setParams(Type.STRING_ARRAY)
                .setDescription("Retrieve all the node corresponding to tokens stored in a String[] and create one if not existing")
                .setFactory(params -> {
                            if (params[0] != null) {
                                return Pawctions.getOrCreateTokensFromStrings((String[]) params[0]);
                            } else return null;
                        }
                );

        graph.actionRegistry()
                .getOrCreateDeclaration(PawctionNames.GET_OR_CREATE_TOKENS_FROM_VAR)
                .setParams(Type.STRING)
                .setDescription("Retrieve all the node corresponding to tokens stored in a variable and create one if not existing")
                .setFactory(params -> Pawctions.getOrCreateTokensFromVar((String) params[0]));
    }

    @Override
    public void stop() {

    }
}
