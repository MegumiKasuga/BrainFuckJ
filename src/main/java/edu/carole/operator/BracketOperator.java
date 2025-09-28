package edu.carole.operator;

import edu.carole.env.Memory;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class BracketOperator extends Operator {

    @Getter
    @Setter
    private int pairPos;

    @Getter
    private final int pos;

    @Getter
    private final boolean isFront;

    public BracketOperator(char op, boolean isFront, int pos) {
        super(op);
        this.pos = pos;
        this.isFront = isFront;
    }

    @Override
    public int execute(int index, @NonNull Memory memory) {
        if (isFront) {
            short loopValue = memory.getData();
            if (loopValue <= 0) {
                index = pairPos;
            }
            return index + 1;
        } else {
            return pairPos;
        }
    }
}
