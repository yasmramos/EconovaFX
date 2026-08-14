package com.econovafx.modules.core.config;

import com.econovafx.modules.core.model.Company;
import com.econovafx.modules.core.model.Currency;
import com.econovafx.modules.core.model.User;
import com.econovafx.modules.core.repository.CompanyRepository;
import com.econovafx.modules.core.repository.CurrencyRepository;
import com.econovafx.modules.core.repository.UserRepository;
import com.econovafx.modules.core.security.PasswordService;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database seeder for initializing default data on first application startup.
 * Creates default company, currencies, and admin user if they don't exist.
 */
@Singleton
public class DatabaseSeeder {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CurrencyRepository currencyRepository;
    private final PasswordService passwordService;

    public DatabaseSeeder() {
        this.userRepository = new UserRepository(io.ebean.DB.getDefault());
        this.companyRepository = new CompanyRepository(io.ebean.DB.getDefault());
        this.currencyRepository = new CurrencyRepository(io.ebean.DB.getDefault());
        this.passwordService = new PasswordService();
    }

    /**
     * Seeds the database with initial data if needed.
     */
    public void seed() {
        logger.info("Checking if database seeding is required...");
        
        seedCurrencies();
        seedDefaultCompany();
        seedAdminUser();
        
        logger.info("Database seeding completed successfully");
    }

    /**
     * Seeds base currencies if they don't exist.
     */
    private void seedCurrencies() {
        if (currencyRepository.findAll().isEmpty()) {
            logger.info("Seeding base currencies...");
            
            Currency usd = new Currency("USD", "US Dollar", "$");
            Currency eur = new Currency("EUR", "Euro", "€");
            Currency cup = new Currency("CUP", "Cuban Peso", "$");
            Currency cuc = new Currency("CUC", "Cuban Convertible Peso", "$");
            
            currencyRepository.save(usd);
            currencyRepository.save(eur);
            currencyRepository.save(cup);
            currencyRepository.save(cuc);
            
            logger.info("Created {} base currencies", 4);
        } else {
            logger.debug("Currencies already exist, skipping seeding");
        }
    }

    /**
     * Seeds default company if it doesn't exist.
     */
    private void seedDefaultCompany() {
        if (companyRepository.findAll().isEmpty()) {
            logger.info("Seeding default company...");
            
            Company defaultCompany = new Company(
                "Demo Company",
                "DEMO",
                "000000000"
            );
            defaultCompany.setAddress("123 Demo Street, Demo City");
            defaultCompany.setPhone("+1 555-123-4567");
            defaultCompany.setEmail("demo@econovafx.com");
            defaultCompany.setDatabaseUrl("jdbc:h2:./db/tenants/econova_demo");
            defaultCompany.setStatus("ACTIVE");
            
            companyRepository.save(defaultCompany);
            logger.info("Created default company: DEMO");
        } else {
            logger.debug("Companies already exist, skipping seeding");
        }
    }

    /**
     * Seeds admin user if no users exist.
     */
    private void seedAdminUser() {
        if (userRepository.findAll().isEmpty()) {
            logger.info("Seeding admin user...");
            
            User adminUser = new User("admin", "admin@econovafx.com", "Administrator");
            adminUser.setPassword(passwordService.hashPassword("admin123"));
            adminUser.setRole(User.UserRole.ADMIN);
            adminUser.setStatus("ACTIVE");
            adminUser.setIsActive(true);
            
            // Associate with default company if it exists
            companyRepository.findByCode("DEMO").ifPresent(adminUser::setCompany);
            
            userRepository.save(adminUser);
            logger.info("Created admin user: admin (email: admin@econovafx.com, password: admin123)");
        } else {
            logger.debug("Users already exist, skipping seeding");
        }
    }
}
