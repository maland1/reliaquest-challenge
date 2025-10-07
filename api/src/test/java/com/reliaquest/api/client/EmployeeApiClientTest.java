package com.reliaquest.api.client;

import static com.reliaquest.api.testdata.EmployeeTestData.ALICE;
import static com.reliaquest.api.testdata.EmployeeTestData.allEmployees;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.reliaquest.api.common.ApiResponse;
import com.reliaquest.api.config.MockEmployeeApiConfig;
import com.reliaquest.api.dto.EmployeeInput;
import com.reliaquest.api.model.Employee;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class EmployeeApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private MockEmployeeApiConfig properties;

    @InjectMocks
    private EmployeeApiClient apiClient;

    private final String BASE_URL = "http://mock-api/employees";

    @BeforeEach
    void setUp() {
        lenient().when(properties.getUrl()).thenReturn(BASE_URL);
    }

    @Test
    void getAllEmployees_returnsListOfEmployees() {
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>();
        apiResponse.setData(allEmployees());

        ResponseEntity<ApiResponse<List<Employee>>> response = new ResponseEntity<>(apiResponse, HttpStatus.OK);
        when(restTemplate.exchange(eq(BASE_URL), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        List<Employee> result = apiClient.getAllEmployees();

        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getName());
    }

    @Test
    void getAllEmployees_returnsEmptyList_whenBodyIsNull() {
        ResponseEntity<ApiResponse<List<Employee>>> response = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        List<Employee> result = apiClient.getAllEmployees();
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllEmployees_returnsEmptyList_whenExceptionThrown() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("API down"));

        List<Employee> result = apiClient.getAllEmployees();
        assertTrue(result.isEmpty());
    }

    @Test
    void getEmployeeById_returnsEmployee() {
        ApiResponse<Employee> apiResponse = new ApiResponse<>();
        apiResponse.setData(ALICE);

        ResponseEntity<ApiResponse<Employee>> response = new ResponseEntity<>(apiResponse, HttpStatus.OK);
        when(restTemplate.exchange(
                        eq(BASE_URL + "/1"), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        Employee result = apiClient.getEmployeeById("1");

        assertNotNull(result);
        assertEquals("Alice", result.getName());
    }

    @Test
    void getEmployeeById_returnsNull_whenExceptionThrown() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("Timeout"));

        Employee result = apiClient.getEmployeeById("999");
        assertNull(result);
    }

    @Test
    void createEmployee_returnsCreatedEmployee() {
        EmployeeInput input = new EmployeeInput("Alice", 1000, 30, "Proj. Mgr");

        ApiResponse<Employee> apiResponse = new ApiResponse<>();
        apiResponse.setData(ALICE);

        ResponseEntity<ApiResponse<Employee>> response = new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
        when(restTemplate.exchange(
                        eq(BASE_URL),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        Employee result = apiClient.createEmployee(input);

        assertNotNull(result);
        assertEquals("Alice", result.getName());
    }

    @Test
    void createEmployee_returnsNull_whenBodyIsNull() {
        ResponseEntity<ApiResponse<Employee>> response = new ResponseEntity<>(null, HttpStatus.CREATED);
        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        Employee result = apiClient.createEmployee(new EmployeeInput());
        assertNull(result);
    }

    @Test
    void deleteEmployeeById_returnsName_whenDeletedSuccessfully() {
        ApiResponse<Boolean> apiResponse = new ApiResponse<>();
        apiResponse.setData(true);
        ResponseEntity<ApiResponse<Boolean>> response = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        EmployeeApiClient spyClient = Mockito.spy(apiClient);
        doReturn(ALICE).when(spyClient).getEmployeeById("1");

        when(restTemplate.exchange(
                        eq(BASE_URL),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        String result = spyClient.deleteEmployeeById("1");

        assertEquals("Alice", result);
    }

    @Test
    void deleteEmployeeById_returnsNull_whenEmployeeNotFound() {
        EmployeeApiClient spyClient = Mockito.spy(apiClient);
        doReturn(null).when(spyClient).getEmployeeById("999");

        String result = spyClient.deleteEmployeeById("999");

        assertNull(result);
        verify(restTemplate, never())
                .exchange(anyString(), eq(HttpMethod.DELETE), any(), any(ParameterizedTypeReference.class));
    }

    @Test
    void deleteEmployeeById_returnsNull_whenApiResponseIsNull() {
        EmployeeApiClient spyClient = Mockito.spy(apiClient);
        doReturn(ALICE).when(spyClient).getEmployeeById("1");

        ResponseEntity<ApiResponse<Boolean>> response = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        String result = spyClient.deleteEmployeeById("1");

        assertNull(result);
        verify(restTemplate)
                .exchange(
                        anyString(),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class));
    }

    @Test
    void deleteEmployeeById_returnsNull_whenHttpClientErrorExceptionNotFound() {
        EmployeeApiClient spyClient = Mockito.spy(apiClient);
        doReturn(ALICE).when(spyClient).getEmployeeById("1");

        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        String result = spyClient.deleteEmployeeById("1");

        assertNull(result);
        verify(restTemplate)
                .exchange(
                        anyString(),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class));
    }

    @Test
    void deleteEmployeeById_returnsNull_whenExceptionThrown() {
        EmployeeApiClient spyClient = Mockito.spy(apiClient);
        doReturn(ALICE).when(spyClient).getEmployeeById("1");

        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("API error"));

        String result = spyClient.deleteEmployeeById("1");
        assertNull(result);
    }
}
