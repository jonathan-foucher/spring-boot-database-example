package com.jonathanfoucher.databaseexample.common.errors;

public class DirectorNotFoundException extends RuntimeException {
    public DirectorNotFoundException(Long id) {
        super("Director with id " + id + " not found");
    }
}
