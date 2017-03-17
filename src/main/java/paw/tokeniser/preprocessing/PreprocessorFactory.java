package paw.tokeniser.preprocessing;

import paw.tokeniser.TokenPreprocessor;

public class PreprocessorFactory {

    /**
     * Method to retrieve a preprocessor instance from its byte representation
     * @param preprocessorType byte representation of the preprocessor type
     * @return the token preprocessor
     */
    public static TokenPreprocessor getPreprocessor(byte preprocessorType) {
        switch (preprocessorType) {
            case PreprocessorType.UPPER_CASE:
                return new UpperCasePreprocessor();
            case PreprocessorType.LOWER_CASE:
                return new LowerCasePreprocessor();
            default:
                return null;
        }
    }
}
