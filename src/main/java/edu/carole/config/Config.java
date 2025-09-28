package edu.carole.config;

import edu.carole.util.IOMode;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.PrintStream;

public record Config(short memSize, short maxStackDepth,
                     InputStream inStream, IOMode inputMode,
                     PrintStream outStream, IOMode outputMode) {

    public void log(Logger logger) {
        logger.info("environment config:");
        logger.info("\tmemory size: " + memSize);
        logger.info("\tstack depth: " + maxStackDepth);
        logger.info("\tinput mode: " + inputMode);
        logger.info("\toutput mode: " + outputMode);
    }

    public String toString() {
        return "  memory size: " + memSize +
                ",\n  stack depth: " + maxStackDepth +
                ",\n  input mode: " + inputMode +
                ",\n  output mode: "+ outputMode;
    }
}
