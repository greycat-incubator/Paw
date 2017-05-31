package paw.tokenizer.token;

import paw.PawConstants;

public class NumberT implements Token {

    private final long integer;

    public NumberT(long integer) {
        this.integer = integer;
    }

    @Override
    public String getToken() {
        return String.valueOf(integer);
    }

    @Override
    public byte getType() {
        return PawConstants.NUMBER_TOKEN;
    }

    public long getInteger() {
        return integer;
    }
}
