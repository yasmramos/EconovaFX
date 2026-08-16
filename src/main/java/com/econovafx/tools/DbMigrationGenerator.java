package com.econovafx.tools;

import io.ebean.dbmigration.DbMigration;
import io.ebean.annotation.Platform;

/**
 * Development-time tool to generate Ebean database migrations.
 * 
 * This class is NOT part of the application runtime. It is used only during
 * development to generate migration scripts based on entity model changes.
 * 
 * Usage:
 * 1. Make changes to your @Entity classes
 * 2. Run this main method: mvn exec:java -Dexec.mainClass="com.econovafx.tools.DbMigrationGenerator"
 * 3. Review generated migrations in src/main/resources/dbmigration/
 * 4. Commit the generated migration files to version control
 * 
 * For multi-tenant applications:
 * - Master database: Contains Company entities for tenant management
 * - Tenant databases: Contain business entities (Accounting, Billing, etc.)
 * 
 * This generator creates separate migration paths for master and tenant databases.
 */
public class DbMigrationGenerator {

    public static void main(String[] args) {
        System.out.println("=== Ebean DB Migration Generator ===");
        System.out.println("Generating migrations for EconoNova FX...");
        
        // Generate migrations for MASTER database (Company management)
        generateMasterMigrations();
        
        // Generate migrations for TENANT databases (business entities)
        generateTenantMigrations();
        
        System.out.println("\n=== Migration generation complete ===");
        System.out.println("Review generated files in:");
        System.out.println("  - src/main/resources/dbmigration/master/");
        System.out.println("  - src/main/resources/dbmigration/tenant/");
        System.out.println("\nNext steps:");
        System.out.println("  1. Review generated SQL for correctness");
        System.out.println("  2. Commit migration files to version control");
        System.out.println("  3. Run application to apply migrations automatically");
    }
    
    /**
     * Generates migrations for the master database (Company entities).
     * The master database manages tenant/company information.
     */
    private static void generateMasterMigrations() {
        System.out.println("\n--- Generating MASTER database migrations ---");
        
        DbMigration dbMigration = DbMigration.create();
        dbMigration.setPlatform(Platform.H2); // Also generates PostgreSQL compatible DDL
        dbMigration.setPathToResources("src/main/resources");
        dbMigration.setMigrationPath("dbmigration/master");
        dbMigration.setServerConfig(io.ebean.Database.builder()
            .addPackage("com.econovafx.modules.core.model"));
        
        try {
            dbMigration.generateMigration();
            System.out.println("Master migrations generated successfully");
        } catch (Exception e) {
            System.err.println("Error generating master migrations: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generates migrations for tenant databases (business entities).
     * Tenant databases contain all business module entities.
     */
    private static void generateTenantMigrations() {
        System.out.println("\n--- Generating TENANT database migrations ---");
        
        DbMigration dbMigration = DbMigration.create();
        dbMigration.setPlatform(Platform.H2); // Also generates PostgreSQL compatible DDL
        dbMigration.setPathToResources("src/main/resources");
        dbMigration.setMigrationPath("dbmigration/tenant");
        
        // Add all business entity packages (excluding core.model which is master-only)
        io.ebean.DatabaseBuilder builder = io.ebean.Database.builder()
            .addPackage("com.econovafx.modules.accounting.model")
            .addPackage("com.econovafx.modules.billing.model")
            .addPackage("com.econovafx.modules.payroll.model")
            .addPackage("com.econovafx.modules.inventory.model")
            .addPackage("com.econovafx.modules.receivables.model")
            .addPackage("com.econovafx.modules.payables.model")
            .addPackage("com.econovafx.modules.bank.model")
            .addPackage("com.econovafx.modules.cash.model")
            .addPackage("com.econovafx.modules.assets.model")
            .addPackage("com.econovafx.modules.fixedassets.model")
            .addPackage("com.econovafx.modules.reporting.model")
            .addPackage("com.econovafx.modules.security.model");
        
        dbMigration.setServerConfig(builder);
        
        try {
            dbMigration.generateMigration();
            System.out.println("Tenant migrations generated successfully");
        } catch (Exception e) {
            System.err.println("Error generating tenant migrations: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
