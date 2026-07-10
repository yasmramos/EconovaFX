package com.econovafx.modules.core.model;

import io.ebean.annotation.*;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entidad para configuración del sistema según Resolución 340/2004.
 * Almacena parámetros generales, datos de la entidad y configuraciones contables.
 */
@Entity
@Table(name = "system_configuration")
public class SystemConfiguration extends BaseEntity {

    // ==================== DATOS DE LA ENTIDAD ====================
    
    @Column(length = 200)
    private String entityName;
    
    @Column(length = 300)
    private String entityAddress;
    
    @Column(length = 50)
    private String entityNIF; // Número de Identificación Fiscal
    
    @Column(length = 20)
    private String entityPhone;
    
    @Column(length = 100)
    private String entityEmail;
    
    @Column(length = 50)
    private String entityCode; // Código de la entidad
    
    @Column(length = 100)
    private String entitySector; // Sector al que pertenece
    
    // ==================== PARÁMETROS CONTABLES ====================
    
    @Column(length = 20)
    private String baseCurrency = "CUP"; // Moneda base (CUP, USD, EUR, etc.)
    
    @Column(length = 100)
    private String accountingPlan = "Normalizado"; // Plan de cuentas utilizado
    
    private Integer fiscalYearStartMonth = 1; // Mes de inicio del ejercicio fiscal (1=Enero)
    
    private Integer fiscalYearEndMonth = 12; // Mes de fin del ejercicio fiscal (12=Diciembre)
    
    @Column(length = 20)
    private String regimeType = "MONO"; // MONO o MULTI usuario
    
    // ==================== CONFIGURACIÓN DE SEGURIDAD ====================
    
    private Integer passwordMinLength = 6;
    
    private Boolean passwordRequireUppercase = false;
    
    private Boolean passwordRequireNumbers = false;
    
    private Integer passwordExpirationDays = 90;
    
    private Integer maxLoginAttempts = 5;
    
    private Integer sessionTimeoutMinutes = 30;
    
    // ==================== CONFIGURACIÓN DE AUDITORÍA ====================
    
    private Boolean auditEnabled = true;
    
    private Integer auditRetentionYears = 5; // Años de conservación de trazas
    
    private Boolean auditLogChanges = true; // Registrar cambios en registros
    
    // ==================== CONFIGURACIÓN DE REPORTES ====================
    
    @Column(length = 200)
    private String reportHeader; // Encabezado personalizado para reportes
    
    @Column(length = 200)
    private String reportFooter; // Pie de página personalizado
    
    private Boolean includeEntityLogo = true;
    
    @Column(length = 300)
    private String logoPath; // Ruta del logo de la entidad
    
    // ==================== CONFIGURACIÓN DE RESPALDOS ====================
    
    private Boolean autoBackupEnabled = false;
    
    private Integer backupFrequencyDays = 7;
    
    @Column(length = 300)
    private String backupPath; // Ruta donde se guardan los respaldos
    
    // ==================== OTROS PARÁMETROS ====================
    
    private Boolean allowNegativeInventory = false;
    
    private String inventoryValuationMethod = "PROMEDIO_PONDERADO"; // PEPS o PROMEDIO_PONDERADO
    
    private Integer decimalPrecision = 2;
    
    @Column(length = 50)
    private String timeZone = "America/Havana";
    
    @Column(length = 10)
    private String locale = "es_CU";
    
    // ==================== GETTERS Y SETTERS ====================
    
    public String getEntityName() {
        return entityName;
    }
    
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
    
    public String getEntityAddress() {
        return entityAddress;
    }
    
    public void setEntityAddress(String entityAddress) {
        this.entityAddress = entityAddress;
    }
    
    public String getEntityNIF() {
        return entityNIF;
    }
    
    public void setEntityNIF(String entityNIF) {
        this.entityNIF = entityNIF;
    }
    
    public String getEntityPhone() {
        return entityPhone;
    }
    
    public void setEntityPhone(String entityPhone) {
        this.entityPhone = entityPhone;
    }
    
    public String getEntityEmail() {
        return entityEmail;
    }
    
    public void setEntityEmail(String entityEmail) {
        this.entityEmail = entityEmail;
    }
    
    public String getEntityCode() {
        return entityCode;
    }
    
    public void setEntityCode(String entityCode) {
        this.entityCode = entityCode;
    }
    
    public String getEntitySector() {
        return entitySector;
    }
    
    public void setEntitySector(String entitySector) {
        this.entitySector = entitySector;
    }
    
    public String getBaseCurrency() {
        return baseCurrency;
    }
    
    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }
    
    public String getAccountingPlan() {
        return accountingPlan;
    }
    
    public void setAccountingPlan(String accountingPlan) {
        this.accountingPlan = accountingPlan;
    }
    
    public Integer getFiscalYearStartMonth() {
        return fiscalYearStartMonth;
    }
    
    public void setFiscalYearStartMonth(Integer fiscalYearStartMonth) {
        this.fiscalYearStartMonth = fiscalYearStartMonth;
    }
    
    public Integer getFiscalYearEndMonth() {
        return fiscalYearEndMonth;
    }
    
    public void setFiscalYearEndMonth(Integer fiscalYearEndMonth) {
        this.fiscalYearEndMonth = fiscalYearEndMonth;
    }
    
    public String getRegimeType() {
        return regimeType;
    }
    
    public void setRegimeType(String regimeType) {
        this.regimeType = regimeType;
    }
    
    public Integer getPasswordMinLength() {
        return passwordMinLength;
    }
    
    public void setPasswordMinLength(Integer passwordMinLength) {
        this.passwordMinLength = passwordMinLength;
    }
    
    public Boolean getPasswordRequireUppercase() {
        return passwordRequireUppercase;
    }
    
    public void setPasswordRequireUppercase(Boolean passwordRequireUppercase) {
        this.passwordRequireUppercase = passwordRequireUppercase;
    }
    
    public Boolean getPasswordRequireNumbers() {
        return passwordRequireNumbers;
    }
    
    public void setPasswordRequireNumbers(Boolean passwordRequireNumbers) {
        this.passwordRequireNumbers = passwordRequireNumbers;
    }
    
    public Integer getPasswordExpirationDays() {
        return passwordExpirationDays;
    }
    
    public void setPasswordExpirationDays(Integer passwordExpirationDays) {
        this.passwordExpirationDays = passwordExpirationDays;
    }
    
    public Integer getMaxLoginAttempts() {
        return maxLoginAttempts;
    }
    
    public void setMaxLoginAttempts(Integer maxLoginAttempts) {
        this.maxLoginAttempts = maxLoginAttempts;
    }
    
    public Integer getSessionTimeoutMinutes() {
        return sessionTimeoutMinutes;
    }
    
    public void setSessionTimeoutMinutes(Integer sessionTimeoutMinutes) {
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
    }
    
    public Boolean getAuditEnabled() {
        return auditEnabled;
    }
    
    public void setAuditEnabled(Boolean auditEnabled) {
        this.auditEnabled = auditEnabled;
    }
    
    public Integer getAuditRetentionYears() {
        return auditRetentionYears;
    }
    
    public void setAuditRetentionYears(Integer auditRetentionYears) {
        this.auditRetentionYears = auditRetentionYears;
    }
    
    public Boolean getAuditLogChanges() {
        return auditLogChanges;
    }
    
    public void setAuditLogChanges(Boolean auditLogChanges) {
        this.auditLogChanges = auditLogChanges;
    }
    
    public String getReportHeader() {
        return reportHeader;
    }
    
    public void setReportHeader(String reportHeader) {
        this.reportHeader = reportHeader;
    }
    
    public String getReportFooter() {
        return reportFooter;
    }
    
    public void setReportFooter(String reportFooter) {
        this.reportFooter = reportFooter;
    }
    
    public Boolean getIncludeEntityLogo() {
        return includeEntityLogo;
    }
    
    public void setIncludeEntityLogo(Boolean includeEntityLogo) {
        this.includeEntityLogo = includeEntityLogo;
    }
    
    public String getLogoPath() {
        return logoPath;
    }
    
    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }
    
    public Boolean getAutoBackupEnabled() {
        return autoBackupEnabled;
    }
    
    public void setAutoBackupEnabled(Boolean autoBackupEnabled) {
        this.autoBackupEnabled = autoBackupEnabled;
    }
    
    public Integer getBackupFrequencyDays() {
        return backupFrequencyDays;
    }
    
    public void setBackupFrequencyDays(Integer backupFrequencyDays) {
        this.backupFrequencyDays = backupFrequencyDays;
    }
    
    public String getBackupPath() {
        return backupPath;
    }
    
    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }
    
    public Boolean getAllowNegativeInventory() {
        return allowNegativeInventory;
    }
    
    public void setAllowNegativeInventory(Boolean allowNegativeInventory) {
        this.allowNegativeInventory = allowNegativeInventory;
    }
    
    public String getInventoryValuationMethod() {
        return inventoryValuationMethod;
    }
    
    public void setInventoryValuationMethod(String inventoryValuationMethod) {
        this.inventoryValuationMethod = inventoryValuationMethod;
    }
    
    public Integer getDecimalPrecision() {
        return decimalPrecision;
    }
    
    public void setDecimalPrecision(Integer decimalPrecision) {
        this.decimalPrecision = decimalPrecision;
    }
    
    public String getTimeZone() {
        return timeZone;
    }
    
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
    
    public String getLocale() {
        return locale;
    }
    
    public void setLocale(String locale) {
        this.locale = locale;
    }
}
