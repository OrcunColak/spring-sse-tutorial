package com.colak.springssetutorial.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class SseEventController {

    // http://localhost:8080/events
    @GetMapping(path = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter eventStream() {
        SseEmitter emitter = new SseEmitter();
        ExecutorService sseMvcExecutor = Executors.newSingleThreadExecutor();
        sseMvcExecutor.execute(() -> executeSseLogic(emitter));
        return emitter;
    }

    private void executeSseLogic(SseEmitter emitter) {
        try {
            for (int counter = 0; counter < 10; counter++) {
                // Create an event with a custom event ID and data
                SseEmitter.SseEventBuilder event = createEvent(counter);
                // Send the event to the client
                emitter.send(event);
                // Wait for one second before sending the next event
                Thread.sleep(1000);
            }
            // Mark the end of the event stream
            emitter.complete();
        } catch (IOException | InterruptedException exception) {
            emitter.completeWithError(exception);
        }
    }

    private static SseEmitter.SseEventBuilder createEvent(int counter) {
        // Event has these fields
        // id : The ID of the event
        // type : the type of event
        // data : The event data
        // reconnectTime : Reconnection time for the event stream
        return SseEmitter
                .event()
                .id(String.valueOf(counter))
                .data("Event data at " + LocalTime.now());
    }
}
