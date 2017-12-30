package se.bjurr.violations.maven.plugin;

import static org.apache.maven.plugins.annotations.LifecyclePhase.VERIFY;
import static se.bjurr.violations.lib.ViolationsApi.violationsApi;
import static se.bjurr.violations.lib.ViolationsReporterApi.violationsReporterApi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import se.bjurr.violations.git.ViolationsGit;
import se.bjurr.violations.lib.ViolationsReporterDetailLevel;
import se.bjurr.violations.lib.model.SEVERITY;
import se.bjurr.violations.lib.model.Violation;
import se.bjurr.violations.lib.util.Filtering;

@Mojo(name = "violations", defaultPhase = VERIFY)
public class ViolationCommentsMojo extends AbstractMojo {

  @Parameter(property = "printViolations", required = false, defaultValue = "true")
  private boolean printViolations;

  @Parameter(property = "violations", required = true)
  private List<ViolationConfig> violations;

  @Parameter(property = "minSeverity", required = false, defaultValue = "INFO")
  private SEVERITY minSeverity;

  @Parameter(property = "detailLevel", required = false, defaultValue = "VERBOSE")
  private ViolationsReporterDetailLevel detailLevel;

  @Parameter(property = "maxViolations", required = false, defaultValue = "999999")
  private Integer maxViolations;

  @Parameter(property = "diffPrintViolations", required = false, defaultValue = "true")
  private boolean diffPrintViolations;

  @Parameter(property = "diffFrom", required = false)
  private String diffFrom;

  @Parameter(property = "diffTo", required = false)
  private String diffTo;

  @Parameter(property = "diffMinSeverity", required = false, defaultValue = "INFO")
  private SEVERITY diffMinSeverity;

  @Parameter(property = "diffMaxViolations", required = false, defaultValue = "999999")
  private Integer diffMaxViolations;

  @Parameter(property = "diffDetailLevel", required = false, defaultValue = "VERBOSE")
  private ViolationsReporterDetailLevel diffDetailLevel;

  @Parameter(property = "gitRepo", required = false, defaultValue = ".")
  private File gitRepo;

  @Override
  public void execute() throws MojoExecutionException {
    try {
      doExecute();
    } catch (final Exception e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  private void doExecute() throws Exception, ScriptException {
    final List<Violation> allParsedViolations = new ArrayList<>();
    final List<Violation> allParsedViolationsInDiff = new ArrayList<>();
    for (final ViolationConfig configuredViolation : violations) {
      final List<Violation> parsedViolations =
          violationsApi() //
              .findAll(configuredViolation.getParser()) //
              .inFolder(configuredViolation.getFolder()) //
              .withPattern(configuredViolation.getPattern()) //
              .withReporter(configuredViolation.getReporter()) //
              .violations();

      allParsedViolations.addAll(getFiltered(parsedViolations, minSeverity));

      allParsedViolationsInDiff.addAll(getAllViolationsInDiff(parsedViolations));
    }

    checkGlobalViolations(allParsedViolations);
    checkDiffViolations(allParsedViolationsInDiff);
  }

  private void checkGlobalViolations(final List<Violation> violations) throws ScriptException {
    final boolean tooManyViolations = violations.size() > maxViolations;
    if (!tooManyViolations && !printViolations) {
      return;
    }

    final String report =
        violationsReporterApi() //
            .withViolations(violations) //
            .getReport(detailLevel);

    if (tooManyViolations) {
      throw new ScriptException(
          "Too many violations found, max is "
              + maxViolations
              + " but found "
              + violations.size()
              + "\n"
              + report);
    } else {
      if (printViolations) {
        getLog().info("\nViolations in repo\n\n" + report);
      }
    }
  }

  private void checkDiffViolations(final List<Violation> violations) throws ScriptException {
    final boolean tooManyViolations = violations.size() > diffMaxViolations;
    if (!tooManyViolations && !diffPrintViolations) {
      return;
    }

    final String report =
        violationsReporterApi() //
            .withViolations(violations) //
            .getReport(diffDetailLevel);

    if (tooManyViolations) {
      throw new ScriptException(
          "Too many violations found in diff, max is "
              + diffMaxViolations
              + " but found "
              + violations.size()
              + "\n"
              + report);
    } else {
      if (diffPrintViolations) {
        getLog().info("\nViolations in diff\n\n" + report);
      }
    }
  }

  private List<Violation> getAllViolationsInDiff(final List<Violation> unfilteredViolations)
      throws Exception {
    if (!isDefined(diffFrom) || !isDefined(diffTo)) {
      getLog().info("No references specified, will not report violations in diff");
      return new ArrayList<>();
    } else {
      final List<Violation> candidates = getFiltered(unfilteredViolations, diffMinSeverity);
      return new ViolationsGit(candidates) //
          .getViolationsInChangeset(gitRepo, diffFrom, diffTo);
    }
  }

  private List<Violation> getFiltered(final List<Violation> unfiltered, final SEVERITY filter) {
    if (filter != null) {
      return Filtering.withAtLEastSeverity(unfiltered, filter);
    }
    return unfiltered;
  }

  private boolean isDefined(final String str) {
    return str != null && !str.isEmpty();
  }
}
