package com.reliaquest.api.interceptor;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

@Slf4j
public class RetryInterceptor implements ClientHttpRequestInterceptor {

    private final int maxRetries;
    private final long baseBackoffMs;

    public RetryInterceptor(int maxRetries, long baseBackoffMs) {
        this.maxRetries = maxRetries;
        this.baseBackoffMs = baseBackoffMs;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        int attempt = 0;

        while (true) {
            ClientHttpResponse response = execution.execute(request, body);

            if (response.getStatusCode() != HttpStatus.TOO_MANY_REQUESTS || attempt >= maxRetries) {
                if (attempt > 0) {
                    log.debug("Finished retries for request {} after {} attempts", request.getURI(), attempt);
                }
                return response;
            }

            attempt++;
            log.warn(
                    "Received 429 Too Many Requests for {}. Retrying attempt {}/{}",
                    request.getURI(),
                    attempt,
                    maxRetries);

            try {
                Thread.sleep(calculateBackoffMillis(attempt));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted during retry backoff", e);
            }
        }
    }

    private long calculateBackoffMillis(int attempt) {
        long jitter = ThreadLocalRandom.current().nextLong(500);
        return baseBackoffMs * (long) Math.pow(2, attempt - 1) + jitter;
    }
}
