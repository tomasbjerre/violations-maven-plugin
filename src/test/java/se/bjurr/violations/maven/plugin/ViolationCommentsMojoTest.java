package se.bjurr.violations.maven.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.bjurr.violations.git.ViolationsReporterDetailLevel;
import se.bjurr.violations.lib.reports.Parser;

public class ViolationCommentsMojoTest {
  @Rule public ExpectedException expectedEx = ExpectedException.none();

  public String getTestResources() throws Exception {
    URI uri = ViolationCommentsMojoTest.class.getResource("/spotbugsXml.xml").toURI();
    return new File(uri).getAbsolutePath();
  }

  @Test
  public void testThatItIsCountingCorrectly() throws Exception {
    ViolationCommentsMojo sut = new ViolationCommentsMojo();
    sut.setMaxViolations(99999);
    ViolationConfig violationConfig = new ViolationConfig();
    violationConfig.setFolder(getTestResources());
    violationConfig.setParser(Parser.FINDBUGS);
    violationConfig.setPattern(".*/spotbugsXml\\.xml");
    violationConfig.setReporter("Spotbugs");
    sut.setViolations(Arrays.asList(violationConfig));

    sut.setPrintViolations(true);
    RecordingLog recordingLog = new RecordingLog();
    sut.setLog(recordingLog);

    sut.setDetailLevel(ViolationsReporterDetailLevel.VERBOSE);

    sut.execute();

    assertThat(recordingLog.getInfo())
        .contains(
            "Summary\n"
                + "|          |      |      |       |       |\n"
                + "| Reporter | INFO | WARN | ERROR | Total |\n"
                + "|          |      |      |       |       |\n"
                + "+----------+------+------+-------+-------+\n"
                + "|          |      |      |       |       |\n"
                + "| Spotbugs | 7    | 1    | 0     | 8     |\n"
                + "|          |      |      |       |       |\n"
                + "+----------+------+------+-------+-------+\n"
                + "|          |      |      |       |       |\n"
                + "|          | 7    | 1    | 0     | 8     |\n"
                + "|          |      |      |       |       |\n"
                + "+----------+------+------+-------+-------+\n"
                + "");
  }

  @Test
  public void testThatItFails() throws Exception {
    expectedEx.expect(MojoExecutionException.class);
    expectedEx.expectMessage("Too many violations found, max is 0 but found 8");

    ViolationCommentsMojo sut = new ViolationCommentsMojo();
    sut.setMaxViolations(0);
    ViolationConfig violationConfig = new ViolationConfig();
    violationConfig.setFolder(getTestResources());
    violationConfig.setParser(Parser.FINDBUGS);
    violationConfig.setPattern(".*/spotbugsXml\\.xml");
    violationConfig.setReporter("Spotbugs");
    sut.setViolations(Arrays.asList(violationConfig));

    RecordingLog recordingLog = new RecordingLog();
    sut.setLog(recordingLog);

    sut.execute();
  }
}
