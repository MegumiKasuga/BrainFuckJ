package edu.carole.env;

import edu.carole.compile.Compiler;
import edu.carole.config.Config;
import edu.carole.compile.Script;
import edu.carole.util.IOMode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.BiFunction;

public class Environment {

    @Getter
    private final Operators operators;

    @Getter
    private final Memory memory;

    @Getter
    private final Compiler compiler;

    @Getter
    private final Logger logger;

    private Script runningScript;

    public Environment(short memorySize, short maxStackSize,
                       Operators operators, Logger logger) {
        this.operators = operators;
        memory = new Memory(memorySize, maxStackSize);
        compiler = new Compiler(this);
        this.logger = logger;
    }

    public Environment(ByteBuf buffer, @Nullable BiFunction<String, String, Reader> scriptProvider, Operators operators, Logger logger) {
        this.operators = operators;
        this.compiler = new Compiler(this);
        this.logger = logger;
        Script script = null;
        boolean isRunning = buffer.readBoolean();
        if (isRunning) {
            String rawName = readString(buffer);
            String rawPath = readString(buffer);
            int index = buffer.readInt();
            if (scriptProvider != null) {
                try {
                    script = compile(rawName, rawPath, scriptProvider.apply(rawName, rawPath));
                    script.setIndex(index);
                } catch (IOException e) {
                    logger.error("Failed to load script '" + rawName + "' at path '" + rawPath + "'", e);
                    script = new Script(rawName, rawPath, 8);
                }
            }
        }
        memory = Memory.readMemory(script, buffer);
    }

    public Script compile(String name, String path, Reader reader) throws IOException {
        while (!reader.ready()) {
            sleep(1);
        }
        int cache;
        StringBuilder sb = new StringBuilder();
        while (true) {
            cache = reader.read();
            if (cache == -1) {break;}
            sb.append((char) cache);
        }
        reader.close();
        return compile(name, path, sb.toString());
    }

    public Script compile(String filePath) throws IOException {
        return compile(new File(filePath));
    }

    public void execute(String filePath) throws IOException {
        execute(compile(filePath));
    }

    public Script compile(File file) throws IOException {
        FileReader fr = new FileReader(file);
        StringBuilder sb = new StringBuilder();
        while (!fr.ready()) {
            sleep(1);
        }
        while (true) {
            int value = fr.read();
            if (value == -1) {break;}
            sb.append((char)value);
        }
        String s = sb.toString();
        fr.close();
        return compile(file.getName(), file.getPath(), s);
    }

    public void execute(File file) throws IOException {
        execute(compile(file));
    }

    public Script compile(String source, String path, String input) {
        return compiler.compile(source, path, input);
    }

    public void execute(String source, String path, String input) {
        execute(compile(source, path, input));
    }

    public void execute(Script script) {
        memory.clear();
        runningScript = script;
        memory.setScriptMarks(script.getScriptMarks());
        script.execute(memory, logger);
        memory.clearStack();
        runningScript = null;
        memory.setScriptMarks(null);
    }

    public boolean isRunning() {
        return runningScript != null;
    }

    public void freeze(ByteBuf buffer) {
        logger.info("saving environment.");
        buffer.writeBoolean(isRunning());
        if (isRunning()) {
            writeString(buffer, runningScript.getScriptName());
            writeString(buffer, runningScript.getScriptPath());
            buffer.writeInt(runningScript.getIndex());
        }
        memory.freeze(buffer);
        logger.info("environment saved.");
    }

    public void clear() {
        memory.clear();
        runningScript = null;
    }

    public static Environment createDefaultEnv(short memorySize, short maxStackSize,
                                               InputStream inputStream, PrintStream printStream,
                                               IOMode inputMode, IOMode outputMode, Logger logger) {
        return new Environment(memorySize, maxStackSize,
                Operators.getDefaultOperators(inputStream, printStream, inputMode, outputMode),
                logger);
    }

    public static Environment createDefaultEnv(Config config, Logger logger) {
        return createDefaultEnv(config.memSize(), config.maxStackDepth(),
                config.inStream(), config.outStream(),
                config.inputMode(), config.outputMode(), logger);
    }

    public static Environment createDefaultEnv(Config config, ByteBuf buffer,
                                               BiFunction<String, String, Reader> scriptProvider,
                                               Logger logger) {
        return new Environment(buffer, scriptProvider,
                Operators.getDefaultOperators(config.inStream(), config.outStream(),
                        config.inputMode(), config.outputMode()),
                logger);
    }

    public static void writeString(ByteBuf buffer, String s) {
        byte[] data = s.getBytes(StandardCharsets.UTF_8);
        buffer.writeInt(data.length);
        buffer.writeBytes(data);
    }

    public static String readString(ByteBuf buf) {
        int len = buf.readInt();
        return buf.readString(len, StandardCharsets.UTF_8);
    }

    public static void sleep(int milliSec) {
        try {
            Thread.sleep(milliSec);
        } catch (InterruptedException ignored) {}
    }

    public void saveToFile(String path) throws IOException {
        ByteBuf buf = Unpooled.buffer();
        this.freeze(buf);
        ByteBuffer buffer = buf.nioBuffer();
        FileChannel channel = FileChannel.open(Path.of(path), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        channel.write(buffer);
        channel.close();
    }

    public static Environment readFromFile(Config config, String path, Logger logger,
                                           @Nullable BiFunction<String, String, Reader> scriptProvider) throws IOException {
        ByteBuf buf = Unpooled.buffer();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        FileChannel readChannel = FileChannel.open(Path.of("src/test/resources/test_freeze.bin"), StandardOpenOption.READ);
        int readResult;
        do {
            readResult = readChannel.read(buffer);
            buffer.rewind();
            buf.writeBytes(buffer);
            buffer.clear();
        } while (readResult > 0);
        readChannel.close();
        return Environment.createDefaultEnv(config, buf, (a, b) -> {
            if (scriptProvider != null) {
                return scriptProvider.apply(a, b);
            }
            try {
                return new FileReader(b);
            } catch (IOException e) {
                logger.error("Failed to open file '" + b + "'", e);
                return new StringReader("");
            }
        }, logger);
    }
}
