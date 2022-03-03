package au.com.cyberavenue.osb.resequencer.repository.seqretryprocessor;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor.ComponentOperationId;
import au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor.OsbRetryConfig;

@Repository
public interface OsbRetryConfigRepository extends JpaRepository<OsbRetryConfig, ComponentOperationId> {

    @Query("select rc from OsbRetryConfig rc where rc.id.componentDn = :componentDn and rc.id.operation = :operation")
    Optional<OsbRetryConfig> findByComponentDnAndOperation(
            @Param("componentDn") String componentDn,
            @Param("operation") String operation);

}
