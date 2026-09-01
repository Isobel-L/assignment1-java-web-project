package com.assignment.speechtotext.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}