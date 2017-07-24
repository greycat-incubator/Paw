package paw.tokenizer.utils;

public class LowerString {

    private int[] mask;
    private String content;

    public LowerString(String s) {
        int size = (int) Math.ceil((double) s.length() / 32);
        mask = new int[size];
        StringBuilder sb = new StringBuilder(s.length());
        int offset = 0;
        int index = 0;
        for (int i = 0; i < s.length(); i++) {
            if (offset == 32) {
                offset = 0;
                index++;
            }
            char c = s.charAt(i);
            if (Character.isUpperCase(c)) {
                mask[index] |= (1 << offset);
                c = Character.toLowerCase(c);
            }
            sb.append(c);
            offset++;
        }
        content = sb.toString();
    }

    public LowerString(String s, int[] mask) {
        this.mask = mask;
        this.content = s;
    }

    public String rebuild() {
        StringBuilder sb = new StringBuilder(content.length());
        int offset = 0;
        int index = 0;
        for (int i = 0; i < content.length(); i++) {
            if (offset == 32) {
                offset = 0;
                index++;
            }
            char c = content.charAt(i);
            int currentmask = mask[index];
            currentmask >>= offset;
            currentmask &= 1;
            if (currentmask != 0) {
                c = Character.toUpperCase(c);
            }
            sb.append(c);
            offset++;
        }
        return sb.toString();
    }

    public int[] getMask() {
        return mask;
    }

    public String getContent() {
        return content;
    }
}
