package application.domain.services.interception;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.MessageUndeterminableException;
import application.domain.valueobjects.MessageFragment;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Rebuilds the original message from the fragments intercepted by the devices.
 *
 * <ol>
 *   <li>Fragments are right-aligned to the longest one (the leading difference is transmission offset).</li>
 *   <li>Empty words ({@code ""}) are ignored; each aligned position takes the received word.</li>
 *   <li>Leading positions without any word are discarded as offset.</li>
 *   <li>Conflicting words or a remaining position without word make the message undeterminable.</li>
 * </ol>
 */
@Service
public class MessageReconstructionService {

    public String reconstruct(List<MessageFragment> fragments) {
        if (fragments == null || fragments.isEmpty()) {
            throw new DomainException("At least one message fragment is required.");
        }
        int length = 0;
        for (MessageFragment fragment : fragments) {
            if (fragment == null) {
                throw new DomainException("Message fragments must not be null.");
            }
            length = Math.max(length, fragment.size());
        }

        String[] merged = new String[length];
        for (MessageFragment fragment : fragments) {
            int offset = length - fragment.size();
            for (int i = 0; i < fragment.size(); i++) {
                if (fragment.isGap(i)) {
                    continue;
                }
                int position = offset + i;
                String word = fragment.wordAt(i);
                if (merged[position] == null) {
                    merged[position] = word;
                } else if (!merged[position].equals(word)) {
                    throw new MessageUndeterminableException(
                            "The message cannot be determined: conflicting words '" + merged[position]
                                    + "' and '" + word + "' at the same position.");
                }
            }
        }

        int start = 0;
        while (start < length && merged[start] == null) {
            start++;
        }
        if (start == length) {
            throw new MessageUndeterminableException("The message cannot be determined: no words were received.");
        }

        List<String> words = new ArrayList<>(length - start);
        for (int position = start; position < length; position++) {
            if (merged[position] == null) {
                throw new MessageUndeterminableException(
                        "The message cannot be determined: word " + (position - start + 1)
                                + " was not received by any device.");
            }
            words.add(merged[position]);
        }
        return String.join(" ", words);
    }
}
