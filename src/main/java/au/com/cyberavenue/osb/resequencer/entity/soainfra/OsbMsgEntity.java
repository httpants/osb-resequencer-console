package au.com.cyberavenue.osb.resequencer.entity.soainfra;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "OSB_MSG")
public class OsbMsgEntity {

    @Id
    @Column(name = "MSG_ID")
    private String msgId;

    @Column(name = "MSG_TYPE")
    private String msgType;

    @Column(name = "CREATION_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Column(name = "MODIFY_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifyTime;

    @Lob
    @Column(name = "MSG_BIN")
    private byte[] msgBin;

    public OsbMsgEntity() {}

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public byte[] getMsgBin() {
        return msgBin;
    }

    public void setMsgBin(byte[] msgBin) {
        this.msgBin = msgBin;
    }

}
