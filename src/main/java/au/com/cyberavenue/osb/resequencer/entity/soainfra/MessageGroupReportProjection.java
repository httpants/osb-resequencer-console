package au.com.cyberavenue.osb.resequencer.entity.soainfra;

import java.util.Date;

public interface MessageGroupReportProjection {

    String getId();

    String getComponentDn();

    String getOperation();

    String getGroupId();

    Integer getGroupStatus();

    Date getLastReceivedTime();

    Integer getMessageCount();

    Integer getReadyCount();

    Integer getCompleteCount();

    Integer getFaultedCount();

    Integer getTimeoutCount();

    Integer getAbortedCount();

}
