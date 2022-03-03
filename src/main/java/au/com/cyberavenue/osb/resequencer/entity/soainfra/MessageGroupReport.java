package au.com.cyberavenue.osb.resequencer.entity.soainfra;

import org.apache.commons.lang3.StringUtils;

public class MessageGroupReport {

    private final String componentDn;

    private final String groupId;

    private final Integer groupStatus;

    private final Integer messageCount;

    private final Integer readyCount;

    private final Integer completeCount;

    private final Integer faultedCount;

    private final Integer timeoutCount;

    private final Integer abortedCount;

    public MessageGroupReport(String componentDn, String groupId, Integer groupStatus, Integer messageCount,
            Integer readyCount, Integer completeCount, Integer faultedCount, Integer timeoutCount,
            Integer abortedCount) {
        this.componentDn = componentDn;
        this.groupId = groupId;
        this.groupStatus = groupStatus;
        this.messageCount = messageCount;
        this.readyCount = readyCount;
        this.completeCount = completeCount;
        this.faultedCount = faultedCount;
        this.timeoutCount = timeoutCount;
        this.abortedCount = abortedCount;
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

    public String getGroupId() {
        return groupId;
    }

    public Integer getGroupStatus() {
        return groupStatus;
    }

    public Integer getMessageCount() {
        return messageCount;
    }

    public Integer getReadyCount() {
        return readyCount;
    }

    public Integer getCompleteCount() {
        return completeCount;
    }

    public Integer getFaultedCount() {
        return faultedCount;
    }

    public Integer getTimeoutCount() {
        return timeoutCount;
    }

    public Integer getAbortedCount() {
        return abortedCount;
    }

}
