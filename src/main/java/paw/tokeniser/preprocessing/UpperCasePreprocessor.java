package paw.tokeniser.preprocessing;

import paw.tokeniser.TokenPreprocessor;

/**
 * Upper Case Token Preprocessor class
 */
public class UpperCasePreprocessor implements TokenPreprocessor {

    public final static String ID = "UPPER CASE PREPROCESSOR";

    /**
     * @param token on which to apply the transformation
     * @return token in upper case
     */
    @Override
    public String transform(String token) {
        return token.toUpperCase();
    }

    @Override
    public String toString(){
        return ID;
    }
}
