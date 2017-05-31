package paw.tokenizer;

import paw.tokenizer.token.Token;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

/**
 * Tokenizer abstract class
 */
public abstract class AbstractTokenizer {


    /**
     * Tokenize
     */

    /**
     * Method to tokenize a reader
     *
     * @param reader to use
     * @return an array of token on which all preprocessor registered have been applied
     * @throws IOException in case of reader exception
     */
    public abstract List<Token> tokenize(Reader reader) throws IOException;

    /**
     * Method to tokenize a string
     *
     * @param s string to tokenize
     * @return an array of token on which all preprocessor registered have been applied
     */
    public List<Token> tokenize(String s) {
        try {
            Reader r = new StringReader(s);
            List<Token> result = tokenize(r);
            r.close();
            return result;
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }


    /**
     * Type of the Tokenizer
     *
     * @return
     */
    public abstract byte getType();
}