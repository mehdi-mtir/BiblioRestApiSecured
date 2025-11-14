package com.m2i.BiblioRestApi.exception;

/**
 * Exception levée lorsqu'on tente de créer une ressource qui existe déjà
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s existe déjà avec %s : '%s'",
                resourceName, fieldName, fieldValue));
    }
}
