package com.colak.springtutorial.repository;

import com.colak.springtutorial.model.ToDo;
import com.colak.springtutorial.model.ToDoSeverity;
import com.colak.springtutorial.model.ToDoStatus;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Getter
public class TodoRepository {

    private final List<ToDo> toDoList = new ArrayList<>();

    public TodoRepository() {
        toDoList.add(new ToDo("1", "title1", "description1", ToDoStatus.NEW, ToDoSeverity.CRITICAL));
        toDoList.add(new ToDo("2", "title2", "description2", ToDoStatus.IN_PROGRESS, ToDoSeverity.HIGH));
        toDoList.add(new ToDo("3", "title3", "description3", ToDoStatus.COMPLETED, ToDoSeverity.LOW));
    }

    public void updateTitle(String title) {
        int randomIndex = ThreadLocalRandom.current().nextInt(toDoList.size());
        ToDo randomTodo = toDoList.get(randomIndex);
        randomTodo.setTitle(title);
    }

}
