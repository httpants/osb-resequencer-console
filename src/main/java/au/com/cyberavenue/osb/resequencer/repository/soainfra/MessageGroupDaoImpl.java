package au.com.cyberavenue.osb.resequencer.repository.soainfra;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MessageGroupDaoImpl implements MessageGroupDao {

    private static final Logger log = LoggerFactory.getLogger(MessageGroupDaoImpl.class);

    @Autowired
    @Qualifier("soaInfraJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Override
    public int recoverGroup(String groupId) {
        jdbcTemplate.update("UPDATE OSB_RESEQUENCER_MESSAGE rm set rm.status = 0 WHERE rm.status = 3 AND rm.owner_Id=?",
                groupId);
        return jdbcTemplate.update("UPDATE OSB_GROUP_STATUS gs SET gs.STATUS = 0 WHERE gs.ID = ?", groupId);
    }

    @Override
    public int recoverAllMessageGroups(List<String> ids) {
        for (String id : ids) {
            log.info("Recovering message group '{}'", id);
            recoverGroup(id);
        }
        return 1;
    }
}
