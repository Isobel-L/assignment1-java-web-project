package com.assignment.speechtotext.controller;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.assignment.speechtotext.model.ErrorResponse;
import com.assignment.speechtotext.model.ShutdownResponse;
import com.assignment.speechtotext.model.UptimeResponse;
import com.assignment.speechtotext.service.AdminService;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/uptime")
    public UptimeResponse getUptime() {
        return adminService.getUptime();
    }

    @PostMapping("/shutdown")
    public ResponseEntity<?> shutdownServer() {

        boolean accepted = adminService.requestShutdown();

        if (!accepted) {

            ErrorResponse errorResponse = new ErrorResponse(
                    Instant.now(),
                    409,
                    "Conflict",
                    "Graceful shutdown is already in progress.",
                    "/api/v1/admin/shutdown"
            );

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(errorResponse);
        }

        ShutdownResponse response =
                new ShutdownResponse(
                        "Graceful shutdown requested."
                );

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }
}