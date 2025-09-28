package edu.carole.env;

import edu.carole.compile.Script;
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

    private final short[] memory;

    private final Stack<StackMark> callStack;

    private final short callStackDepth;

    @Getter
    boolean saving;

    @Getter
    @Setter
    private ArrayList<Mark> scriptMarks;

    public Memory(short size, short callStackSize) {
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
        Arrays.fill(memory, (short) 0);
        pointer = 0;
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
        memory[pointer] = data;
    }

    // 右移游标
    public void next(short len) {
        waitIfSaving();
        pointer = (short) ((pointer + len) % Math.min(256, memory.length));
        if (pointer < 0) pointer += (short) Math.min(256, memory.length);
    }

    // 左移游标
    public void prev(short len) {
        waitIfSaving();
        next((short) (- len));
    }

    public void increase(short len) {
        waitIfSaving();
        short temp = memory[pointer];
        temp = (short) ((temp + len) % 256);
        if (temp < 0) temp += 256;
        memory[pointer] = temp;
    }

    public void decrease(short len) {
        waitIfSaving();
        increase((short) (- len));
    }

    public void leftShift(short len) {
        waitIfSaving();
        setData((short) (getData() << len));
    }

    public void rightShift(short len) {
        waitIfSaving();
        setData((short) (getData() >> len));
    }

    public void setData(short value) {
        waitIfSaving();
        while (value < 0) {
            value += 256;
        }
        value %= 256;
        memory[pointer] = value;
    }

    public void setPointer(short pointer) {
        waitIfSaving();
        pointer = (short) (pointer % (Math.min(256, memory.length)));
        this.pointer = pointer;
    }

    public void pushStack(StackMark sm) {
        waitIfSaving();
        if (callStackDepth >= 0 && callStack.size() >= callStackDepth) {
            throw new StackOverflowError();
        }
        callStack.push(sm);
    }

    public StackMark popStack() {
        waitIfSaving();
        if (callStack.isEmpty()) {
            throw new IllegalStateException("Stack is empty.");
        }
        return callStack.pop();
    }

    public void resetPointer() {
        waitIfSaving();
        pointer = 0;
    }

    public short getMemSize() {
        return (short) memory.length;
    }

    public void freeze(ByteBuf stream) {
        saving = true;
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
        saving = false;
    }

    public static Memory readMemory(@Nullable Script script, ByteBuf in) {
        short pointer = in.readShort();
        short size = in.readShort();
        int callStackSize = in.readInt();
        short callStackDepth = in.readShort();
        Memory memo = new Memory(size, callStackDepth);
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
        return memo;
    }

    public void waitIfSaving() {
        Environment.sleep(1);
    }
}
