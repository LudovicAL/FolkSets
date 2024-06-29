package com.bandito.folksets.exception;

public class FolkSetsException extends Exception {
    public final boolean fatal;

    public FolkSetsException(String message, Throwable cause) {
        super(message, cause);
        this.fatal = false;
    }
    public FolkSetsException(String message, Throwable cause, boolean fatal) {
        super(message, cause);
        this.fatal = fatal;
    }
}
