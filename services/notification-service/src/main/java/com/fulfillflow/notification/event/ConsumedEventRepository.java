package com.fulfillflow.notification.event;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumedEventRepository extends JpaRepository<ConsumedEvent, UUID> {
}
