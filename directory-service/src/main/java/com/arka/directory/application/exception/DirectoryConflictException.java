package com.arka.directory.application.exception;

public class DirectoryConflictException extends ApplicationException {

    public DirectoryConflictException(String message) {
        super("directory_conflict", message);
    }
}
