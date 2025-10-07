package com.reliaquest.api.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.reliaquest.api.client.EmployeeApiClient;
import com.reliaquest.api.dto.EmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import com.reliaquest.api.service.impl.EmployeeCacheServiceImpl;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.SimpleKey;

@SpringBootTest
@EnableCaching
class EmployeeCacheServiceIntegrationTest {

    @Autowired
    private EmployeeCacheServiceImpl cacheService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private EmployeeApiClient apiClient;

    @BeforeEach
    void clearCache() {
        Cache cache = cacheManager.getCache("employees");
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    void getAllEmployees_isCached() {
        List<Employee> employees = List.of(new Employee("1", "Alice", 1000, 30, "Proj. Mgr", "alice@test.com"));
        when(apiClient.getAllEmployees()).thenReturn(employees);

        List<Employee> firstCall = cacheService.getAllEmployees();

        List<Employee> secondCall = cacheService.getAllEmployees();

        assertEquals(firstCall, secondCall);
        verify(apiClient, times(1)).getAllEmployees();

        Cache cache = cacheManager.getCache("employees");
        assertNotNull(cache.get(SimpleKey.EMPTY));
    }

    @Test
    void createEmployee_ShouldEvictCache() {
        List<Employee> employees = List.of(new Employee("1", "Alice", 1000, 30, "PM", "alice@test.com"));
        when(apiClient.getAllEmployees()).thenReturn(employees);

        cacheService.getAllEmployees();
        verify(apiClient, times(1)).getAllEmployees();

        Employee newEmp = new Employee("2", "Bob", 900, 25, "Dev", "bob@test.com");
        when(apiClient.createEmployee(any())).thenReturn(newEmp);
        employeeService.createEmployee(new EmployeeInput("Bob", 900, 25, "Dev"));

        cacheService.getAllEmployees();
        verify(apiClient, times(2)).getAllEmployees();
    }

    @Test
    void deleteEmployee_ShouldEvictCache() {
        List<Employee> employees = List.of(new Employee("1", "Alice", 1000, 30, "PM", "alice@test.com"));
        when(apiClient.getAllEmployees()).thenReturn(employees);

        cacheService.getAllEmployees();
        verify(apiClient, times(1)).getAllEmployees();

        when(apiClient.deleteEmployeeById("1")).thenReturn("Alice");
        employeeService.deleteEmployeeById("1");

        cacheService.getAllEmployees();
        verify(apiClient, times(2)).getAllEmployees();
    }

    @Test
    void getAllEmployees_ConcurrentAccess_ShouldCallApiOnce() throws InterruptedException {
        List<Employee> employees = List.of(new Employee("1", "Alice", 1000, 30, "PM", "alice@test.com"));
        when(apiClient.getAllEmployees()).thenReturn(employees);

        Runnable task = () -> cacheService.getAllEmployees();

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        Thread t3 = new Thread(task);

        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();

        verify(apiClient, times(1)).getAllEmployees();
    }
}
