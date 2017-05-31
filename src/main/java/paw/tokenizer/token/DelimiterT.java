package paw.tokenizer.token;

import paw.PawConstants;

public class DelimiterT implements Token{


    private final String delimiter;

    public DelimiterT(String delimiter){
        this.delimiter = delimiter;
    }

    @Override
    public String getToken() {
        return delimiter;
    }

    @Override
    public byte getType() {
        return PawConstants.DELIMITER_TOKEN;
    }
}
