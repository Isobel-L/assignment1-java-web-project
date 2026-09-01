package com.assignment.speechtotext.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.assignment.speechtotext.model.GlobalStatsResponse;
import com.assignment.speechtotext.service.GlobalStatsService;

@RestController
@RequestMapping("/api/v1/global")
public class GlobalStatsController {

    private final GlobalStatsService globalStatsService;

    public GlobalStatsController(GlobalStatsService globalStatsService) {
        this.globalStatsService = globalStatsService;
    }

    @GetMapping("/stats")
    public GlobalStatsResponse getGlobalStats() {
        return globalStatsService.getStats();
    }
}