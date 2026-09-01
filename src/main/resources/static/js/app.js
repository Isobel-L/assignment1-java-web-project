const startButton = document.getElementById("startButton");
const stopButton = document.getElementById("stopButton");
const statusElement = document.getElementById("status");
const errorMessage = document.getElementById("errorMessage");
const recordingSection = document.getElementById("recordingSection");
const audioPlayback = document.getElementById("audioPlayback");

let mediaRecorder = null;
let audioChunks = [];
let microphoneStream = null;
let currentAudioUrl = null;


startButton.addEventListener("click", startRecording);
stopButton.addEventListener("click", stopRecording);


async function startRecording() {

    clearError();

    try {

        microphoneStream = await navigator.mediaDevices.getUserMedia({
            audio: true
        });

        audioChunks = [];

        mediaRecorder = new MediaRecorder(microphoneStream);

        mediaRecorder.addEventListener("dataavailable", event => {

            if (event.data.size > 0) {
                audioChunks.push(event.data);
            }
        });

        mediaRecorder.addEventListener("stop", handleRecordingStopped);

        mediaRecorder.start();

        updateRecordingState(true);

    } catch (error) {

        console.error("Unable to access microphone:", error);

        showError(
            "Microphone access failed. Please allow microphone access and try again."
        );

        updateRecordingState(false);
    }
}


function stopRecording() {

    if (mediaRecorder === null) {
        return;
    }

    if (mediaRecorder.state !== "recording") {
        return;
    }

    statusElement.textContent = "Finishing recording...";
    statusElement.classList.remove("recording");
    statusElement.classList.add("processing");

    stopButton.disabled = true;

    mediaRecorder.stop();
}


function handleRecordingStopped() {

    const mimeType = mediaRecorder.mimeType || "audio/webm";

    const audioBlob = new Blob(audioChunks, {
        type: mimeType
    });

    if (currentAudioUrl !== null) {
        URL.revokeObjectURL(currentAudioUrl);
    }

    currentAudioUrl = URL.createObjectURL(audioBlob);

    audioPlayback.src = currentAudioUrl;

    recordingSection.hidden = false;

    stopMicrophoneStream();

    statusElement.textContent = "Recording complete. Ready to record again.";
    statusElement.classList.remove("recording", "processing");

    startButton.disabled = false;
    stopButton.disabled = true;
}


function updateRecordingState(recording) {

    if (recording) {

        statusElement.textContent = "Recording... speak now";
        statusElement.classList.add("recording");
        statusElement.classList.remove("processing");

        startButton.disabled = true;
        stopButton.disabled = false;

    } else {

        statusElement.textContent = "Ready to record";
        statusElement.classList.remove("recording", "processing");

        startButton.disabled = false;
        stopButton.disabled = true;
    }
}


function stopMicrophoneStream() {

    if (microphoneStream === null) {
        return;
    }

    microphoneStream.getTracks().forEach(track => {
        track.stop();
    });

    microphoneStream = null;
}


function showError(message) {

    errorMessage.textContent = message;
}


function clearError() {

    errorMessage.textContent = "";
}