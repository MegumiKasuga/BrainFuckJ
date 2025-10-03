package edu.carole.event.memory;

import edu.carole.env.Memory;
import edu.carole.event.Stage;
import edu.carole.operator.StackMark;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

public class MemoryIOEvent {

    public enum Type {
        SAVE, LOAD;
    }

    @Getter
    private final Memory memory;

    @Getter
    private final Stage stage;

    @Getter
    private final Type type;

    @Getter
    private final ByteBuf buf;

    public MemoryIOEvent(Memory memory, Stage stage, Type type, ByteBuf buf) {
        this.memory = memory;
        this.stage = stage;
        this.type = type;
        this.buf = buf;
    }
}
