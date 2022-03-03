package au.com.cyberavenue.osb.resequencer.entity.soainfra;

import java.util.Date;

public interface MessageProjection {

    String getId();

    String getOwnerId();

    String getComponentDn();

    String getOperation();

    String getGroupId();

    Date getCreationDate();

    Integer getMessageStatus();

    Integer getGroupStatus();

}
