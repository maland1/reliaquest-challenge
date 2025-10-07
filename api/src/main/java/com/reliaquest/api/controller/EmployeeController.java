package com.reliaquest.api.controller;

import com.reliaquest.api.dto.EmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeCacheService;
import com.reliaquest.api.service.EmployeeService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employee")
public class EmployeeController implements IEmployeeController<Employee, EmployeeInput> {

    private final EmployeeService employeeService;
    private final EmployeeCacheService employeeCacheService;

    EmployeeController(EmployeeService employeeService, EmployeeCacheService employeeCacheService) {
        this.employeeService = employeeService;
        this.employeeCacheService = employeeCacheService;
    }

    /**
     * Creates a new employee with the provided input and returns the created employee.
     *
     * @param employeeInput the employee data to create
     */
    @Override
    public ResponseEntity<Employee> createEmployee(EmployeeInput employeeInput) {
        Employee employee = employeeService.createEmployee(employeeInput);
        return employee == null ? ResponseEntity.badRequest().build() : ResponseEntity.ok(employee);
    }

    /**
     * Returns a list of all employees.
     */
    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeCacheService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    /**
     * Returns the employee with the given ID.
     *
     * @param id the ID of the employee
     */
    @Override
    public ResponseEntity<Employee> getEmployeeById(String id) {
        Employee employee = employeeService.getEmployeeById(id);
        return employee == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(employee);
    }

    /**
     * Returns a list of employees whose names contain the given search string.
     *
     * @param searchString the string to search for in employee names
     */
    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        List<Employee> employees = employeeService.getEmployeesByNameSearch(searchString);
        return ResponseEntity.ok(employees);
    }

    /**
     * Returns the highest salary among all employees.
     */
    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        Integer highestSalary = employeeService.getHighestSalaryOfEmployees();

        if (highestSalary == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(highestSalary);
    }

    /**
     * Returns a list of the names of the top 10 highest-earning employees.
     */
    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        List<String> topTenEarnerNames = employeeService.getTopTenHighestEarningEmployeeNames();

        if (topTenEarnerNames == null || topTenEarnerNames.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(topTenEarnerNames);
    }

    /**
     * Deletes the employee with the given ID and returns their name.
     *
     * @param id the ID of the employee to delete
     */
    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        String employeeName = employeeService.deleteEmployeeById(id);
        return employeeName == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(employeeName);
    }
}
