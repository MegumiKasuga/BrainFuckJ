package edu.carole.event.memory;

import edu.carole.env.Memory;
import edu.carole.event.Stage;
import lombok.Getter;

public class ClearDataEvent {

    @Getter
    private final Memory memory;

    @Getter
    private final Stage stage;

    public ClearDataEvent(Memory memory, Stage stage) {
        this.memory = memory;
        this.stage = stage;
    }
}
