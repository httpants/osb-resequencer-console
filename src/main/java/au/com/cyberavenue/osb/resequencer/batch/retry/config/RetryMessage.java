package au.com.cyberavenue.osb.resequencer.batch.retry.config;

import java.util.Date;

public class RetryMessage {

    private String messageId;
    private String ownerId;
    private String componentDn;
    private String groupId;
    private Date retryDate;

    public RetryMessage(String messageId, String ownerId, String componentDn, String groupId) {
        this.messageId = messageId;
        this.ownerId = ownerId;
        this.componentDn = componentDn;
        this.groupId = groupId;
        this.retryDate = new Date();
    }

    public RetryMessage(String messageId, String ownerId, String componentDn, String groupId, Date retryDate) {
        this.messageId = messageId;
        this.ownerId = ownerId;
        this.componentDn = componentDn;
        this.groupId = groupId;
        this.retryDate = retryDate;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getComponentDn() {
        return componentDn;
    }

    public void setComponentDn(String componentDn) {
        this.componentDn = componentDn;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Date getRetryDate() {
        return retryDate;
    }

    public void setRetryDate(Date retryDate) {
        this.retryDate = retryDate;
    }

    @Override
    public String toString() {
        return "RetryMessage{" +
                "messageId='" + messageId + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", componentDn='" + componentDn + '\'' +
                ", groupId='" + groupId + '\'' +
                ", retryDate=" + retryDate +
                '}';
    }
}
