package com.reliaquest.api.client;

import com.reliaquest.api.common.ApiResponse;
import com.reliaquest.api.config.MockEmployeeApiConfig;
import com.reliaquest.api.dto.EmployeeInput;
import com.reliaquest.api.model.Employee;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class EmployeeApiClient {

    private final RestTemplate restTemplate;
    private final MockEmployeeApiConfig properties;

    public EmployeeApiClient(RestTemplate restTemplate, MockEmployeeApiConfig properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public Employee createEmployee(EmployeeInput input) {
        try {
            log.debug("Creating employee with input: {}", input);

            HttpEntity<EmployeeInput> request = new HttpEntity<>(input);

            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                    properties.getUrl(), HttpMethod.POST, request, new ParameterizedTypeReference<>() {});

            if (response.getBody() != null && response.getBody().getData() != null) {
                Employee created = response.getBody().getData();
                log.debug("Successfully created employee: {}", created);
                return created;
            } else {
                log.warn("Received empty response body when creating employee with input: {}", input);
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to create employee with input: {}", input, e);
            return null;
        }
    }

    public List<Employee> getAllEmployees() {
        try {
            ResponseEntity<ApiResponse<List<Employee>>> response = restTemplate.exchange(
                    properties.getUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

            return Optional.ofNullable(response.getBody())
                    .map(ApiResponse::getData)
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            log.error("Failed to fetch employees from API", e);
            return Collections.emptyList();
        }
    }

    public Employee getEmployeeById(String id) {
        try {
            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                    properties.getUrl() + "/" + id, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
            return Optional.ofNullable(response.getBody())
                    .map(ApiResponse::getData)
                    .orElse(null);
        } catch (Exception e) {
            log.error("Failed to fetch employee {}", id, e);
            return null;
        }
    }

    public String deleteEmployeeById(String id) {
        try {
            Employee employee = getEmployeeById(id);
            if (employee == null
                    || employee.getName() == null
                    || employee.getName().isBlank()) {
                log.warn("Cannot delete employee: ID '{}' not found or name missing", id);
                return null;
            }

            String employeeName = employee.getName();
            Map<String, String> body = Map.of("name", employeeName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                    properties.getUrl(), HttpMethod.DELETE, request, new ParameterizedTypeReference<>() {});

            ApiResponse<Boolean> apiResponse = response.getBody();
            boolean deleted = apiResponse != null && Boolean.TRUE.equals(apiResponse.getData());

            if (deleted) {
                log.debug("Successfully deleted employee '{}'", employeeName);
                return employeeName;
            } else {
                log.warn("API did not confirm deletion for '{}'. Response: {}", employeeName, apiResponse);
                return null;
            }

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Employee with id '{}' not found", id);
            return null;
        } catch (Exception e) {
            log.error("Failed to delete employee with id '{}'", id, e);
            return null;
        }
    }
}
