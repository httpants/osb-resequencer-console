package au.com.cyberavenue.osb.resequencer.repository.soainfra;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import au.com.cyberavenue.osb.resequencer.entity.soainfra.OsbMsgEntity;

@Repository
public interface OsbMsgRepository extends JpaRepository<OsbMsgEntity, String>,
                                  QuerydslPredicateExecutor<OsbMsgEntity> {

}
