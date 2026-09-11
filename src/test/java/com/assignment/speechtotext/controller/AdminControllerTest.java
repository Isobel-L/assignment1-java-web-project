package com.assignment.speechtotext.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.assignment.speechtotext.model.UptimeResponse;
import com.assignment.speechtotext.service.AdminService;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @Test
    void getUptimeReturnsExpectedResponse() throws Exception {

        Instant start =
                Instant.parse("2026-09-01T00:00:00Z");

        Instant now =
                Instant.parse("2026-09-01T00:01:30Z");

        when(adminService.getUptime())
                .thenReturn(
                        new UptimeResponse(
                                start,
                                now,
                                90.0
                        )
                );

        mockMvc.perform(
                        get("/api/v1/admin/uptime")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.utcServerStart")
                                .value("2026-09-01T00:00:00Z")
                )
                .andExpect(
                        jsonPath("$.utcNow")
                                .value("2026-09-01T00:01:30Z")
                )
                .andExpect(
                        jsonPath("$.serverUptimeSeconds")
                                .value(90.0)
                );
    }

    @Test
    void shutdownReturnsAcceptedWhenRequestSucceeds()
            throws Exception {

        when(adminService.requestShutdown())
                .thenReturn(true);

        mockMvc.perform(
                        post("/api/v1/admin/shutdown")
                )
                .andExpect(status().isAccepted())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Graceful shutdown requested."
                                )
                );
    }

    @Test
    void shutdownReturnsConflictWhenAlreadyInProgress()
            throws Exception {

        when(adminService.requestShutdown())
                .thenReturn(false);

        mockMvc.perform(
                        post("/api/v1/admin/shutdown")
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.status")
                                .value(409)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Conflict")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Graceful shutdown is already in progress."
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/v1/admin/shutdown"
                                )
                );
    }
}