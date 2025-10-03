package edu.carole.event;

import edu.carole.compile.Script;
import edu.carole.env.Environment;
import edu.carole.env.Memory;
import edu.carole.operator.Operator;
import lombok.Getter;

public class OperatorConsumeEvent {

    @Getter
    private final Operator operator;

    @Getter
    private final ExtendedStage stage;

    @Getter
    private final Script script;

    @Getter
    private final Environment environment;

    @Getter
    private final int index;

    @Getter
    private final Memory memory;

    public OperatorConsumeEvent(int index,
                                Operator operator, Memory memory,
                                ExtendedStage stage, Script script,
                                Environment environment) {
        this.operator = operator;
        this.stage = stage;
        this.script = script;
        this.environment = environment;
        this.index = index;
        this.memory = memory;
    }
}
