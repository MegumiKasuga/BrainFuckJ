package edu.carole.event.memory;

import edu.carole.env.Memory;
import edu.carole.event.Stage;
import lombok.Getter;

public class SetDataEvent {

    @Getter
    private final short pointer;

    @Getter
    private final short value;

    @Getter
    private final short operate;

    @Getter
    private final Stage stage;

    @Getter
    private final Type type;

    @Getter
    private final Memory memory;

    public enum Type
    {
        SET, INC, DEC, L_SHIFT, R_SHIFT
    }

    public SetDataEvent(Memory memory, short pointer, short value, short operate, Stage stage, Type type) {
        this.pointer = pointer;
        this.value = value;
        this.stage = stage;
        this.type = type;
        this.memory = memory;
        this.operate = operate;
    }

}
