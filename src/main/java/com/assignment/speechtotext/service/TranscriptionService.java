package com.assignment.speechtotext.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.web.multipart.MultipartFile;

public interface TranscriptionService {

    CompletableFuture<String> transcribe(MultipartFile audio);
}