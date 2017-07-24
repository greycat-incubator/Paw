package paw.graph.customTypes.tokenizedContent;

import java.io.IOException;
import java.util.List;

public interface InterfaceTokenizeContent {

    void clear();

    void save() throws IOException;

    void addWord(Word word);

    List<Word> decodeWords();



}
