package com.reliaquest.api.service;

import static com.reliaquest.api.testdata.EmployeeTestData.ALICE;
import static com.reliaquest.api.testdata.EmployeeTestData.allEmployees;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.reliaquest.api.client.EmployeeApiClient;
import com.reliaquest.api.dto.EmployeeInput;
import com.reliaquest.api.exception.EmployeeDeletionFailedException;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.impl.EmployeeServiceImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeCacheService employeeCacheService;

    @Mock
    private EmployeeApiClient apiClient;

    @InjectMocks
    private EmployeeServiceImpl service;

    @Test
    void createEmployee_ShouldDelegateToApiClient() {
        EmployeeInput input = new EmployeeInput("Charlie", 80_000, 28, "QA Engineer");
        Employee createdEmployee = new Employee("3", "Charlie", 80_000, 28, "QA Engineer", "charlie@test.com");

        when(apiClient.createEmployee(input)).thenReturn(createdEmployee);

        Employee result = service.createEmployee(input);

        assertNotNull(result);
        assertEquals("Charlie", result.getName());
        verify(apiClient).createEmployee(input);
    }

    @Test
    void createEmployee_ShouldReturnNullIfApiFails() {
        EmployeeInput input = new EmployeeInput("Charlie", 80_000, 28, "QA Engineer");

        when(apiClient.createEmployee(input)).thenReturn(null);

        Employee result = service.createEmployee(input);

        assertNull(result);
        verify(apiClient).createEmployee(input);
    }

    @Test
    void getEmployeeById_ShouldDelegateToApiClient() {
        when(apiClient.getEmployeeById("1")).thenReturn(ALICE);

        Employee result = service.getEmployeeById("1");

        assertEquals("Alice", result.getName());
        verify(apiClient).getEmployeeById("1");
    }

    @Test
    void getEmployeeById_ShouldThrowNotFoundException() {
        when(apiClient.getEmployeeById("999")).thenReturn(null);

        EmployeeNotFoundException ex =
                assertThrows(EmployeeNotFoundException.class, () -> service.getEmployeeById("999"));

        assertEquals("Employee with ID '999' not found", ex.getMessage());
        verify(apiClient).getEmployeeById("999");
    }

    @Test
    void getEmployeesByNameSearch_ShouldFilterCorrectly() {
        when(employeeCacheService.getAllEmployees()).thenReturn(allEmployees());

        List<Employee> result = service.getEmployeesByNameSearch("Alice");

        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getName());
    }

    @Test
    void getHighestSalaryOfEmployees_ShouldReturnMaxSalary() {
        when(employeeCacheService.getAllEmployees()).thenReturn(allEmployees());

        Integer maxSalary = service.getHighestSalaryOfEmployees();

        assertEquals(1000, maxSalary);
    }

    @Test
    void getHighestSalaryOfEmployees_ShouldReturnZeroIfEmpty() {
        when(employeeCacheService.getAllEmployees()).thenReturn(Collections.emptyList());

        Integer maxSalary = service.getHighestSalaryOfEmployees();

        assertEquals(0, maxSalary);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_ShouldReturnTopNames() {
        when(employeeCacheService.getAllEmployees()).thenReturn(allEmployees());

        List<String> topNames = service.getTopTenHighestEarningEmployeeNames();

        assertEquals(List.of("Alice", "Bob"), topNames);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_ShouldReturnTop10IfMoreThan10Employees() {
        List<Employee> employees = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            employees.add(new Employee(String.valueOf(i), "Emp" + i, i * 1000, 25, "Dev", "emp" + i + "@test.com"));
        }

        when(employeeCacheService.getAllEmployees()).thenReturn(employees);

        List<String> top10 = service.getTopTenHighestEarningEmployeeNames();

        // The top 10 salaries should be Emp15..Emp6
        List<String> expectedTop10 = employees.stream()
                .sorted(Comparator.comparing(Employee::getSalary).reversed())
                .limit(10)
                .map(Employee::getName)
                .toList();

        assertEquals(expectedTop10, top10);
    }

    @Test
    void deleteEmployeeById_ShouldThrowDeletionFailedException() {
        when(apiClient.deleteEmployeeById("1")).thenReturn(null);
        when(apiClient.deleteEmployeeById("1")).thenThrow(new EmployeeDeletionFailedException("1"));

        EmployeeDeletionFailedException ex =
                assertThrows(EmployeeDeletionFailedException.class, () -> service.deleteEmployeeById("1"));

        assertEquals("Failed to delete employee with ID '1'", ex.getMessage());
        verify(apiClient).deleteEmployeeById("1");
    }
}
