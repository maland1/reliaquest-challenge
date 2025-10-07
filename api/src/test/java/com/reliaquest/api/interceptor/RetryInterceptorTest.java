package com.reliaquest.api.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;

class RetryInterceptorTest {

    private ClientHttpRequestInterceptor interceptor;
    private ClientHttpRequestExecution execution;
    private final URI testUri = URI.create("http://localhost/test");

    @BeforeEach
    void setUp() {
        interceptor = new RetryInterceptor(3, 0);
        execution = mock(ClientHttpRequestExecution.class);
    }

    @Test
    void intercept_ShouldReturnResponseOnSuccessWithoutRetries() throws IOException {
        ClientHttpResponse okResponse = new MockClientHttpResponse(new byte[0], HttpStatus.OK);
        when(execution.execute(any(), any())).thenReturn(okResponse);

        ClientHttpResponse response = interceptor.intercept(mockRequest(), new byte[0], execution);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(execution, times(1)).execute(any(), any());
    }

    @Test
    void intercept_ShouldRetryOn429ThenReturnSuccess() throws IOException {
        ClientHttpResponse tooManyRequests = new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS);
        ClientHttpResponse success = new MockClientHttpResponse(new byte[0], HttpStatus.OK);

        when(execution.execute(any(), any()))
                .thenReturn(tooManyRequests)
                .thenReturn(tooManyRequests)
                .thenReturn(success);

        ClientHttpResponse response = interceptor.intercept(mockRequest(), new byte[0], execution);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(execution, times(3)).execute(any(), any());
    }

    @Test
    void intercept_ShouldReturn429AfterMaxRetries() throws IOException {
        ClientHttpResponse tooManyRequests = new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS);
        when(execution.execute(any(), any())).thenReturn(tooManyRequests);

        ClientHttpResponse response = interceptor.intercept(mockRequest(), new byte[0], execution);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        verify(execution, times(4)).execute(any(), any());
    }

    private org.springframework.http.HttpRequest mockRequest() {
        org.springframework.http.HttpRequest request = mock(org.springframework.http.HttpRequest.class);
        when(request.getURI()).thenReturn(testUri);
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        return request;
    }
}
