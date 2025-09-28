package edu.carole;

import edu.carole.config.Config;
import edu.carole.config.ConfigBuilder;
import edu.carole.util.IOMode;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

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
            BrainFuckJ bfj = new BrainFuckJ(config, logger);
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
