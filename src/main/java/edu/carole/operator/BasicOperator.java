package edu.carole.operator;

import edu.carole.env.Memory;

import java.util.function.BiFunction;

public class BasicOperator extends Operator {

    private final BiFunction<Integer, Memory, Integer> execution;

    public BasicOperator(char op, BiFunction<Integer, Memory, Integer> execution) {
        super(op);
        this.execution = execution;
    }

    @Override
    public int execute(int index, Memory memory) {
        return execution.apply(index, memory);
    }
}
