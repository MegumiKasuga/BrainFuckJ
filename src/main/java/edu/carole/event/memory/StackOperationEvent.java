package edu.carole.event.memory;

import edu.carole.env.Memory;
import edu.carole.event.Stage;
import edu.carole.operator.StackMark;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class StackOperationEvent {

    public enum Type {
        PUSH, POP;
    }

    @Getter
    private final Stage stage;

    @Getter
    private final Memory memory;

    @Getter
    @Nullable
    private final StackMark mark;

    @Getter
    private final Type type;

    @Getter
    @Nullable
    private final Throwable exception;

    public StackOperationEvent(Memory memory, @Nullable StackMark mark,
                               Type type, Stage stage, Throwable exception) {
        this.stage = stage;
        this.memory = memory;
        this.mark = mark;
        this.type = type;
        this.exception = exception;
    }
}
