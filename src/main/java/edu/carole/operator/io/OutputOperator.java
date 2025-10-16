package edu.carole.operator.io;

import edu.carole.env.Memory;
import edu.carole.util.IOMode;
import edu.carole.operator.Operator;
import lombok.Getter;
import lombok.NonNull;

import java.io.PrintStream;

public class OutputOperator extends Operator {

    @Getter
    private final PrintStream printer;

    @Getter
    private final IOMode mode;

    public OutputOperator(char op, PrintStream printer, IOMode mode) {
        super(op);
        this.printer = printer;
        this.mode = mode;
    }

    @Override
    public int execute(int index, @NonNull Memory memory) {
        switch (mode) {
            case CHAR -> printer.print((char) memory.getData());
            case NUMBER ->  printer.print(memory.getData());
        }
        return index + 1;
    }
}
