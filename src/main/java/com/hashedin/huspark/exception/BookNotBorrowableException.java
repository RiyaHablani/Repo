package com.hashedin.huspark.exception;

public class BookNotBorrowableException extends RuntimeException {
    public BookNotBorrowableException(String message) {
        super(message);
    }
}
