package paw.tokenizer.token;

import paw.PawConstants;
import paw.tokenizer.utils.LowerString;

public class ContentT implements Token {

    private final LowerString content;

    public ContentT(String content) {
        this.content = new LowerString(content);
    }

    @Override
    public String getToken() {
        return content.getContent();
    }

    @Override
    public byte getType() {
        return PawConstants.CONTENT_TOKEN;
    }

    public LowerString getLowerString() {
        return content;
    }
}
