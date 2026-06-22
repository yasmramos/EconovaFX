package com.econovafx.security;

import com.econovafx.config.TenantContext;
import com.econovafx.domain.Company;
import com.econovafx.domain.User;
import io.ebean.DB;
import io.ebean.Query;
import jakarta.inject.Singleton;

import java.util.Optional;

/**
 * Servicio de autenticación y autorización.
 */
@Singleton
public class AuthService {

    private final PasswordService passwordService;

    public AuthService() {
        this.passwordService = new PasswordService();
    }

    /**
     * Autentica un usuario por email y contraseña.
     * @return El usuario autenticado o null si falla.
     */
    public User authenticate(String email, String password) {
        Query<User> query = DB.find(User.class).where().eq("email", email).query();
        Optional<User> userOpt = query.findOneOrEmpty();
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordService.checkPassword(password, user.getPassword())) {
                // Establecer el tenant actual (la empresa del usuario)
                if (user.getCompany() != null) {
                    TenantContext.setCurrentTenant(user.getCompany());
                }
                return user;
            }
        }
        return null;
    }

    /**
     * Verifica si el usuario tiene un rol específico.
     */
    public boolean hasRole(User user, User.UserRole role) {
        return user != null && user.getRole() == role;
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    public void logout() {
        TenantContext.clear();
    }
}
