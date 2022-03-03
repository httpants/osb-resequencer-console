package au.com.cyberavenue.osb.resequencer.repository.soainfra;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor.ComponentOperationId;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.ComponentGroupStatusProjection;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.MessageGroupReport;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.MessageGroupReportProjection;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.OsbGroupStatusEntity;

@Repository
public interface OsbGroupStatusRepository extends JpaRepository<OsbGroupStatusEntity, String>,
                                          QuerydslPredicateExecutor<OsbGroupStatusEntity> {

    List<OsbGroupStatusEntity> findAllByStatusOrderByLastReceivedTimeDesc(Integer status);

    @Query("SELECT DISTINCT ogs.componentDn FROM OsbGroupStatusEntity ogs")
    List<String> findDistinctComponentDn();

    @Query("SELECT new au.com.energyq.dms.batch.retry.entity.seqretryprocessor.ComponentOperationId(g.componentDn, g.operation) "
            + "FROM OsbGroupStatusEntity g GROUP BY g.componentDn, g.operation ORDER BY g.componentDn, g.operation")
    List<ComponentOperationId> findDistinctComponentOperations();

    @Transactional
    @Modifying
    @Query("UPDATE OsbGroupStatusEntity ogs SET ogs.status = 0 where ogs.id =:groupId")
    void recoverMessageGroup(@Param("groupId") String groupId);

    @Modifying
    @Transactional(propagation = Propagation.REQUIRED)
    @Query("UPDATE OsbGroupStatusEntity ogs SET ogs.status = :status where ogs.id in :groupIds")
    int updateMessageGroupStatus(@Param("status") Integer status, @Param("groupIds") List<String> groupIds);

    @Query(nativeQuery = true,
        value = "SELECT * FROM (SELECT"
                + " ID as id"
                + ", COMPONENT_DN as componentDn"
                + ", OPERATION as operation"
                + ", LAST_RECEIVED_TIME as lastReceivedTime"
                + ", GROUP_ID as groupId"
                + ", GROUP_STATUS as groupStatus"
                + ", (READY + COMPLETED + FAULTED + TIMEOUT + ABORTED) as messageCount"
                + ", READY as readyCount"
                + ", COMPLETED as completedCount"
                + ", FAULTED as faultedCount"
                + ", TIMEOUT as timeoutCount"
                + ", ABORTED as abortedCount"
                + " FROM (( SELECT * FROM ("
                + "select g.ID, m.COMPONENT_DN, m.OPERATION, g.LAST_RECEIVED_TIME, m.GROUP_ID, "
                + "g.STATUS as GROUP_STATUS, m.STATUS from OSB_RESEQUENCER_MESSAGE m  "
                + "INNER JOIN OSB_GROUP_STATUS g ON  m.OWNER_ID = g.ID "
                + "where (:groupStatus is null or g.STATUS = TO_NUMBER(:groupStatus))"
                + " and (:componentDn is null or g.COMPONENT_DN = :componentDn) "
                + " and (:groupId is null or g.GROUP_ID = :groupId) "
                + ") PIVOT (COUNT(*) FOR STATUS IN (0 as READY, 2 as COMPLETED, 3 as FAULTED, "
                + "4 as TIMEOUT, 5 as ABORTED))"
                + ") UNION ALL ("
                + "SELECT ID, COMPONENT_DN, OPERATION, LAST_RECEIVED_TIME, GROUP_ID, "
                + "STATUS as GROUP_STATUS, 0 as READY, 0 as COMPLETED, 0 as FAULTED, "
                + "0 as TIMEOUT, 0 as ABORTED "
                + "FROM OSB_GROUP_STATUS g "
                + "where (:groupStatus is null or g.STATUS = TO_NUMBER(:groupStatus))"
                + " and (:componentDn is null or g.COMPONENT_DN = :componentDn) "
                + " and (:groupId is null or g.GROUP_ID = :groupId)"
                + " and NOT EXISTS (SELECT 1 FROM OSB_RESEQUENCER_MESSAGE m WHERE m.OWNER_ID = g.ID)"
                + "))"
                + ") m where (:total is null or (TO_NUMBER(:total) = 0 and messageCount = 0) or (TO_NUMBER(:total) > 0 and messageCount >= TO_NUMBER(:total)))"
                + " and (:ready is null or (TO_NUMBER(:ready) = 0 and readyCount = 0) or (TO_NUMBER(:ready) > 0 and readyCount >= TO_NUMBER(:ready)))"
                + " and (:aborted is null or (TO_NUMBER(:aborted) = 0 and abortedCount = 0) or (TO_NUMBER(:aborted) > 0 and abortedCount >= TO_NUMBER(:aborted)))"
                + " and (:faulted is null or (TO_NUMBER(:faulted) = 0 and faultedCount = 0) or (TO_NUMBER(:faulted) > 0 and faultedCount >= TO_NUMBER(:faulted)))",
        countQuery = "SELECT count(*) FROM (SELECT"
                + " ID as id"
                + ", COMPONENT_DN as componentDn"
                + ", OPERATION as operation"
                + ", LAST_RECEIVED_TIME as lastReceivedTime"
                + ", GROUP_ID as groupId"
                + ", GROUP_STATUS as groupStatus"
                + ", (READY + COMPLETED + FAULTED + TIMEOUT + ABORTED) as messageCount"
                + ", READY as readyCount"
                + ", COMPLETED as completedCount"
                + ", FAULTED as faultedCount"
                + ", TIMEOUT as timeoutCount"
                + ", ABORTED as abortedCount"
                + " FROM (( SELECT * FROM ("
                + "select g.ID, m.COMPONENT_DN, m.OPERATION, g.LAST_RECEIVED_TIME, m.GROUP_ID, "
                + "g.STATUS as GROUP_STATUS, m.STATUS from OSB_RESEQUENCER_MESSAGE m  "
                + "INNER JOIN OSB_GROUP_STATUS g ON  m.OWNER_ID = g.ID "
                + "where (:groupStatus is null or g.STATUS = TO_NUMBER(:groupStatus))"
                + " and (:componentDn is null or g.COMPONENT_DN = :componentDn) "
                + " and (:groupId is null or g.GROUP_ID = :groupId) "
                + ") PIVOT (COUNT(*) FOR STATUS IN (0 as READY, 2 as COMPLETED, 3 as FAULTED, "
                + "4 as TIMEOUT, 5 as ABORTED))"
                + ") UNION ALL ("
                + "SELECT ID, COMPONENT_DN, OPERATION, LAST_RECEIVED_TIME, GROUP_ID, "
                + "STATUS as GROUP_STATUS, 0 as READY, 0 as COMPLETED, 0 as FAULTED, "
                + "0 as TIMEOUT, 0 as ABORTED "
                + "FROM OSB_GROUP_STATUS g "
                + "where (:groupStatus is null or g.STATUS = TO_NUMBER(:groupStatus))"
                + " and (:componentDn is null or g.COMPONENT_DN = :componentDn) "
                + " and (:groupId is null or g.GROUP_ID = :groupId)"
                + " and NOT EXISTS (SELECT 1 FROM OSB_RESEQUENCER_MESSAGE m WHERE m.OWNER_ID = g.ID)"
                + "))"
                + ") m where (:total is null or (TO_NUMBER(:total) = 0 and messageCount = 0) or (TO_NUMBER(:total) > 0 and messageCount >= TO_NUMBER(:total)))"
                + " and (:ready is null or (TO_NUMBER(:ready) = 0 and readyCount = 0) or (TO_NUMBER(:ready) > 0 and readyCount >= TO_NUMBER(:ready)))"
                + " and (:aborted is null or (TO_NUMBER(:aborted) = 0 and abortedCount = 0) or (TO_NUMBER(:aborted) > 0 and abortedCount >= TO_NUMBER(:aborted)))"
                + " and (:faulted is null or (TO_NUMBER(:faulted) = 0 and faultedCount = 0) or (TO_NUMBER(:faulted) > 0 and faultedCount >= TO_NUMBER(:faulted)))")
    Page<MessageGroupReportProjection> getMessageGroupReport(
            @Param("groupStatus") Integer groupStatus,
            @Param("componentDn") String componentDn,
            @Param("groupId") String groupId,
            @Param("faulted") Integer faulted,
            @Param("aborted") Integer aborted,
            @Param("ready") Integer ready,
            @Param("total") Integer total,
            Pageable pageable);

    @Query(nativeQuery = true,
        value = "select "
                + "COMPONENT_DN as componentDn"
                + ", READY as ready"
                + ", LOCKED as locked"
                + ", ERROR as error"
                + ", TIMEOUT as timeout"
                + ", SUSPENDED as suspended"
                + ", PROCESSING as processing"
                + " from ("
                + "select component_dn, status from OSB_GROUP_STATUS"
                + ") m "
                + "pivot (count(*) for status in (0 as READY, 1 as LOCKED, 3 as ERROR, 4 as TIMEOUT, 6 as SUSPENDED, 8 as PROCESSING))")
    List<ComponentGroupStatusProjection> getComponentGroupStatusReport();

    @Query(nativeQuery = true,
        value = "SELECT * FROM (SELECT"
                + " ID as id"
                + ", COMPONENT_DN as componentDn"
                + ", OPERATION as operation"
                + ", LAST_RECEIVED_TIME as lastReceivedTime"
                + ", GROUP_ID as groupId"
                + ", GROUP_STATUS as groupStatus"
                + ", (READY + COMPLETED + FAULTED + TIMEOUT + ABORTED) as messageCount"
                + ", READY as readyCount"
                + ", COMPLETED as completedCount"
                + ", FAULTED as faultedCount"
                + ", TIMEOUT as timeoutCount"
                + ", ABORTED as abortedCount"
                + " FROM (( SELECT * FROM ("
                + "select g.ID, m.COMPONENT_DN, m.OPERATION, g.LAST_RECEIVED_TIME, m.GROUP_ID, "
                + "g.STATUS as GROUP_STATUS, m.STATUS from OSB_RESEQUENCER_MESSAGE m  "
                + "INNER JOIN OSB_GROUP_STATUS g ON  m.OWNER_ID = g.ID "
                + "where (g.ID = :id)"
                + ") PIVOT (COUNT(*) FOR STATUS IN (0 as READY, 2 as COMPLETED, 3 as FAULTED, "
                + "4 as TIMEOUT, 5 as ABORTED))"
                + ") UNION ALL ("
                + "SELECT ID, COMPONENT_DN, OPERATION, LAST_RECEIVED_TIME, GROUP_ID, "
                + "STATUS as GROUP_STATUS, 0 as READY, 0 as COMPLETED, 0 as FAULTED, "
                + "0 as TIMEOUT, 0 as ABORTED "
                + "FROM OSB_GROUP_STATUS g "
                + "where (g.ID = :id)"
                + " and NOT EXISTS (SELECT 1 FROM OSB_RESEQUENCER_MESSAGE m WHERE m.OWNER_ID = g.ID)"
                + "))"
                + ") m")
    Optional<MessageGroupReportProjection> getMessageGroupReport(@Param("id") String id);

    @Query(nativeQuery = true, name = "OsbGroupStatusEntity.getMessageGroupReport")
    List<MessageGroupReport> getMessageGroupReport();

}