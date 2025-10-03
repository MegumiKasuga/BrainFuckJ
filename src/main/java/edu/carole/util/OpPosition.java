package edu.carole.util;

import edu.carole.operator.Operator;
import org.jetbrains.annotations.NotNull;

public record OpPosition(Operator operator, int row, int column) {

    @Override
    public @NotNull String toString() {
        return operator.toString() + " at (" + row + ", " + column + ")";
    }

    public String getRowAndColumn() {
        return "[" + row + ":" + column + "]";
    }
}
