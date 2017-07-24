package paw.greycatold;

import static paw.greycatold.VocabularyAccess.accessVocabulary;

/**
public class TokenizedContent {


    private final Graph graph;
    private final long currentWorld;
    private final long currentTime;
    private final LongLongArrayMap index;
    private final Node tokenizeContentList;
    private final Node delimiterVocab;
    private Node father = null;
    private Node tokenizeContent = null;
    private int tcNumber = -1;


    private TokenizedContent(Graph graph, long currentWorld, long currentTime, Node index, Node tokenizeContentList, Node delimiterVocab) {
        this.graph = graph;
        this.currentWorld = currentWorld;
        this.currentTime = currentTime;
        this.index = index.getLongLongArrayMap("map");
        this.tokenizeContentList = tokenizeContentList;
        this.delimiterVocab = delimiterVocab;
    }

    private void free() {
        tokenizeContent.free();
        father = null;
        tokenizeContent = null;
    }

    private void synchronizeFatherInTime(TaskContext ctx, long currentTime, String fatherNodeVar) {
        Node fatherNode = (Node) ctx.variable(fatherNodeVar).get(0);
        //Update the father time if necessary
        if (fatherNode.time() != currentTime) {
            if (fatherNode.time() > currentTime) {
                ctx.endTask(ctx.result(), new RuntimeException("node is in the future"));
            } else {
                fatherNode.travelInTime(currentTime, result -> {
                    fatherNode.free();
                    father = result;
                    ctx.setVariable(fatherNodeVar, result);
                });
            }
        } else {
            father = fatherNode;
        }
    }

    private void getOrCreateTokenizeContentNode(String nameOfContent) {
        //check wether the content exist
        long[] result = ((RelationIndexed) father.getOrCreate(RELATION_INDEXED_NODE_TO_TOKENIZECONTENT, Type.RELATION_INDEXED)).select(NODE_NAME, nameOfContent);
        //If not creating it
        if (result.length == 0) {
            createTokenizeContentNode(nameOfContent);
            addTCNumber();
        } else {
            //otherwise retrieve it and prepare it for the new version
            graph.lookup(currentWorld, currentTime, result[0], tc -> {
                tokenizeContent = tc;
                lookForTcNumber();
            });
        }
    }

    private void lookForTcNumber() {
        LongArray tcIDS = tokenizeContentList.getLongArray("listID");
        for (int i = tcIDS.size() - 1; i >= 0; i--) {
            Node[] newTCHandle = new Node[1];
            graph.lookup(0, BEGINNING_OF_TIME, tcIDS.get(i), result -> newTCHandle[0] = result);
            LongSet ls = newTCHandle[0].getLongSet("tcs");
            int result = ls.index(tokenizeContent.id());
            if (result != -1) {
                tcNumber = i * 32768 + result;
                newTCHandle[0].free();
                break;
            }
            newTCHandle[0].free();
        }
    }

    private void addTCNumber() {
        int current = (int) tokenizeContentList.get("current");
        LongArray tcIDS = tokenizeContentList.getLongArray("listID");
        Node[] newTCHandle = new Node[1];
        if (tcIDS.size() == 0 || tcIDS.size() == current) {
            newTCHandle[0] = graph.newNode(0, BEGINNING_OF_TIME);
            newTCHandle[0].setTimeSensitivity(-1, 0);
            newTCHandle[0].getOrCreate("tcs", Type.LONG_SET);
            tcIDS.addElement(newTCHandle[0].id());
        } else {
            graph.lookup(0, BEGINNING_OF_TIME, tcIDS.get(current), result -> newTCHandle[0] = result);
        }
        LongSet ls = newTCHandle[0].getLongSet("tcs");
        tcNumber = current * 32768 + ls.size();
        ls.put(tokenizeContent.id());
        if (ls.size() == 32768) {
            tokenizeContentList.set("current", Type.INT, current + 1);
        }
        newTCHandle[0].free();

    }

    private void createTokenizeContentNode(String nameOfContent) {
        Node tc = graph.newNode(currentWorld, currentTime);
        tc.set(NODE_NAME, Type.STRING, nameOfContent);
        tc.set(NODE_TYPE, Type.INT, TOKENIZED_CONTENT);
        tc.getOrCreate(TOKENIZED_CONTENT_HASH, Type.INT_ARRAY);
        tc.getOrCreate(TOKENIZED_CONTENT_SUB_ID, Type.LONG_ARRAY);
        tc.addToRelation(RELATION_TOKENIZECONTENT_TO_NODE, father);
        father.addToRelation(RELATION_INDEXED_NODE_TO_TOKENIZECONTENT, tc, NODE_NAME);
        tokenizeContent = tc;
    }

    private void addTokens(List<Token> tokens) {
        int size = tokens.size();
        tokenizeContent.getLongArray(TOKENIZED_CONTENT_SUB_ID).init(size);
        tokenizeContent.getIntArray(TOKENIZED_CONTENT_HASH).init(size);
        tokenizeContent.remove(TOKENIZED_CONTENT_MASKS);
        int delimiters = 0;
        int numbers = 0;
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.getType() == NUMBER_TOKEN) {
                numbers++;
                tokenizeContent.getLongArray(TOKENIZED_CONTENT_SUB_ID).set(i, NUMBER_TOKEN);
                tokenizeContent.getIntArray(TOKENIZED_CONTENT_HASH).set(i, ((NumberT) token).getInt());
            } else {
                if (token.getType() == DELIMITER_TOKEN) {
                    delimiters++;
                    tokenizeContent.getLongArray(TOKENIZED_CONTENT_SUB_ID).set(i, DELIMITER_TOKEN);
                }
                TokenHandler th = new TokenHandler(i, token);
                th.addTokenToTokenizeContent();
                th.free();
            }
        }
        tokenizeContent.set(TOKENIZED_CONTENT_DELN, Type.INT, delimiters);
        tokenizeContent.set(TOKENIZED_CONTENT_NUMBER, Type.INT, numbers);
    }

    class TokenHandler {
        private final int inc;
        private final Token tokenObject;
        private final String token;
        private final byte type;
        private int hashToLookFor;
        private String subToken = null;
        private Node radix = null;
        private Node ii = null;
        private Node map = null;


        private TokenHandler(int inc, Token tokenO) {
            this.inc = inc;
            this.token = tokenO.getToken();
            this.type = tokenO.getType();
            this.tokenObject = tokenO;
        }

        private void addTokenToTokenizeContent() {
            this.hashToLookFor = HashHelper.hash(token);
            tokenizeContent.getIntArray(TOKENIZED_CONTENT_HASH).set(inc, hashToLookFor);
            if (type == DELIMITER_TOKEN) {
                delimiterVocab.getIntStringMap(DELIMITER_VOCABULARY).put(hashToLookFor, token);
            } else {
                LongLongArrayMap masks = (LongLongArrayMap) tokenizeContent.getOrCreate(TOKENIZED_CONTENT_MASKS, Type.LONG_TO_LONG_ARRAY_MAP);
                int[] mask = ((ContentT) tokenObject).getLowerString().getMask();
                if (!(mask.length == 1 && mask[0] == 0)) {
                    for (int i = mask.length - 1; i >= 0; i--) {
                        masks.putNoCheck(inc, mask[i]);
                    }
                }
                addIndexableTokenToTokenizeContent();
            }
        }

        private void free() {
            if (map != null) {
                map.free();
                map = null;
            }
            if (radix != null) {
                radix.free();
                radix = null;
            }
            if (ii != null) {
                ii.free();
                ii = null;
            }
        }

        private void addIndexableTokenToTokenizeContent() {
            subToken = (token.length() > 2) ? token.substring(0, 3) : "0";

            int subHash = HashHelper.hash(subToken);
            long[] res = index.get(subHash);

            if (res.length == 0) {
                createSubIndexNode(subHash);
            } else {
                graph.lookupAll(0, BEGINNING_OF_TIME, res, result ->
                        {
                            this.map = result[2];
                            this.radix = result[1];
                            this.ii = result[0];
                        }

                );
            }
            tokenizeContent.getLongArray(TOKENIZED_CONTENT_SUB_ID).set(inc, subHash);


            IntIntMap hashId = (IntIntMap) map.getOrCreate(SUB_INDEX_HASH_ID, Type.INT_TO_INT_MAP);
            int id = hashId.get(hashToLookFor);
            if (id == Constants.NULL_INT) {
                id = createToken();
                hashId.put(hashToLookFor, id);
            }

            LongLongMap iiIndex = ii.getLongLongMap("iiIndex");
            long nodeId = iiIndex.get(id);
            Node[] subII = new Node[1];
            if (nodeId == Constants.NULL_LONG) {
                Node subii = graph.newNode(0, BEGINNING_OF_TIME);
                iiIndex.put(id, subii.id());
                subii.getOrCreate("presence", Type.LONG_TO_LONG_MAP);
                subii.setTimeSensitivity(-1, 0);
                subII[0] = subii;
            } else {
                graph.lookup(0, BEGINNING_OF_TIME, nodeId, result -> subII[0] = result);
            }

            LongLongMap llmap = subII[0].getLongLongMap("presence");
            int block_10000 = tcNumber / 10000;
            int position_10000 = tcNumber % 10000;
            long currentNode = llmap.get(block_10000);
            if (currentNode == Constants.NULL_LONG) {
                Node node = graph.newNode(0, BEGINNING_OF_TIME);
                node.setTimeSensitivity(-1,0);
                llmap.put(block_10000, node.id());
                LongArray la = (LongArray) node.getOrCreate("positions", Type.LONG_ARRAY);
                la.init(200);
                subII[0].free();
                subII[0] = node;
            } else {
                graph.lookup(0, BEGINNING_OF_TIME, currentNode, result -> {
                    subII[0].free();
                    subII[0] = result;
                });
            }


            LongArray la = subII[0].getLongArray("positions");
            int block_50 = position_10000 / 50;
            int position50 = position_10000 % 50;

            long currentValue = la.get(block_50);

            if (currentValue == Constants.NULL_LONG) {
                long toPut = 1 << position50;
                la.set(block_50, toPut);
            } else {
                if ((currentValue & (1 << position50)) == 0) {
                    long toPut = currentValue + (1 << position50);
                    la.set(block_50, toPut);
                }
            }
            subII[0].free();
        }


        private int createToken() {
            RadixTreeArray rtArray = new RadixTreeArray(radix);
            return rtArray.getOrCreate(token);
        }


        private void createSubIndexNode(int hash) {
            Node map = graph.newNode(0, BEGINNING_OF_TIME);
            map.set(NODE_NAME, Type.STRING, subToken);
            map.getOrCreate(SUB_INDEX_HASH_ID, Type.INT_TO_INT_MAP);
            map.setTimeSensitivity(-1, 0);
            index.put(hash, map.id());
            this.map = map;


            Node radix = graph.newNode(0, BEGINNING_OF_TIME);
            radix.setTimeSensitivity(-1, 0);
            index.put(hash, radix.id());
            this.radix = radix;

            Node ii = graph.newNode(0, BEGINNING_OF_TIME);
            ii.setTimeSensitivity(-1, 0);
            ii.getOrCreate("iiIndex", Type.LONG_TO_LONG_MAP);
            index.put(hash, ii.id());
            this.ii = ii;
        }

    }

    public static Task updateOrCreateTokenizeContent(List<Token> tokens, String nameOfContent, String fatherNodeVar) {
        return newTask()
                .ifThen(ctx -> ctx.variable("vocabNodes") == null,
                        accessVocabulary()
                                //Retrieving for both their cache and index node
                                .traverse(RELATION_VOCAB_CACHE_INDEX_DEL)
                                .defineAsGlobalVar("vocabNodes")
                )
                .thenDo(ctx -> {
                    long currentWorld = ctx.world();
                    long currentTime = ctx.time();
                    Graph graph = ctx.graph();
                    Node tokenizeContentList = (Node) ctx.variable("vocabNodes").get(0);
                    Node index = (Node) ctx.variable("vocabNodes").get(1);
                    Node vocabdel = (Node) ctx.variable("vocabNodes").get(2);

                    TokenizedContent tc = new TokenizedContent(graph, currentWorld, currentTime, index, tokenizeContentList, vocabdel);

                    //Retrieving the father node
                    tc.synchronizeFatherInTime(ctx, currentTime, fatherNodeVar);

                    //Get the indexed relation of the father containing all of its tokenize content
                    tc.getOrCreateTokenizeContentNode(nameOfContent);

                    tc.addTokens(tokens);
                    tc.free();
                    //Add each token to the tokenized content node
                    ctx.continueTask();
                });
    }


}
*/