package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.reliaquest.api.client.EmployeeApiClient;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.impl.EmployeeCacheServiceImpl;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmployeeCacheServiceImplTest {

    @Mock
    private EmployeeApiClient apiClient;

    @InjectMocks
    private EmployeeCacheServiceImpl cacheService;

    @Test
    void getAllEmployees_returnsList_whenApiSucceeds() {
        List<Employee> employees = List.of(
                new Employee("1", "Alice", 100_000, 30, "Proj. Mgr", "alice@test.com"),
                new Employee("2", "Bob", 90_000, 25, "Dev", "bob@test.com"));

        when(apiClient.getAllEmployees()).thenReturn(employees);

        List<Employee> result = cacheService.getAllEmployees();

        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getName());
        verify(apiClient).getAllEmployees();
    }

    @Test
    void getAllEmployees_returnsEmptyList_whenApiThrowsException() {
        when(apiClient.getAllEmployees()).thenThrow(new RuntimeException("API error"));

        List<Employee> result = cacheService.getAllEmployees();

        assertTrue(result.isEmpty());
        verify(apiClient).getAllEmployees();
    }
}
