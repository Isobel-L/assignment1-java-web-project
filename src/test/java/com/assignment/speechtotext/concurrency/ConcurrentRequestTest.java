package com.assignment.speechtotext.concurrency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class ConcurrentRequestTest {

    private static final int REQUEST_COUNT = 250;

    @LocalServerPort
    private int port;

    @Test
    void handlesMoreThan200SimultaneousBlockingRequests()
            throws Exception {

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        ExecutorService executor =
                Executors.newFixedThreadPool(REQUEST_COUNT);

        CountDownLatch startGate =
                new CountDownLatch(1);

        List<Future<Integer>> results =
                new ArrayList<>();

        try {

            for (int i = 0; i < REQUEST_COUNT; i++) {

                results.add(
                        executor.submit(() -> {

                            startGate.await();

                            HttpRequest request =
                                    HttpRequest.newBuilder()
                                            .uri(
                                                URI.create(
                                                    "http://localhost:"
                                                    + port
                                                    + "/api/v1/global/stats"
                                                )
                                            )
                                            .timeout(
                                                Duration.ofSeconds(10)
                                            )
                                            .GET()
                                            .build();

                            HttpResponse<String> response =
                                    client.send(
                                        request,
                                        HttpResponse.BodyHandlers.ofString()
                                    );

                            return response.statusCode();
                        })
                );
            }

            // Release all 250 request tasks at approximately
            // the same time.
            startGate.countDown();

            for (Future<Integer> result : results) {

                assertEquals(
                        200,
                        result.get(
                                15,
                                TimeUnit.SECONDS
                        )
                );
            }

        } finally {

            executor.shutdownNow();

            executor.awaitTermination(
                    5,
                    TimeUnit.SECONDS
            );
        }
    }
}