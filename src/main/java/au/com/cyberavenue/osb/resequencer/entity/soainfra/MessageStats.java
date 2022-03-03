package au.com.cyberavenue.osb.resequencer.entity.soainfra;

import org.apache.commons.lang3.StringUtils;

public class MessageStats implements Comparable<MessageStats> {

    private final String componentDn;

    private final int messageReadyCount;

    private final int messageCompleteCount;

    private final int messageFaultedCount;

    private final int messageTimeoutCount;

    private final int messageAbortedCount;

    public MessageStats(String componentDn, Integer messageReadyCount,
            Integer messageCompleteCount, Integer messageFaultedCount, Integer messageTimeoutCount,
            Integer messageAbortedCount) {
        super();
        this.componentDn = componentDn;
        this.messageReadyCount = messageReadyCount != null ? messageReadyCount : 0;
        this.messageCompleteCount = messageCompleteCount != null ? messageCompleteCount : 0;
        this.messageFaultedCount = messageFaultedCount != null ? messageFaultedCount : 0;
        this.messageTimeoutCount = messageTimeoutCount != null ? messageTimeoutCount : 0;
        this.messageAbortedCount = messageAbortedCount != null ? messageAbortedCount : 0;
    }

    public String getComponentDn() {
        return componentDn;
    }

    public String getComponent() {
        int index = StringUtils.lastIndexOf(componentDn, '$');
        if (index != -1) {
            return StringUtils.substring(componentDn, index + 1);
        }
        return componentDn;
    }

    public int getMessageReadyCount() {
        return messageReadyCount;
    }

    public int getMessageCompleteCount() {
        return messageCompleteCount;
    }

    public int getMessageFaultedCount() {
        return messageFaultedCount;
    }

    public int getMessageTimeoutCount() {
        return messageTimeoutCount;
    }

    public int getMessageAbortedCount() {
        return messageAbortedCount;
    }

    public int getMessageTotalCount() {
        return messageReadyCount + messageCompleteCount + messageFaultedCount + messageTimeoutCount
                + messageAbortedCount;
    }

    @Override
    public int compareTo(MessageStats o) {
        int compare = Integer.compare(messageFaultedCount, o.messageFaultedCount);
        if (compare == 0) {
            compare = Integer.compare(getMessageTotalCount(), o.getMessageTotalCount());
        }
        return compare;
    }

}
