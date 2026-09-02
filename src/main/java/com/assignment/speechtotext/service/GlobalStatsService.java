package com.assignment.speechtotext.service;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.assignment.speechtotext.model.GlobalStatsResponse;

@Service
public class GlobalStatsService {

    private final AtomicLong inputTokens =
            new AtomicLong(0);

    private final AtomicLong outputTokens =
            new AtomicLong(0);

    public GlobalStatsResponse getStats() {

        return new GlobalStatsResponse(
                inputTokens.get(),
                outputTokens.get()
        );
    }

    public void addUsage(
            long input,
            long output) {

        inputTokens.addAndGet(input);
        outputTokens.addAndGet(output);
    }
}