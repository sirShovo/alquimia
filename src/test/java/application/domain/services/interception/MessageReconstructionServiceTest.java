package application.domain.services.interception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.MessageUndeterminableException;
import application.domain.valueobjects.MessageFragment;
import java.util.List;
import org.junit.jupiter.api.Test;

class MessageReconstructionServiceTest {

    private final MessageReconstructionService service = new MessageReconstructionService();

    @Test
    void rebuildsTheMessageIgnoringEmptyWords() {
        String message = service.reconstruct(List.of(
                fragment("this", "", "a", "", ""),
                fragment("", "is", "", "secret", ""),
                fragment("this", "", "", "", "message")));
        assertThat(message).isEqualTo("this is a secret message");
    }

    @Test
    void rebuildsTheMessageWithTransmissionOffset() {
        String message = service.reconstruct(List.of(
                fragment("savitar", "", "", "the", "lab"),
                fragment("", "returns", "", "the", ""),
                fragment("", "", "returns", "at", "", "")));
        assertThat(message).isEqualTo("savitar returns at the lab");
    }

    @Test
    void failsWhenAWordWasNotReceivedByAnyDevice() {
        assertThatThrownBy(() -> service.reconstruct(List.of(
                fragment("this", "", "a"),
                fragment("this", "", ""),
                fragment("", "", "a"))))
                .isInstanceOf(MessageUndeterminableException.class)
                .hasMessageContaining("word 2");
    }

    @Test
    void failsOnConflictingWords() {
        assertThatThrownBy(() -> service.reconstruct(List.of(
                fragment("this", "is"),
                fragment("that", "is"))))
                .isInstanceOf(MessageUndeterminableException.class)
                .hasMessageContaining("conflicting");
    }

    @Test
    void failsWhenNoWordWasReceived() {
        assertThatThrownBy(() -> service.reconstruct(List.of(fragment("", ""), fragment(""), fragment())))
                .isInstanceOf(MessageUndeterminableException.class);
    }

    @Test
    void requiresAtLeastOneFragment() {
        assertThatThrownBy(() -> service.reconstruct(List.of())).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> service.reconstruct(null)).isInstanceOf(DomainException.class);
    }

    private static MessageFragment fragment(String... words) {
        return new MessageFragment(List.of(words));
    }
}
