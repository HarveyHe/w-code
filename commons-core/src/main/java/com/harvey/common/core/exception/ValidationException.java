package com.harvey.common.core.exception;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.harvey.common.core.model.Errors;

public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Errors errors = new Errors();

    public ValidationException() {

    }

    public ValidationException(Map<Serializable, String> errors) {
        this.errors.putAll(errors);
    }

    public ValidationException(String field, String message) {
        this.addError(field, message);
    }

    public void addError(Serializable prop, String message) {
        this.errors.put(prop, message);
    }

    public String getError(Serializable field) {
        return this.errors.get(field);
    }

    public Map<Serializable, String> getErrors() {
        return new LinkedHashMap<Serializable, String>(errors);
    }

    public List<String> getMessages() {
        List<String> messages = new LinkedList<String>();
        for (String message : this.errors.values()) {
            messages.add(message);
        }
        return messages;
    }

    public boolean hasError() {
        return !this.errors.isEmpty();
    }
}
