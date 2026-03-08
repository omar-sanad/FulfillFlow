package com.fulfillflow.order.event;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumedEventRepository extends JpaRepository<ConsumedEvent, UUID> {
}
