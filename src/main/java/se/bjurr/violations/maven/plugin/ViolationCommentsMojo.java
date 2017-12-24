package se.bjurr.violations.maven.plugin;

import static org.apache.maven.plugins.annotations.LifecyclePhase.NONE;
import static se.bjurr.violations.lib.ViolationsApi.violationsApi;
import static se.bjurr.violations.lib.ViolationsReporterApi.violationsReporterApi;
import static se.bjurr.violations.lib.ViolationsReporterDetailLevel.VERBOSE;
import static se.bjurr.violations.lib.model.SEVERITY.INFO;

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

@Mojo(name = "violations", defaultPhase = NONE)
public class ViolationCommentsMojo extends AbstractMojo {

  @Parameter(property = "violations", required = false)
  private final List<ViolationConfig> violations = new ArrayList<ViolationConfig>();

  @Parameter(property = "minSeverity", required = false)
  private final SEVERITY minSeverity = INFO;

  @Parameter(property = "detailLevel", required = false)
  private final ViolationsReporterDetailLevel detailLevel = VERBOSE;

  @Parameter(property = "maxViolations", required = false)
  private final Integer maxViolations = Integer.MAX_VALUE;

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
