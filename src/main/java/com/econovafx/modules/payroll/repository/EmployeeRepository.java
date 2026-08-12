package com.econovafx.modules.payroll.repository;

import com.econovafx.modules.payroll.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Employee entity.
 * Provides data access methods for payroll employee management.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Find employee by employee code.
     */
    Optional<Employee> findByEmployeeCode(String employeeCode);

    /**
     * Find employees by department.
     */
    List<Employee> findByDepartment(String department);

    /**
     * Find employees by employment status.
     */
    List<Employee> findByEmploymentStatus(Employee.EmploymentStatus status);

    /**
     * Find active employees (status = ACTIVE).
     */
    @Query("SELECT e FROM Employee e WHERE e.employmentStatus = 'ACTIVE'")
    List<Employee> findActiveEmployees();

    /**
     * Find employees with ThirdParty type CUSTOMER or BOTH.
     */
    @Query("SELECT e FROM Employee e JOIN e.thirdParty tp WHERE tp.type IN ('CUSTOMER', 'BOTH')")
    List<Employee> findCustomerEmployees();

    /**
     * Count employees by department.
     */
    long countByDepartment(String department);

    /**
     * Check if employee code exists.
     */
    boolean existsByEmployeeCode(String employeeCode);
}
