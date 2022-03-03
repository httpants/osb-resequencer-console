package au.com.cyberavenue.osb.resequencer.batch.retry.config;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RetryMessageRowMapper implements RowMapper<RetryMessage> {

    @Override
    public RetryMessage mapRow(ResultSet resultSet, int i) throws SQLException {
        return new RetryMessage(resultSet.getString("MESSAGE_ID"),
                resultSet.getString("OWNER_ID"),
                resultSet.getString("COMPONENT_DN"),
                resultSet.getString("GROUP_ID"),
                new java.util.Date(resultSet.getDate("RETRY_DATE").getTime()));
    }
}
