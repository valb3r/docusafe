package org.adorsys.docusafe.rest.exceptions;

/**
 * Created by peter on 05.02.18 at 12:00.
 */
public class RestError {
    public RestError(String message) {
        this.message = message;
    }
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
