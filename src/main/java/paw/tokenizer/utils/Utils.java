package paw.tokenizer.utils;

import paw.PawConstants;
import paw.tokenizer.token.Token;

import java.util.List;

public class Utils {

    public String rebuild(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        byte previous = PawConstants.DELIMITER_TOKEN;
        for (int i = 0; i < tokens.size(); i++) {
            Token tok = tokens.get(i);
            byte type = tok.getType();
            if (type != PawConstants.DELIMITER_TOKEN && previous != PawConstants.DELIMITER_TOKEN) {
                sb.append("");
            }
            sb.append(tok.getToken());
            previous = type;
        }
        return sb.toString();
    }

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
        //size of long (javascript long are 53bits)
        return str.length() < 16;
    }
}
