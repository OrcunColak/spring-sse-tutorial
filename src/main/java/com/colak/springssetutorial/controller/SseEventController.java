package com.colak.springssetutorial.controller;

import com.colak.springssetutorial.model.ToDo;
import com.colak.springssetutorial.repository.TodoRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController

@RequiredArgsConstructor
@Slf4j
public class SseEventController {
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private final TodoRepository repository;
    private final Faker faker = new Faker();

    // http://localhost:8080/home.html
    // http://localhost:8080/todos/sse
    // SseEmitter is actually a subclass of ResponseBodyEmitter and provides additional Server-Sent Event (SSE) support out-of-the-box.
    @GetMapping(path = "/todos/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter eventStream() {
        log.info("Adding new emitter");

        // Create an emitter that does not time out
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);

        // Remove the emitter when it times out or completes
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));

        sendUpdates();

        return emitter;
    }

    // Update every 5 seconds
    @Scheduled(fixedRate = 5_000)
    public void updateTodoRandomly() {
        log.info("Updating random data");
        String fakeString = faker.lorem().sentence();
        repository.updateTitle(fakeString);
        sendUpdates();
    }

    private void sendUpdates() {
        log.info("Emitter size : {}", emitters.size());

        List<SseEmitter> emittersToRemove = new ArrayList<>();
        List<ToDo> toDoList = repository.getToDoList();
        for (SseEmitter emitter : emitters) {
            for (ToDo toDo : toDoList) {
                try {
                    SseEmitter.SseEventBuilder sseEventBuilder = SseEmitter.event()
                            .id(toDo.getTaskId())
                            .data(toDo, MediaType.APPLICATION_JSON);
                    emitter.send(sseEventBuilder);
                } catch (Exception exception) {
                    log.error(exception.getMessage());
                    emitter.completeWithError(exception);
                    emittersToRemove.add(emitter);
                }
            }
        }
        emitters.removeAll(emittersToRemove);
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

    private SseEmitter.SseEventBuilder createEvent(int counter) {
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
