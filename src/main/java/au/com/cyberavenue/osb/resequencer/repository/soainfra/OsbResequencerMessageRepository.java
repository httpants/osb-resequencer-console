package au.com.cyberavenue.osb.resequencer.repository.soainfra;

import java.util.List;

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

import au.com.cyberavenue.osb.resequencer.entity.soainfra.MessageProjection;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.MessageStats;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.OsbResequencerMessageEntity;

@Repository
public interface OsbResequencerMessageRepository extends JpaRepository<OsbResequencerMessageEntity, String>,
                                                 QuerydslPredicateExecutor<OsbResequencerMessageEntity> {

    List<OsbResequencerMessageEntity> findAllByOwnerIdEqualsOrderByStatusDesc(String groupId);

    @Query(nativeQuery = true, value = "select * from (select"
            + " m.ID as id"
            + ", m.OWNER_ID as ownerId"
            + ", m.COMPONENT_DN as componentDn"
            + ", m.OPERATION as operation"
            + ", m.GROUP_ID as groupId"
            + ", m.CREATION_DATE as creationDate"
            + ", m.STATUS as messageStatus"
            + ", g.STATUS as groupStatus"
            + " from OSB_RESEQUENCER_MESSAGE m"
            + ", OSB_GROUP_STATUS g"
            + " where m.OWNER_ID = g.ID"
            + " and (:componentDn is null or m.COMPONENT_DN = :componentDn)"
            + " and (:groupId is null or m.GROUP_ID = :groupId)"
            + " and (:groupStatus is null or g.STATUS = TO_NUMBER(:groupStatus))"
            + " and (:messageStatus is null or m.STATUS = TO_NUMBER(:messageStatus))) m",
        countQuery = "select"
                + " count(*)"
                + " from OSB_RESEQUENCER_MESSAGE m"
                + ", OSB_GROUP_STATUS g"
                + " where m.OWNER_ID = g.ID"
                + " and (:componentDn is null or m.COMPONENT_DN = :componentDn)"
                + " and (:groupId is null or m.GROUP_ID = :groupId)"
                + " and (:groupStatus is null or g.STATUS = TO_NUMBER(:groupStatus))"
                + " and (:messageStatus is null or m.STATUS = TO_NUMBER(:messageStatus))")
    Page<MessageProjection> findMessages(
            @Param("componentDn") String componentDn,
            @Param("groupId") String groupId,
            @Param("groupStatus") Integer groupStatus,
            @Param("messageStatus") Integer messageStatus,
            Pageable pageable);

    @Query("SELECT DISTINCT m.componentDn FROM OsbResequencerMessageEntity m")
    List<String> findDistinctComponentDn();

    @Query("SELECT DISTINCT m.status FROM OsbResequencerMessageEntity m where m.ownerId = :ownerId")
    List<Integer> findDistinctStatusByOwnerId(@Param("ownerId") String ownerId);

    @Query("select m FROM OsbResequencerMessageEntity m where m.ownerId = :ownerId and m.id in :messageIds")
    List<OsbResequencerMessageEntity> findByOwnerIdWithIdIn(
            @Param("ownerId") String ownerId,
            @Param("messageIds") List<String> messageIds);

    @Transactional
    @Modifying
    @Query("UPDATE OsbResequencerMessageEntity me set me.status = 0 WHERE me.status in (3, 4) AND me.ownerId=:groupId")
    void recoverMessagesByGroup(@Param("groupId") String groupId);

    @Transactional
    @Modifying
    @Query("UPDATE OsbResequencerMessageEntity me set me.status = 0 WHERE me.status in (3, 4) AND me.ownerId in (:groupIds)")
    void recoverMessagesByGroups(@Param("groupIds") List<String> groupIds);

    @Modifying
    @Transactional(propagation = Propagation.REQUIRED)
    @Query("UPDATE OsbResequencerMessageEntity m SET m.status = :status where m.id in :messageIds")
    int updateMessageStatus(@Param("status") Integer status, @Param("messageIds") List<String> messageIds);

    @Query(nativeQuery = true)
    List<MessageStats> getMessageReport();
}
