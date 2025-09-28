package edu.carole.operator;

import edu.carole.env.Memory;
import lombok.Getter;
import lombok.NonNull;

public class Mark extends Operator {

    @Getter
    private final int index;

    public Mark(char op, int index) {
        super(op);
        this.index = index;
    }

    @Override
    public int execute(int index, @NonNull Memory memory) {
        return this.index + 1;
    }
}
