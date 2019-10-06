package se.bjurr.violations.maven.plugin;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.apache.maven.plugins.annotations.LifecyclePhase.VERIFY;
import static se.bjurr.violations.git.ViolationsReporterApi.violationsReporterApi;
import static se.bjurr.violations.lib.ViolationsApi.violationsApi;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.script.ScriptException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import se.bjurr.violations.git.ViolationsGit;
import se.bjurr.violations.git.ViolationsReporterDetailLevel;
import se.bjurr.violations.lib.model.SEVERITY;
import se.bjurr.violations.lib.model.Violation;
import se.bjurr.violations.lib.model.codeclimate.CodeClimateTransformer;
import se.bjurr.violations.lib.util.Filtering;
import se.bjurr.violations.violationslib.com.google.gson.GsonBuilder;

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

  @Parameter(property = "diffPrintViolations", required = false, defaultValue = "false")
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

  @Parameter(property = "maxLineColumnWidth", required = false, defaultValue = "0")
  private int maxLineColumnWidth;

  @Parameter(property = "maxMessageColumnWidth", required = false, defaultValue = "50")
  private int maxMessageColumnWidth;

  @Parameter(property = "maxReporterColumnWidth", required = false, defaultValue = "0")
  private int maxReporterColumnWidth;

  @Parameter(property = "maxRuleColumnWidth", required = false, defaultValue = "10")
  private int maxRuleColumnWidth;

  @Parameter(property = "maxSeverityColumnWidth", required = false, defaultValue = "0")
  private int maxSeverityColumnWidth;

  @Parameter(property = "codeClimateFile", required = false)
  private File codeClimateFile;

  @Parameter(property = "violationsFile", required = false)
  private File violationsFile;

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

      if (shouldCheckDiff()) {
        allParsedViolationsInDiff.addAll(getAllViolationsInDiff(parsedViolations));
      } else {
        getLog().debug("No references specified, will not report violations in diff");
      }
    }

    if (this.codeClimateFile != null) {
      createJsonFile(
          CodeClimateTransformer.fromViolations(allParsedViolations), this.codeClimateFile);
    }
    if (this.violationsFile != null) {
      createJsonFile(allParsedViolations, this.violationsFile);
    }
    checkGlobalViolations(allParsedViolations);
    if (shouldCheckDiff()) {
      checkDiffViolations(allParsedViolationsInDiff);
    }
  }

  private void createJsonFile(final Object object, final File file) throws IOException {
    final String codeClimateReport = new GsonBuilder().setPrettyPrinting().create().toJson(object);
    Files.write(
        file.toPath(),
        codeClimateReport.getBytes(StandardCharsets.UTF_8),
        TRUNCATE_EXISTING,
        CREATE,
        WRITE);
  }

  private void checkGlobalViolations(final List<Violation> violations) throws ScriptException {
    final boolean tooManyViolations = violations.size() > maxViolations;
    if (!tooManyViolations && !printViolations) {
      return;
    }

    final String report =
        violationsReporterApi() //
            .withViolations(violations) //
            .withMaxLineColumnWidth(maxLineColumnWidth) //
            .withMaxMessageColumnWidth(maxMessageColumnWidth) //
            .withMaxReporterColumnWidth(maxReporterColumnWidth) //
            .withMaxRuleColumnWidth(maxRuleColumnWidth) //
            .withMaxSeverityColumnWidth(maxSeverityColumnWidth) //
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
            .withMaxLineColumnWidth(maxLineColumnWidth) //
            .withMaxMessageColumnWidth(maxMessageColumnWidth) //
            .withMaxReporterColumnWidth(maxReporterColumnWidth) //
            .withMaxRuleColumnWidth(maxRuleColumnWidth) //
            .withMaxSeverityColumnWidth(maxSeverityColumnWidth) //
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
    final List<Violation> candidates = getFiltered(unfilteredViolations, diffMinSeverity);
    return new ViolationsGit(candidates) //
        .getViolationsInChangeset(gitRepo, diffFrom, diffTo);
  }

  private boolean shouldCheckDiff() {
    return isDefined(diffFrom) && isDefined(diffTo);
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
