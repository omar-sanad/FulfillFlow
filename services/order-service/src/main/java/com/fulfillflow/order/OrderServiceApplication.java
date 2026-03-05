package com.fulfillflow.order;

import com.fulfillflow.common.error.GlobalExceptionHandler;
import com.fulfillflow.common.openapi.OpenApiConfig;
import com.fulfillflow.common.security.KeycloakRoleConverter;
import com.fulfillflow.common.security.SecurityContextHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({GlobalExceptionHandler.class, OpenApiConfig.class, KeycloakRoleConverter.class, SecurityContextHelper.class})
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
