package com.arka.directory.application.exception;

public class DirectoryResourceNotFoundException extends ApplicationException {

    public DirectoryResourceNotFoundException(String message) {
        super("directory_resource_not_found", message);
    }
}
