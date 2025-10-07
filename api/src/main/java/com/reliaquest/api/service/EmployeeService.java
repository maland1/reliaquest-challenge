package com.reliaquest.api.service;

import com.reliaquest.api.dto.EmployeeInput;
import com.reliaquest.api.model.Employee;
import java.util.List;

public interface EmployeeService {

    /**
     * Creates a new employee in the external service.
     *
     * @param employeeInput the input details for the new employee
     * @return the created employee
     */
    Employee createEmployee(EmployeeInput employeeInput);

    /**
     * Retrieves a specific employee by ID.
     *
     * @param id the ID of the employee to return
     * @return the employee matching the provided ID
     */
    Employee getEmployeeById(String id);

    /**
     * Searches employees by name or partial name.
     *
     * @param searchString the fragment or full name to match
     * @return a list of matching employees
     */
    List<Employee> getEmployeesByNameSearch(String searchString);

    /**
     * Gets the highest salary value among all employees.
     *
     * @return the highest salary as an integer
     */
    Integer getHighestSalaryOfEmployees();

    /**
     * Retrieves the names of the top 10 highest-earning employees.
     *
     * @return a list of names of the top 10 earners
     */
    List<String> getTopTenHighestEarningEmployeeNames();

    /**
     * Deletes an employee by ID.
     *
     * @param id the ID of the employee to delete
     * @return the deleted employee's name
     */
    String deleteEmployeeById(String id);
}
