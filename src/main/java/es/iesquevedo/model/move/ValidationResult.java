package es.iesquevedo.model.move;

import es.iesquevedo.model.board.Group;

import java.util.Collections;
import java.util.List;

public class ValidationResult {
    private final boolean valid;
    private final String reason;
    private final List<Group> capturedGroups;

    private ValidationResult(boolean valid, String reason, List<Group> capturedGroups) {
        this.valid = valid;
        this.reason = reason;
        this.capturedGroups = capturedGroups == null ? Collections.emptyList() : Collections.unmodifiableList(capturedGroups);
    }

    public static ValidationResult ok() {
        return new ValidationResult(true, null, Collections.emptyList());
    }

    public static ValidationResult okWithCaptures(List<Group> capturedGroups) {
        return new ValidationResult(true, null, capturedGroups);
    }

    public static ValidationResult fail(String reason) {
        return new ValidationResult(false, reason, Collections.emptyList());
    }

    public boolean isValid() {
        return valid;
    }

    public String getReason() {
        return reason;
    }

    public List<Group> getCapturedGroups() {
        return capturedGroups;
    }
}
