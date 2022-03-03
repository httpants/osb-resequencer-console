package au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Access(value = AccessType.FIELD)
@Table(name = "OSB_RETRY_MESSAGE")
public class RetryMessageEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", sequenceName = "OSB_RETRY_MESSAGE_SEQ", allocationSize = 1)

    @Column(name = "ID")
    private Long id;

    @Column(name = "MESSAGE_ID")
    private String messageId;

    @Column(name = "OWNER_ID")
    private String ownerId;

    @Column(name = "COMPONENT_DN")
    private String componentDn;

    @Column(name = "GROUP_ID")
    private String groupId;

    @Column(name = "RETRY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date retryDate;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        return "RetryMessageEntity{" +
                "id=" + id +
                ", messageId='" + messageId + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", componentDn='" + componentDn + '\'' +
                ", groupId='" + groupId + '\'' +
                ", retryDate=" + retryDate +
                '}';
    }
}
