package com.assignment.speechtotext.concurrency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.assignment.speechtotext.model.GlobalStatsResponse;
import com.assignment.speechtotext.service.GlobalStatsService;

class GlobalStatsRaceConditionTest {

    private static final int THREAD_COUNT = 250;
    private static final int UPDATES_PER_THREAD = 1000;

    @Test
    void concurrentTokenUpdatesDoNotLoseData()
            throws Exception {

        GlobalStatsService service =
                new GlobalStatsService();

        ExecutorService executor =
                Executors.newFixedThreadPool(
                        THREAD_COUNT
                );

        CountDownLatch startGate =
                new CountDownLatch(1);

        List<Future<?>> tasks =
                new ArrayList<>();

        try {

            for (int i = 0; i < THREAD_COUNT; i++) {

                tasks.add(
                        executor.submit(() -> {

                            try {

                                startGate.await();

                                for (
                                    int update = 0;
                                    update < UPDATES_PER_THREAD;
                                    update++
                                ) {

                                    service.addUsage(
                                            2,
                                            3
                                    );
                                }

                            } catch (InterruptedException e) {

                                Thread.currentThread()
                                        .interrupt();

                                throw new RuntimeException(e);
                            }
                        })
                );
            }

            // Make the threads compete for the shared counters.
            startGate.countDown();

            for (Future<?> task : tasks) {

                task.get(
                        15,
                        TimeUnit.SECONDS
                );
            }

            GlobalStatsResponse result =
                    service.getStats();

            long expectedInput =
                    (long) THREAD_COUNT
                    * UPDATES_PER_THREAD
                    * 2;

            long expectedOutput =
                    (long) THREAD_COUNT
                    * UPDATES_PER_THREAD
                    * 3;

            assertEquals(
                    expectedInput,
                    result.inputTokens()
            );

            assertEquals(
                    expectedOutput,
                    result.outputTokens()
            );

        } finally {

            executor.shutdownNow();

            executor.awaitTermination(
                    5,
                    TimeUnit.SECONDS
            );
        }
    }
}