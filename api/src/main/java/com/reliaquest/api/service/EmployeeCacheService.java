package com.reliaquest.api.service;

import com.reliaquest.api.model.Employee;
import java.util.List;

public interface EmployeeCacheService {

    /**
     * Retrieves all employees and caches the result.
     *
     * @return a list of all employees
     */
    List<Employee> getAllEmployees();
}
