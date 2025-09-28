package edu.carole.operator;

import edu.carole.env.Memory;
import lombok.NonNull;

public class StackMark extends Mark {

    public StackMark(char op, int index) {
        super(op, index);
    }

    @Override
    public int execute(int index, @NonNull Memory memory) {
        memory.pushStack(this);
        return memory.getMark().execute(index, memory);
    }
}
