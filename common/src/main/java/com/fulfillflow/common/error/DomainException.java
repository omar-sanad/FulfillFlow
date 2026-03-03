package com.fulfillflow.common.error;

/**
 * Base class for all domain errors that a service can translate into a
 * structured {@link ErrorResponse}. Subclasses fix the HTTP status and the
 * stable error code so controllers stay thin.
 */
public abstract class DomainException extends RuntimeException {

    private final String code;

    protected DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    protected DomainException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public abstract int httpStatus();
}
