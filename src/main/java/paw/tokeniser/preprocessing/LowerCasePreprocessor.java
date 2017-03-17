package paw.tokeniser.preprocessing;

import paw.tokeniser.TokenPreprocessor;

/**
 * Lower case Token Preprocessor
 */
public class LowerCasePreprocessor implements TokenPreprocessor {

    public final static String ID = "LOWER CASE PREPROCESSOR";

    /**
     * @param token on which to apply the transformation
     * @return token in lower case
     */
    @Override
    public String transform(String token) {
        return token.toLowerCase();
    }

    @Override
    public String toString(){
        return ID;
    }
}
