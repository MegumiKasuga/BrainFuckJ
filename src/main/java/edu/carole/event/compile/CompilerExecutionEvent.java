package edu.carole.event.compile;

import edu.carole.compile.Compiler;
import edu.carole.event.ExtendedStage;
import edu.carole.event.Stage;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class CompilerExecutionEvent {

    @Getter
    private final ExtendedStage stage;

    @Getter
    private final Compiler compiler;

    @Getter
    @Nullable
    private final Throwable cause;

    @Getter
    private final CompileParams compileParams;

    @Getter
    private final int row, column;

    public CompilerExecutionEvent(int row, int column, Compiler compiler, CompileParams params, ExtendedStage stage, Throwable cause) {
        this.stage = stage;
        this.compiler = compiler;
        this.cause = cause;
        this.compileParams = params;
        this.row = row;
        this.column = column;
    }
}
