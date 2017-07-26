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

import greycat.Type;
import greycat.struct.EStructArray;
import greycat.utility.HashHelper;
import paw.graph.customTypes.bitset.fastbitset.CTFastBitSet;

import java.util.List;

@SuppressWarnings("Duplicates")
public class CTTCBitset extends CTFastBitSet {
    public static final String NAME = "FastBitSetEncoded";

    private static final String CURRENTSTOP = "cs";
    private static final int CURRENTSTOP_H = HashHelper.hash(CURRENTSTOP);

    private int currentStop;
    private boolean dirty = false;


    public CTTCBitset(EStructArray backend) {
        super(backend);
        Object result = root.getAt(CURRENTSTOP_H);
        if (result == null) {
            currentStop = 0;
        } else {
            currentStop = (int) result;
        }
    }

    @Override
    public void clear() {
        currentStop = 0;
        root.setAt(CURRENTSTOP_H, Type.INT, 0);
        super.clear();
        dirty = true;
    }

    @Override
    public void save() {
        if (dirty) {
            root.setAt(CURRENTSTOP_H, Type.INT, currentStop);
            super.save();
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void addWords(List<Word> word) {
        currentStop = CTTokenizeContent.addWord(word, this, currentStop);
        dirty = true;
    }

    public List<Word> decodeWords() {
        return CTTokenizeContent.decodeWords(this);
    }
}
