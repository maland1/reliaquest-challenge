package com.reliaquest.api.service.impl;

import com.reliaquest.api.client.EmployeeApiClient;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeCacheService;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmployeeCacheServiceImpl implements EmployeeCacheService {

    private final EmployeeApiClient apiClient;

    public EmployeeCacheServiceImpl(EmployeeApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    @Cacheable(value = "employees", sync = true)
    public List<Employee> getAllEmployees() {
        try {
            List<Employee> employees = apiClient.getAllEmployees();
            log.debug("Fetched {} employees from API", employees.size());
            return employees;
        } catch (Exception e) {
            log.error("Failed to fetch employees", e);
            return Collections.emptyList();
        }
    }
}
