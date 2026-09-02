package com.assignment.speechtotext.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("local")
public class LocalTranscriptionService
        implements TranscriptionService {

    @Override
    public CompletableFuture<String> transcribe(
            MultipartFile audio) {

        String fakeTranscription =
                "Local test transcription. "
                + "The audio upload was received successfully.";

        return CompletableFuture.completedFuture(
                fakeTranscription
        );
    }
}