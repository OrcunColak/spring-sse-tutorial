package com.colak.springtutorial.controller;

import com.colak.springtutorial.model.ToDo;
import com.colak.springtutorial.repository.TodoRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
                            // Used to identify the event and resume from where it left off if the connection is lost.
                            .id(toDo.getTaskId())
                            // Specifies a custom event type.
                            .name("update")
                            // tells the client how many milliseconds to wait before trying again.
                            .reconnectTime(5000)
                            // The payload of the message.
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
}
