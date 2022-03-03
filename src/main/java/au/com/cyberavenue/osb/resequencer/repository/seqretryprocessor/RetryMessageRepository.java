package au.com.cyberavenue.osb.resequencer.repository.seqretryprocessor;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor.RetryMessageEntity;

public interface RetryMessageRepository extends JpaRepository<RetryMessageEntity, Long> {
    List<RetryMessageEntity> findByMessageIdOrderByRetryDateDesc(String messageId);
}
