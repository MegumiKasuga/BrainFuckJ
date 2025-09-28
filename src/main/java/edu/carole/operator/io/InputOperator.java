package edu.carole.operator.io;

import edu.carole.util.IOMode;
import edu.carole.env.Memory;
import edu.carole.operator.Operator;
import lombok.NonNull;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Objects;
import java.util.Scanner;

public class InputOperator extends Operator {

    private final InputStream inStream;

    private final PrintStream outStream;

    private final IOMode mode;

    public InputOperator(char op, PrintStream outStream,
                         InputStream inStream, IOMode ioMode) {
        super(op);
        this.inStream = inStream;
        this.outStream = outStream;
        this.mode = ioMode;
    }

    @Override
    public int execute(int index, @NonNull Memory memory) {
        outStream.print("input> ");
        Scanner scanner = new Scanner(inStream);
        String line = scanner.nextLine();
        short result;
        if (line.isBlank()) {
            switch (mode) {
                case NUMBER -> memory.setData((short) 0);
                case CHAR ->  memory.setData((short) 10);
            }
            return index + 1;
        } else if (line.length() == 1) {
            if (Objects.requireNonNull(mode) == IOMode.NUMBER) {
                result = ((short) Integer.parseInt(line.substring(0, 1)));
            } else {
                result = (short) ((line.charAt(0)));
            }
        } else {
            try {
                result = (short) Integer.parseInt(line);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid input: " + line);
            }
        }
        memory.setData(result);
        return index + 1;
    }
}
