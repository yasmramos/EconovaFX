package com.econovafx.modules.core.security;

import org.mindrot.jbcrypt.BCrypt;
import io.avaje.inject.Component;

/**
 * Servicio para el manejo de contraseñas usando BCrypt.
 */
@Component
public class PasswordService {

    private static final int STRENGTH = 12;

    /**
     * Hashea una contraseña en texto plano.
     */
    public String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(STRENGTH));
    }

    /**
     * Verifica si una contraseña en texto plano coincide con un hash.
     */
    public boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            throw new IllegalArgumentException("Password and hashed password cannot be null");
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Manejo de hash inválido
            return false;
        }
    }
}
