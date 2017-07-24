package paw.graph.customType.tokenizedContent;

import java.io.IOException;
import java.util.List;

public interface CTTokenizeContent {

    void clear();

    void save() throws IOException;

    void addWord(Word word);

    List<Word> decodeWords();

}
