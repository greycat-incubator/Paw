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
