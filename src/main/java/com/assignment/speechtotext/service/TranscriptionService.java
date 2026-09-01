package com.assignment.speechtotext.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TranscriptionService {

    public String transcribe(MultipartFile audio) {

        // Temporary implementation.
        // The OpenAI speech-to-text API will be integrated next.
        return "Audio received successfully.";
    }
}