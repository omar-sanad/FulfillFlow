package com.fulfillflow.common.error;

/**
 * Thrown when a resource referenced by id or natural key cannot be found.
 * Maps to HTTP 404.
 */
public class NotFoundException extends DomainException {

    public NotFoundException(String code, String message) {
        super(code, message);
    }

    @Override
    public int httpStatus() {
        return 404;
    }
}
