package au.com.cyberavenue.osb.resequencer.batch.retry.config;

import java.util.Date;
import java.util.Objects;

public class Message {
    private final String id;
    private final String ownerId;
    private final String componentDn;
    private final String operation;
    private final String groupId;
    private final Integer status;
    private final Date groupLastUpdated;

    public Message(String id, String ownerId, String componentDn, String operation, String groupId, Integer status,
            Date groupLastUpdated) {
        this.id = id;
        this.ownerId = ownerId;
        this.componentDn = componentDn;
        this.operation = operation;
        this.groupId = groupId;
        this.status = status;
        this.groupLastUpdated = groupLastUpdated;
    }

    public String getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getComponentDn() {
        return componentDn;
    }

    public String getOperation() {
        return operation;
    }

    public String getGroupId() {
        return groupId;
    }

    public Integer getStatus() {
        return status;
    }

    public Date getGroupLastUpdated() {
        return groupLastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Message message = (Message) o;
        return id.equals(message.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Message [id=" + id + ", ownerId=" + ownerId + ", componentDn=" + componentDn + ", operation="
                + operation + ", groupId=" + groupId + ", status=" + status + ", groupLastUpdated=" + groupLastUpdated
                + "]";
    }

}
