package se.bjurr.violations.maven.plugin;

import static org.apache.maven.plugins.annotations.LifecyclePhase.VERIFY;
import static se.bjurr.violations.lib.ViolationsApi.violationsApi;
import static se.bjurr.violations.lib.ViolationsReporterApi.violationsReporterApi;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import se.bjurr.violations.lib.ViolationsReporterDetailLevel;
import se.bjurr.violations.lib.model.SEVERITY;
import se.bjurr.violations.lib.model.Violation;
import se.bjurr.violations.lib.util.Filtering;

@Mojo(name = "violations", defaultPhase = VERIFY)
public class ViolationCommentsMojo extends AbstractMojo {

  @Parameter(property = "violations", required = true)
  private List<ViolationConfig> violations;

  @Parameter(property = "minSeverity", required = false, defaultValue = "INFO")
  private SEVERITY minSeverity;

  @Parameter(property = "detailLevel", required = false, defaultValue = "VERBOSE")
  private ViolationsReporterDetailLevel detailLevel;

  @Parameter(property = "maxViolations", required = false, defaultValue = "999999")
  private Integer maxViolations;

  @Override
  public void execute() throws MojoExecutionException {
    List<Violation> allParsedViolations = new ArrayList<Violation>();
    for (final ViolationConfig configuredViolation : violations) {
      final List<Violation> parsedViolations =
          violationsApi() //
              .findAll(configuredViolation.getParser()) //
              .inFolder(configuredViolation.getFolder()) //
              .withPattern(configuredViolation.getPattern()) //
              .withReporter(configuredViolation.getReporter()) //
              .violations();
      allParsedViolations = Filtering.withAtLEastSeverity(allParsedViolations, minSeverity);
      allParsedViolations.addAll(parsedViolations);
    }

    final String report =
        violationsReporterApi() //
            .withViolations(allParsedViolations) //
            .getReport(detailLevel);

    getLog().info("\n" + report);

    if (allParsedViolations.size() > maxViolations) {
      throw new MojoExecutionException(
          "To many violations found, max is "
              + maxViolations
              + " but found "
              + allParsedViolations.size());
    }
  }
}
