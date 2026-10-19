package com.sms.dao;

/**
 * Custom exception thrown when attempting to insert a student with an existing roll number.
 */
public class DuplicateRollException extends Exception {
    public DuplicateRollException(String message) {
        super(message);
    }
}
