package se.bjurr.violations.maven.plugin;

import java.io.IOException;
import org.junit.Test;
import se.bjurr.violations.lib.util.Utils;

public class UpdateReadmeTest {

  @Test
  public void doUpdateReadmeWithReporters() throws IOException {
    Utils.updateReadmeWithReporters();
  }
}
