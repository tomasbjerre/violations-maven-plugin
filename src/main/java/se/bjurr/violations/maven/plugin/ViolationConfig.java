package se.bjurr.violations.maven.plugin;

import org.apache.maven.plugins.annotations.Parameter;

import se.bjurr.violations.lib.reports.Parser;

public class ViolationConfig {
  @Parameter(property = "reporter", required = false)
  private String reporter;

  @Parameter(property = "parser", required = true)
  private Parser parser;

  @Parameter(property = "folder", required = true)
  private String folder;

  @Parameter(property = "pattern", required = false)
  private String pattern;

  public void setFolder(String folder) {
    this.folder = folder;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  public void setReporter(String reporter) {
    this.reporter = reporter;
  }

  public void setParser(Parser parser) {
    this.parser = parser;
  }

  public Parser getParser() {
    return parser;
  }

  public String getFolder() {
    return folder;
  }

  public String getPattern() {
    return pattern;
  }

  public String getReporter() {
    return reporter;
  }
}
