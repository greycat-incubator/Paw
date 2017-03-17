package paw.tokeniser.tokenisation;

import paw.tokeniser.Tokenizer;

public class TokenizerFactory {

    /**
      *Method to retrieve a tokenizer instance from its byte representation
     * @param tokenizerType byte representation of the tokenizer type
     * @return the tokenizer
     */
    public static Tokenizer getTokenizer(byte tokenizerType) {
        switch (tokenizerType) {
            case TokenizerType.IDENTITY:
                return new IdentityTokenizer();
            case TokenizerType.SIMPLE:
                return new SimpleTokenizer();
            case TokenizerType.ENGLISH:
                return new EnglishTokenizer();
            case TokenizerType.UTF:
                return new UTFTokeniser();
            case TokenizerType.TWITTER:
                return new TwitterTokenizer();
            default:
                return null;
        }
    }
}
