/**
 * Copyright 2017 Matthieu Jimenez.  All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
