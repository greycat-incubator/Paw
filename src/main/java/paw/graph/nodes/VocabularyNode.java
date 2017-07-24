package paw.graph.nodes;

import greycat.Constants;
import greycat.Graph;
import greycat.Type;
import greycat.base.BaseNode;
import greycat.struct.IntIntMap;
import greycat.utility.HashHelper;
import paw.graph.customTypes.bitset.CTBitset;
import paw.graph.customTypes.radix.structii.RadixTreeWithII;

/**
 * Class representing a vocabulary node, words are splitted between vocabulary nodes based on their first character
 */
public class VocabularyNode extends BaseNode {
    public final static String NAME = "Vocabulary";

    public final static String FIRST_CHAR = "fc";
    public final static String RADIX = "radix";
    public final static String MAPOFWORD = "map";

    private final static int FIRST_CHAR_H = HashHelper.hash(FIRST_CHAR);
    private final static int RADIX_H = HashHelper.hash(RADIX);
    private final static int MAPOFWORD_H = HashHelper.hash(MAPOFWORD);

    /**
     * Constructor
     *
     * @param p_world
     * @param p_time
     * @param p_id
     * @param p_graph
     */
    public VocabularyNode(long p_world, long p_time, long p_id, Graph p_graph) {
        super(p_world, p_time, p_id, p_graph);
    }

    /**
     * method to initialize the node
     *
     * @param firstChar first character of all words that will be present in the vocabulary
     */
    public final void initVocNode(char firstChar) {
        setAt(FIRST_CHAR_H, Type.STRING, firstChar);
        setTimeSensitivity(-1, 0);
        getOrCreateAt(MAPOFWORD_H, Type.INT_TO_INT_MAP);
        getOrCreateCustomAt(RADIX_H, RadixTreeWithII.NAME);
    }

    /**
     * Method to retrieve the character this vocabulary is using as first character
     *
     * @return the character
     */
    public final String getFirstChar() {
        return (String) getAt(FIRST_CHAR_H);
    }

    /**
     * Method to know whether a word is present in this vocabulary based on its hash
     *
     * @param hash of the word to look foe
     * @return the position of the word in the radix tree
     */
    public final int getWord(int hash) {
        IntIntMap map = (IntIntMap) getAt(MAPOFWORD_H);
        return map.get(hash);
    }

    /**
     * Method to get the position of a word in the radix and create it if necessary, the tokenizecontent id requesting it will be stored in the radix tree.
     *
     * @param word to look for
     * @param tcId tokenize content id
     * @return the position of the word in the radix tree.
     */
    public final int getOrCreateWordForTC(String word, int tcId) {
        this.cacheLock();
        int hash = HashHelper.hash(word);
        int result = getWord(hash);
        RadixTreeWithII radixTreeWithII = (RadixTreeWithII) getOrCreateCustomAt(RADIX_H, RadixTreeWithII.NAME);
        if (result == Constants.NULL_INT) {
            result = radixTreeWithII.getOrCreateWithID(word, tcId);
            IntIntMap map = (IntIntMap) getAt(MAPOFWORD_H);
            map.put(hash, result);
        } else {
            radixTreeWithII.addIDToNode(result, tcId);
        }
        this.cacheUnlock();
        return result;
    }

    /**
     * @param position
     * @return
     */
    public final String getWordForPosition(int position) {
        RadixTreeWithII radixTreeWithII = (RadixTreeWithII) getOrCreateCustomAt(RADIX_H, RadixTreeWithII.NAME);
        return radixTreeWithII.getNameOfToken(position);
    }

    /**
     * @param position
     * @return
     */
    public final CTBitset getIteratorForTCID(int position) {
        RadixTreeWithII radixTreeWithII = (RadixTreeWithII) getOrCreateCustomAt(RADIX_H, RadixTreeWithII.NAME);
        return radixTreeWithII.retrieveInvertedIndexFor(position);
    }
}
