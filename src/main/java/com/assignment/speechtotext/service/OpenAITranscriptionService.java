package com.assignment.speechtotext.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
@Profile("!local")
public class OpenAITranscriptionService
        implements TranscriptionService {

    private static final String TRANSCRIPTION_URL =
            "https://api.openai.com/v1/audio/transcriptions";

    private static final String MODEL =
            "gpt-4o-mini-transcribe";

    private final HttpClient httpClient;
    private final JsonMapper jsonMapper;
    private final String apiKey;

    public OpenAITranscriptionService(
            JsonMapper jsonMapper,
            @Value("${OPENAI_API_KEY:}") String apiKey) {

        this.jsonMapper = jsonMapper;
        this.apiKey = apiKey;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public CompletableFuture<String> transcribe(
            MultipartFile audio) {

        if (apiKey == null || apiKey.isBlank()) {

            return CompletableFuture.failedFuture(
                    new IllegalStateException(
                            "OPENAI_API_KEY is not configured."
                    )
            );
        }

        String boundary =
                "----SpeechToTextBoundary"
                        + UUID.randomUUID();

        byte[] requestBody;

        try {

            requestBody =
                    buildMultipartBody(
                            audio,
                            boundary
                    );

        } catch (IOException e) {

            return CompletableFuture.failedFuture(
                    new IllegalStateException(
                            "Unable to prepare audio for transcription.",
                            e
                    )
            );
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TRANSCRIPTION_URL))
                .timeout(Duration.ofSeconds(30))
                .header(
                        "Authorization",
                        "Bearer " + apiKey
                )
                .header(
                        "Content-Type",
                        "multipart/form-data; boundary="
                                + boundary
                )
                .POST(
                        HttpRequest.BodyPublishers
                                .ofByteArray(requestBody)
                )
                .build();

        return httpClient.sendAsync(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                )
                .thenApply(this::handleResponse);
    }

    private String handleResponse(
            HttpResponse<String> response) {

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new CompletionException(
                    new IllegalStateException(
                            "OpenAI transcription request failed "
                                    + "with HTTP status "
                                    + response.statusCode()
                    )
            );
        }

        try {

            JsonNode json =
                    jsonMapper.readTree(
                            response.body()
                    );

            JsonNode textNode =
                    json.get("text");

            if (textNode == null
                    || !textNode.isString()) {

                throw new IllegalStateException(
                        "OpenAI response did not contain "
                                + "transcription text."
                );
            }

            return textNode.asString();

        } catch (Exception e) {

            throw new CompletionException(
                    new IllegalStateException(
                            "Unable to read OpenAI response.",
                            e
                    )
            );
        }
    }

    private byte[] buildMultipartBody(
            MultipartFile audio,
            String boundary) throws IOException {

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        String fileName =
                audio.getOriginalFilename();

        if (fileName == null
                || fileName.isBlank()) {

            fileName = "recording.webm";
        }

        fileName = sanitiseFileName(fileName);

        String contentType =
                audio.getContentType();

        if (contentType == null
                || contentType.isBlank()) {

            contentType =
                    "application/octet-stream";
        }

        writeText(
                output,
                "--" + boundary + "\r\n"
        );

        writeText(
                output,
                "Content-Disposition: form-data; "
                        + "name=\"model\"\r\n\r\n"
        );

        writeText(
                output,
                MODEL + "\r\n"
        );

        writeText(
                output,
                "--" + boundary + "\r\n"
        );

        writeText(
                output,
                "Content-Disposition: form-data; "
                        + "name=\"file\"; filename=\""
                        + fileName
                        + "\"\r\n"
        );

        writeText(
                output,
                "Content-Type: "
                        + contentType
                        + "\r\n\r\n"
        );

        output.write(audio.getBytes());

        writeText(
                output,
                "\r\n--"
                        + boundary
                        + "--\r\n"
        );

        return output.toByteArray();
    }

    private void writeText(
            ByteArrayOutputStream output,
            String value) throws IOException {

        output.write(
                value.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String sanitiseFileName(
            String fileName) {

        return fileName
                .replace("\"", "")
                .replace("\r", "")
                .replace("\n", "");
    }
}