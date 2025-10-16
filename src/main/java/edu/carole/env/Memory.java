package edu.carole.env;

import edu.carole.compile.Script;
import edu.carole.event.memory.*;
import edu.carole.event.Stage;
import edu.carole.exceptions.StackOverflowException;
import edu.carole.operator.Mark;
import edu.carole.operator.Operator;
import edu.carole.operator.StackMark;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class Memory {

    @Getter
    private short pointer = 0;

    @Getter
    private final Environment environment;

    private final short[] memory;

    private final Stack<StackMark> callStack;

    @Getter
    private final short callStackDepth;

    @Getter
    boolean saving;

    @Getter
    @Setter
    private ArrayList<Mark> scriptMarks;

    public Memory(Environment environment, short size, short callStackSize) {
        this.environment = environment;
        while (size < 0) {size += 256;}
        size = (short) (size % 256);
        if (size == 0) {
            size = 256;
        }
        memory = new short[size];
        callStack = new Stack<>();
        callStackDepth = callStackSize;
        saving = false;
    }

    public void clear() {
        waitIfSaving();
        post(new ClearDataEvent(this, Stage.PRE));
        Arrays.fill(memory, (short) 0);
        pointer = 0;
        post(new ClearDataEvent(this, Stage.POST));
    }

    public void clearStack() {
        waitIfSaving();
        callStack.clear();
    }

    public Mark getMark() {
        if (scriptMarks == null) {
            throw new IllegalStateException("Script dose not have mark.");
        }
        if (scriptMarks.isEmpty()) {
            throw new NullPointerException("Invalid script mark id.");
        }
        Mark mark = scriptMarks.get(getData());
        if (mark == null) {
            throw new NullPointerException("Invalid script mark id.");
        }
        return mark;
    }

    // 获取数值
    public short getData() {
        return memory[pointer];
    }

    public short getData(short pointer) {
        pointer %= (short) memory.length;
        return memory[pointer];
    }

    public void setData(short pointer, short data) {
        waitIfSaving();
        pointer %= (short) memory.length;
        data %= (short) 256;
        post(new SetDataEvent(this, pointer, memory[pointer], data,
                Stage.PRE, SetDataEvent.Type.SET));
        memory[pointer] = data;
        post(new SetDataEvent(this, pointer, data, data,
                Stage.POST, SetDataEvent.Type.SET));
    }

    // 右移游标
    public void next(short len) {
        if (len == 0) return;
        waitIfSaving();
        short temp = (short) ((pointer + len) % memory.length);
        post(new SetPointerEvent(this, pointer, temp,
                len > 0 ? SetPointerEvent.Type.INC : SetPointerEvent.Type.DEC, Stage.PRE));
        if (temp < 0) temp += (short) memory.length;
        pointer = temp;
        post(new SetPointerEvent(this, pointer, temp,
                len > 0 ? SetPointerEvent.Type.INC : SetPointerEvent.Type.DEC, Stage.POST));
    }

    // 左移游标
    public void prev(short len) {
        if (len == 0) return;
        waitIfSaving();
        next((short) (- len));
    }

    public void increase(short len) {
        waitIfSaving();
        short temp = memory[pointer];
        post(new SetDataEvent(this, pointer, temp, (short) Math.abs(len),
                Stage.PRE, len >= 0 ? SetDataEvent.Type.INC : SetDataEvent.Type.DEC));
        temp = (short) ((temp + len) % 256);
        if (temp < 0) temp += 256;
        memory[pointer] = temp;
        post(new SetDataEvent(this, pointer, temp, (short) Math.abs(len),
                Stage.POST, len >= 0 ? SetDataEvent.Type.INC : SetDataEvent.Type.DEC));
    }

    public void decrease(short len) {
        waitIfSaving();
        increase((short) (- len));
    }

    public void leftShift(short len) {
        if (len == 0) return;
        if (len < 0) {
            rightShift((short) (- len));
            return;
        }
        waitIfSaving();
        post(new SetDataEvent(this, pointer, memory[pointer], len,
                Stage.PRE, SetDataEvent.Type.L_SHIFT));
        setData((short) (getData() << len));
        post(new SetDataEvent(this, pointer, memory[pointer], len,
                Stage.POST, SetDataEvent.Type.L_SHIFT));
    }

    public void rightShift(short len) {
        if (len == 0) return;
        if (len < 0) {
            leftShift((short) (- len));
            return;
        }
        waitIfSaving();
        post(new SetDataEvent(this, pointer, memory[pointer], len,
                Stage.PRE, SetDataEvent.Type.R_SHIFT));
        setData((short) (getData() >> len));
        post(new SetDataEvent(this, pointer, memory[pointer], len,
                Stage.POST, SetDataEvent.Type.R_SHIFT));
    }

    public void setData(short value) {
        setData(pointer, value);
    }

    public void setPointer(short pointer) {
        waitIfSaving();
        pointer = (short) (pointer % memory.length);
        while (pointer < 0) pointer += (short) memory.length;
        post(new SetPointerEvent(this, this.pointer,
                pointer, SetPointerEvent.Type.SET, Stage.PRE));
        this.pointer = pointer;
        post(new SetPointerEvent(this, this.pointer,
                pointer, SetPointerEvent.Type.SET, Stage.POST));
    }

    public void pushStack(StackMark sm) {
        waitIfSaving();
        post(new StackOperationEvent(this, sm,
                StackOperationEvent.Type.PUSH, Stage.PRE, null));
        if (callStackDepth >= 0 && callStack.size() >= callStackDepth) {
            StackOverflowException error = new StackOverflowException(
                    "Stack Overflow! Max stack depth is " + callStackDepth);
            post(new StackOperationEvent(this, sm,
                    StackOperationEvent.Type.PUSH, Stage.ERROR, error));
        }
        callStack.push(sm);
        post(new StackOperationEvent(this, sm,
                StackOperationEvent.Type.PUSH, Stage.POST, null));
    }

    public StackMark popStack() {
        waitIfSaving();
        post(new StackOperationEvent(this, null,
                StackOperationEvent.Type.POP, Stage.PRE, null));
        if (callStack.isEmpty()) {
            IllegalStateException exception = new IllegalStateException("Stack is empty.");
            post(new StackOperationEvent(this, null,
                    StackOperationEvent.Type.POP, Stage.ERROR, exception));
            throw exception;
        }
        StackMark mark = callStack.pop();
        post(new StackOperationEvent(this, mark,
                StackOperationEvent.Type.POP, Stage.POST, null));
        return mark;
    }

    public void resetPointer() {
        waitIfSaving();
        short temp = pointer;
        post(new SetPointerEvent(this, temp, (short) 0,
                SetPointerEvent.Type.RESET, Stage.PRE));
        pointer = 0;
        post(new SetPointerEvent(this, temp, (short) 0,
                SetPointerEvent.Type.RESET, Stage.POST));
    }

    public short getMemSize() {
        return (short) memory.length;
    }

    public void freeze(ByteBuf stream) {
        saving = true;
        post(new MemoryIOEvent(this, Stage.PRE, MemoryIOEvent.Type.SAVE, stream));
        stream.writeShort(pointer);
        stream.writeShort((short) memory.length);
        stream.writeInt(callStack.size());
        stream.writeShort(callStackDepth);
        for (short value : memory) {
            stream.writeShort(value);
        }
        Stack<StackMark> backup = new Stack<>();
        while (!callStack.isEmpty()) {
            backup.push(callStack.pop());
        }
        while (!backup.isEmpty()) {
            StackMark sm = backup.pop();
            stream.writeInt(sm.getIndex());
            callStack.push(sm);
        }
        post(new MemoryIOEvent(this, Stage.POST, MemoryIOEvent.Type.SAVE, stream));
        saving = false;
    }

    public static Memory readMemory(Environment environment, @Nullable Script script, ByteBuf in) {
        short pointer = in.readShort();
        short size = in.readShort();
        int callStackSize = in.readInt();
        short callStackDepth = in.readShort();
        Memory memo = new Memory(environment, size, callStackDepth);
        memo.post(new MemoryIOEvent(memo, Stage.PRE, MemoryIOEvent.Type.LOAD, in));
        for (short i = 0; i < size; i++) {
            memo.setData(i, in.readShort());
        }
        memo.setPointer(pointer);
        if (callStackSize < 1 || script == null) {
            return memo;
        }
        for (int i = 0; i < callStackSize; i++) {
            int index = in.readInt();
            Operator op = script.getOperator(index);
            if (!(op instanceof StackMark sm)) {
                throw new IllegalStateException("Incompatible script '" + script.getScriptName() + "'");
            }
            memo.pushStack(sm);
        }
        memo.post(new MemoryIOEvent(memo, Stage.POST, MemoryIOEvent.Type.LOAD, in));
        return memo;
    }

    public void waitIfSaving() {
        while (saving) {
            Environment.sleep(1);
        }
    }

    public void post(Object event) {
        environment.getEventBus().post(event);
    }
}
