package au.com.cyberavenue.osb.resequencer.batch.retry.config;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class MessageRowMapper implements RowMapper<Message> {

    @Override
    public Message mapRow(ResultSet rs, int i) throws SQLException {
        return new Message(
                rs.getString("id"),
                rs.getString("owner_id"),
                rs.getString("component_dn"),
                rs.getString("operation"),
                rs.getString("group_id"),
                rs.getInt("status"),
                rs.getDate("last_received_time")

        );
    }

}
