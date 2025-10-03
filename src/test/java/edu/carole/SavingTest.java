package edu.carole;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.carole.env.ExceptionTracer;
import edu.carole.config.Config;
import edu.carole.config.ConfigBuilder;
import edu.carole.event.compile.CompileCharEvent;
import edu.carole.util.IOMode;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class SavingTest extends TestCase {

    public SavingTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(SavingTest.class);
    }

    public void testApp() {
        try {
            Logger logger = BrainFuckJ.createLogger();
            Config config = (new ConfigBuilder()
                    .inputMode(IOMode.NUMBER)
                    .outputMode(IOMode.NUMBER)
            ).build();
            ExceptionTracer tracer = new ExceptionTracer();
            BrainFuckJ bfj = new BrainFuckJ(config, tracer, logger, () -> new AsyncEventBus(Executors.newCachedThreadPool()));
            UUID first = bfj.create();
            bfj.execute(first, new File("src/test/resources/test.bf"));
            bfj.saveToFile(first, "src/test/resources/test_freeze.bin");
            bfj.createFromFile("src/test/resources/test_freeze.bin");
            System.out.println("done");
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }
}
