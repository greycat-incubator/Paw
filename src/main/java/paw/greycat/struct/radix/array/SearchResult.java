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
package paw.greycat.struct.radix.array;

class SearchResult {
    final CharSequence key;
    final int nodeFound;
    final int charsMatched;
    final int charsMatchedInNodeFound;
    final int parentNode;
    final int parentNodesParent;
    final Classification classification;
    private final String[] stringArray;

    enum Classification {
        EXACT_MATCH,
        INCOMPLETE_MATCH_TO_END_OF_EDGE,
        INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE,
        KEY_ENDS_MID_EDGE,
    }

    SearchResult(String[] stringArray, CharSequence key, int nodeFound, int charsMatched, int charsMatchedInNodeFound, int parentNode, int parentNodesParent) {
        this.key = key;
        this.nodeFound = nodeFound;
        this.charsMatched = charsMatched;
        this.charsMatchedInNodeFound = charsMatchedInNodeFound;
        this.parentNode = parentNode;
        this.parentNodesParent = parentNodesParent;
        this.stringArray = stringArray;

        // Classify this search result...
        this.classification = classify(key, nodeFound, charsMatched, charsMatchedInNodeFound);
    }

    protected Classification classify(CharSequence key, int nodeFound, int charsMatched, int charsMatchedInNodeFound) {
        if(nodeFound == -1){
            return Classification.INCOMPLETE_MATCH_TO_END_OF_EDGE;
        }
        int length = stringArray[nodeFound].length();
        if (charsMatched == key.length()) {
            if (charsMatchedInNodeFound == length) {
                return Classification.EXACT_MATCH;
            } else if (charsMatchedInNodeFound < length) {
                return Classification.KEY_ENDS_MID_EDGE;
            }
        } else if (charsMatched < key.length()) {
            if (charsMatchedInNodeFound == length) {
                return Classification.INCOMPLETE_MATCH_TO_END_OF_EDGE;
            } else if (charsMatchedInNodeFound < length) {
                return Classification.INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE;
            }
        }
        throw new IllegalStateException("Unexpected failure to classify SearchResult: " + this);
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "key=" + key +
                ", nodeFound=" + nodeFound +
                ", charsMatched=" + charsMatched +
                ", charsMatchedInNodeFound=" + charsMatchedInNodeFound +
                ", parentNode=" + parentNode +
                ", parentNodesParent=" + parentNodesParent +
                ", classification=" + classification +
                '}';
    }
}