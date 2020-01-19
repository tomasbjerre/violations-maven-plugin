package se.bjurr.violations.maven.plugin;

public class RecordingLog implements org.apache.maven.plugin.logging.Log {

  private CharSequence info;

  @Override
  public boolean isDebugEnabled() {

    return false;
  }

  @Override
  public void debug(CharSequence content) {}

  @Override
  public void debug(CharSequence content, Throwable error) {}

  @Override
  public void debug(Throwable error) {}

  @Override
  public boolean isInfoEnabled() {

    return false;
  }

  @Override
  public void info(CharSequence content) {
    this.info = content;
  }

  public CharSequence getInfo() {
    return info;
  }

  @Override
  public void info(CharSequence content, Throwable error) {}

  @Override
  public void info(Throwable error) {}

  @Override
  public boolean isWarnEnabled() {

    return false;
  }

  @Override
  public void warn(CharSequence content) {}

  @Override
  public void warn(CharSequence content, Throwable error) {}

  @Override
  public void warn(Throwable error) {}

  @Override
  public boolean isErrorEnabled() {

    return false;
  }

  @Override
  public void error(CharSequence content) {}

  @Override
  public void error(CharSequence content, Throwable error) {}

  @Override
  public void error(Throwable error) {}
}
