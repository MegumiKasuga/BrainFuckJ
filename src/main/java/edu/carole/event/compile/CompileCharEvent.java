package edu.carole.event.compile;

import edu.carole.compile.Compiler;
import edu.carole.event.Stage;
import lombok.Getter;

public class CompileCharEvent {

    @Getter
    private final Compiler compiler;

    @Getter
    private final Stage stage;

    @Getter
    private final CompileParams params;

    @Getter
    private final int row, col;

    @Getter
    private final boolean annotate;

    public CompileCharEvent(int row, int col, Compiler compiler,
                            CompileParams params, boolean inAnnotate, Stage stage) {
        this.row = row;
        this.col = col;
        this.compiler = compiler;
        this.params = params;
        this.stage = stage;
        this.annotate = inAnnotate;
    }
}
