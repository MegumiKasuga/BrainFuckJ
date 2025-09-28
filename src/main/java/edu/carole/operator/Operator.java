package edu.carole.operator;

import edu.carole.env.Memory;
import lombok.Getter;
import lombok.NonNull;

public abstract class Operator {

    @Getter
    private final char op;

    public Operator(char op) {
        this.op = op;
    }

    public abstract int execute(int index, @NonNull Memory memory);

    public boolean is(char op) {
        return this.op == op;
    }
}
