package paw.tokeniser;

/**
 * Token Preprocessor interface
 */
public interface TokenPreprocessor {
    /**
     * transform method
     * @param token on which to apply the transformation
     * @return the transformed String
     */
    String transform(String token);
}