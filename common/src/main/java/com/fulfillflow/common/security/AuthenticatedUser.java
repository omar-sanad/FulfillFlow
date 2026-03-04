package com.fulfillflow.common.security;

import java.util.Set;
import java.util.UUID;

/**
 * Identity of the authenticated principal extracted from the JWT, made
 * available to service code without depending on Spring Security types.
 *
 * @param subject    the Keycloak user id (JWT {@code sub} claim)
 * @param username   preferred username
 * @param email      email claim, may be null
 * @param roles      realm roles granted to the user
 * @param customerId optional customer id (custom claim), null when absent
 * @param courierId  optional courier id (custom claim), null when absent
 */
public record AuthenticatedUser(
        String subject,
        String username,
        String email,
        Set<String> roles,
        UUID customerId,
        UUID courierId
) {
}
