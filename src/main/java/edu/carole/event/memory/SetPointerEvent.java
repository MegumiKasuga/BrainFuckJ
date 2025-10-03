package edu.carole.event.memory;

import edu.carole.env.Memory;
import edu.carole.event.Stage;
import lombok.Getter;

public class SetPointerEvent {

    @Getter
    private final Memory memory;

    @Getter
    private final short pointer;

    @Getter
    private final short originalPointer;

    @Getter
    private final Stage stage;

    @Getter
    private final Type type;

    public enum Type {
        SET, INC, DEC, RESET;
    }

    public SetPointerEvent(Memory memory, short originalPointer, short pointer, Type type, Stage stage) {
        this.memory = memory;
        this.pointer = pointer;
        this.originalPointer = originalPointer;
        this.stage = stage;
        this.type = type;
    }
}
