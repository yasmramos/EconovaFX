package com.econovafx.security;

import java.util.HashSet;
import java.util.Set;

/**
 * Clase que representa un permiso granular en el sistema.
 * Los permisos controlan el acceso a operaciones específicas.
 */
public class Permission {
    
    private final String code;
    private final String name;
    private final String description;
    private final String module;
    
    public Permission(String code, String name, String description, String module) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.module = module;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getModule() {
        return module;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return code.equals(that.code);
    }
    
    @Override
    public int hashCode() {
        return code.hashCode();
    }
    
    @Override
    public String toString() {
        return "Permission{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", module='" + module + '\'' +
                '}';
    }
}
