package edu.carole.exceptions;

import edu.carole.util.Couple;
import edu.carole.util.OpPosition;
import lombok.Getter;

public class ScriptException extends RuntimeException {

    @Getter
    private final String currentLine;

    @Getter
    private final OpPosition position;

    @Getter
    private final Phase phase;

    public enum Phase {
        COMPILE, EXECUTE;
    }

    public ScriptException(String message, Exception cause,
                           String currentLine, OpPosition position, Phase phase) {
        super(message, cause);
        this.currentLine = currentLine;
        this.position = position;
        this.phase = phase;
    }

    @Override
    public String getMessage() {
        return super.getMessage() +
                System.lineSeparator() +
                getStringFigure();
    }

    public String getStringFigure() {
        String rAc = position.getRowAndColumn();
        StringBuilder builder = new StringBuilder(rAc)
                .append(" ").append(currentLine)
                .append(System.lineSeparator())
                .append(" ".repeat(rAc.length() + 1 + position.column()))
                .append("^");
        return builder.toString();
    }
}
