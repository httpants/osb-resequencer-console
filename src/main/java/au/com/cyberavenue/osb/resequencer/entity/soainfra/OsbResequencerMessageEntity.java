package au.com.cyberavenue.osb.resequencer.entity.soainfra;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Access(value = AccessType.FIELD)
@Table(name = "OSB_RESEQUENCER_MESSAGE")
@SqlResultSetMapping(name = "messageReportMapping", classes = {
        @ConstructorResult(targetClass = MessageStats.class, columns = {
                @ColumnResult(name = "COMPONENT_DN", type = String.class),
                @ColumnResult(name = "READY", type = Integer.class),
                @ColumnResult(name = "COMPLETED", type = Integer.class),
                @ColumnResult(name = "FAULTED", type = Integer.class),
                @ColumnResult(name = "TIMEOUT", type = Integer.class),
                @ColumnResult(name = "ABORTED", type = Integer.class)
        })
})
@NamedNativeQuery(name = "OsbResequencerMessageEntity.getMessageReport", query = ""
        + "select * from ("
        + "select component_dn, status from OSB_RESEQUENCER_MESSAGE "
        + ") m "
        + "pivot (count(*) for status IN (0 as READY, 2 as COMPLETED, 3 as FAULTED, 4 as TIMEOUT, 5 as ABORTED))"
        + "",
    resultSetMapping = "messageReportMapping")
public class OsbResequencerMessageEntity implements Serializable {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "OWNER_ID")
    private String ownerId;

    @Column(name = "COMPONENT_DN")
    private String componentDn;

    @Column(name = "OPERATION")
    private String operation;

    @Column(name = "GROUP_ID")
    private String groupId;

    @Column(name = "CREATION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @Column(name = "STATUS")
    private Integer status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "OsbResequencerMessageEntity{" +
                "id='" + id + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", componentDn='" + componentDn + '\'' +
                ", operation='" + operation + '\'' +
                ", groupId='" + groupId + '\'' +
                ", creationDate=" + creationDate +
                ", status=" + status +
                '}';
    }
}
