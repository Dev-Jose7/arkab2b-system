package com.arka.directory.application.exception;

public class DirectoryValidationException extends ApplicationException {

    public DirectoryValidationException(String message) {
        super("directory_validation_error", message);
    }
}
