package com.assignment.speechtotext.model;

public record GlobalStatsResponse(
        long inputTokens,
        long outputTokens
) {
}