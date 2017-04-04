package paw.greycat.actions;

import greycat.Action;
import paw.greycat.actions.tokenization.*;
import paw.greycat.actions.tokenization.java.ActionSetRemoveComment;
import paw.greycat.actions.tokenizedcontent.ActionUpdateOrCreateTokenizeRelationFromString;
import paw.greycat.actions.tokenizedcontent.ActionUpdateOrCreateTokenizeRelationFromVar;
import paw.greycat.actions.vocabulary.ActionGetOrCreateTokensFromStrings;
import paw.greycat.actions.vocabulary.ActionGetOrCreateTokensFromVar;
import paw.greycat.actions.vocabulary.ActionRetrieveVocabularyNode;

public class Pawctions {


    // Vocabulary

    public static Action retrieveVocabularyNode() {
        return new ActionRetrieveVocabularyNode();
    }

    public static Action getOrCreateTokensFromStrings(String... tokens) {
        return new ActionGetOrCreateTokensFromStrings(tokens);
    }

    public static Action getOrCreateTokensFromVar(String variable) {
        return new ActionGetOrCreateTokensFromVar(variable);
    }

    //Tokenization

    public static Action createTokenizer(String tokenizerVar, byte typeOfTokenizer, boolean keepDelimiter) {
        return new ActionCreateTokenizer(tokenizerVar, typeOfTokenizer, keepDelimiter);
    }

    public static Action addPreprocessors(String tokenizerVar, byte... typeOfPreprocessors) {
        return new ActionAddPreprocessors(tokenizerVar, typeOfPreprocessors);
    }

    public static Action setTypOfToken(String tokenizerVar, String typeOfToken) {
        return new ActionSetTypeOfToken(tokenizerVar, typeOfToken);
    }

    public static Action setRemoveContent(String tokenizerVar, boolean removeComment) {
        return new ActionSetRemoveComment(tokenizerVar, removeComment);
    }

    public static Action tokenizeFromStrings(String tokenizerVar, String... toTokenize) {
        return new ActionTokenizeFromStrings(tokenizerVar, toTokenize);
    }

    public static Action tokenizeFromVar(String tokenizerVar, String varToTokenize) {
        return new ActionTokenizeFromVar(tokenizerVar, varToTokenize);
    }

    // Tokenized Content
    public static Action updateOrCreateTokenizeRelationFromString(String tokenizerVar, String nodeVar, String content, String relationName) {
        return new ActionUpdateOrCreateTokenizeRelationFromString(tokenizerVar, nodeVar, content, relationName);
    }

    public static Action updateOrCreateTokenizeRelationFromVar(String tokenizerVar, String nodeVar, String contentVar, String... relationNames) {
        return new ActionUpdateOrCreateTokenizeRelationFromVar(tokenizerVar, nodeVar, contentVar, relationNames);
    }

}
