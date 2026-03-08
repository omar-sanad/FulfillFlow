package com.fulfillflow.order;

import com.fulfillflow.common.error.GlobalExceptionHandler;
import com.fulfillflow.common.openapi.OpenApiConfig;
import com.fulfillflow.common.outbox.OutboxHelper;
import com.fulfillflow.common.outbox.OutboxPublisher;
import com.fulfillflow.common.outbox.OutboxSchedulingConfig;
import com.fulfillflow.common.security.KeycloakRoleConverter;
import com.fulfillflow.common.security.SecurityContextHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@Import({GlobalExceptionHandler.class, OpenApiConfig.class, KeycloakRoleConverter.class,
        SecurityContextHelper.class, OutboxHelper.class, OutboxPublisher.class,
        OutboxSchedulingConfig.class})
@EntityScan(basePackages = {"com.fulfillflow.order", "com.fulfillflow.common.outbox"})
@EnableJpaRepositories(basePackages = {"com.fulfillflow.order.domain", "com.fulfillflow.order.event", "com.fulfillflow.common.outbox"})
@EnableKafka
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
