package edu.carole.exceptions;

import lombok.Getter;
import org.apache.log4j.Logger;

import java.util.UUID;

public class EnvNotFound extends RuntimeException {

    @Getter
    private final UUID id;

    public EnvNotFound(UUID id, String message) {
        super(message + ", cause: environment '" + id + "' not found.");
        this.id = id;
    }



    public void log(Logger logger) {
        logger.error(getMessage());
    }
}
