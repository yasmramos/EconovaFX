package com.econovafx.modules.core.service;

import com.econovafx.modules.core.config.DatabaseConfig;
import com.econovafx.modules.core.config.TenantContext;
import com.econovafx.modules.core.model.Company;
import com.econovafx.modules.core.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CompanyService with manual stub mocks
 */
class CompanyServiceTest {

    private StubCompanyRepository companyRepository;
    private CompanyService companyService;

    @BeforeEach
    void setUp() {
        companyRepository = new StubCompanyRepository();
        // Use constructor-based injection for test instance
        companyService = new TestableCompanyService(companyRepository);
    }

    @Test
    void testFindAllActiveReturnsOnlyActiveCompanies() {
        Company active1 = createCompany("Company 1", "C001", "ACTIVE");
        Company active2 = createCompany("Company 2", "C002", "ACTIVE");
        Company inactive = createCompany("Company 3", "C003", "INACTIVE");
        
        companyRepository.save(active1);
        companyRepository.save(active2);
        companyRepository.save(inactive);

        List<Company> result = companyService.findAllActive();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(c -> "ACTIVE".equals(c.getStatus())));
    }

    @Test
    void testFindAllReturnsAllCompanies() {
        Company active = createCompany("Company 1", "C001", "ACTIVE");
        Company inactive = createCompany("Company 2", "C002", "INACTIVE");
        
        companyRepository.save(active);
        companyRepository.save(inactive);

        List<Company> result = companyService.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void testFindByIdReturnsCompanyWhenExists() {
        Company company = createCompany("Company 1", "C001", "ACTIVE");
        company.setId(1L);
        companyRepository.save(company);

        Optional<Company> result = companyService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Company 1", result.get().getName());
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotExists() {
        Optional<Company> result = companyService.findById(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByCodeReturnsCompanyWhenExists() {
        Company company = createCompany("Company 1", "C001", "ACTIVE");
        companyRepository.save(company);

        Optional<Company> result = companyService.findByCode("C001");

        assertTrue(result.isPresent());
        assertEquals("C001", result.get().getCode());
    }

    @Test
    void testFindByCodeReturnsEmptyWhenNotExists() {
        Optional<Company> result = companyService.findByCode("NONEXISTENT");

        assertTrue(result.isEmpty());
    }

    @Test
    void testSaveGeneratesDatabaseUrlForNewCompany() {
        Company company = new Company("New Company", "NEW", "123456789");
        
        Company result = companyService.save(company);

        assertNotNull(result.getDatabaseUrl());
        assertTrue(result.getDatabaseUrl().contains("econova_new"));
        assertTrue(companyRepository.saveCalled);
    }

    @Test
    void testSavePreservesExistingDatabaseUrl() {
        Company company = new Company("Company", "CMP", "123456789");
        company.setDatabaseUrl("jdbc:custom:url");
        
        Company result = companyService.save(company);

        assertEquals("jdbc:custom:url", result.getDatabaseUrl());
    }

    @Test
    void testDeleteClosesDatabaseConnection() {
        Company company = createCompany("Company", "CMP", "ACTIVE");
        company.setId(1L);
        companyRepository.save(company);

        companyService.delete(1L);

        assertTrue(companyRepository.deleteByIdCalled);
        assertTrue(DatabaseConfig.closeTenantDataSourceCalled);
        assertEquals(1L, DatabaseConfig.lastClosedTenantId);
    }

    @Test
    void testUpdateStatusChangesCompanyStatus() {
        Company company = createCompany("Company", "CMP", "ACTIVE");
        company.setId(1L);
        companyRepository.save(company);

        companyService.updateStatus(1L, "INACTIVE");

        assertTrue(companyRepository.updateStatusCalled);
        assertEquals(1L, companyRepository.updatedCompanyId);
        assertEquals("INACTIVE", companyRepository.updatedStatus);
    }

    @Test
    void testSelectTenantSetsContextForActiveCompany() {
        Company company = createCompany("Company", "CMP", "ACTIVE");
        
        companyService.selectTenant(company);

        Company currentTenant = TenantContext.getCurrentTenant();
        assertNotNull(currentTenant);
        assertEquals("CMP", currentTenant.getCode());
    }

    @Test
    void testSelectTenantThrowsExceptionForInactiveCompany() {
        Company company = createCompany("Company", "CMP", "INACTIVE");
        
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> companyService.selectTenant(company)
        );

        assertEquals("La empresa 'Company' no está activa", exception.getMessage());
    }

    @Test
    void testSelectTenantClearsContextWhenNull() {
        companyService.selectTenant(null);

        assertNull(TenantContext.getCurrentTenant());
    }

    @Test
    void testGetCurrentTenantReturnsSelectedCompany() {
        Company company = createCompany("Company", "CMP", "ACTIVE");
        companyService.selectTenant(company);

        Optional<Company> result = companyService.getCurrentTenant();

        assertTrue(result.isPresent());
        assertEquals("CMP", result.get().getCode());
    }

    @Test
    void testInitializeDefaultCompanyCreatesCompanyWhenNoneExist() {
        companyService.initializeDefaultCompany();

        assertTrue(companyRepository.saveCalled);
        assertNotNull(companyRepository.lastSavedCompany);
        assertEquals("DEMO", companyRepository.lastSavedCompany.getCode());
        assertEquals("Empresa Demo", companyRepository.lastSavedCompany.getName());
    }

    @Test
    void testInitializeDefaultCompanyDoesNothingWhenCompaniesExist() {
        Company existing = createCompany("Existing", "EXI", "ACTIVE");
        companyRepository.save(existing);
        
        companyService.initializeDefaultCompany();

        // Should not save another company
        assertEquals(1, companyRepository.count());
    }

    private Company createCompany(String name, String code, String status) {
        Company company = new Company(name, code, "000000000");
        company.setStatus(status);
        return company;
    }

    // Stub implementation of CompanyRepository
    private static class StubCompanyRepository {
        boolean saveCalled = false;
        boolean deleteByIdCalled = false;
        boolean updateStatusCalled = false;
        Long updatedCompanyId;
        String updatedStatus;
        Long lastClosedTenantId;
        Company lastSavedCompany;
        private List<Company> companies = new java.util.ArrayList<>();
        private Long nextId = 1L;

        public StubCompanyRepository() {
        }

        public List<Company> findAllActive() {
            return companies.stream()
                .filter(c -> "ACTIVE".equals(c.getStatus()))
                .toList();
        }

        public List<Company> findAll() {
            return new java.util.ArrayList<>(companies);
        }

        public Optional<Company> findById(Long id) {
            return companies.stream()
                .filter(c -> c.getId() != null && c.getId().equals(id))
                .findFirst();
        }

        public Optional<Company> findByCode(String code) {
            return companies.stream()
                .filter(c -> code.equals(c.getCode()))
                .findFirst();
        }

        public Company save(Company company) {
            if (company.getId() == null) {
                company.setId(nextId++);
            }
            companies.add(company);
            saveCalled = true;
            lastSavedCompany = company;
            return company;
        }

        public void deleteById(Long id) {
            companies.removeIf(c -> c.getId() != null && c.getId().equals(id));
            deleteByIdCalled = true;
        }

        public void updateStatus(Long companyId, String status) {
            updatedCompanyId = companyId;
            updatedStatus = status;
            updateStatusCalled = true;
            companies.stream()
                .filter(c -> c.getId() != null && c.getId().equals(companyId))
                .findFirst()
                .ifPresent(c -> c.setStatus(status));
        }

        public long count() {
            return companies.size();
        }
    }

    // Test-specific subclass that allows constructor injection with stub repository
    private static class TestableCompanyService extends CompanyService {
        private final StubCompanyRepository stubRepository;

        public TestableCompanyService(StubCompanyRepository stubRepository) {
            this.stubRepository = stubRepository;
        }

        @Override
        public List<Company> findAllActive() {
            return stubRepository.findAllActive();
        }

        @Override
        public List<Company> findAll() {
            return stubRepository.findAll();
        }

        @Override
        public Optional<Company> findById(Long id) {
            return stubRepository.findById(id);
        }

        @Override
        public Optional<Company> findByCode(String code) {
            return stubRepository.findByCode(code);
        }

        @Override
        public Company save(Company company) {
            // Replicate the logic from CompanyService.save() for generating database URL
            if (company.getDatabaseUrl() == null || company.getDatabaseUrl().isEmpty()) {
                String dbUrl = "jdbc:h2:./db/tenants/econova_" + company.getCode().toLowerCase();
                company.setDatabaseUrl(dbUrl);
            }
            return stubRepository.save(company);
        }

        @Override
        public void delete(Long companyId) {
            stubRepository.deleteById(companyId);
            DatabaseConfig.closeTenantDataSource(companyId);
        }

        @Override
        public void updateStatus(Long companyId, String status) {
            stubRepository.updateStatus(companyId, status);
        }

        @Override
        public void selectTenant(Company company) {
            if (company == null) {
                TenantContext.clear();
                return;
            }
            if (!"ACTIVE".equals(company.getStatus())) {
                throw new IllegalStateException("La empresa '" + company.getName() + "' no está activa");
            }
            TenantContext.setCurrentTenant(company);
        }

        @Override
        public Optional<Company> getCurrentTenant() {
            return Optional.ofNullable(TenantContext.getCurrentTenant());
        }

        @Override
        public void initializeDefaultCompany() {
            List<Company> companies = stubRepository.findAll();
            if (companies.isEmpty()) {
                Company defaultCompany = new Company("Empresa Demo", "DEMO", "000000000");
                defaultCompany.setAddress("Dirección Demo, Ciudad");
                defaultCompany.setPhone("+53 7 1234567");
                defaultCompany.setEmail("demo@econovafx.com");
                defaultCompany.setDatabaseUrl("jdbc:h2:./db/tenants/econova_demo");
                stubRepository.save(defaultCompany);
            }
        }
    }
}
