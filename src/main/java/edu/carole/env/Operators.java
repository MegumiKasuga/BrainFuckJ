package edu.carole.env;

import edu.carole.operator.BasicOperator;
import edu.carole.operator.Operator;
import edu.carole.operator.StackMark;
import edu.carole.operator.io.InputOperator;
import edu.carole.operator.io.OutputOperator;
import edu.carole.util.IOMode;
import lombok.Getter;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;

public class Operators {

    private final HashMap<Character, Operator> operators;

    @Getter
    private InputOperator input = null;

    @Getter
    private OutputOperator output = null;

    public Operators() {
        operators = new HashMap<>();
    }

    public void register(Operator operator) {
        if (operator instanceof InputOperator in) {
            if (this.input != null) {
                throw new RuntimeException("input operator is already registered");
            }
            this.input = in;
        } else if (operator instanceof OutputOperator out) {
            if (this.output != null) {
                throw new RuntimeException("output operator is already registered");
            }
            this.output = out;
        }
        operators.put(operator.getOp(), operator);
    }

    public InputStream getInputStream() {
        if (input == null) {
            throw new RuntimeException("input operator is null");
        }
        return input.getInStream();
    }

    public PrintStream getPrintStream() {
        if (output == null) {
            throw new RuntimeException("output operator is null");
        }
        return output.getPrinter();
    }

    public Operator getOperator(char op) {
        return operators.get(op);
    }

    public boolean containsOperator(char op) {
        return operators.containsKey(op);
    }

    public static Operators getDefaultOperators(InputStream inputStream,
                                                PrintStream printStream,
                                                IOMode inputMode,
                                                IOMode outputMode) {
        Operators operators = new Operators();
        operators.register(new BasicOperator('<', (i, m) -> {
            m.prev((short) 1);
            return i + 1;
        }));
        operators.register(new BasicOperator('>', (i, m) -> {
            m.next((short) 1);
            return i + 1;
        }));
        operators.register(new BasicOperator('+', (i, m) -> {
            m.increase((short) 1);
            return i + 1;
        }));
        operators.register(new BasicOperator('-', (i, m) -> {
            m.decrease((short) 1);
            return i + 1;
        }));
        operators.register(new InputOperator(',', printStream, inputStream, inputMode));
        operators.register(new OutputOperator('.', printStream, outputMode));
        operators.register(new BasicOperator('*', (i, m) ->
                m.getMark().execute(i, m)));
        operators.register(new BasicOperator('!', (i, m) -> {
            m.clear();
            return i + 1;
        }));
        operators.register(new BasicOperator('(', (i, m) -> {
            m.leftShift((short) 1);
            return i + 1;
        }));
        operators.register(new BasicOperator(')', (i, m) -> {
            m.rightShift((short) 1);
            return i + 1;
        }));
        operators.register(new BasicOperator('^', (i, m) -> {
            m.setPointer(m.getData());
            return i + 1;
        }));
        operators.register(new BasicOperator('@', (i, m) -> {
            m.setData((short) 0);
            return i + 1;
        }));
        operators.register(new BasicOperator('}', (i, m) -> {
            StackMark sm = m.popStack();
            return sm.getIndex() + 1;
        }));
        operators.register(new BasicOperator('|', (i, m) -> {
            m.resetPointer();
            return i + 1;
        }));
        operators.register(new BasicOperator('%',  (i, m) -> {
            short data = m.getData();
            m.setData(m.getData(data));
            return i + 1;
        }));
        return operators;
    }
}
