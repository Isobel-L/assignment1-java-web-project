# Speech-to-Text Web Application

COMP3011 Assignment 1 Java web application implemented using Spring Boot.

The application records audio through the user's web browser, sends the recording to a Java REST backend, and transcribes the audio using the OpenAI speech-to-text API with the `gpt-4o-mini-transcribe` model.

## Features

- Browser-based microphone recording
- Start and stop recording controls
- Clear recording, uploading and completion states
- Playback of the captured audio
- Audio upload through a REST endpoint
- OpenAI speech-to-text integration using `gpt-4o-mini-transcribe`
- Local stub transcription profile for development and automated testing
- Server uptime REST API
- Global input and output token statistics REST API
- Graceful server shutdown REST API
- Asynchronous external API communication
- Thread-safe shared application statistics
- Automated controller regression tests
- More than 200 simultaneous blocking HTTP request regression test
- Race-condition regression test

## Technology

- Java 17
- Spring Boot 4.1.1
- Spring MVC
- Maven
- Java HttpClient
- JUnit 5
- Mockito
- HTML
- CSS
- JavaScript

## Architecture

The application uses a layered architecture that separates HTTP request handling, application logic and data returned to clients.

```text
Browser
   |
   | HTTP / REST
   v
Spring MVC Controllers
   |
   v
Service Layer
   |
   +---- OpenAITranscriptionService
   |
   +---- LocalTranscriptionService
   |
   +---- GlobalStatsService
   |
   +---- AdminService
   |
   v
External OpenAI Speech-to-Text API
```

This separation allows the REST layer to remain independent of the specific transcription implementation and makes the application easier to test.

## Project Structure

```text
src/main/java/com/assignment/speechtotext
|
+-- controller
|   +-- AdminController.java
|   +-- GlobalStatsController.java
|   +-- TranscriptionController.java
|
+-- model
|   +-- ErrorResponse.java
|   +-- GlobalStatsResponse.java
|   +-- ShutdownResponse.java
|   +-- TranscriptionResponse.java
|   +-- UptimeResponse.java
|
+-- service
|   +-- AdminService.java
|   +-- GlobalStatsService.java
|   +-- TranscriptionService.java
|   +-- OpenAITranscriptionService.java
|   +-- LocalTranscriptionService.java
|
+-- Assignment1JavaWebProjectApplication.java

src/main/resources
|
+-- static
|   +-- css
|   +-- js
|   +-- index.html
|
+-- application.properties

src/test/java/com/assignment/speechtotext
|
+-- Assignment1JavaWebProjectApplicationTests.java
|
+-- controller
|   +-- AdminControllerTest.java
|   +-- GlobalStatsControllerTest.java
|   +-- TranscriptionControllerTest.java
|
+-- concurrency
    +-- ConcurrentRequestTest.java
    +-- GlobalStatsRaceConditionTest.java
```

## Controllers

### TranscriptionController

Handles uploaded audio recordings and delegates speech-to-text processing to the configured `TranscriptionService`.

The controller works with a `CompletableFuture`, allowing transcription results to be returned asynchronously.

### GlobalStatsController

Provides cumulative OpenAI input and output token usage since the application process started.

### AdminController

Provides:

- server uptime information
- graceful server shutdown

Repeated shutdown requests are handled safely so that shutdown cannot be started multiple times.

## Service Layer

### TranscriptionService

Defines the abstraction used by the application for speech transcription.

Two implementations are available:

- `OpenAITranscriptionService` for the normal production/TITAN environment
- `LocalTranscriptionService` for local development and testing

This separation allows the transcription implementation to be changed without modifying the controller.

### OpenAITranscriptionService

Uses Java's `HttpClient` to send multipart audio requests to:

```text
https://api.openai.com/v1/audio/transcriptions
```

using:

```text
gpt-4o-mini-transcribe
```

The service uses `sendAsync` so the application does not need to synchronously wait for the external API response.

The returned transcription text is passed back to the controller and the returned input/output token usage is added to the global statistics.

### LocalTranscriptionService

Provides a deterministic local response instead of contacting OpenAI.

This allows the browser workflow and automated tests to run without requiring a real OpenAI API key.

### GlobalStatsService

Maintains cumulative input and output token counts.

The counters use `AtomicLong` so multiple transcription requests can update the totals concurrently without losing updates.

### AdminService

Maintains server startup information and graceful shutdown state.

An `AtomicBoolean` is used to ensure that only one graceful shutdown request can begin the shutdown process.

## OpenAI API Configuration

The OpenAI API key is **not stored in the source code**.

The application retrieves it from the environment variable:

```text
OPENAI_API_KEY
```

The normal transcription service reads this value at runtime.

TITAN supplies the required environment variable when the application is executed in the assignment testing environment.

The key:

- is not hard-coded into Java source files
- is not stored in GitHub
- is not returned by a REST endpoint
- is not exposed to browser JavaScript
- is not intentionally written to application logs

## Local Development Profile

The project includes a Spring profile named:

```text
local
```

When this profile is active, `LocalTranscriptionService` is used instead of the real OpenAI service.

This makes it possible to test the complete browser recording and upload workflow without an OpenAI API key.

### Running with the local profile

First build the project:

```powershell
.\mvnw.cmd clean package
```

Then activate the local profile:

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
```

Run the executable JAR:

```powershell
java -jar .\target\assignment1-java-web-project-0.0.1-SNAPSHOT.jar
```

Open:

```text
http://localhost:8080
```

in a web browser.

When finished, stop the application with:

```text
Ctrl+C
```

The local profile can then be removed from the PowerShell environment with:

```powershell
Remove-Item Env:SPRING_PROFILES_ACTIVE
```

## Building the Application

The project targets Java 17 and uses the Maven wrapper included in the repository.

Build and run all tests with:

```powershell
.\mvnw.cmd clean package
```

The executable Spring Boot JAR is produced at:

```text
target/assignment1-java-web-project-0.0.1-SNAPSHOT.jar
```

The `.jar.original` file is not the executable submission JAR.

## Running the Automated Tests

Run the complete test suite with:

```powershell
.\mvnw.cmd test
```

The current automated test suite contains nine tests covering application startup, REST API behaviour, transcription behaviour, concurrency and race-condition protection.

## REST API

### Server Uptime

```http
GET /api/v1/admin/uptime
```

Returns:

- UTC server start time
- current UTC time
- server uptime in seconds

Example:

```json
{
  "utcServerStart": "2026-09-11T00:00:00Z",
  "utcNow": "2026-09-11T00:01:30Z",
  "serverUptimeSeconds": 90.0
}
```

### Graceful Shutdown

```http
POST /api/v1/admin/shutdown
```

A successful request returns HTTP `202 Accepted`.

Example:

```json
{
  "message": "Graceful shutdown requested."
}
```

If graceful shutdown has already been requested, the API returns HTTP `409 Conflict`.

### Global Statistics

```http
GET /api/v1/global/stats
```

Returns cumulative OpenAI transcription token usage since the Java process started.

Example:

```json
{
  "inputTokens": 120,
  "outputTokens": 42
}
```

The initial values are zero and increase as successful OpenAI transcription responses report token usage.

### Audio Transcription

```http
POST /api/v1/transcriptions
```

Accepts an audio recording as multipart form data using the field:

```text
audio
```

A successful response contains the transcription text.

Example:

```json
{
  "text": "Example transcription."
}
```

Empty audio data produces a `400 Bad Request` error response.

## Browser Workflow

The user opens:

```text
http://localhost:8080
```

The browser then performs the following workflow:

```text
Start Recording
      |
      v
Microphone recording begins
      |
      v
Stop Recording
      |
      v
Recorded audio is prepared
      |
      v
POST /api/v1/transcriptions
      |
      v
Java backend
      |
      v
OpenAI speech-to-text service
      |
      v
Transcription displayed in browser
```

The page displays visible state changes so the user can determine when recording is active, when the recording has stopped and when the transcription is being processed.

## Concurrency Design

Concurrency was considered in both external API communication and shared application state.

### Asynchronous Transcription

`OpenAITranscriptionService` uses:

```java
HttpClient.sendAsync(...)
```

rather than the blocking `send(...)` operation for communication with the external OpenAI API.

The transcription service returns a:

```java
CompletableFuture<String>
```

which is propagated through the controller.

This prevents the application from unnecessarily synchronously waiting for the external speech-to-text API.

### Thread-Safe Token Statistics

Input and output token totals are shared application state.

They are stored using:

```java
AtomicLong
```

rather than ordinary mutable `long` variables.

This prevents lost-update race conditions when multiple transcription requests complete at approximately the same time.

### Thread-Safe Shutdown State

The graceful shutdown state uses:

```java
AtomicBoolean
```

with an atomic state transition.

This prevents multiple concurrent HTTP requests from independently starting the shutdown operation.

## Regression Testing

The project includes regression tests for the main REST API behaviour.

### AdminControllerTest

Tests:

- uptime response
- successful graceful shutdown request
- conflict response when shutdown has already been requested

### GlobalStatsControllerTest

Tests that the global statistics endpoint returns the expected input and output token totals.

### TranscriptionControllerTest

Uses a mocked `TranscriptionService` rather than calling the real OpenAI service.

Tests:

- successful audio transcription response
- rejection of empty audio data

Using a mocked transcription service keeps the controller tests deterministic and prevents automated tests from depending on an external API or API key.

## Concurrent HTTP Request Test

`ConcurrentRequestTest` starts the Spring Boot application on a random HTTP port using the local transcription profile.

The test creates:

```text
250 request tasks
```

which is greater than 200.

A `CountDownLatch` releases the tasks together so that they attempt their requests at approximately the same time.

Each task uses the blocking Java HTTP operation:

```java
HttpClient.send(...)
```

to make a request to the running Spring Boot application.

Every response must return HTTP status `200`.

This regression test provides evidence that the application can handle more than 200 simultaneous blocking HTTP requests in a single Java process.

## Race-Condition Regression Test

`GlobalStatsRaceConditionTest` specifically tests shared mutable state.

The test creates:

```text
250 concurrent threads
```

and each thread performs:

```text
1,000 token-statistic updates
```

against the same `GlobalStatsService` instance.

Each update adds:

```text
2 input tokens
3 output tokens
```

The expected final totals are therefore:

```text
Input tokens  = 500,000
Output tokens = 750,000
```

The test asserts that the exact expected totals are produced.

If updates were performed using unsafe read-modify-write operations, concurrent updates could be lost. The use of `AtomicLong` ensures that the expected totals are maintained.

## Test Results

The complete Maven test suite currently reports:

```text
Tests run: 9
Failures: 0
Errors: 0
Skipped: 0
```

## TITAN Testing

The executable Spring Boot JAR has been tested using the TITAN assignment testing environment.

The latest TITAN functional test successfully passed:

```text
11 / 11 features
```

The successful checks included:

- initial server uptime API response
- initial global statistics API response
- webpage loading successfully
- presence of the start-recording control
- clear recording-in-progress state
- presence of the stop-recording control
- clear recording-stopped state
- correct transcription within five seconds
- correct final global token statistics
- correct final server uptime
- graceful server shutdown

This also confirmed that the application could perform a real transcription using the API configuration supplied by TITAN.

## Error Handling

REST endpoints return appropriate HTTP status codes for invalid or conflicting requests.

Structured error responses use the following fields:

```text
timestamp
status
error
message
path
```

This provides clients with consistent information when a request cannot be completed successfully.

## Graceful Shutdown

Spring graceful shutdown is enabled through application configuration.

When:

```http
POST /api/v1/admin/shutdown
```

is called, the application accepts the shutdown request and closes the Spring application context.

The shutdown state is protected so repeated requests cannot initiate multiple shutdown operations.

## API Specification

The assignment API specification is stored in:

```text
docs/assignment1api.yaml
```

The implemented administration and global statistics endpoints follow this specification.

## Security Considerations

The main security consideration for the application is protection of the external API credential.

The application follows these practices:

- credentials are obtained from environment configuration
- credentials are not committed to source control
- API requests are made by the Java backend rather than directly by browser JavaScript
- the API key is not returned to clients
- test environments can use a local stub rather than requiring a real credential

## Final Verification

Before producing the submission JAR, the project can be verified with:

```powershell
.\mvnw.cmd clean test
```

followed by:

```powershell
.\mvnw.cmd clean package
```

The executable file used for TITAN testing is:

```text
target/assignment1-java-web-project-0.0.1-SNAPSHOT.jar
```