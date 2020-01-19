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
    return new File(uri).getParentFile().getAbsolutePath();
  }

  @Test
  public void testThatItIsCountingCorrectly() throws Exception {
    ViolationCommentsMojo sut = new ViolationCommentsMojo();
    sut.setMaxViolations(99999);

    setupViolationPatterns(sut);

    sut.setPrintViolations(true);
    RecordingLog recordingLog = new RecordingLog();
    sut.setLog(recordingLog);

    sut.setDetailLevel(ViolationsReporterDetailLevel.VERBOSE);

    sut.execute();

    assertThat(recordingLog.getInfo())
        .contains(
            "Summary\n"
                + "|            |      |      |       |       |\n"
                + "| Reporter   | INFO | WARN | ERROR | Total |\n"
                + "|            |      |      |       |       |\n"
                + "+------------+------+------+-------+-------+\n"
                + "|            |      |      |       |       |\n"
                + "| Checkstyle | 0    | 3    | 0     | 3     |\n"
                + "|            |      |      |       |       |\n"
                + "+------------+------+------+-------+-------+\n"
                + "|            |      |      |       |       |\n"
                + "| PMD        | 0    | 1    | 0     | 1     |\n"
                + "|            |      |      |       |       |\n"
                + "+------------+------+------+-------+-------+\n"
                + "|            |      |      |       |       |\n"
                + "| Spotbugs   | 1    | 0    | 0     | 1     |\n"
                + "|            |      |      |       |       |\n"
                + "+------------+------+------+-------+-------+\n"
                + "|            |      |      |       |       |\n"
                + "|            | 1    | 4    | 0     | 5     |\n"
                + "|            |      |      |       |       |\n"
                + "+------------+------+------+-------+-------+");
  }

  @Test
  public void testThatItFails() throws Exception {
    expectedEx.expect(MojoExecutionException.class);
    expectedEx.expectMessage("Too many violations found, max is 0 but found 5");

    ViolationCommentsMojo sut = new ViolationCommentsMojo();
    sut.setMaxViolations(0);
    setupViolationPatterns(sut);

    RecordingLog recordingLog = new RecordingLog();
    sut.setLog(recordingLog);

    sut.execute();
  }

  private void setupViolationPatterns(ViolationCommentsMojo sut) throws Exception {
    ViolationConfig vc1 = new ViolationConfig();
    vc1.setFolder(getTestResources());
    vc1.setParser(Parser.CHECKSTYLE);
    vc1.setPattern(".*/checkstyle-result\\.xml");
    vc1.setReporter("Checkstyle");

    ViolationConfig vc2 = new ViolationConfig();
    vc2.setFolder(getTestResources());
    vc2.setParser(Parser.PMD);
    vc2.setPattern(".*/pmd\\.xml");
    vc2.setReporter("PMD");

    ViolationConfig vc3 = new ViolationConfig();
    vc3.setFolder(getTestResources());
    vc3.setParser(Parser.FINDBUGS);
    vc3.setPattern(".*/spotbugsXml\\.xml");
    vc3.setReporter("Spotbugs");

    sut.setViolations(Arrays.asList(vc1, vc2, vc3));
  }
}
