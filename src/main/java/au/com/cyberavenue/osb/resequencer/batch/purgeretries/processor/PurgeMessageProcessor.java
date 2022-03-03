package au.com.cyberavenue.osb.resequencer.batch.purgeretries.processor;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

public class PurgeMessageProcessor implements ItemProcessor<String, String> {

	private static final Logger log = LoggerFactory.getLogger(PurgeMessageProcessor.class);

	private JdbcOperations soaInfraJdbcTemplate;

	@Override
	public String process(String id) {
		int messageCount = soaInfraJdbcTemplate.queryForObject(
				"select count(*) from OSB_RESEQUENCER_MESSAGE WHERE ID = ?", new Object[] { id }, Integer.class);
		if (messageCount == 0) {
			log.info(":::: Purging messages with id [" + id + "]");
			return id;
		}
		return null;

	}

	@Autowired
	@Qualifier("soaInfraDataSource")
	public void setSoaInfraDataSource(DataSource soaInfraDataSource) {
		soaInfraJdbcTemplate = new JdbcTemplate(soaInfraDataSource);
	}

}
