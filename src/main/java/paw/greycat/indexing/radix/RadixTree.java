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
package paw.greycat.indexing.radix;

import greycat.Type;
import greycat.struct.EGraph;
import greycat.struct.ENode;
import greycat.struct.IntArray;
import paw.greycat.indexing.WordIndex;
import paw.utils.CharSequences;

import java.util.ArrayList;
import java.util.List;

public class RadixTree implements WordIndex {


    private final EGraph egraph;

    public RadixTree(final EGraph eGraph) {
        if (eGraph.root() == null) {
            ENode root = eGraph.newNode();
            root.set(NODE_ADDITION, Type.STRING, "");
            root.getOrCreate(NODE_CHILD, Type.INT_ARRAY);
            root.set(VOCABULARY_SIZE, Type.INT, 0);
            eGraph.setRoot(root);
        }
        this.egraph = eGraph;
    }


    @Override
    public EGraph eGraph() {
        return egraph;
    }

    @Override
    public int getOrCreate(String key) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        if (key.length() == 0) {
            throw new IllegalArgumentException("key is empty");
        }

        SearchResult searchResult = searchTree(key);
        ENode enode;
        CharSequence keyCharsFromStartOfNodeFound;
        CharSequence commonPrefix;
        CharSequence suffixFromExistingEdge;
        IntArray eChild;

        switch (searchResult.classification) {
            case EXACT_MATCH:
                if (!(boolean) searchResult.nodeFound.get(NODE_REAL_WORD)) {
                    searchResult.nodeFound.set(NODE_REAL_WORD, Type.BOOL, true);
                    incrementVocabularySize();
                }
                return searchResult.nodeFound.id();
            case KEY_ENDS_MID_EDGE:
                keyCharsFromStartOfNodeFound = key.subSequence(searchResult.charsMatched - searchResult.charsMatchedInNodeFound, key.length());
                commonPrefix = CharSequences.getCommonPrefix(keyCharsFromStartOfNodeFound, (String) searchResult.nodeFound.get(NODE_ADDITION));
                suffixFromExistingEdge = CharSequences.subtractPrefix((String) searchResult.nodeFound.get(NODE_ADDITION), commonPrefix);

                //New Intermediary Node
                enode = egraph.newNode();
                enode.set(NODE_ADDITION, Type.STRING, commonPrefix);
                eChild = (IntArray) enode.getOrCreate(NODE_CHILD, Type.INT_ARRAY);
                eChild.addElement(searchResult.nodeFound.id());


                int parent = searchResult.parentNode.id();
                enode.set(NODE_PARENT, Type.INT, parent);

                //former Parent
                IntArray children = (IntArray) searchResult.parentNode.get(NODE_CHILD);
                children.replaceElementby(searchResult.nodeFound.id(), enode.id());

                //Former Node
                searchResult.nodeFound.set(NODE_ADDITION, Type.STRING, suffixFromExistingEdge);
                searchResult.nodeFound.set(NODE_PARENT, Type.INT, enode.id());

                enode.set(NODE_REAL_WORD, Type.BOOL, true);
                incrementVocabularySize();
                return enode.id();

            case INCOMPLETE_MATCH_TO_END_OF_EDGE:
                CharSequence keySuffix = key.subSequence(searchResult.charsMatched, key.length());


                enode = egraph.newNode();
                enode.set(NODE_ADDITION, Type.STRING, keySuffix);
                enode.set(NODE_PARENT, Type.INT, searchResult.nodeFound.id());
                enode.getOrCreate(NODE_CHILD, Type.INT_ARRAY);

                eChild = (IntArray) searchResult.nodeFound.get(NODE_CHILD);
                eChild.addElement(enode.id());

                enode.set(NODE_REAL_WORD, Type.BOOL, true);
                incrementVocabularySize();
                return enode.id();

            case INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE:

                keyCharsFromStartOfNodeFound = key.subSequence(searchResult.charsMatched - searchResult.charsMatchedInNodeFound, key.length());
                commonPrefix = CharSequences.getCommonPrefix(keyCharsFromStartOfNodeFound, (String) searchResult.nodeFound.get(NODE_ADDITION));
                suffixFromExistingEdge = CharSequences.subtractPrefix((String) searchResult.nodeFound.get(NODE_ADDITION), commonPrefix);
                CharSequence suffixFromKey = key.subSequence(searchResult.charsMatched, key.length());

                ENode intermediaryEnode = egraph.newNode();
                intermediaryEnode.set(NODE_ADDITION, Type.STRING, commonPrefix);
                intermediaryEnode.set(NODE_REAL_WORD, Type.BOOL, false);

                enode = egraph.newNode();
                enode.set(NODE_ADDITION, Type.STRING, suffixFromKey);
                enode.set(NODE_REAL_WORD, Type.BOOL, true);
                enode.getOrCreate(NODE_CHILD, Type.INT_ARRAY);

                eChild = (IntArray) intermediaryEnode.getOrCreate(NODE_CHILD, Type.INT_ARRAY);
                eChild.addAll(new int[]{enode.id(), searchResult.nodeFound.id()});

                parent = searchResult.parentNode.id();
                intermediaryEnode.set(NODE_PARENT, Type.INT, parent);

                //former Parent
                children = (IntArray) searchResult.parentNode.get(NODE_CHILD);
                children.replaceElementby(searchResult.nodeFound.id(), intermediaryEnode.id());

                enode.set(NODE_PARENT, Type.INT, intermediaryEnode.id());
                searchResult.nodeFound.set(NODE_PARENT, Type.INT, intermediaryEnode.id());
                searchResult.nodeFound.set(NODE_ADDITION, Type.STRING, suffixFromExistingEdge);
                incrementVocabularySize();
                return enode.id();
            default:
                return -1;
        }
    }

    @Override
    public CharSequence[] getClosestWordsFrom(String candidate) {
        SearchResult searchResult = searchTree(candidate);
        switch (searchResult.classification) {
            case EXACT_MATCH: {
                return getDescendantKeys(candidate, searchResult.nodeFound);
            }
            case KEY_ENDS_MID_EDGE: {
                // Append the remaining characters of the edge to the key.
                // For example if we searched for CO, but first matching node was COFFEE,
                // the key associated with the first node should be COFFEE...
                CharSequence edgeSuffix = CharSequences.getSuffix((CharSequence) searchResult.nodeFound.get(NODE_ADDITION), searchResult.charsMatchedInNodeFound);
                CharSequence word = CharSequences.concatenate(candidate, edgeSuffix);
                return getDescendantKeys(word, searchResult.nodeFound);
            }
            case INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE: {
                // Example: if we searched for CX, but deepest matching node was CO,
                // the results should include node CO and its descendants...
                CharSequence keyOfParentNode = CharSequences.getPrefix(candidate, searchResult.charsMatched - searchResult.charsMatchedInNodeFound);
                CharSequence keyOfNodeFound = CharSequences.concatenate(keyOfParentNode, (CharSequence) searchResult.nodeFound.get(NODE_ADDITION));
                return getDescendantKeys(keyOfNodeFound, searchResult.nodeFound);
            }
            case INCOMPLETE_MATCH_TO_END_OF_EDGE: {
                if (searchResult.charsMatched == 0) {
                    // Closest match is the root node, we don't consider this a match for anything...
                    break;
                }
                // Example: if we searched for COFFEE, but deepest matching node was CO,
                // the results should include node CO and its descendants...
                CharSequence keyOfNodeFound = CharSequences.getPrefix(candidate, searchResult.charsMatched);
                return getDescendantKeys(keyOfNodeFound, searchResult.nodeFound);
            }
        }
        return new String[0];
    }

    @Override
    public int[] getClosestNodesFrom(String word) {
        SearchResult searchResult = searchTree(word);
        switch (searchResult.classification) {
            case EXACT_MATCH: {
                return getDescendantNodes(word, searchResult.nodeFound);
            }
            case KEY_ENDS_MID_EDGE: {
                // Append the remaining characters of the edge to the key.
                // For example if we searched for CO, but first matching node was COFFEE,
                // the key associated with the first node should be COFFEE...
                CharSequence edgeSuffix = CharSequences.getSuffix((CharSequence) searchResult.nodeFound.get(NODE_ADDITION), searchResult.charsMatchedInNodeFound);
                CharSequence candidate = CharSequences.concatenate(word, edgeSuffix);
                return getDescendantNodes(candidate, searchResult.nodeFound);
            }
            case INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE: {
                // Example: if we searched for CX, but deepest matching node was CO,
                // the results should include node CO and its descendants...
                CharSequence keyOfParentNode = CharSequences.getPrefix(word, searchResult.charsMatched - searchResult.charsMatchedInNodeFound);
                CharSequence keyOfNodeFound = CharSequences.concatenate(keyOfParentNode, (CharSequence) searchResult.nodeFound.get(NODE_ADDITION));
                return getDescendantNodes(keyOfNodeFound, searchResult.nodeFound);
            }
            case INCOMPLETE_MATCH_TO_END_OF_EDGE: {
                if (searchResult.charsMatched == 0) {
                    // Closest match is the root node, we don't consider this a match for anything...
                    break;
                }
                // Example: if we searched for COFFEE, but deepest matching node was CO,
                // the results should include node CO and its descendants...
                CharSequence keyOfNodeFound = CharSequences.getPrefix(word, searchResult.charsMatched);
                return getDescendantNodes(keyOfNodeFound, searchResult.nodeFound);
            }
        }
        return new int[0];
    }

    @Override
    public boolean removeWord(String word) {
        if (word == null) {
            throw new IllegalArgumentException("The key argument was null");
        }
        SearchResult searchResult = searchTree(word);
        SearchResult.Classification classification = searchResult.classification;
        switch (classification) {
            case EXACT_MATCH:
                if (!(boolean) searchResult.nodeFound.get(NODE_REAL_WORD)) {
                    return false;
                }
                IntArray children = ((IntArray) searchResult.nodeFound.get(NODE_CHILD));
                if (children.size() == 0) {
                    int id = searchResult.nodeFound.id();
                    ((IntArray) searchResult.parentNode.get(NODE_CHILD)).removeElement(id);
                    egraph.drop(searchResult.nodeFound);
                    return true;
                } else if (children.size() == 1) {
                    int id = searchResult.nodeFound.id();
                    ENode child = egraph.node(children.get(0));
                    IntArray parentChild = (IntArray) searchResult.parentNode.get(NODE_CHILD);
                    parentChild.removeElement(id);
                    parentChild.addElement(child.id());
                    child.set(NODE_PARENT, Type.INT, searchResult.parentNode.id());
                    egraph.drop(searchResult.nodeFound);
                    return true;
                } else {
                    return false;
                }
            default:
                return false;
        }

    }

    @Override
    public int size() {
        return (int) egraph.root().get(VOCABULARY_SIZE);
    }


    public CharSequence[] getKeysStartingWith(CharSequence prefix) {
        SearchResult searchResult = searchTree(prefix);
        switch (searchResult.classification) {
            case EXACT_MATCH:
                return getDescendantKeys(prefix, searchResult.nodeFound);
            case KEY_ENDS_MID_EDGE:
                CharSequence edgeSuffix = CharSequences.getSuffix((String) searchResult.nodeFound.get(NODE_ADDITION), searchResult.charsMatchedInNodeFound);
                prefix = CharSequences.concatenate(prefix, edgeSuffix);
                return getDescendantKeys(prefix, searchResult.nodeFound);
            default:
                return new String[0];
        }
    }


    public int[] getNodesWithKeyStartingWith(CharSequence prefix) {
        SearchResult searchResult = searchTree(prefix);
        switch (searchResult.classification) {
            case EXACT_MATCH:
                return getDescendantNodes(prefix, searchResult.nodeFound);
            case KEY_ENDS_MID_EDGE:
                CharSequence edgeSuffix = CharSequences.getSuffix((String) searchResult.nodeFound.get(NODE_ADDITION), searchResult.charsMatchedInNodeFound);
                prefix = CharSequences.concatenate(prefix, edgeSuffix);
                return getDescendantNodes(prefix, searchResult.nodeFound);
            default:
                return new int[0];
        }
    }

    private CharSequence[] getDescendantKeys(CharSequence prefix, ENode nodeFound) {
        List<NodeKeyPair> nodeKeyPairs = getDescendant(prefix, nodeFound);
        CharSequence[] words = new CharSequence[nodeKeyPairs.size()];
        for (int i = 0; i < nodeKeyPairs.size(); i++) {
            words[i] = nodeKeyPairs.get(i).key;
        }
        return words;
    }

    private int[] getDescendantNodes(CharSequence prefix, ENode nodeFound) {
        List<NodeKeyPair> nodeKeyPairs = getDescendant(prefix, nodeFound);
        int[] eNodes = new int[nodeKeyPairs.size()];
        for (int i = 0; i < nodeKeyPairs.size(); i++) {
            eNodes[i] = nodeKeyPairs.get(i).node.id();
        }
        return eNodes;
    }

    private List<NodeKeyPair> getDescendant(CharSequence prefix, ENode nodeFound) {
        IntArray children = (IntArray) nodeFound.get(NODE_CHILD);
        List<NodeKeyPair> nodeKeyPairs = new ArrayList<>();
        if ((boolean) nodeFound.get(NODE_REAL_WORD)) {
            nodeKeyPairs.add(new NodeKeyPair(nodeFound, prefix));
        }
        for (int i = 0; i < children.size(); i++) {
            ENode child = egraph.node(i);
            CharSequence newContent = CharSequences.concatenate(prefix, (CharSequence) child.get(NODE_ADDITION)).toString();
            nodeKeyPairs.addAll(getDescendant(newContent, child));
        }
        return nodeKeyPairs;

    }

    private void incrementVocabularySize() {
        ENode root = egraph.root();
        int size = (int) root.get(VOCABULARY_SIZE);
        root.set(VOCABULARY_SIZE, Type.INT, size + 1);
    }

    SearchResult searchTree(CharSequence key) {
        ENode parentNodesParent = null;
        ENode parentNode = null;
        ENode currentNode = egraph.root();
        int charsMatched = 0, charsMatchedInNodeFound = 0;

        final int keyLength = key.length();
        outer_loop:
        while (charsMatched < keyLength) {
            ENode nextNode = null;
            IntArray intArray = (IntArray) currentNode.get(NODE_CHILD);
            for (int i = 0; i < intArray.size(); i++) {
                ENode eNode = egraph.node(intArray.get(i));
                if (((String) eNode.get(NODE_ADDITION)).startsWith(String.valueOf(key.charAt(charsMatched)))) {
                    nextNode = eNode;
                    break;
                }
            }
            if (nextNode == null) {
                // Next node is a dead end...
                //noinspection UnnecessaryLabelOnBreakStatement
                break outer_loop;
            }

            parentNodesParent = parentNode;
            parentNode = currentNode;
            currentNode = nextNode;
            charsMatchedInNodeFound = 0;
            CharSequence currentNodeEdgeCharacters = (String) currentNode.get(NODE_ADDITION);
            for (int i = 0, numEdgeChars = currentNodeEdgeCharacters.length(); i < numEdgeChars && charsMatched < keyLength; i++) {
                if (currentNodeEdgeCharacters.charAt(i) != key.charAt(charsMatched)) {
                    // Found a difference in chars between character in key and a character in current node.
                    // Current node is the deepest match (inexact match)....
                    break outer_loop;
                }
                charsMatched++;
                charsMatchedInNodeFound++;
            }
        }
        return new SearchResult(key, currentNode, charsMatched, charsMatchedInNodeFound, parentNode, parentNodesParent);
    }

    public String getNameOfToken(int tokenId) {
        StringBuilder token = new StringBuilder();
        ENode eNode = egraph.node(tokenId);
        token.insert(0, (String) eNode.get(NODE_ADDITION));
        while (eNode.get(NODE_PARENT) != null) {
            eNode = egraph.node((Integer) eNode.get(NODE_PARENT));
            token.insert(0, (String) eNode.get(NODE_ADDITION));
        }
        return token.toString();
    }


    protected static class NodeKeyPair {
        public final ENode node;
        public final CharSequence key;

        public NodeKeyPair(ENode node, CharSequence key) {
            this.node = node;
            this.key = key;
        }
    }

    protected static String NODE_ADDITION = "incoming";
    private static String NODE_PARENT = "father";
    private static String NODE_CHILD = "children";
    private static String NODE_REAL_WORD = "real";
    private static String VOCABULARY_SIZE = "size";
}
