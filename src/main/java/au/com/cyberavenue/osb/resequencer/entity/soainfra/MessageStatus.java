package au.com.cyberavenue.osb.resequencer.entity.soainfra;

import java.util.stream.Stream;

public enum MessageStatus {
    READY(0),
    COMPLETED(2),
    FAULTED(3),
    TIMEOUT(4),
    ABORTED(5);

    private int status;

    private MessageStatus(int status) {
        this.status = status;
    }

    public int intValue() {
        return status;
    }

    public static MessageStatus of(int status) {
        return Stream.of(MessageStatus.values())
                .filter(s -> s.intValue() == status)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
