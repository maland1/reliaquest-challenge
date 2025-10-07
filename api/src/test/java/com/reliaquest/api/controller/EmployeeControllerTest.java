package com.reliaquest.api.controller;

import static com.reliaquest.api.testdata.EmployeeTestData.ALICE;
import static com.reliaquest.api.testdata.EmployeeTestData.allEmployees;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.dto.EmployeeInput;
import com.reliaquest.api.exception.EmployeeDeletionFailedException;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.InvalidEmployeeInputException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeCacheService;
import com.reliaquest.api.service.EmployeeService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    EmployeeService employeeService;

    @MockBean
    EmployeeCacheService employeeCacheService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createEmployee_ShouldReturnCreatedEmployee() throws Exception {
        EmployeeInput input = new EmployeeInput("Charlie", 80000, 28, "QA Engineer");
        Employee created = new Employee("3", "Charlie", 80000, 28, "QA Engineer", "charlie@test.com");
        String json = objectMapper.writeValueAsString(input);

        when(employeeService.createEmployee(any(EmployeeInput.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee_name").value("Charlie"))
                .andExpect(jsonPath("$.employee_salary").value(80000))
                .andExpect(jsonPath("$.employee_age").value(28))
                .andExpect(jsonPath("$.employee_title").value("QA Engineer"));
    }

    @Test
    void createEmployee_ShouldReturnBadRequestForInvalidInput() throws Exception {
        EmployeeInput input = new EmployeeInput("", -100, 0, "");
        String json = objectMapper.writeValueAsString(input);

        when(employeeService.createEmployee(any(EmployeeInput.class)))
                .thenThrow(new InvalidEmployeeInputException("Invalid input"));

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllEmployees_ShouldReturn200AndList() throws Exception {
        when(employeeCacheService.getAllEmployees()).thenReturn(allEmployees());

        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employee_name").value("Alice"))
                .andExpect(jsonPath("$[1].employee_name").value("Bob"));
    }

    @Test
    void getEmployeeById_ShouldReturnEmployeeIfExists() throws Exception {
        when(employeeService.getEmployeeById("1")).thenReturn(ALICE);

        mockMvc.perform(get("/api/v1/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee_name").value("Alice"));
    }

    @Test
    void getEmployeeById_ShouldReturn404IfNotFound() throws Exception {
        when(employeeService.getEmployeeById("999")).thenReturn(null);

        mockMvc.perform(get("/api/v1/employee/999")).andExpect(status().isNotFound());
    }

    @Test
    void getEmployeesByNameSearch_ShouldReturnMatchingEmployees() throws Exception {
        List<Employee> matched = List.of(ALICE);
        when(employeeService.getEmployeesByNameSearch("Alice")).thenReturn(matched);

        mockMvc.perform(get("/api/v1/employee/search/Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employee_name").value("Alice"));
    }

    @Test
    void getEmployeesByNameSearch_ShouldReturnEmptyListIfNoMatch() throws Exception {
        when(employeeService.getEmployeesByNameSearch("Charlie")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/employee/search/Charlie"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getHighestSalaryOfEmployees_ShouldReturnHighestSalary() throws Exception {
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(100000);

        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().string("100000"));
    }

    @Test
    void getHighestSalaryOfEmployees_ShouldReturnZero() throws Exception {
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(0);

        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    void getHighestSalaryOfEmployees_ShouldReturnNoContentIfNull() throws Exception {
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(null);

        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_ShouldReturnList() throws Exception {
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(List.of("Alice", "Bob"));

        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Alice"))
                .andExpect(jsonPath("$[1]").value("Bob"));
    }

    @Test
    void getTopTenHighestEarningEmployees_ShouldReturnNoContentIfEmpty() throws Exception {
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void deleteEmployeeById_ShouldReturnEmployeeNameIfDeleted() throws Exception {
        when(employeeService.deleteEmployeeById("1")).thenReturn("Alice");

        mockMvc.perform(delete("/api/v1/employee/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Alice"));
    }

    @Test
    void deleteEmployeeById_ShouldThrowEmployeeDeletionFailed() throws Exception {
        when(employeeService.deleteEmployeeById("2")).thenReturn(null);

        when(employeeService.deleteEmployeeById("2"))
                .thenThrow(new EmployeeDeletionFailedException("Internal error has occurred"));

        mockMvc.perform(delete("/api/v1/employee/2")).andExpect(status().isInternalServerError());
    }

    @Test
    void deleteEmployeeById_ShouldThrowNotFoundException() throws Exception {
        when(employeeService.deleteEmployeeById("999")).thenReturn(null);

        when(employeeService.deleteEmployeeById("999")).thenThrow(new EmployeeNotFoundException("Employee not found"));

        mockMvc.perform(delete("/api/v1/employee/999")).andExpect(status().isNotFound());
    }
}
