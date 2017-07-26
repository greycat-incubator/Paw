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
package paw.graph.customTypes.tokenizedContent;

import org.roaringbitmap.IntIterator;
import paw.PawConstants;
import paw.graph.customTypes.bitset.CTBitset;

import java.util.ArrayList;
import java.util.List;

public class CTTokenizeContent {

    private static int[] SIZE_CODING_FC = new int[]{7, 8, 16, 32};
    private static int[] SIZE_CODING_CONTENT = new int[]{8, 16, 24, 32};

    private static int encodingSize(int toEncode, List<Integer> bitset, int currentStop, int[] encodingArraySize) {
        int minimumBitSize = 32 - Integer.numberOfLeadingZeros(toEncode);
        int iterationMax;
        //00
        if (minimumBitSize <= encodingArraySize[0]) {
            iterationMax = encodingArraySize[0];
        }
        //01
        else if (minimumBitSize <= encodingArraySize[1]) {
            bitset.add(currentStop + 1);
            iterationMax = encodingArraySize[1];
        }
        //10
        else if (minimumBitSize <= encodingArraySize[2]) {
            bitset.add(currentStop);
            iterationMax = encodingArraySize[2];
        }
        //11
        else {
            bitset.add(currentStop);
            bitset.add(currentStop + 1);
            iterationMax = encodingArraySize[3];
        }
        return iterationMax;
    }

    private static void encoding(int iterationMax, int toEncode, List<Integer> bitset, int currentStop) {
        int stop = currentStop;
        for (int i = iterationMax - 1; i >= 0; i--) {
            if ((toEncode & 1 << i) != 0) {
                bitset.add(stop);
            }
            stop++;
        }
    }

    private final static byte TYPE_ENCODING = 0;
    private final static byte FIRST_CHAR_SIZE_ENCODING = 1;
    private final static byte FIRST_CHAR_ENCODING = 2;
    private final static byte CONTENT_SIZE_ENCODING = 3;
    private final static byte CONTENT_ENCODING = 4;
    private final static byte END_OF_ENCODING = 5;

    public static int addWord(List<Word> words, CTBitset bitset, int currentStop) {
        int newStop = currentStop;
        List<Integer> toAdd = new ArrayList<>();
        for (Word word : words) {
            byte state = TYPE_ENCODING;
            int iterationMax = 0;
            while (state != END_OF_ENCODING) {
                switch (state) {
                    case (TYPE_ENCODING):
                        switch (word.type) {
                            case PawConstants.NUMBER_TOKEN:
                                toAdd.add(newStop + 1);
                            case PawConstants.DELIMITER_TOKEN:
                                toAdd.add(newStop);
                                newStop += 2;
                                state = CONTENT_SIZE_ENCODING;
                                break;
                            case PawConstants.CONTENT_TOKEN:
                                newStop++;
                                state = FIRST_CHAR_SIZE_ENCODING;
                                break;
                        }
                        break;
                    case (FIRST_CHAR_SIZE_ENCODING):
                        iterationMax = encodingSize(word.firstChar, toAdd, newStop, SIZE_CODING_FC);
                        newStop += 2;
                        state = FIRST_CHAR_ENCODING;
                        break;
                    case FIRST_CHAR_ENCODING:
                        encoding(iterationMax, word.firstChar, toAdd, newStop);
                        newStop += iterationMax;
                        state = CONTENT_SIZE_ENCODING;
                        break;

                    case (CONTENT_SIZE_ENCODING):
                        iterationMax = encodingSize(word.wordID, toAdd, newStop, SIZE_CODING_CONTENT);
                        newStop += 2;
                        state = CONTENT_ENCODING;
                        break;
                    case (CONTENT_ENCODING):
                        encoding(iterationMax, word.wordID, toAdd, newStop);
                        newStop += iterationMax;
                        state = END_OF_ENCODING;
                        break;
                }
            }
        }
        bitset.addAll(toAdd);
        return newStop;
    }


    private final static byte TYPE_DECODING = 0;
    private final static byte FIRST_CHAR_SIZE_DECODING = 1;
    private final static byte FIRST_CHAR_DECODING = 2;
    private final static byte CONTENT_SIZE_DECODING = 3;
    private final static byte CONTENT_DECODING = 4;

    private static int decodeSize(boolean b0, boolean b1, int[] decodeArray) {
        if (b1) {
            if (b0) {
                return decodeArray[3];
            } else {
                return decodeArray[2];
            }
        } else {
            if (b0) {
                return decodeArray[1];
            } else {
                return decodeArray[0];
            }
        }
    }


    public static List<Word> decodeWords(CTBitset bitset) throws IllegalArgumentException {
        List<Word> words = new ArrayList<>();
        IntIterator iterator = bitset.iterator();

        //read information
        int index = 0;
        int startWord = 0;
        byte state = TYPE_DECODING;
        int nextInterestingBit = startWord;


        // word variable
        byte type = 0;
        int wordId = 0;
        int firstChar = 0;


        //state variable
        boolean type_second_bit = false;
        int toRead = 0;

        boolean used = true;

        while (iterator.hasNext() || !used) {
            if (used) {
                index = iterator.next();
                used = false;
            }
            switch (state) {
                case (TYPE_DECODING):
                    if (!type_second_bit) {
                        nextInterestingBit = startWord;
                        if (index != startWord) {
                            type = PawConstants.CONTENT_TOKEN;
                            state = FIRST_CHAR_SIZE_DECODING;
                            nextInterestingBit += 2;
                        } else {
                            type_second_bit = true;
                            nextInterestingBit += 3;
                        }
                    } else {
                        state = CONTENT_SIZE_DECODING;
                        type_second_bit = false;
                        if (index == startWord + 1) {
                            type = PawConstants.NUMBER_TOKEN;
                            used = true;
                        } else {
                            type = PawConstants.DELIMITER_TOKEN;
                        }
                    }
                    break;

                case (FIRST_CHAR_SIZE_DECODING):
                    if (index > nextInterestingBit) {
                        boolean bitSize0 = bitset.get(nextInterestingBit);
                        boolean bitSize1 = bitset.get(nextInterestingBit - 1);
                        toRead = decodeSize(bitSize0, bitSize1, SIZE_CODING_FC);
                        nextInterestingBit += toRead;
                        state = FIRST_CHAR_DECODING;
                        firstChar = 0;
                    } else {
                        used = true;
                    }
                    break;

                case (FIRST_CHAR_DECODING):
                    if (index <= nextInterestingBit) {
                        firstChar += (1 << (nextInterestingBit - index));
                        used = true;
                    } else {
                        state = CONTENT_SIZE_DECODING;
                        nextInterestingBit += 2;
                    }
                    break;
                case (CONTENT_SIZE_DECODING):
                    if (index > nextInterestingBit) {
                        boolean bitSize0 = bitset.get(nextInterestingBit);
                        boolean bitSize1 = bitset.get(nextInterestingBit - 1);
                        toRead = decodeSize(bitSize0, bitSize1, SIZE_CODING_CONTENT);
                        nextInterestingBit += toRead;
                        state = CONTENT_DECODING;
                        wordId = 0;
                    } else {
                        used = true;
                    }
                    break;

                case (CONTENT_DECODING):
                    if (index <= nextInterestingBit) {
                        wordId += (1 << (nextInterestingBit - index));
                        used = true;
                    } else {
                        words.add(new Word(type, wordId, firstChar));

                        state = TYPE_DECODING;
                        startWord = nextInterestingBit + 1;
                    }
                    break;
            }
        }
        if (state != TYPE_ENCODING) {
            switch (state) {
                case (FIRST_CHAR_SIZE_DECODING):
                    firstChar = 0;
                case (FIRST_CHAR_DECODING):
                case (CONTENT_SIZE_DECODING):
                    wordId = 0;
                case (CONTENT_DECODING):
                    words.add(new Word(type, wordId, firstChar));
                    break;
            }
        }
        return words;
    }
}
