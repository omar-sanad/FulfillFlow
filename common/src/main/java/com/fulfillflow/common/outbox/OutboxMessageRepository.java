package com.fulfillflow.common.outbox;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {

    /**
     * Returns up to {@code limit} messages due for publication: status NEW
     * with next_attempt_at in the past. Ordered by creation for FIFO.
     */
    @Query("""
            select m from OutboxMessage m
            where m.status = com.fulfillflow.common.outbox.OutboxMessage.Status.NEW
            and m.nextAttemptAt <= :now
            order by m.createdAt asc
            """)
    List<OutboxMessage> findDue(Instant now, Pageable pageable);
}
