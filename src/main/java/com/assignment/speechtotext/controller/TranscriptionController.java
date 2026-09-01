package com.assignment.speechtotext.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.assignment.speechtotext.model.TranscriptionResponse;
import com.assignment.speechtotext.service.TranscriptionService;

@RestController
public class TranscriptionController {

    private final TranscriptionService transcriptionService;

    public TranscriptionController(
            TranscriptionService transcriptionService) {

        this.transcriptionService = transcriptionService;
    }

    @PostMapping(
            value = "/api/v1/transcriptions",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<TranscriptionResponse> uploadAudio(
            @RequestParam("audio") MultipartFile audio) {

        if (audio.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            new TranscriptionResponse(
                                    "No audio data was received."
                            )
                    );
        }

        String transcription =
                transcriptionService.transcribe(audio);

        return ResponseEntity.ok(
                new TranscriptionResponse(transcription)
        );
    }
}