package com.assignment.speechtotext.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.multipart.MultipartFile;

import com.assignment.speechtotext.service.TranscriptionService;

@WebMvcTest(TranscriptionController.class)
class TranscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TranscriptionService transcriptionService;

    @Test
    void validAudioReturnsTranscription() throws Exception {

        MockMultipartFile audio =
                new MockMultipartFile(
                        "audio",
                        "recording.webm",
                        "audio/webm",
                        "fake audio data".getBytes()
                );

        when(
                transcriptionService.transcribe(
                        any(MultipartFile.class)
                )
        ).thenReturn(
                CompletableFuture.completedFuture(
                        "This is a test transcription."
                )
        );

        MvcResult result =
                mockMvc.perform(
                        multipart("/api/v1/transcriptions")
                                .file(audio)
                )
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.text")
                                .value(
                                        "This is a test transcription."
                                )
                );
    }

    @Test
    void emptyAudioReturnsBadRequest() throws Exception {

        MockMultipartFile audio =
                new MockMultipartFile(
                        "audio",
                        "recording.webm",
                        "audio/webm",
                        new byte[0]
                );

        MvcResult result =
                mockMvc.perform(
                        multipart("/api/v1/transcriptions")
                                .file(audio)
                )
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Bad Request")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "No audio data was received."
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/v1/transcriptions"
                                )
                );
    }
}