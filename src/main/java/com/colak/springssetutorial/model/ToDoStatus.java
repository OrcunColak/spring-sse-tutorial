package com.colak.springssetutorial.model;

import lombok.Getter;

@Getter
public enum ToDoStatus {
    NEW("New"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    private final String displayName;

    ToDoStatus(String displayName) {
        this.displayName = displayName;
    }

}
