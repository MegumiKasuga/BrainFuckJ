package edu.carole;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.carole.env.ExceptionTracer;
import edu.carole.compile.Script;
import edu.carole.config.Config;
import edu.carole.config.ConfigBuilder;
import edu.carole.env.Environment;
import edu.carole.event.Stage;
import edu.carole.event.compile.CompileCharEvent;
import edu.carole.event.compile.CompileParams;
import edu.carole.exceptions.EnvNotFound;
import edu.carole.util.Couple;
import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import org.apache.log4j.*;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;


public class BrainFuckJ {

    public static void main(String[] args) {
        String path = null;
        Config config;
        boolean pathFlag = (args.length == 1 && args[0].startsWith("path="));
        if (args.length < 1 || pathFlag) {
            if (pathFlag) {
                path = args[0].split("=")[1];
            }
            File configFile = new File("config.toml");
            try {
                if (configFile.exists()) {
                    FileReader fr = new FileReader(configFile);
                    config = ConfigBuilder.fromToml(fr);
                    fr.close();
                } else {
                    FileWriter fw = new FileWriter(configFile);
                    config = ConfigBuilder.createDefaultTomlString(fw);
                    fw.flush();
                    fw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            Couple<Config, String> configCouple = ConfigBuilder.fromArgs(args);
            config = configCouple.getFirst();
            path = configCouple.getSecond();
        }
        Logger logger;
        try {
            logger = createLogger();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        ExceptionTracer tracer = new ExceptionTracer();
        // BrainFuckJ bfj = new BrainFuckJ(config, tracer, logger, () -> new AsyncEventBus(Executors.newCachedThreadPool()));
        BrainFuckJ bfj = new BrainFuckJ(config, tracer, logger, EventBus::new);
        UUID id = bfj.create();
        // bfj.addEventListener(id, bfj);
        config.log(logger);
        PrintStream outputStream = config.outStream();
        InputStream inputStream = config.inStream();

        outputStream.println("Welcome!");
        outputStream.println("BFJ initialized with config: ");
        outputStream.println(config);;
        try {
            if (path != null) {
                outputStream.println("<file '" + path + "' found, executing.>");
                bfj.execute(id, path);
            } else {
                outputStream.println("<BFJ command line started, type 'exit' to exit.>");
                Scanner scanner = new Scanner(inputStream);
                String command;
                while (true) {
                    outputStream.print("=>   ");
                    command = scanner.nextLine();
                    if (command.equals("exit")) {
                        break;
                    }
                    bfj.execute(id, "<command>", "", command);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            outputStream.println("<BFJ terminated.>");
            logger.info("BFJ terminated.");
        }
    }

    protected static Logger createLogger() throws IOException {
        File file = new File("logs");
        if (!file.isDirectory() && !file.mkdir()) {
            throw new IOException("Could not create logs directory");
        }
        Logger logger = Logger.getLogger("bfj-main");
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
        File appenderFile = new File("logs/log_" + format.format(date) + ".log");
        if (!appenderFile.createNewFile()) {
            throw new IOException("Could not create log file");
        }
        File latestFile = new File("logs/latest.log");
        if (latestFile.exists()) {
            if (!latestFile.delete()) {
                throw new IOException("Could not delete latest log file");
            }
        }
        if (!latestFile.createNewFile()) {
            throw new IOException("Could not create log file");
        }
        logger.addAppender(getAppender(appenderFile.getPath()));
        logger.addAppender(getAppender(latestFile.getPath()));
        return logger;
    }

    private static Appender getAppender(String filePath) throws IOException {
        FileAppender appender = new FileAppender(
                new TTCCLayout("HH:mm:ss"), filePath);
        appender.setAppend(true);
        appender.setEncoding("UTF-8");
        appender.activateOptions();
        return appender;
    }

    private final HashMap<UUID, Environment> environments;

    private final Logger logger;

    private final Config defaultConfig;

    private final ExceptionTracer tracer;
    private final Supplier<EventBus> busSupplier;

    public BrainFuckJ(@NonNull Config defaultConfig,
                      @NonNull ExceptionTracer defaultTracer,
                      @NonNull Logger logger,
                      @NonNull Supplier<EventBus> eventBusSupplier) {
        this.logger = logger;
        this.defaultConfig = defaultConfig;
        this.environments = new HashMap<>();
        this.tracer = defaultTracer;
        this.busSupplier = eventBusSupplier;
    }

    public UUID put(@NonNull Environment environment) {
        UUID uuid = createUUID();
        environments.put(uuid, environment);
        return uuid;
    }

    public UUID create(@Nullable Config config,
                       @Nullable ExceptionTracer tracer,
                       @Nullable Logger logger,
                       @Nullable EventBus bus) {
        UUID uuid = createUUID();
        config = config == null ? this.defaultConfig : config;
        tracer = tracer == null ? this.tracer : tracer;
        logger = logger == null ? this.logger : logger;
        bus = bus == null ? this.busSupplier.get() : bus;
        environments.put(uuid, Environment.createDefaultEnv(config, tracer, logger, bus));
        return uuid;
    }

    public UUID create(@NonNull ByteBuf buffer,
                       @Nullable Config config,
                       @Nullable ExceptionTracer tracer,
                       @Nullable BiFunction<String, String, Reader> scriptProvider,
                       @Nullable Logger logger,
                       @Nullable EventBus bus) throws Exception {
        UUID uuid = createUUID();
        config = config == null ? this.defaultConfig : config;
        tracer = tracer == null ? this.tracer : tracer;
        logger = logger == null ? this.logger : logger;
        bus = bus == null ? busSupplier.get() : bus;
        environments.put(uuid, Environment.createDefaultEnv(config, buffer, scriptProvider, tracer, logger, bus));
        return uuid;
    }

    public UUID create() {
        return create(null, null, null, null);
    }

    public UUID createFromFile(@NonNull String path,
                               @Nullable Config config,
                               @Nullable ExceptionTracer tracer,
                               @Nullable BiFunction<String, String, Reader> scriptProvider,
                               @Nullable Logger logger,
                               @Nullable EventBus bus) throws Exception {
        UUID uuid = createUUID();
        config = config == null ? this.defaultConfig : config;
        tracer = tracer == null ? this.tracer : tracer;
        logger = logger == null ? this.logger : logger;
        bus = bus == null ? busSupplier.get() : bus;
        environments.put(uuid, Environment.readFromFile(config, path, tracer, logger, bus, scriptProvider));
        return uuid;
    }

    public UUID createFromFile(@NonNull String filePath) throws Exception {
        return createFromFile(filePath, null, null, null, null, null);
    }

    public Environment delete(UUID uuid) {
        Environment environment = environments.remove(uuid);
        if (environment != null) environment.clear();
        return environment;
    }

    public void save(UUID id, ByteBuf buf) {
        if (!contains(id)) {
            EnvNotFound exception = new EnvNotFound(id,
                    "Failed to save environment '" + id + "'");
            exception.log(logger);
            throw exception;
        }
        Environment environment = get(id);
        Objects.requireNonNull(environment);
        environment.freeze(buf);
    }

    public void saveToFile(UUID id, String filePath) throws IOException {
        if (!contains(id)) {
            EnvNotFound exception = new EnvNotFound(id,
                    "Failed to save environment '" + id + "'");
            exception.log(logger);
            throw exception;
        }
        Environment environment = get(id);
        Objects.requireNonNull(environment);
        environment.saveToFile(filePath);
    }

    public Script compile(UUID id, String scriptName,
                          String scriptPath, String script) throws Exception {
        if (!contains(id)) {
            EnvNotFound exception = new EnvNotFound(id,
                    "Failed to compile script '" + scriptName + "'");
            exception.log(logger);
            throw exception;
        }
        Environment environment = get(id);
        Objects.requireNonNull(environment);
        return environment.compile(scriptName, scriptPath, script);
    }

    public Script compile(UUID id, File file) throws Exception {
        if (!contains(id)) {
            EnvNotFound exception = new EnvNotFound(id,
                    "Failed to compile script '" + file.getName() + "'");
            exception.log(logger);
            throw exception;
        }
        Environment environment = get(id);
        Objects.requireNonNull(environment);
        return environment.compile(file);
    }

    public Script compile(UUID id, String filePath) throws Exception {
        if (!contains(id)) {
            EnvNotFound exception = new EnvNotFound(id,
                    "Failed to compile script '" + filePath + "'");
            exception.log(logger);
            throw exception;
        }
        Environment environment = get(id);
        Objects.requireNonNull(environment);
        return environment.compile(filePath);
    }

    public void execute(UUID id, String scriptName, String scriptPath, String script) throws Exception {
        if (!contains(id)) {
            EnvNotFound exception = new EnvNotFound(id,
                    "Failed to execute script '" + scriptName + "'");
            exception.log(logger);
            throw exception;
        }
        Environment environment = get(id);
        Objects.requireNonNull(environment);
        environment.execute(scriptName, scriptPath, script);
    }


    public void execute(UUID id, File file) throws Exception {
        if (!contains(id)) {
            EnvNotFound exception = new EnvNotFound(id,
                    "Failed to execute script '" + file.getName() + "'");
            exception.log(logger);
            throw exception;
        }
        Environment environment = get(id);
        Objects.requireNonNull(environment);
        environment.execute(file);
    }


    public void execute(UUID id, String filePath) throws Exception {
        if (!contains(id)) {
            EnvNotFound exception = new EnvNotFound(id,
                    "Failed to execute script '" + filePath + "'");
            exception.log(logger);
            throw exception;
        }
        Environment environment = get(id);
        Objects.requireNonNull(environment);
        environment.execute(filePath);
    }

    public void execute(UUID id, Script script) throws Exception {
        if (!contains(id)) {
            EnvNotFound exception = new EnvNotFound(id,
                    "Failed to execute script '" + script.getScriptName() + "'");
            exception.log(logger);
            throw exception;
        }
        Environment environment = get(id);
        Objects.requireNonNull(environment);
        environment.execute(script);
    }

    public void addEventListener(UUID id, Object listener) {
        if (!contains(id)) {
            EnvNotFound exception = new EnvNotFound(id,
                    "Failed to add event listener for env '" + id + "'");
            exception.log(logger);
            throw exception;
        }
        Environment environment = get(id);
        Objects.requireNonNull(environment);
        environment.subscribeListener(listener);
    }

    public boolean contains(UUID uuid) {
        return environments.containsKey(uuid);
    }

    public Environment get(UUID uuid) {
        return environments.get(uuid);
    }

    private UUID createUUID() {
        UUID result = UUID.randomUUID();
        while (environments.containsKey(result)) {
            result = UUID.randomUUID();
        }
        return result;
    }
}
