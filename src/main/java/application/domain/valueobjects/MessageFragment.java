package application.domain.valueobjects;

import application.domain.exceptions.DomainException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Portion of the message intercepted by one device. Each position holds a word; an empty string
 * ({@code ""}) means the word was not received because of interference.
 */
public record MessageFragment(List<String> words) {

    public MessageFragment {
        if (words == null) {
            throw new DomainException("Message fragment is required.");
        }
        List<String> copy = new ArrayList<>(words.size());
        for (String word : words) {
            if (word == null) {
                throw new DomainException("Message fragment must not contain null words; use \"\" for missing words.");
            }
            copy.add(word.trim());
        }
        words = Collections.unmodifiableList(copy);
    }

    public int size() {
        return words.size();
    }

    public String wordAt(int index) {
        return words.get(index);
    }

    /** {@code true} when the word at {@code index} was not received. */
    public boolean isGap(int index) {
        return words.get(index).isEmpty();
    }
}
