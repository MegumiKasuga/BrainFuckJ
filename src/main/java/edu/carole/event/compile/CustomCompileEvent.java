package edu.carole.event.compile;

import edu.carole.compile.Compiler;
import lombok.Getter;

public class CustomCompileEvent {

    @Getter
    private final Compiler compiler;

    @Getter
    private final CompileParams params;

    @Getter
    private final int row, col;

    public CustomCompileEvent(int row, int col, Compiler compiler, CompileParams params) {
        this.row = row;
        this.col = col;
        this.compiler = compiler;
        this.params = params;
    }
}
