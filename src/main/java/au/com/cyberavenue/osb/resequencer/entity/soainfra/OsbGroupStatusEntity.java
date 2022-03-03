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

import org.apache.commons.lang3.StringUtils;

@Entity
@Access(value = AccessType.FIELD)
@Table(name = "OSB_GROUP_STATUS")
@SqlResultSetMapping(
    name = "messageGroupReportMapping",
    classes = {
            @ConstructorResult(
                targetClass = MessageGroupReport.class,
                columns = {
                        @ColumnResult(name = "COMPONENT_DN", type = String.class),
                        @ColumnResult(name = "GROUP_ID", type = String.class),
                        @ColumnResult(name = "GROUP_STATUS", type = Integer.class),
                        @ColumnResult(name = "MESSAGE_COUNT", type = Integer.class),
                        @ColumnResult(name = "READY", type = Integer.class),
                        @ColumnResult(name = "COMPLETED", type = Integer.class),
                        @ColumnResult(name = "FAULTED", type = Integer.class),
                        @ColumnResult(name = "TIMEOUT", type = Integer.class),
                        @ColumnResult(name = "ABORTED", type = Integer.class)
                })
    })
@NamedNativeQuery(
    name = "OsbGroupStatusEntity.getMessageGroupReport",
    query = ""
            + "SELECT COMPONENT_DN, GROUP_ID, GROUP_STATUS, "
            + "(READY + COMPLETED + FAULTED + TIMEOUT + ABORTED) as MESSAGE_COUNT, "
            + "READY, COMPLETED, FAULTED, TIMEOUT, ABORTED FROM ( "
            + "select m.COMPONENT_DN, m.GROUP_ID, g.STATUS as GROUP_STATUS, "
            + "m.STATUS from OSB_RESEQUENCER_MESSAGE m  "
            + "INNER JOIN OSB_GROUP_STATUS g ON  m.OWNER_ID = g.ID "
            + ") a "
            + "PIVOT (COUNT(*) FOR STATUS IN (0 as READY, 2 as COMPLETED, "
            + "3 as FAULTED, 4 as TIMEOUT, 5 as ABORTED)) ",
    resultSetMapping = "messageGroupReportMapping")
public class OsbGroupStatusEntity implements Serializable {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "COMPONENT_DN")
    private String componentDn;

    @Column(name = "OPERATION")
    private String operation;

    @Column(name = "GROUP_ID")
    private String groupId;

    @Column(name = "LOCK_TIME_1")
    private Long lockTime;

    @Column(name = "LAST_RECEIVED_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastReceivedTime;

    @Column(name = "STATUS")
    private Integer status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComponentDn() {
        return componentDn;
    }

    public String getShortComponentName() {
        return getComponentName(componentDn);
    }

    public static String getComponentName(String componentDn) {
        int index = StringUtils.lastIndexOf(componentDn, '$');
        if (index != -1) {
            return StringUtils.substring(componentDn, index + 1);
        }
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

    public Long getLockTime() {
        return lockTime;
    }

    public void setLockTime(Long lockTime) {
        this.lockTime = lockTime;
    }

    public Date getLastReceivedTime() {
        return lastReceivedTime;
    }

    public void setLastReceivedTime(Date lastReceivedTime) {
        this.lastReceivedTime = lastReceivedTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "OsbGroupStatusEntity{" +
                "id='" + id + '\'' +
                ", componentDn='" + componentDn + '\'' +
                ", operation='" + operation + '\'' +
                ", groupId='" + groupId + '\'' +
                ", lockTime=" + lockTime +
                ", lastReceivedTime=" + lastReceivedTime +
                ", status=" + status +
                '}';
    }
}
