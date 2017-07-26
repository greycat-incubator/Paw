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
package paw.graph.customTypes.bitset.roaring;

import greycat.Type;
import greycat.struct.EStruct;
import greycat.struct.EStructArray;
import greycat.utility.HashHelper;
import org.roaringbitmap.IntIterator;
import org.roaringbitmap.PeekableIntIterator;
import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.buffer.ImmutableRoaringBitmap;
import paw.graph.customTypes.bitset.CTBitset;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Base64;

public class CTRoaringBitMap extends CTBitset {

    private static final String BITS = "bits";
    private static final int BITS_H = HashHelper.hash(BITS);
    public static final String NAME = "RoaringBitMap";
    private String gBits;
    protected RoaringBitmap bitmap;
    protected EStruct root;

    public CTRoaringBitMap(EStructArray backend) {
        super(backend);
        root = backend.root();
        if (root == null) {
            root = backend.newEStruct();
            backend.setRoot(root);
        }
        gBits = (String) root.getAt(BITS_H);
        if (gBits != null) {
            ByteBuffer newbb = ByteBuffer.wrap(Base64.getDecoder().decode(gBits));
            bitmap = new ImmutableRoaringBitmap(newbb).toRoaringBitmap();
        } else {
            bitmap = new RoaringBitmap();
        }
    }

    public void save() {
        bitmap.runOptimize();
        ByteBuffer outbb = ByteBuffer.allocate(bitmap.serializedSizeInBytes());
        try {
            bitmap.serialize(new DataOutputStream(new OutputStream() {
                ByteBuffer mBB;

                OutputStream init(ByteBuffer mbb) {
                    mBB = mbb;
                    return this;
                }

                public void close() {
                }

                public void flush() {
                }

                public void write(int b) {
                    mBB.put((byte) b);
                }

                public void write(byte[] b) {
                    mBB.put(b);
                }

                public void write(byte[] b, int off, int l) {
                    mBB.put(b, off, l);
                }
            }.init(outbb)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //
        outbb.flip();
        String serializedstring = Base64.getEncoder().encodeToString(outbb.array());
        root.setAt(BITS_H, Type.STRING, serializedstring);
    }

    public RoaringBitmap getBitMap() {
        return bitmap;
    }

    public void setBitmap(RoaringBitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void clear() {
        root.setAt(BITS_H, Type.STRING, "");
        bitmap.clear();
    }

    @Override
    public boolean add(int index) {
        return bitmap.checkedAdd(index);
    }

    @Override
    public void clear(int index) {
        bitmap.checkedRemove(index);
    }

    @Override
    public int size() {
        return bitmap.last() + 1;
    }

    @Override
    public int cardinality() {
        return bitmap.getCardinality();
    }

    @Override
    public boolean get(int index) {
        return bitmap.contains(index);
    }

    @Override
    public int nextSetBit(int startIndex) {
        PeekableIntIterator iterator = bitmap.getIntIterator();
        iterator.advanceIfNeeded(startIndex);
        return iterator.peekNext();
    }

    @Override
    public IntIterator iterator() {
        return bitmap.getIntIterator();
    }
}
