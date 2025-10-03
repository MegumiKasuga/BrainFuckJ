package edu.carole.env;

import edu.carole.compile.Script;
import edu.carole.exceptions.ScriptException;
import edu.carole.util.OpPosition;
import org.jetbrains.annotations.Nullable;

public class ExceptionTracer {

    public ExceptionTracer() {}

    public boolean traceRuntime(Script script, int index, Exception e) {
        return false;
    }

    public @Nullable ScriptException getRuntimeTraceMessage(Script script, int index, Exception e) {
        OpPosition position = script.getPositions().get(index);
        return new ScriptException(e.getMessage(), e,
                script.getLines().get(position.row()), position,
                ScriptException.Phase.EXECUTE);
    }

    public boolean traceCompile(String currentLine, int column, Exception e) {
        return false;
    }

    public @Nullable ScriptException getCompileTraceMessage(String currentLine, OpPosition position, Exception e) {
        return new ScriptException(e.getMessage(), e,
                currentLine, position,
                ScriptException.Phase.EXECUTE);
    }
}
