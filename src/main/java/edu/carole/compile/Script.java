package edu.carole.compile;

import edu.carole.env.Memory;
import edu.carole.operator.Mark;
import edu.carole.operator.Operator;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class Script {

    @Getter
    @Setter
    private int index;

    @Getter
    private final String scriptName;

    @Getter
    private final String scriptPath;

    @Getter
    private final ArrayList<Operator> operators;

    @Getter
    private final ArrayList<Mark> scriptMarks;

    public Script(String scriptName, String scriptPath, int size) {
        operators = new ArrayList<>(size);
        index = 0;
        this.scriptName = scriptName;
        this.scriptPath = scriptPath;
        scriptMarks = new ArrayList<>(256);
    }

    public Script(String scriptName, String scriptPath, List<Operator> operators, ArrayList<Mark> scriptMarks) {
        this.operators = new ArrayList<>(operators);
        this.scriptMarks = scriptMarks;
        this.scriptName = scriptName;
        this.scriptPath = scriptPath;
        if (scriptMarks.size() > 256) {
            throw new IllegalStateException("Could only have at most 256 " +
                    "script marks (found " + scriptMarks.size() +")");
        }
    }

    public void addOperator(Operator operator) {
        operators.add(operator);
    }

    public void execute(Memory memory, Logger logger) {
        logger.info(String.format("Executing script \"%s\"", scriptName));
        try {
            while (index < operators.size() && index >= 0) {
                index = operators.get(index).execute(index, memory);
            }
        } catch (Exception e) {
            logger.trace(e.getLocalizedMessage(), e);
        } finally {
            logger.info(String.format("Finished executing script \"%s\"", scriptName));
        }
    }

    public Operator getOperator(int index) {
        return operators.get(index);
    }
}
