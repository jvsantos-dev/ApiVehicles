package com.ford.vehicleapi.security;

import com.ford.vehicleapi.domain.Role;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/**
 * Usuário autenticado extraído das claims do JWT validado.
 * Evita uma consulta ao banco em cada requisição para saber "quem" e "qual perfil".
 */
public record CurrentUser(Long id, String email, Role role) {

    public static CurrentUser from(Jwt jwt) {
        Long id = Long.valueOf(jwt.getClaimAsString(JwtConfig.USER_ID_CLAIM));
        List<String> roles = jwt.getClaimAsStringList(JwtConfig.ROLES_CLAIM);
        return new CurrentUser(id, jwt.getSubject(), Role.valueOf(roles.get(0)));
    }

    public boolean isStaff() {
        return role == Role.ADMIN || role == Role.DEALER;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
