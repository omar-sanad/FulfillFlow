package com.fulfillflow.common.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Extracts an {@link AuthenticatedUser} from the Spring Security context for
 * the current request. Centralises claim access so controllers and services
 * stay free of JWT parsing details.
 */
@Component
public class SecurityContextHelper {

    private static final String REALM_ACCESS = "realm_access";
    private static final String ROLES = "roles";
    private static final String PREFERRED_USERNAME = "preferred_username";
    private static final String EMAIL = "email";
    private static final String CUSTOMER_ID = "customer_id";
    private static final String COURIER_ID = "courier_id";

    /**
     * @return the current authenticated user, or {@code null} when no JWT is present
     */
    public AuthenticatedUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }
        return fromJwt(jwt, auth.getAuthorities());
    }

    /**
     * @return the current authenticated user, throwing if absent
     */
    public AuthenticatedUser requireUser() {
        AuthenticatedUser user = currentUser();
        if (user == null) {
            throw new IllegalStateException("No authenticated user in security context");
        }
        return user;
    }

    private AuthenticatedUser fromJwt(Jwt jwt, Collection<? extends GrantedAuthority> authorities) {
        Set<String> roles = new HashSet<>();
        // Realm roles from the realm_access claim
        Object realmAccess = jwt.getClaim(REALM_ACCESS);
        if (realmAccess instanceof Map<?, ?> ra && ra.get(ROLES) instanceof Collection<?> rc) {
            rc.forEach(r -> roles.add(String.valueOf(r)));
        }
        // Also include Spring Security authorities (ROLE_ prefixed)
        authorities.forEach(a -> roles.add(a.getAuthority()));

        return new AuthenticatedUser(
                jwt.getSubject(),
                jwt.getClaim(PREFERRED_USERNAME),
                jwt.getClaim(EMAIL),
                roles,
                uuidClaim(jwt, CUSTOMER_ID),
                uuidClaim(jwt, COURIER_ID)
        );
    }

    private UUID uuidClaim(Jwt jwt, String claim) {
        Object value = jwt.getClaim(claim);
        if (value == null) {
            return null;
        }
        if (value instanceof UUID u) {
            return u;
        }
        try {
            return UUID.fromString(String.valueOf(value));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
