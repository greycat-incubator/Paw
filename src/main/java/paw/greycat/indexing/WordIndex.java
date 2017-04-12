package paw.greycat.indexing;

import greycat.struct.EGraph;

public interface WordIndex {

    /**
     * Method returning the Egraph used as a base of the index
     *
     * @return
     */
    EGraph eGraph();

    /**
     * Method to add or retrieve a word from the EGraph
     *
     * @param word to add to the index
     * @return the id of the corresponding ENode
     */
    int getOrCreate(String word);

    /**
     * Get the words that are the closest in the index to the given word
     *
     * @param word to look for similar ones
     * @return an array containing the closest word
     */
    CharSequence[] getClosestWordsFrom(String word);

    /**
     * Get the Enode that are the closest in the index to the given word
     *
     * @param word to look for similar ones
     * @return
     */
    int[] getClosestNodesFrom(String word);

    /**
     * Method to remove a word from the index
     *
     * @param word to remove
     * @return true if successful false otherwise
     */
    boolean removeWord(String word);

    /**
     * Method to get the current size of the index
     *
     * @return size of the index
     */
    int size();

    String getNameOfToken(int tokenId);
}
