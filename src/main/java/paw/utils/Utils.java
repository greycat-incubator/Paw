package paw.utils;

public class Utils {
    public static boolean isNumericArray(String str) {
        if (str == null)
            return false;
        char[] data = str.toCharArray();
        if (data.length <= 0)
            return false;
        int index = 0;
        if (data[0] == '-' && data.length > 1)
            index = 1;
        for (; index < data.length; index++) {
            if (data[index] < '0' || data[index] > '9') // Character.isDigit() can go here too.
                return false;
        }
        return true;
    }


}
