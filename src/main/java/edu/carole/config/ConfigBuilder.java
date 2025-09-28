package edu.carole.config;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import edu.carole.util.IOMode;
import edu.carole.util.Couple;

import java.io.*;
import java.util.HashMap;

public class ConfigBuilder {

    private short memSize = 256;
    private short maxStackDepth = 32;
    private InputStream inStream = System.in;
    private IOMode inputMode = IOMode.CHAR;
    private PrintStream outStream = System.out;
    private IOMode outputMode = IOMode.CHAR;

    public ConfigBuilder memorySize(short memSize) {
        this.memSize = (short) (memSize % 256);
        return this;
    }

    public ConfigBuilder maxStackDepth(short maxStackDepth) {
        this.maxStackDepth = maxStackDepth;
        return this;
    }

    public ConfigBuilder inStream(InputStream inStream) {
        this.inStream = inStream;
        return this;
    }

    public ConfigBuilder inputMode(IOMode inputMode) {
        this.inputMode = inputMode;
        return this;
    }

    public ConfigBuilder outStream(PrintStream outStream) {
        this.outStream = outStream;
        return this;
    }

    public ConfigBuilder outputMode(IOMode outputMode) {
        this.outputMode = outputMode;
        return this;
    }

    public Config build() {
        return new Config(
                memSize, maxStackDepth,
                inStream, inputMode,
                outStream, outputMode
        );
    }

    public static Couple<Config, String> fromArgs(String[] args) {
        ConfigBuilder builder = new ConfigBuilder();
        String path = null;
        for (String arg : args) {
            if (arg.startsWith("mem=")) {
                builder.memorySize(Short.parseShort(arg.split("=")[1]));
            } else if (arg.startsWith("stack=")) {
                builder.maxStackDepth(Short.parseShort(arg.split("=")[1]));
            } else if (arg.startsWith("path=")) {
                path = arg.split("=")[1];
            } else if (arg.startsWith("input_mode=")) {
                builder.inputMode(IOMode.valueOf(arg.split("=")[1].toUpperCase()));
            } else if (arg.startsWith("output_mode=")) {
                builder.outputMode(IOMode.valueOf(arg.split("=")[1].toUpperCase()));
            }
        }
        return Couple.of(builder.build(), path);
    }

    public static Config fromToml(Reader reader) {
        Toml toml = new Toml().read(reader);
        ConfigBuilder builder = new ConfigBuilder();
        if (toml.contains("input_mode")) {
            builder.inputMode(IOMode.valueOf(toml.getString("input_mode").toUpperCase()));
        }
        if (toml.contains("output_mode")) {
            builder.outputMode(IOMode.valueOf(toml.getString("output_mode").toUpperCase()));
        }
        if (toml.contains("mem_size")) {
            builder.memorySize(toml.getLong("mem_size").shortValue());
        }
        if (toml.contains("max_stack_depth")) {
            builder.maxStackDepth(toml.getLong("max_stack_depth").shortValue());
        }
        return builder.build();
    }

    public static Config createDefaultTomlString(Writer writer) throws IOException {
        TomlWriter tomlWriter = new TomlWriter();
        ConfigBuilder builder = new ConfigBuilder();
        HashMap<String, Object> map = new HashMap<>();
        map.put("mem_size", builder.memSize);
        map.put("max_stack_depth", builder.maxStackDepth);
        map.put("input_mode", builder.inputMode);
        map.put("output_mode", builder.outputMode);
        tomlWriter.write(map, writer);
        return builder.build();
    }
}
