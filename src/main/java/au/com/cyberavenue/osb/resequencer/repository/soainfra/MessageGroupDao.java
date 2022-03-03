package au.com.cyberavenue.osb.resequencer.repository.soainfra;

import java.util.List;

public interface MessageGroupDao {
    int recoverGroup(String groupId);
    int recoverAllMessageGroups(List<String> ids);
}
