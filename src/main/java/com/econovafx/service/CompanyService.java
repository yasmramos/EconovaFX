package com.econovafx.service;

import com.econovafx.config.DatabaseConfig;
import com.econovafx.config.TenantContext;
import com.econovafx.domain.Company;
import com.econovafx.repository.CompanyRepository;
import io.ebean.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de empresas en el sistema multi-tenant.
 */
@Singleton
public class CompanyService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyService.class);

    @Inject
    private CompanyRepository companyRepository;

    /**
     * Obtiene todas las empresas activas.
     */
    public List<Company> findAllActive() {
        return companyRepository.findAllActive();
    }

    /**
     * Obtiene todas las empresas (activas e inactivas).
     */
    public List<Company> findAll() {
        return companyRepository.findAll();
    }

    /**
     * Busca una empresa por su ID.
     */
    public Optional<Company> findById(Long id) {
        return companyRepository.findById(id);
    }

    /**
     * Busca una empresa por su código.
     */
    public Optional<Company> findByCode(String code) {
        return companyRepository.findByCode(code);
    }

    /**
     * Crea o actualiza una empresa.
     * Si es nueva, genera automáticamente la URL de la base de datos.
     */
    @Transactional
    public Company save(Company company) {
        // Generar URL de BD si no existe
        if (company.getDatabaseUrl() == null || company.getDatabaseUrl().isEmpty()) {
            String dbUrl = generateDatabaseUrl(company.getCode());
            company.setDatabaseUrl(dbUrl);
        }

        Company saved = companyRepository.save(company);
        logger.info("Empresa guardada: {} ({})", saved.getName(), saved.getCode());
        return saved;
    }

    /**
     * Elimina una empresa y cierra su conexión a la base de datos.
     */
    @Transactional
    public void delete(Long companyId) {
        // Cerrar conexión a la BD del tenant
        DatabaseConfig.closeTenantDatabase(companyId);
        
        // Eliminar registro
        companyRepository.deleteById(companyId);
        logger.info("Empresa eliminada: ID {}", companyId);
    }

    /**
     * Cambia el estado de una empresa.
     */
    @Transactional
    public void updateStatus(Long companyId, String status) {
        companyRepository.updateStatus(companyId, status);
        logger.info("Estado de empresa actualizado: ID {} -> {}", companyId, status);
    }

    /**
     * Selecciona una empresa como tenant activo en el contexto actual.
     * Esto configura la conexión a la base de datos específica de esa empresa.
     */
    public void selectTenant(Company company) {
        if (company == null) {
            TenantContext.clear();
            logger.info("Tenant context cleared");
            return;
        }

        // Verificar que la empresa esté activa
        if (!"ACTIVE".equals(company.getStatus())) {
            throw new IllegalStateException("La empresa '" + company.getName() + "' no está activa");
        }

        // Establecer en el contexto
        TenantContext.setCurrentTenant(company);
        
        // Inicializar/obtener la BD del tenant
        DatabaseConfig.getOrCreateTenantDatabase(company);
        
        logger.info("Tenant seleccionado: {} ({})", company.getName(), company.getCode());
    }

    /**
     * Obtiene la empresa actualmente seleccionada como tenant.
     */
    public Optional<Company> getCurrentTenant() {
        return Optional.ofNullable(TenantContext.getCurrentTenant());
    }

    /**
     * Genera una URL de base de datos para una nueva empresa.
     */
    private String generateDatabaseUrl(String companyCode) {
        // Para H2: jdbc:h2:./db/tenants/econova_{code}
        return "jdbc:h2:./db/tenants/econova_" + companyCode.toLowerCase();
    }

    /**
     * Inicializa la empresa por defecto si no existe ninguna.
     */
    @Transactional
    public void initializeDefaultCompany() {
        List<Company> companies = companyRepository.findAll();
        if (companies.isEmpty()) {
            logger.info("Creating default company...");
            Company defaultCompany = new Company(
                "Empresa Demo",
                "DEMO",
                "000000000"
            );
            defaultCompany.setAddress("Dirección Demo, Ciudad");
            defaultCompany.setPhone("+53 7 1234567");
            defaultCompany.setEmail("demo@econovafx.com");
            defaultCompany.setDatabaseUrl(generateDatabaseUrl("DEMO"));
            
            companyRepository.save(defaultCompany);
            logger.info("Default company created: {}", defaultCompany.getCode());
        }
    }
}
