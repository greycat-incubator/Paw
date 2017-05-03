package paw.utils;

import me.lemire.integercompression.FastPFOR;
import me.lemire.integercompression.IntCompressor;
import me.lemire.integercompression.SkippableComposition;
import me.lemire.integercompression.VariableByte;

public class IntArrayHandler {

    public static int[] removeElement(int[] compressedArray, int value) {

        if (compressedArray == null || compressedArray.length == 0) {
            return new int[0];
        }
        int[] data = iic.uncompress(compressedArray);
        int index = -1;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == value) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            int[] newData = new int[data.length - 1];
            System.arraycopy(data, 0, newData, 0, index);
            System.arraycopy(data, index + 1, newData, index, newData.length - index - 1);
            return iic.compress(newData);
        } else {
            return compressedArray;
        }
    }


    public static int[] addElement(int[] compressedArray, int value) {
        int[] newData;
        if (compressedArray == null || compressedArray.length == 0) {
            newData = new int[]{value};
        } else {
            int[] data = iic.uncompress(compressedArray);
            newData = new int[data.length + 1];
            System.arraycopy(data, 0, newData, 0, data.length);
            newData[data.length] = value;
        }
        return iic.compress(newData);
    }

    public static int[] insertElementAt(int[] compressedArray, int position, int value) {
        int[] newData;
        if (compressedArray == null || compressedArray.length == 0) {
            if (position == 0) {
                newData = new int[]{value};
            } else return new int[0];
        } else {
            int[] data = iic.uncompress(compressedArray);
            if (position < 0 || position >= data.length) {
                return compressedArray;
            } else {
                newData = new int[data.length + 1];
                System.arraycopy(data, 0, newData, 0, position);
                newData[position] = value;
                System.arraycopy(data, position, newData, position + 1, data.length - position);
            }
        }
        return iic.compress(newData);
    }

    public static int[] replaceElementby(int[] compressedArray, int element, int value) {
        if (compressedArray == null) {
            return new int[0];
        }
        int[] data = iic.uncompress(compressedArray);
        int index = -1;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == value) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            data[index] = value;
            return iic.compress(data);
        } else {
            return compressedArray;
        }
    }

    public static int[] addAll(int[] compressedArray, int[] values) {
        int[] newData;
        if (values == null) {
            return compressedArray;
        }
        if (compressedArray == null || compressedArray.length == 0) {
            newData = values;
        } else {
            int[] data = iic.uncompress(compressedArray);
            newData = new int[data.length + values.length];
            System.arraycopy(data, 0, newData, 0, data.length);
            System.arraycopy(values, 0, newData, data.length, values.length);
        }
        return iic.compress(newData);
    }

    public static int[] uncompress(int[] compressedArray) {
        if (compressedArray == null || compressedArray.length == 0) {
            return new int[0];
        }
        return iic.uncompress(compressedArray);
    }

    public static int[] compress(int[] uncompressedArray) {
        return iic.compress(uncompressedArray);
    }


    static IntCompressor iic = new IntCompressor(new SkippableComposition(new FastPFOR(), new VariableByte()));
}
