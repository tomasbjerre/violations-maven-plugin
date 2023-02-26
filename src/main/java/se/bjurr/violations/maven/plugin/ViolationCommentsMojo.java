package se.bjurr.violations.maven.plugin;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.apache.maven.plugins.annotations.LifecyclePhase.VALIDATE;
import static se.bjurr.violations.git.ViolationsReporterApi.violationsReporterApi;
import static se.bjurr.violations.lib.ViolationsApi.violationsApi;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import javax.script.ScriptException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import se.bjurr.violations.git.ViolationsGit;
import se.bjurr.violations.git.ViolationsReporterDetailLevel;
import se.bjurr.violations.lib.ViolationsLogger;
import se.bjurr.violations.lib.model.SEVERITY;
import se.bjurr.violations.lib.model.Violation;
import se.bjurr.violations.lib.model.codeclimate.CodeClimateTransformer;
import se.bjurr.violations.lib.util.Filtering;
import se.bjurr.violations.violationslib.com.google.gson.GsonBuilder;

@Mojo(name = "violations", defaultPhase = VALIDATE)
public class ViolationCommentsMojo extends AbstractMojo {

  @Parameter(property = "printViolations", required = false, defaultValue = "true")
  private boolean printViolations;

  public void setPrintViolations(final boolean printViolations) {
    this.printViolations = printViolations;
  }

  @Parameter(property = "violations", required = true)
  private List<ViolationConfig> violations;

  @Parameter(property = "minSeverity", required = false, defaultValue = "INFO")
  private SEVERITY minSeverity;

  @Parameter(property = "detailLevel", required = false, defaultValue = "VERBOSE")
  private ViolationsReporterDetailLevel detailLevel;

  public void setDetailLevel(final ViolationsReporterDetailLevel detailLevel) {
    this.detailLevel = detailLevel;
  }

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

  @Parameter(property = "maxMessageColumnWidth", required = false, defaultValue = "30")
  private int maxMessageColumnWidth;

  @Parameter(property = "maxReporterColumnWidth", required = false, defaultValue = "0")
  private int maxReporterColumnWidth;

  @Parameter(property = "maxRuleColumnWidth", required = false, defaultValue = "0")
  private int maxRuleColumnWidth;

  @Parameter(property = "maxSeverityColumnWidth", required = false, defaultValue = "0")
  private int maxSeverityColumnWidth;

  @Parameter(property = "codeClimateFile", required = false)
  private File codeClimateFile;

  @Parameter(property = "violationsFile", required = false)
  private File violationsFile;

  private ViolationsLogger violationsLogger;

  public void setMaxViolations(final Integer maxViolations) {
    this.maxViolations = maxViolations;
  }

  public void setViolations(final List<ViolationConfig> violations) {
    this.violations = violations;
  }

  @Override
  public void execute() throws MojoExecutionException {
    try {
      this.doExecute();
    } catch (final Exception e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  private void doExecute() throws Exception, ScriptException {
    this.violationsLogger =
        new ViolationsLogger() {
          @Override
          public void log(final Level level, final String string) {
            if (level == Level.FINE) {
              ViolationCommentsMojo.this.getLog().debug(string);
            } else if (level == Level.SEVERE) {
              ViolationCommentsMojo.this.getLog().error(string);
            } else if (level == Level.WARNING) {
              ViolationCommentsMojo.this.getLog().warn(string);
            } else {
              ViolationCommentsMojo.this.getLog().info(string);
            }
          }

          @Override
          public void log(final Level level, final String string, final Throwable t) {
            if (level == Level.FINE) {
              ViolationCommentsMojo.this.getLog().debug(string, t);
            } else if (level == Level.SEVERE) {
              ViolationCommentsMojo.this.getLog().error(string, t);
            } else if (level == Level.WARNING) {
              ViolationCommentsMojo.this.getLog().warn(string, t);
            } else {
              ViolationCommentsMojo.this.getLog().info(string);
            }
          }
        };

    final Set<Violation> allParsedViolations = new TreeSet<>();
    final Set<Violation> allParsedViolationsInDiff = new TreeSet<>();
    for (final ViolationConfig configuredViolation : this.violations) {
      final Set<Violation> parsedViolations =
          violationsApi() //
              .withViolationsLogger(this.violationsLogger) //
              .findAll(configuredViolation.getParser()) //
              .inFolder(configuredViolation.getFolder()) //
              .withPattern(configuredViolation.getPattern()) //
              .withReporter(configuredViolation.getReporter()) //
              .violations();

      allParsedViolations.addAll(this.getFiltered(parsedViolations, this.minSeverity));

      if (this.shouldCheckDiff()) {
        allParsedViolationsInDiff.addAll(this.getAllViolationsInDiff(parsedViolations));
      } else {
        this.getLog().debug("No references specified, will not report violations in diff");
      }
    }

    if (this.codeClimateFile != null) {
      this.createJsonFile(
          CodeClimateTransformer.fromViolations(allParsedViolations), this.codeClimateFile);
    }
    if (this.violationsFile != null) {
      this.createJsonFile(allParsedViolations, this.violationsFile);
    }
    this.checkGlobalViolations(allParsedViolations);
    if (this.shouldCheckDiff()) {
      this.checkDiffViolations(allParsedViolationsInDiff);
    }
  }

  private void createJsonFile(final Object object, final File file) throws IOException {
    final String codeClimateReport = new GsonBuilder().setPrettyPrinting().create().toJson(object);
    final Path path = file.toPath();
    path.toFile().getParentFile().mkdirs();
    Files.write(
        path, codeClimateReport.getBytes(StandardCharsets.UTF_8), TRUNCATE_EXISTING, CREATE, WRITE);
  }

  private void checkGlobalViolations(final Set<Violation> violations) throws ScriptException {
    final boolean tooManyViolations = violations.size() > this.maxViolations;
    if (!tooManyViolations && !this.printViolations) {
      return;
    }

    final String report =
        violationsReporterApi() //
            .withViolations(violations) //
            .withMaxLineColumnWidth(this.maxLineColumnWidth) //
            .withMaxMessageColumnWidth(this.maxMessageColumnWidth) //
            .withMaxReporterColumnWidth(this.maxReporterColumnWidth) //
            .withMaxRuleColumnWidth(this.maxRuleColumnWidth) //
            .withMaxSeverityColumnWidth(this.maxSeverityColumnWidth) //
            .getReport(this.detailLevel);

    if (tooManyViolations) {
      this.getLog().error(report);
      throw new ScriptException(
          "Too many violations found, max is "
              + this.maxViolations
              + " but found "
              + violations.size()
              + ". You can adjust this with the 'maxViolations' configuration parameter.");
    } else {
      if (this.printViolations) {
        this.getLog().info("\nViolations in repo\n\n" + report);
      }
    }
  }

  private void checkDiffViolations(final Set<Violation> violations) throws ScriptException {
    final boolean tooManyViolations = violations.size() > this.diffMaxViolations;
    if (!tooManyViolations && !this.diffPrintViolations) {
      return;
    }

    final String report =
        violationsReporterApi() //
            .withViolations(violations) //
            .withMaxLineColumnWidth(this.maxLineColumnWidth) //
            .withMaxMessageColumnWidth(this.maxMessageColumnWidth) //
            .withMaxReporterColumnWidth(this.maxReporterColumnWidth) //
            .withMaxRuleColumnWidth(this.maxRuleColumnWidth) //
            .withMaxSeverityColumnWidth(this.maxSeverityColumnWidth) //
            .getReport(this.diffDetailLevel);

    if (tooManyViolations) {
      this.getLog().error(report);
      throw new ScriptException(
          "Too many violations found in diff, max is "
              + this.diffMaxViolations
              + " but found "
              + violations.size()
              + ". You can adjust this with the 'maxViolations' configuration parameter.");
    } else {
      if (this.diffPrintViolations) {
        this.getLog().info("\nViolations in diff\n\n" + report);
      }
    }
  }

  private Set<Violation> getAllViolationsInDiff(final Set<Violation> unfilteredViolations)
      throws Exception {
    final Set<Violation> candidates = this.getFiltered(unfilteredViolations, this.diffMinSeverity);
    return new ViolationsGit(this.violationsLogger, candidates) //
        .getViolationsInChangeset(this.gitRepo, this.diffFrom, this.diffTo);
  }

  private boolean shouldCheckDiff() {
    return this.isDefined(this.diffFrom) && this.isDefined(this.diffTo);
  }

  private Set<Violation> getFiltered(final Set<Violation> unfiltered, final SEVERITY filter) {
    if (filter != null) {
      return Filtering.withAtLEastSeverity(unfiltered, filter);
    }
    return unfiltered;
  }

  private boolean isDefined(final String str) {
    return str != null && !str.isEmpty();
  }
}
