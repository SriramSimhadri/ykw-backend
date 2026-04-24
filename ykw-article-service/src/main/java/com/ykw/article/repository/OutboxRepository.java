package com.ykw.article.repository;

import com.ykw.article.model.outbox.OutboxEvent;
import com.ykw.article.model.outbox.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * FOR UPDATE:Locks the rows that are under selection with limit given, so that no other schedular instance can
     * process those rows.
     * SKIP LOCKED: If Another schedular instance is waiting for a row within that same query range can be skipped to wait for
     * long time. and move on with the remaining rows.
     * @param limit number of records
     * @return list of unprocessed & failed outbox events
     */
    @Query(value = """
                SELECT * FROM outbox_event
                WHERE status IN ('NEW', 'FAILED')
                  AND retries < 5
                ORDER BY created_at
                LIMIT :limit
                FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> fetchBatchForUpdate(int limit);


    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE OutboxEvent oe
            SET oe.status = :status,
                oe.updatedAt = :updatedAt,
                oe.retries = :retries
            WHERE oe.id = :id
    """)
    void updateStatus(Long id, OutboxEventStatus status, Instant updatedAt, int retries);
}
