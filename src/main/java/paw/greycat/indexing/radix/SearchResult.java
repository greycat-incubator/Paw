package paw.greycat.indexing.radix;

import greycat.struct.ENode;

import static paw.greycat.indexing.radix.RadixTree.NODE_ADDITION;

class SearchResult {
    final CharSequence key;
    final ENode nodeFound;
    final int charsMatched;
    final int charsMatchedInNodeFound;
    final ENode parentNode;
    final ENode parentNodesParent;
    final Classification classification;

    enum Classification {
        EXACT_MATCH,
        INCOMPLETE_MATCH_TO_END_OF_EDGE,
        INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE,
        KEY_ENDS_MID_EDGE,
    }

    SearchResult(CharSequence key, ENode nodeFound, int charsMatched, int charsMatchedInNodeFound, ENode parentNode, ENode parentNodesParent) {
        this.key = key;
        this.nodeFound = nodeFound;
        this.charsMatched = charsMatched;
        this.charsMatchedInNodeFound = charsMatchedInNodeFound;
        this.parentNode = parentNode;
        this.parentNodesParent = parentNodesParent;

        // Classify this search result...
        this.classification = classify(key, nodeFound, charsMatched, charsMatchedInNodeFound);
    }

    protected Classification classify(CharSequence key, ENode nodeFound, int charsMatched, int charsMatchedInNodeFound) {
        if (charsMatched == key.length()) {
            if (charsMatchedInNodeFound == ((String) nodeFound.get(NODE_ADDITION)).length()) {
                return Classification.EXACT_MATCH;
            } else if (charsMatchedInNodeFound < ((String) nodeFound.get(NODE_ADDITION)).length()) {
                return Classification.KEY_ENDS_MID_EDGE;
            }
        } else if (charsMatched < key.length()) {
            if (charsMatchedInNodeFound == ((String) nodeFound.get(NODE_ADDITION)).length()) {
                return Classification.INCOMPLETE_MATCH_TO_END_OF_EDGE;
            } else if (charsMatchedInNodeFound < ((String) nodeFound.get(NODE_ADDITION)).length()) {
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