package paw.tokeniser;

import paw.utils.LowerString;

import java.util.Map;

public class TokenizedString {
    private Map<Integer, Integer> _delimitersPosition;
    private Map<Integer, Integer> _numberPosition;
    private Map<Integer, LowerString> _tokensPosition;
    private Map<Integer, String> _outcastPosition;
    private int _size;


    public TokenizedString(Map<Integer, LowerString> tokensPosition, Map<Integer, Integer> numberPosition, Map<Integer, Integer> delimitersPosition, Map<Integer, String> outcastPosition, int size) {
        this._size = size;
        this._delimitersPosition = delimitersPosition;
        this._numberPosition = numberPosition;
        this._tokensPosition = tokensPosition;
        this._outcastPosition = outcastPosition;
        this._size = size;
    }

    public String rebuild() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < _size; i++) {
            if (_delimitersPosition.containsKey(i)) {
                sb.append((char) _delimitersPosition.get(i).intValue());
            } else if (_tokensPosition.containsKey(i)) {
                sb.append(_tokensPosition.get(i));
            } else if (_numberPosition.containsKey(i)) {
                sb.append(_numberPosition.get(i).longValue());
            } else if (_outcastPosition.containsKey(i)) {
                sb.append(_outcastPosition.get(i));
            } else {
                throw new RuntimeException("Failed to find the value of " + i + "th position in the Tokenized String");
            }
        }
        return sb.toString();
    }

    public Map<Integer, Integer> get_delimitersPosition() {
        return _delimitersPosition;
    }

    public Map<Integer, Integer> get_numberPosition() {
        return _numberPosition;
    }

    public Map<Integer, LowerString> get_tokensPosition() {
        return _tokensPosition;
    }

    public Map<Integer, String> get_outcastPosition() {
        return _outcastPosition;
    }

    public int get_size() {
        return _size;
    }
}
