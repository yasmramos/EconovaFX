package com.econovafx.modules.payroll.repository;

import com.econovafx.modules.payroll.model.Employee;
import io.avaje.inject.Component;
import io.ebean.Database;
import io.ebean.ExpressionList;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Repository class for Employee entity.
 * Provides data access methods for payroll employee management.
 */
@Component
public class EmployeeRepository {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeRepository.class);
    
    private final Database database;
    
    @Inject
    public EmployeeRepository(Database database) {
        this.database = database;
    }

    /**
     * Find all employees.
     */
    public List<Employee> findAll() {
        return database.find(Employee.class).orderBy().asc("employeeCode").findList();
    }

    /**
     * Find employee by ID.
     */
    public Optional<Employee> findById(Long id) {
        return Optional.ofNullable(database.find(Employee.class, id));
    }

    /**
     * Save an employee.
     */
    public Employee save(Employee employee) {
        database.save(employee);
        logger.debug("Employee saved: {}", employee.getEmployeeCode());
        return employee;
    }

    /**
     * Delete an employee.
     */
    public void delete(Employee employee) {
        database.delete(employee);
        logger.debug("Employee deleted: {}", employee.getEmployeeCode());
    }

    /**
     * Find employee by employee code.
     */
    public Optional<Employee> findByEmployeeCode(String employeeCode) {
        return Optional.ofNullable(database.find(Employee.class)
                .where().eq("employeeCode", employeeCode).findOne());
    }

    /**
     * Find employees by department.
     */
    public List<Employee> findByDepartment(String department) {
        return database.find(Employee.class)
                .where().eq("department", department)
                .orderBy().asc("employeeCode").findList();
    }

    /**
     * Find active employees.
     */
    public List<Employee> findByActiveTrue() {
        return database.find(Employee.class)
                .where().eq("employmentStatus", Employee.EmploymentStatus.ACTIVE)
                .orderBy().asc("employeeCode").findList();
    }

    /**
     * Count employees by department.
     */
    public long countByDepartment(String department) {
        return database.find(Employee.class)
                .where().eq("department", department).findCount();
    }

    /**
     * Check if employee code exists.
     */
    public boolean existsByEmployeeCode(String employeeCode) {
        return database.find(Employee.class)
                .where().eq("employeeCode", employeeCode).exists();
    }
    
    /**
     * Get query expression list for advanced queries.
     */
    public ExpressionList<Employee> query() {
        return database.find(Employee.class).where();
    }
}
