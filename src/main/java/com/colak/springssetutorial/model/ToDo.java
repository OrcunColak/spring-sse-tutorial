package com.colak.springssetutorial.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ToDo {
    private String taskId;

    private String title;

    private String description;

    private ToDoStatus toDoStatus;

    private ToDoSeverity severity;
}
