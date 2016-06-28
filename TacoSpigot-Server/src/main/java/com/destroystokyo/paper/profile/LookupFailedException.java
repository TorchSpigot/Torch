package com.destroystokyo.paper.profile;

/**
 * Thrown when the lookup fails, for reason other then a profile not found
 */
public class LookupFailedException extends RuntimeException {
    public LookupFailedException(Throwable cause) {
        super(cause);
    }

    public LookupFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public LookupFailedException(String s) {
        super(s);
    }

}