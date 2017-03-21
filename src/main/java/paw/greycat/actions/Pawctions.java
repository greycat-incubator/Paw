package paw.greycat.actions;

import greycat.Action;
import paw.greycat.actions.vocabulary.ActionGetOrCreateTokensFromStrings;
import paw.greycat.actions.vocabulary.ActionGetOrCreateTokensFromVar;
import paw.greycat.actions.vocabulary.ActionRetrieveVocabularyNode;

public class Pawctions {
    public static Action retrieveVocabularyNode() {
        return new ActionRetrieveVocabularyNode();
    }

    public static Action getOrCreateTokensFromStrings(String... tokens) {
        return new ActionGetOrCreateTokensFromStrings(tokens);
    }

    public static Action getOrCreateTokensFromVar(String variable) {
        return new ActionGetOrCreateTokensFromVar(variable);
    }


}
