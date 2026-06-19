package com.econovafx.security;

import java.util.*;

/**
 * Definicion de todos los permisos disponibles en el sistema.
 * Organizados por modulos para facilitar la gestion.
 */
public class Permissions {
    
    // Modulo de Compania/Tenant
    public static final Permission COMPANY_VIEW = new Permission("COMPANY_VIEW", "Ver Compania", "Puede ver informacion de la compania", "COMPANY");
    public static final Permission COMPANY_CREATE = new Permission("COMPANY_CREATE", "Crear Compania", "Puede crear nuevas companias", "COMPANY");
    public static final Permission COMPANY_EDIT = new Permission("COMPANY_EDIT", "Editar Compania", "Puede editar informacion de la compania", "COMPANY");
    public static final Permission COMPANY_DELETE = new Permission("COMPANY_DELETE", "Eliminar Compania", "Puede eliminar companias", "COMPANY");
    
    // Modulo de Usuarios
    public static final Permission USER_VIEW = new Permission("USER_VIEW", "Ver Usuarios", "Puede ver usuarios del sistema", "USER");
    public static final Permission USER_CREATE = new Permission("USER_CREATE", "Crear Usuario", "Puede crear nuevos usuarios", "USER");
    public static final Permission USER_EDIT = new Permission("USER_EDIT", "Editar Usuario", "Puede editar usuarios existentes", "USER");
    public static final Permission USER_DELETE = new Permission("USER_DELETE", "Eliminar Usuario", "Puede eliminar usuarios", "USER");
    public static final Permission USER_CHANGE_PASSWORD = new Permission("USER_CHANGE_PASSWORD", "Cambiar Contrasena", "Puede cambiar contrasenas de usuarios", "USER");
    public static final Permission USER_ASSIGN_ROLE = new Permission("USER_ASSIGN_ROLE", "Asignar Rol", "Puede asignar roles a usuarios", "USER");
    
    // Modulo de Contabilidad - Transacciones
    public static final Permission TRANSACTION_VIEW = new Permission("TRANSACTION_VIEW", "Ver Transacciones", "Puede ver transacciones contables", "TRANSACTION");
    public static final Permission TRANSACTION_CREATE = new Permission("TRANSACTION_CREATE", "Crear Transaccion", "Puede crear nuevas transacciones", "TRANSACTION");
    public static final Permission TRANSACTION_EDIT = new Permission("TRANSACTION_EDIT", "Editar Transaccion", "Puede editar transacciones no publicadas", "TRANSACTION");
    public static final Permission TRANSACTION_DELETE = new Permission("TRANSACTION_DELETE", "Eliminar Transaccion", "Puede eliminar transacciones no publicadas", "TRANSACTION");
    public static final Permission TRANSACTION_POST = new Permission("TRANSACTION_POST", "Publicar Transaccion", "Puede publicar transacciones (afectar saldos)", "TRANSACTION");
    public static final Permission TRANSACTION_REVERSE = new Permission("TRANSACTION_REVERSE", "Reversar Transaccion", "Puede reversar transacciones publicadas", "TRANSACTION");
    public static final Permission TRANSACTION_EXPORT = new Permission("TRANSACTION_EXPORT", "Exportar Transacciones", "Puede exportar transacciones", "TRANSACTION");
    
    // Modulo de Contabilidad - Cuentas
    public static final Permission ACCOUNT_VIEW = new Permission("ACCOUNT_VIEW", "Ver Cuentas", "Puede ver el plan de cuentas", "ACCOUNT");
    public static final Permission ACCOUNT_CREATE = new Permission("ACCOUNT_CREATE", "Crear Cuenta", "Puede crear nuevas cuentas contables", "ACCOUNT");
    public static final Permission ACCOUNT_EDIT = new Permission("ACCOUNT_EDIT", "Editar Cuenta", "Puede editar cuentas contables", "ACCOUNT");
    public static final Permission ACCOUNT_DELETE = new Permission("ACCOUNT_DELETE", "Eliminar Cuenta", "Puede eliminar cuentas contables", "ACCOUNT");
    
    // Modulo de Periodos Contables
    public static final Permission PERIOD_VIEW = new Permission("PERIOD_VIEW", "Ver Periodos", "Puede ver periodos contables", "PERIOD");
    public static final Permission PERIOD_OPEN = new Permission("PERIOD_OPEN", "Abrir Periodo", "Puede abrir periodos contables", "PERIOD");
    public static final Permission PERIOD_CLOSE = new Permission("PERIOD_CLOSE", "Cerrar Periodo", "Puede cerrar periodos contables", "PERIOD");
    public static final Permission PERIOD_LOCK = new Permission("PERIOD_LOCK", "Bloquear Periodo", "Puede bloquear periodos contables", "PERIOD");
    
    // Modulo de Monedas
    public static final Permission CURRENCY_VIEW = new Permission("CURRENCY_VIEW", "Ver Monedas", "Puede ver monedas y tasas de cambio", "CURRENCY");
    public static final Permission CURRENCY_CREATE = new Permission("CURRENCY_CREATE", "Crear Moneda", "Puede crear nuevas monedas", "CURRENCY");
    public static final Permission CURRENCY_EDIT = new Permission("CURRENCY_EDIT", "Editar Moneda", "Puede editar monedas existentes", "CURRENCY");
    public static final Permission CURRENCY_RATE_UPDATE = new Permission("CURRENCY_RATE_UPDATE", "Actualizar Tasa de Cambio", "Puede actualizar tasas de cambio", "CURRENCY");
    
    // Modulo de Terceros
    public static final Permission THIRD_PARTY_VIEW = new Permission("THIRD_PARTY_VIEW", "Ver Terceros", "Puede ver terceros (clientes/proveedores)", "THIRD_PARTY");
    public static final Permission THIRD_PARTY_CREATE = new Permission("THIRD_PARTY_CREATE", "Crear Tercero", "Puede crear nuevos terceros", "THIRD_PARTY");
    public static final Permission THIRD_PARTY_EDIT = new Permission("THIRD_PARTY_EDIT", "Editar Tercero", "Puede editar terceros existentes", "THIRD_PARTY");
    public static final Permission THIRD_PARTY_DELETE = new Permission("THIRD_PARTY_DELETE", "Eliminar Tercero", "Puede eliminar terceros", "THIRD_PARTY");
    
    // Modulo de Inventarios
    public static final Permission INVENTORY_VIEW = new Permission("INVENTORY_VIEW", "Ver Inventarios", "Puede ver inventarios", "INVENTORY");
    public static final Permission INVENTORY_CREATE = new Permission("INVENTORY_CREATE", "Crear Item de Inventario", "Puede crear items de inventario", "INVENTORY");
    public static final Permission INVENTORY_EDIT = new Permission("INVENTORY_EDIT", "Editar Inventario", "Puede editar items de inventario", "INVENTORY");
    public static final Permission INVENTORY_DELETE = new Permission("INVENTORY_DELETE", "Eliminar Inventario", "Puede eliminar items de inventario", "INVENTORY");
    public static final Permission INVENTORY_MOVEMENT = new Permission("INVENTORY_MOVEMENT", "Movimiento de Inventario", "Puede realizar movimientos de inventario", "INVENTORY");
    
    // Modulo de Reportes
    public static final Permission REPORT_VIEW = new Permission("REPORT_VIEW", "Ver Reportes", "Puede ver reportes del sistema", "REPORT");
    public static final Permission REPORT_EXPORT = new Permission("REPORT_EXPORT", "Exportar Reportes", "Puede exportar reportes", "REPORT");
    public static final Permission REPORT_CONFIGURE = new Permission("REPORT_CONFIGURE", "Configurar Reportes", "Puede configurar reportes personalizados", "REPORT");
    
    // Modulo de Auditoria
    public static final Permission AUDIT_VIEW = new Permission("AUDIT_VIEW", "Ver Auditoria", "Puede ver logs de auditoria", "AUDIT");
    public static final Permission AUDIT_EXPORT = new Permission("AUDIT_EXPORT", "Exportar Auditoria", "Puede exportar logs de auditoria", "AUDIT");
    
    // Modulo de Configuracion del Sistema
    public static final Permission SYSTEM_CONFIG_VIEW = new Permission("SYSTEM_CONFIG_VIEW", "Ver Configuracion", "Puede ver configuracion del sistema", "SYSTEM");
    public static final Permission SYSTEM_CONFIG_EDIT = new Permission("SYSTEM_CONFIG_EDIT", "Editar Configuracion", "Puede editar configuracion del sistema", "SYSTEM");
    
    // Roles predefinidos con sus permisos
    private static final Map<String, Set<Permission>> ROLE_PERMISSIONS = new HashMap<>();
    
    static {
        // Rol ADMIN - Todos los permisos
        Set<Permission> adminPermissions = getAllPermissions();
        ROLE_PERMISSIONS.put("ADMIN", adminPermissions);
        
        // Rol ACCOUNTANT - Permisos contables completos
        Set<Permission> accountantPermissions = new HashSet<>();
        accountantPermissions.add(TRANSACTION_VIEW);
        accountantPermissions.add(TRANSACTION_CREATE);
        accountantPermissions.add(TRANSACTION_EDIT);
        accountantPermissions.add(TRANSACTION_POST);
        accountantPermissions.add(TRANSACTION_REVERSE);
        accountantPermissions.add(TRANSACTION_EXPORT);
        accountantPermissions.add(ACCOUNT_VIEW);
        accountantPermissions.add(ACCOUNT_CREATE);
        accountantPermissions.add(ACCOUNT_EDIT);
        accountantPermissions.add(PERIOD_VIEW);
        accountantPermissions.add(PERIOD_OPEN);
        accountantPermissions.add(PERIOD_CLOSE);
        accountantPermissions.add(CURRENCY_VIEW);
        accountantPermissions.add(CURRENCY_RATE_UPDATE);
        accountantPermissions.add(THIRD_PARTY_VIEW);
        accountantPermissions.add(THIRD_PARTY_CREATE);
        accountantPermissions.add(THIRD_PARTY_EDIT);
        accountantPermissions.add(INVENTORY_VIEW);
        accountantPermissions.add(INVENTORY_MOVEMENT);
        accountantPermissions.add(REPORT_VIEW);
        accountantPermissions.add(REPORT_EXPORT);
        accountantPermissions.add(AUDIT_VIEW);
        ROLE_PERMISSIONS.put("ACCOUNTANT", accountantPermissions);
        
        // Rol USER - Permisos basicos
        Set<Permission> userPermissions = new HashSet<>();
        userPermissions.add(TRANSACTION_VIEW);
        userPermissions.add(TRANSACTION_CREATE);
        userPermissions.add(ACCOUNT_VIEW);
        userPermissions.add(PERIOD_VIEW);
        userPermissions.add(CURRENCY_VIEW);
        userPermissions.add(THIRD_PARTY_VIEW);
        userPermissions.add(INVENTORY_VIEW);
        userPermissions.add(REPORT_VIEW);
        ROLE_PERMISSIONS.put("USER", userPermissions);
        
        // Rol VIEWER - Solo lectura
        Set<Permission> viewerPermissions = new HashSet<>();
        viewerPermissions.add(TRANSACTION_VIEW);
        viewerPermissions.add(ACCOUNT_VIEW);
        viewerPermissions.add(PERIOD_VIEW);
        viewerPermissions.add(CURRENCY_VIEW);
        viewerPermissions.add(THIRD_PARTY_VIEW);
        viewerPermissions.add(INVENTORY_VIEW);
        viewerPermissions.add(REPORT_VIEW);
        ROLE_PERMISSIONS.put("VIEWER", viewerPermissions);
    }
    
    /**
     * Obtiene todos los permisos definidos en el sistema
     */
    public static Set<Permission> getAllPermissions() {
        Set<Permission> allPermissions = new HashSet<>();
        allPermissions.add(COMPANY_VIEW);
        allPermissions.add(COMPANY_CREATE);
        allPermissions.add(COMPANY_EDIT);
        allPermissions.add(COMPANY_DELETE);
        allPermissions.add(USER_VIEW);
        allPermissions.add(USER_CREATE);
        allPermissions.add(USER_EDIT);
        allPermissions.add(USER_DELETE);
        allPermissions.add(USER_CHANGE_PASSWORD);
        allPermissions.add(USER_ASSIGN_ROLE);
        allPermissions.add(TRANSACTION_VIEW);
        allPermissions.add(TRANSACTION_CREATE);
        allPermissions.add(TRANSACTION_EDIT);
        allPermissions.add(TRANSACTION_DELETE);
        allPermissions.add(TRANSACTION_POST);
        allPermissions.add(TRANSACTION_REVERSE);
        allPermissions.add(TRANSACTION_EXPORT);
        allPermissions.add(ACCOUNT_VIEW);
        allPermissions.add(ACCOUNT_CREATE);
        allPermissions.add(ACCOUNT_EDIT);
        allPermissions.add(ACCOUNT_DELETE);
        allPermissions.add(PERIOD_VIEW);
        allPermissions.add(PERIOD_OPEN);
        allPermissions.add(PERIOD_CLOSE);
        allPermissions.add(PERIOD_LOCK);
        allPermissions.add(CURRENCY_VIEW);
        allPermissions.add(CURRENCY_CREATE);
        allPermissions.add(CURRENCY_EDIT);
        allPermissions.add(CURRENCY_RATE_UPDATE);
        allPermissions.add(THIRD_PARTY_VIEW);
        allPermissions.add(THIRD_PARTY_CREATE);
        allPermissions.add(THIRD_PARTY_EDIT);
        allPermissions.add(THIRD_PARTY_DELETE);
        allPermissions.add(INVENTORY_VIEW);
        allPermissions.add(INVENTORY_CREATE);
        allPermissions.add(INVENTORY_EDIT);
        allPermissions.add(INVENTORY_DELETE);
        allPermissions.add(INVENTORY_MOVEMENT);
        allPermissions.add(REPORT_VIEW);
        allPermissions.add(REPORT_EXPORT);
        allPermissions.add(REPORT_CONFIGURE);
        allPermissions.add(AUDIT_VIEW);
        allPermissions.add(AUDIT_EXPORT);
        allPermissions.add(SYSTEM_CONFIG_VIEW);
        allPermissions.add(SYSTEM_CONFIG_EDIT);
        return allPermissions;
    }
    
    /**
     * Obtiene los permisos asociados a un rol especifico
     * @param roleName Nombre del rol (ADMIN, ACCOUNTANT, USER, VIEWER)
     * @return Conjunto de permisos para el rol
     */
    public static Set<Permission> getPermissionsForRole(String roleName) {
        Set<Permission> permissions = ROLE_PERMISSIONS.get(roleName.toUpperCase());
        if (permissions == null) {
            return ROLE_PERMISSIONS.get("VIEWER");
        }
        return Collections.unmodifiableSet(permissions);
    }
    
    /**
     * Verifica si un rol tiene un permiso especifico
     * @param roleName Nombre del rol
     * @param permissionCode Codigo del permiso a verificar
     * @return true si el rol tiene el permiso, false en caso contrario
     */
    public static boolean hasPermission(String roleName, String permissionCode) {
        Set<Permission> permissions = getPermissionsForRole(roleName);
        for (Permission permission : permissions) {
            if (permission.getCode().equals(permissionCode)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Obtiene todos los permisos de un modulo especifico
     * @param module Nombre del modulo
     * @return Lista de permisos del modulo
     */
    public static List<Permission> getPermissionsByModule(String module) {
        List<Permission> modulePermissions = new ArrayList<>();
        for (Permission permission : getAllPermissions()) {
            if (permission.getModule().equals(module.toUpperCase())) {
                modulePermissions.add(permission);
            }
        }
        return modulePermissions;
    }
}
