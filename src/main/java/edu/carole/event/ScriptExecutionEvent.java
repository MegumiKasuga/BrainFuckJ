package edu.carole.event;

import edu.carole.compile.Script;
import edu.carole.env.Environment;
import edu.carole.env.Memory;
import lombok.Getter;

public class ScriptExecutionEvent {

    @Getter
    private final Stage stage;

    @Getter
    private final Script script;

    @Getter
    private final Memory memory;

    @Getter
    private final Environment environment;

    public ScriptExecutionEvent(Environment environment, Memory memory, Script script, Stage stage) {
        this.stage = stage;
        this.script = script;
        this.memory = memory;
        this.environment = environment;
    }
}
