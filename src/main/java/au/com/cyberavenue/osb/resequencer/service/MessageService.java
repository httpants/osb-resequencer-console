package au.com.cyberavenue.osb.resequencer.service;

import java.util.Date;
import java.util.List;

import au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor.RetryMessageEntity;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.OsbGroupStatusEntity;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.OsbMsgEntity;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.OsbResequencerMessageEntity;

public interface MessageService {

    List<OsbGroupStatusEntity> getSuspendedGroups();

    List<OsbResequencerMessageEntity> getMessageGroup(String groupId);

    List<RetryMessageEntity> getMessageRetries(String messageId);

    int recoverMessageGroup(String groupId);

    int recoverAllMessageGroups();

    String getPayload(OsbMsgEntity osbMsg);

    String getIConsoleLink(OsbMsgEntity osbMsg, String groupId, Date lastUpdated);
}
