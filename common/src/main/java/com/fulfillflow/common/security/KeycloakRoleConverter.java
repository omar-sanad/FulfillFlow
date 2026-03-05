package com.fulfillflow.common.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Converts Keycloak realm roles (nested under the {@code realm_access.roles}
 * claim) into Spring Security {@code ROLE_} authorities. Keycloak's default
 * token layout puts realm roles in a nested object, which Spring Security's
 * default converter does not understand.
 */
@Component
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String REALM_ACCESS = "realm_access";
    private static final String ROLES = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        Object realmAccess = jwt.getClaim(REALM_ACCESS);
        if (realmAccess instanceof Map<?, ?> ra && ra.get(ROLES) instanceof Collection<?> roles) {
            for (Object role : roles) {
                authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role));
            }
        }
        return authorities;
    }

    /**
     * Convenience method listing role names without the ROLE_ prefix.
     */
    public List<String> roleNames(Jwt jwt) {
        return convert(jwt).stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.substring(ROLE_PREFIX.length()))
                .toList();
    }
}
