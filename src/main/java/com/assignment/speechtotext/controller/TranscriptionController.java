package com.assignment.speechtotext.controller;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.assignment.speechtotext.model.ErrorResponse;
import com.assignment.speechtotext.model.TranscriptionResponse;
import com.assignment.speechtotext.service.TranscriptionService;

@RestController
public class TranscriptionController {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    TranscriptionController.class
            );

    private static final String PATH =
            "/api/v1/transcriptions";

    private final TranscriptionService transcriptionService;

    public TranscriptionController(
            TranscriptionService transcriptionService) {

        this.transcriptionService =
                transcriptionService;
    }

    @PostMapping(
            value = PATH,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public CompletableFuture<ResponseEntity<?>>
            uploadAudio(
                    @RequestParam("audio")
                    MultipartFile audio) {

        if (audio.isEmpty()) {

            ErrorResponse error =
                    new ErrorResponse(
                            Instant.now(),
                            400,
                            "Bad Request",
                            "No audio data was received.",
                            PATH
                    );

            return CompletableFuture.completedFuture(
                    ResponseEntity
                            .badRequest()
                            .body(error)
            );
        }

        return transcriptionService
                .transcribe(audio)

                .<ResponseEntity<?>>thenApply(
                        transcription ->

                                ResponseEntity.ok(
                                        new TranscriptionResponse(
                                                transcription
                                        )
                                )
                )

                .exceptionally(error -> {

                    Throwable cause =
                            unwrap(error);

                    logger.error(
                            "Transcription request failed: {}",
                            cause.getMessage()
                    );

                    ErrorResponse response =
                            new ErrorResponse(
                                    Instant.now(),
                                    502,
                                    "Bad Gateway",
                                    "Speech transcription failed.",
                                    PATH
                            );

                    return ResponseEntity
                            .status(HttpStatus.BAD_GATEWAY)
                            .body(response);
                });
    }

    private Throwable unwrap(
            Throwable throwable) {

        Throwable current = throwable;

        while (current
                instanceof CompletionException
                && current.getCause() != null) {

            current =
                    current.getCause();
        }

        return current;
    }
}