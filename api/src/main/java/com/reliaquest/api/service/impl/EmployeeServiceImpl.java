package com.reliaquest.api.service.impl;

import com.reliaquest.api.client.EmployeeApiClient;
import com.reliaquest.api.dto.EmployeeInput;
import com.reliaquest.api.exception.EmployeeDeletionFailedException;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeCacheService;
import com.reliaquest.api.service.EmployeeService;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Slf4j
@Service("employeeService")
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeCacheService employeeCacheService;
    private final EmployeeApiClient apiClient;

    public EmployeeServiceImpl(EmployeeCacheService employeeCacheService, EmployeeApiClient apiClient) {
        this.employeeCacheService = employeeCacheService;
        this.apiClient = apiClient;
    }

    @CacheEvict(value = "employees", allEntries = true)
    @Override
    public Employee createEmployee(EmployeeInput employeeInput) {
        return apiClient.createEmployee(employeeInput);
    }

    @Override
    public Employee getEmployeeById(String id) {
        Employee employee = apiClient.getEmployeeById(id);
        if (employee == null) {
            throw new EmployeeNotFoundException(id);
        }
        return employee;
    }

    @Override
    public List<Employee> getEmployeesByNameSearch(String searchString) {
        return employeeCacheService.getAllEmployees().stream()
                .filter(e -> e.getName() != null && e.getName().toLowerCase().contains(searchString.toLowerCase()))
                .toList();
    }

    @Override
    public Integer getHighestSalaryOfEmployees() {
        return employeeCacheService.getAllEmployees().stream()
                .map(Employee::getSalary)
                .max(Integer::compareTo)
                .orElse(0);
    }

    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        var allEmployees = employeeCacheService.getAllEmployees();
        var minHeap = new PriorityQueue<>(Comparator.comparing(Employee::getSalary));

        for (Employee emp : allEmployees) {
            minHeap.offer(emp);
            if (minHeap.size() > 10) minHeap.poll();
        }

        return minHeap.stream()
                .sorted(Comparator.comparing(Employee::getSalary).reversed())
                .map(Employee::getName)
                .toList();
    }

    @CacheEvict(value = "employees", allEntries = true)
    @Override
    public String deleteEmployeeById(String id) {
        String deletedName = apiClient.deleteEmployeeById(id);
        if (deletedName == null) {
            throw new EmployeeDeletionFailedException(id);
        }
        return deletedName;
    }
}
