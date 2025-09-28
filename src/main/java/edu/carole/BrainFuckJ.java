package edu.carole;

import edu.carole.compile.Script;
import edu.carole.config.Config;
import edu.carole.config.ConfigBuilder;
import edu.carole.env.Environment;
import edu.carole.util.Couple;
import io.netty.buffer.ByteBuf;
import org.apache.log4j.*;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiFunction;


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
        BrainFuckJ bfj = new BrainFuckJ(config, logger);
        UUID id = bfj.create();
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

    public BrainFuckJ(Config defaultConfig, Logger logger) {
        this.logger = logger;
        this.defaultConfig = defaultConfig;
        this.environments = new HashMap<>();
    }

    public UUID put(Environment environment) {
        UUID uuid = createUUID();
        environments.put(uuid, environment);
        return uuid;
    }

    public UUID create(Config config) {
        UUID uuid = createUUID();
        environments.put(uuid, Environment.createDefaultEnv(config, logger));
        return uuid;
    }

    public UUID create(Config config, ByteBuf buffer,
                                BiFunction<String, String, Reader> scriptProvider) {
        UUID uuid = createUUID();
        environments.put(uuid, Environment.createDefaultEnv(config, buffer, scriptProvider, logger));
        return uuid;
    }

    public UUID create(ByteBuf buffer,
                                  BiFunction<String, String, Reader> scriptProvider) {
        return create(defaultConfig, buffer, scriptProvider);
    }

    public UUID create() {
        UUID uuid = createUUID();
        environments.put(uuid, Environment.createDefaultEnv(defaultConfig, logger));
        return uuid;
    }

    public UUID createFromFile(Config config, String filePath,
                               @Nullable BiFunction<String, String, Reader> scriptProvider) throws IOException {
        UUID uuid = createUUID();
        environments.put(uuid, Environment.readFromFile(config, filePath, logger, scriptProvider));
        return uuid;
    }

    public UUID createFromFile(Config config, String filePath) throws IOException {
        return createFromFile(config, filePath, null);
    }

    public UUID createFromFile(String filePath, @Nullable BiFunction<String, String, Reader> scriptProvider) throws IOException {
        UUID uuid = createUUID();
        environments.put(uuid, Environment.readFromFile(defaultConfig, filePath, logger, scriptProvider));
        return uuid;
    }

    public UUID createFromFile(String filePath) throws IOException {
        return createFromFile(filePath, null);
    }

    public Environment delete(UUID uuid) {
        Environment environment = environments.remove(uuid);
        if (environment != null) environment.clear();
        return environment;
    }

    public void save(UUID id, ByteBuf buf) {
        if (!contains(id)) {
            logger.error("Failed to save environment '" + id +
                    "'. cause: environment does not exist.");
            return;
        }
        Environment environment = get(id);
        Objects.requireNonNull(environment);
        environment.freeze(buf);
    }

    public void saveToFile(UUID id, String filePath) throws IOException {
        if (!contains(id)) {
            logger.error("Failed to save environment '" + id +
                    "'. cause: environment does not exist.");
            return;
        }
        Environment environment = get(id);
        Objects.requireNonNull(environment);
        environment.saveToFile(filePath);
    }

    public Script compile(UUID id, String scriptName,
                          String scriptPath, String script) {
        if (!contains(id)) {
            logger.error("Failed to compile script '" + scriptName +
                    "'. cause: environment '" + id + "' does not exist.");
            return null;
        }
        Environment environment = get(id);
        Objects.requireNonNull(environment);
        return environment.compile(scriptName, scriptPath, script);
    }

    public Script compile(UUID id, File file) throws IOException {
        if (!contains(id)) {
            logger.error("Failed to compile script '" + file.getName() +
                    "'. cause: environment '" + id + "' does not exist.");
            return null;
        }
        Environment environment = get(id);
        Objects.requireNonNull(environment);
        return environment.compile(file);
    }

    public Script compile(UUID id, String filePath) throws IOException {
        if (!contains(id)) {
            logger.warn("Failed to compile script '" + filePath +
                    "'. cause: environment '" + id + "' does not exist.");
            return null;
        }
        Environment environment = get(id);
        Objects.requireNonNull(environment);
        return environment.compile(filePath);
    }

    public void execute(UUID id, String scriptName, String scriptPath, String script) {
        if (!contains(id)) {
            logger.warn("Failed to execute script '" + scriptName +
                    "'. cause: environment '" + id + "' does not exist.");
            return;
        }
        Environment environment = get(id);
        Objects.requireNonNull(environment);
        environment.execute(scriptName, scriptPath, script);
    }


    public void execute(UUID id, File file) throws IOException {
        if (!contains(id)) {
            logger.warn("Failed to execute script '" + file.getName() +
                    "'. cause: environment '" + id + "' does not exist.");
            return;
        }
        Environment environment = get(id);
        Objects.requireNonNull(environment);
        environment.execute(file);
    }


    public void execute(UUID id, String filePath) throws IOException {
        if (!contains(id)) {
            logger.warn("Failed to execute script '" + filePath +
                    "'. cause: environment '" + id + "' does not exist.");
            return;
        }
        Environment environment = get(id);
        Objects.requireNonNull(environment);
        environment.execute(filePath);
    }

    public void execute(UUID id, Script script) {
        if (!contains(id)) {
            logger.warn("Failed to execute script '" + script.getScriptName() +
                    "'. cause: environment '" + id + "' does not exist.");
            return;
        }
        Environment environment = get(id);
        Objects.requireNonNull(environment);
        environment.execute(script);
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
