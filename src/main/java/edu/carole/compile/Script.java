package edu.carole.compile;

import com.google.common.eventbus.EventBus;
import edu.carole.env.Environment;
import edu.carole.env.ExceptionTracer;
import edu.carole.env.Memory;
import edu.carole.event.ExtendedStage;
import edu.carole.event.OperatorConsumeEvent;
import edu.carole.event.ScriptExecutionEvent;
import edu.carole.event.Stage;
import edu.carole.operator.Mark;
import edu.carole.operator.Operator;
import edu.carole.util.OpPosition;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.EventListener;
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

    @Getter
    private final ExceptionTracer tracer;

    @Getter
    private final ArrayList<OpPosition> positions;

    @Getter
    private final ArrayList<String> lines;

    public Script(String scriptName, String scriptPath, ExceptionTracer tracer, int size) {
        operators = new ArrayList<>(size);
        index = 0;
        this.scriptName = scriptName;
        this.scriptPath = scriptPath;
        this.positions = new ArrayList<>(size);
        this.lines = new ArrayList<>();
        scriptMarks = new ArrayList<>(256);
        this.tracer = tracer;
    }

    public Script(String scriptName, String scriptPath, List<Operator> operators,
                  ArrayList<Mark> scriptMarks, ArrayList<OpPosition> opPositions,
                  ArrayList<String> lines, ExceptionTracer tracer) {
        this.operators = new ArrayList<>(operators);
        this.scriptMarks = scriptMarks;
        this.scriptName = scriptName;
        this.scriptPath = scriptPath;
        this.positions = opPositions;
        this.tracer = tracer;
        this.lines = lines;
        if (scriptMarks.size() > 256) {
            throw new IllegalStateException("Could only have at most 256 " +
                    "script marks (found " + scriptMarks.size() +")");
        }
    }

    public Script(String scriptName, String scriptPath, List<Operator> operators,
                  ArrayList<Mark> scriptMarks, ExceptionTracer tracer) {
        this.operators = new ArrayList<>(operators);
        this.scriptMarks = scriptMarks;
        this.scriptName = scriptName;
        this.scriptPath = scriptPath;
        this.positions = new ArrayList<>(operators.size());
        this.tracer = tracer;
        this.lines = new ArrayList<>();
        if (scriptMarks.size() > 256) {
            throw new IllegalStateException("Could only have at most 256 " +
                    "script marks (found " + scriptMarks.size() +")");
        }
    }

    public void addOperator(Operator operator) {
        operators.add(operator);
    }

    public void execute(Environment environment, Memory memory, Logger logger) throws Exception {
        logger.info(String.format("Executing script \"%s\"", scriptName));
        long milliSec = System.currentTimeMillis();
        EventBus bus = environment.getEventBus();
        try {
            bus.post(new ScriptExecutionEvent(environment, memory,
                    this, Stage.PRE));
            Operator operator;
            int i;
            while (index < operators.size() && index >= 0) {
                i = index;
                operator = operators.get(index);
                try {
                    bus.post(new OperatorConsumeEvent(index, operator, memory,
                            ExtendedStage.PRE, this, environment));
                    index = operator.execute(index, memory);
                    bus.post(new OperatorConsumeEvent(index, operator, memory,
                            ExtendedStage.POST, this, environment));
                } catch (Exception e) {
                    Exception e2 = tracer.getRuntimeTraceMessage(this, i, e);
                    if (tracer.traceRuntime(this, i, e)) {
                        bus.post(new OperatorConsumeEvent(index, operator, memory,
                                ExtendedStage.IGNORED_ERROR, this, environment));
                        logger.error(
                                String.format("Error executing script \"%s\", ignored!", scriptName),
                                e2 == null ? e : e2);
                        logger.trace("Error message:", e2 == null ? e : e2);
                        continue;
                    }
                    bus.post(new OperatorConsumeEvent(index, operator, memory,
                            ExtendedStage.FATAL_ERROR, this, environment));
                    throw e2 == null ? e : e2;
                }
            }
        } catch (Exception e) {
            bus.post(new ScriptExecutionEvent(environment, memory,
                    this, Stage.ERROR));
            logger.fatal("Error executing script \"" + scriptName + "\"", e);
            logger.trace("Error message: ", e);
            throw e;
        } finally {
            bus.post(new ScriptExecutionEvent(environment, memory,
                    this, Stage.POST));
            logger.info(String.format("Finished executing script \"%s\" in %d milliseconds", scriptName, System.currentTimeMillis() - milliSec));
        }
    }

    public Operator getOperator(int index) {
        return operators.get(index);
    }
}
