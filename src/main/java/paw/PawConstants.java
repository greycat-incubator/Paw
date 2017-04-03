package paw;

public class PawConstants {


    public final static String TYPE_TOKEN_WITHOUT_TYPE = "noType";


    public final static String RELATION_INDEX_ENTRY_POINT = "entryPoint";
    public final static String RELATION_INDEX_VOCABULARY_TO_TOKENINDEX = "indexing";
    public final static String RELATION_INDEX_TOKENINDEX_TO_TOKEN = "tokens";
    public final static String RELATION_TOKEN_TO_TOKENINDEX = "tokenIndex";
    public final static String RELATION_INDEX_NODE_TO_TOKENIZECONTENT = "tokenizedContents";
    public final static String RELATION_TOKENIZECONTENT_TO_NODE = "father";
    public final static String RELATION_TOKENIZECONTENT_TO_TOKENS = "tokens";
    public final static String RELATION_INDEX_TOKEN_TO_TYPEINDEX = "typeIndex";
    public final static String RELATION_TYPEINDEX_TO_TOKEN = "token";
    public final static String RELATION_INDEX_TYPEINDEX_TO_INVERTEDINDEX = "invertedIndex";
    public final static String RELATION_INVERTEDINDEX_TO_TYPEINDEX = "typeIndex";
    public final static String RELATION_INVERTEDINDEX_TO_TOKEN = "token";


    public final static String NODE_NAME = "name";
    public final static String NODE_NAME_TOKENINDEX = "firstLetters";
    public final static String NODE_NAME_TYPEINDEX = "typeOfToken";

    public final static String NODE_TYPE = "type";
    public final static String NODE_TYPE_VOCABULARY = "Vocabulary";
    public final static String NODE_TYPE_TOKENINDEX = "TokenIndex";
    public final static String NODE_TYPE_TOKEN = "Token";
    public final static String NODE_TYPE_TOKENIZE_CONTENT = "TokenizedContent";
    public final static String NODE_TYPE_TYPEINDEX = "TypeIndex";
    public final static String NODE_TYPE_INVERTED_INDEX = "InvertedIndex";

    public final static int SIZE_OF_INDEX = 3;


    public final static String TOKENIZE_CONTENT_PLUGIN = "plugin";
    public final static String TOKENIZE_CONTENT_PATCH = "patch";
    public final static String TOKENIZE_CONTENT_DELIMITERS = "delimiters";
    public final static String TOKENIZE_CONTENT_TYPE = "typeOfToken";

    public final static String INVERTEDINDEX_TOKENIZEDCONTENT = "tokenizedContent";
    public final static String INVERTEDINDEX_POSITION = "position";
}
