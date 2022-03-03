package au.com.cyberavenue.osb.resequencer.entity.soainfra;

import java.util.stream.Stream;

public enum MessageGroupStatus {
    SUSPENDED(6),
    READY(0),
    LOCKED(1),
    ERROR(3),
    TIMEOUT(4),
    PROCESSING(8);

    private int status;

    private MessageGroupStatus(int status) {
        this.status = status;
    }

    public int intValue() {
        return status;
    }

    public static MessageGroupStatus of(int status) {
        return Stream.of(MessageGroupStatus.values())
                .filter(s -> s.intValue() == status)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
