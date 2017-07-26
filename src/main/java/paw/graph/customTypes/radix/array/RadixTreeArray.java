package paw.graph.customTypes.radix.array;

import greycat.Type;
import greycat.base.BaseCustomType;
import greycat.struct.*;
import paw.graph.customTypes.radix.CharSequences;

import java.util.ArrayList;
import java.util.List;


public class RadixTreeArray extends BaseCustomType {
    private EStruct tree;
    private final LongLongArrayMap childs;
    private final StringArray addition;
    private final IntArray realWord;
    private final IntArray fathers;
    private int size;
    public static final String NAME = "RadixTreeArray" ;


    public RadixTreeArray(EStructArray backend) {
        super(backend);
        tree = backend.root();
        if (tree == null) {
            tree = backend.newEStruct();
            backend.setRoot(tree);
        }
        addition = (StringArray) tree.getOrCreate("addition", Type.STRING_ARRAY);
        childs = (LongLongArrayMap) tree.getOrCreate("child", Type.LONG_TO_LONG_ARRAY_MAP);
        fathers = (IntArray) tree.getOrCreate("father", Type.INT_ARRAY);
        realWord = (IntArray) tree.getOrCreate("real", Type.INT_ARRAY);
        size = tree.getWithDefault("size", 0);
    }

    public int getIndexOfKey(String key) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        if (key.length() == 0) {
            throw new IllegalArgumentException("key is empty");
        }

        SearchResult searchResult = searchTree(key);

        switch (searchResult.classification) {
            case EXACT_MATCH:
                if (realWord.get(searchResult.nodeFound) == 1) {
                    return searchResult.nodeFound;
                }
                return -1;
            default:
                return -1;
        }
    }

    public int getOrCreate(String key) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        if (key.length() == 0) {
            throw new IllegalArgumentException("key is empty");
        }

        SearchResult searchResult = searchTree(key);

        CharSequence keyCharsFromStartOfNodeFound;
        CharSequence commonPrefix;
        CharSequence suffixFromExistingEdge;

        switch (searchResult.classification) {
            case EXACT_MATCH:
                if (realWord.get(searchResult.nodeFound) != 1) {
                    realWord.set(searchResult.nodeFound, 1);
                    tree.set("size", Type.INT, size + 1);
                    size++;
                }
                return searchResult.nodeFound;
            case KEY_ENDS_MID_EDGE:
                keyCharsFromStartOfNodeFound = key.subSequence(searchResult.charsMatched - searchResult.charsMatchedInNodeFound, key.length());
                commonPrefix = CharSequences.getCommonPrefix(keyCharsFromStartOfNodeFound, addition.get(searchResult.nodeFound));
                suffixFromExistingEdge = CharSequences.subtractPrefix(addition.get(searchResult.nodeFound), commonPrefix);

                //New Intermediary Node
                int newNode = addition.size();
                addition.addElement((String) commonPrefix);
                childs.put(newNode, searchResult.nodeFound);
                int parent = searchResult.parentNode;
                fathers.addElement(parent);


                //former Parent
                childs.delete(searchResult.parentNode, searchResult.nodeFound);
                childs.put(searchResult.parentNode, newNode);

                //Former Node
                addition.set(searchResult.nodeFound, (String) suffixFromExistingEdge);
                fathers.set(searchResult.nodeFound, newNode);

                realWord.addElement(1);
                tree.set("size", Type.INT, size + 1);
                size++;
                return newNode;

            case INCOMPLETE_MATCH_TO_END_OF_EDGE:
                CharSequence keySuffix = key.subSequence(searchResult.charsMatched, key.length());

                newNode = addition.size();
                addition.addElement((String) keySuffix);
                fathers.addElement(searchResult.nodeFound);
                realWord.addElement(1);

                childs.put(searchResult.nodeFound, newNode);

                tree.set("size", Type.INT, size + 1);
                size++;
                return newNode;

            case INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE:

                keyCharsFromStartOfNodeFound = key.subSequence(searchResult.charsMatched - searchResult.charsMatchedInNodeFound, key.length());
                commonPrefix = CharSequences.getCommonPrefix(keyCharsFromStartOfNodeFound, addition.get(searchResult.nodeFound));
                suffixFromExistingEdge = CharSequences.subtractPrefix(addition.get(searchResult.nodeFound), commonPrefix);
                CharSequence suffixFromKey = key.subSequence(searchResult.charsMatched, key.length());

                int intermediaryNode = addition.size();
                newNode = addition.size() + 1;
                addition.addAll(new String[]{(String) commonPrefix, (String) suffixFromKey});
                realWord.addAll(new int[]{0, 1});

                childs.put(intermediaryNode, searchResult.nodeFound);
                childs.put(intermediaryNode, newNode);


                fathers.addAll(new int[]{searchResult.parentNode, intermediaryNode});

                childs.delete(searchResult.parentNode, searchResult.nodeFound);
                childs.put(searchResult.parentNode, intermediaryNode);

                fathers.set(searchResult.nodeFound, intermediaryNode);
                addition.set(searchResult.nodeFound, (String) suffixFromExistingEdge);

                tree.set("size", Type.INT, size + 1);
                size++;
                return newNode;
            default:
                return -1;
        }
    }


    SearchResult searchTree(CharSequence key) {
        int parentNodesParent = -1;
        int parentNode = -1;
        int currentNode = -1;
        int charsMatched = 0, charsMatchedInNodeFound = 0;

        final int keyLength = key.length();
        outer_loop:
        while (charsMatched < keyLength) {
            int nextNode = -1;
            long[] child = childs.get(currentNode);
            for (int i = 0; i < child.length; i++) {
                int studiedNode = (int) child[i];
                if (addition.get(studiedNode).startsWith(String.valueOf(key.charAt(charsMatched)))) {
                    nextNode = studiedNode;
                    break;
                }
            }
            if (nextNode == -1) {
                // Next node is a dead end...
                //noinspection UnnecessaryLabelOnBreakStatement
                break outer_loop;
            }
            parentNodesParent = parentNode;
            parentNode = currentNode;
            currentNode = nextNode;
            charsMatchedInNodeFound = 0;
            CharSequence currentNodeEdgeCharacters = addition.get(currentNode);
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
        return new SearchResult(addition.extract(), key, currentNode, charsMatched, charsMatchedInNodeFound, parentNode, parentNodesParent);
    }

    public String getNameOfToken(int tokenId) {
        StringBuilder token = new StringBuilder();
        token.insert(0, addition.get(tokenId));
        int id = fathers.get(tokenId);
        while (id != -1) {
            token.insert(0, addition.get(id));
            id = fathers.get(id);
        }
        return token.toString();
    }


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
                CharSequence edgeSuffix = CharSequences.getSuffix(addition.get(searchResult.nodeFound), searchResult.charsMatchedInNodeFound);
                CharSequence word = CharSequences.concatenate(candidate, edgeSuffix);
                return getDescendantKeys(word, searchResult.nodeFound);
            }
            case INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE: {
                // Example: if we searched for CX, but deepest matching node was CO,
                // the results should include node CO and its descendants...
                CharSequence keyOfParentNode = CharSequences.getPrefix(candidate, searchResult.charsMatched - searchResult.charsMatchedInNodeFound);
                CharSequence keyOfNodeFound = CharSequences.concatenate(keyOfParentNode, addition.get(searchResult.nodeFound));
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
                CharSequence edgeSuffix = CharSequences.getSuffix(addition.get(searchResult.nodeFound), searchResult.charsMatchedInNodeFound);
                CharSequence candidate = CharSequences.concatenate(word, edgeSuffix);
                return getDescendantNodes(candidate, searchResult.nodeFound);
            }
            case INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE: {
                // Example: if we searched for CX, but deepest matching node was CO,
                // the results should include node CO and its descendants...
                CharSequence keyOfParentNode = CharSequences.getPrefix(word, searchResult.charsMatched - searchResult.charsMatchedInNodeFound);
                CharSequence keyOfNodeFound = CharSequences.concatenate(keyOfParentNode, addition.get(searchResult.nodeFound));
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


    public boolean removeWord(String word) {
        if (word == null) {
            throw new IllegalArgumentException("The key argument was null");
        }
        SearchResult searchResult = searchTree(word);
        SearchResult.Classification classification = searchResult.classification;
        switch (classification) {
            case EXACT_MATCH:
                if (realWord.get(searchResult.nodeFound) == 1) {
                    realWord.set(searchResult.nodeFound, 0);
                } else {
                    return false;
                }
            default:
                return false;
        }
    }

    public int size() {
        return size;
    }


    public CharSequence[] getKeysStartingWith(CharSequence prefix) {
        SearchResult searchResult = searchTree(prefix);
        switch (searchResult.classification) {
            case EXACT_MATCH:
                return getDescendantKeys(prefix, searchResult.nodeFound);
            case KEY_ENDS_MID_EDGE:
                CharSequence edgeSuffix = CharSequences.getSuffix(addition.get(searchResult.nodeFound), searchResult.charsMatchedInNodeFound);
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
                CharSequence edgeSuffix = CharSequences.getSuffix(addition.get(searchResult.nodeFound), searchResult.charsMatchedInNodeFound);
                prefix = CharSequences.concatenate(prefix, edgeSuffix);
                return getDescendantNodes(prefix, searchResult.nodeFound);
            default:
                return new int[0];
        }
    }

    private CharSequence[] getDescendantKeys(CharSequence prefix, int nodeFound) {
        List<NodeKeyPair> nodeKeyPairs = getDescendant(prefix, nodeFound);
        CharSequence[] words = new CharSequence[nodeKeyPairs.size()];
        for (int i = 0; i < nodeKeyPairs.size(); i++) {
            words[i] = nodeKeyPairs.get(i).key;
        }
        return words;
    }

    private int[] getDescendantNodes(CharSequence prefix, int nodeFound) {
        List<NodeKeyPair> nodeKeyPairs = getDescendant(prefix, nodeFound);
        int[] eNodes = new int[nodeKeyPairs.size()];
        for (int i = 0; i < nodeKeyPairs.size(); i++) {
            eNodes[i] = nodeKeyPairs.get(i).node;
        }
        return eNodes;
    }

    private List<NodeKeyPair> getDescendant(CharSequence prefix, int nodeFound) {
        long[] children = childs.get(nodeFound);

        List<NodeKeyPair> nodeKeyPairs = new ArrayList<>();
        if (realWord.get(nodeFound) == 1) {
            nodeKeyPairs.add(new NodeKeyPair(nodeFound, prefix));
        }
        for (int i = 0; i < children.length; i++) {
            int child = (int) children[i];
            CharSequence newContent = CharSequences.concatenate(prefix, addition.get(child));
            nodeKeyPairs.addAll(getDescendant(newContent, child));
        }
        return nodeKeyPairs;

    }

    protected static class NodeKeyPair {
        public final int node;
        public final CharSequence key;

        public NodeKeyPair(int node, CharSequence key) {
            this.node = node;
            this.key = key;
        }
    }

}
