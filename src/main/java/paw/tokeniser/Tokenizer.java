package paw.tokeniser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static paw.PawConstants.NO_TYPE_TOKEN;

/**
 * Tokenizer abstract class
 */
public abstract class Tokenizer {

    /**
     * Preprocessor
     */

    protected List<TokenPreprocessor> listOfPreprocessor = new ArrayList<>();

    /**
     * Method to add a preprocessor to your tokenizer
     *
     * @param tokenPreprocessor to add
     */
    public void addPreprocessor(TokenPreprocessor tokenPreprocessor) {
        this.listOfPreprocessor.add(tokenPreprocessor);
    }

    /**
     * Method to return the list of Preprocessor that will be applied to every token
     *
     * @return list of [TokenPreprocessor]
     */
    public List<TokenPreprocessor> getListOfPreprocessor() {
        return listOfPreprocessor;
    }

    /**
     * Method to remove a token Preprocessor from the list of TokenPreprocessor to apply to all tokens
     *
     * @param index of the token preprocessor to remove
     * @return the token preprocessor that was remove
     */
    public TokenPreprocessor removeTokenPreprocessor(int index) {
        return listOfPreprocessor.remove(index);
    }

    /**
     * Method to apply all token preprocessor of the tokenizer to a token
     *
     * @param s token on which preprocessor should be applied
     * @return the token in its final form
     */
    protected String applyAllTokenPreprocessorTo(String s) {
        String toModify = s;
        for (int i = 0; i < listOfPreprocessor.size(); i++) {
            toModify = listOfPreprocessor.get(i).transform(s);
        }
        return toModify;
    }

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
    public abstract String[] tokenize(Reader reader) throws IOException;

    /**
     * Method to tokenize a string
     *
     * @param s string to tokenize
     * @return an array of token on which all preprocessor registered have been applied
     */
    public String[] tokenize(String s) {
        try {
            return tokenize(new StringReader(s));
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Type Of Token
     */

    private String typeOfToken = NO_TYPE_TOKEN;

    /**
     * Method to set a type of Token, type can be used for further filtering
     *
     * @param typeOfToken type of the token that will be created
     */
    public void setTypeOfToken(String typeOfToken) {
        this.typeOfToken = typeOfToken;
    }

    /**
     * Method to return the type of Token currently used
     *
     * @return the type of Token currently used
     */
    public String getTypeOfToken() {
        return typeOfToken;
    }

    /**
     * To string method to print characteristics of the tokenizer
     *
     * @return string describing the tokenizer
     */
    @Override
    public String toString() {
        String s = "With Token Type: " + typeOfToken + "\n";
        if (listOfPreprocessor.size() > 0) {
            s += "With Prepocessor:\n";
            for (int i = 0; i < listOfPreprocessor.size(); i++) {
                s += i + ": " + listOfPreprocessor.get(i).toString() + "\n";
            }
        }
        return s;
    }


    private boolean keepDelimiter = false;

    /**
     * Return if the delimiter should be kept as well
     * @return
     */
    public boolean isKeepingDelimiterActivate() {
        return keepDelimiter;
    }

    /**
     * set keepDelimiter
     * @param keepDelimiter
     */
    public void setKeepDelimiter(boolean keepDelimiter) {
        this.keepDelimiter = keepDelimiter;
    }
}
