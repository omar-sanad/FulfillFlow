package com.fulfillflow.common.error;

/**
 * Thrown when a domain operation violates a business rule (e.g. transitioning
 * from an incompatible state). Maps to HTTP 409.
 */
public class ConflictException extends DomainException {

    public ConflictException(String code, String message) {
        super(code, message);
    }

    @Override
    public int httpStatus() {
        return 409;
    }
}
