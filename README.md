# Violations Maven Plugin
[![Build Status](https://travis-ci.org/tomasbjerre/violations-maven-plugin.svg?branch=master)](https://travis-ci.org/tomasbjerre/violations-maven-plugin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.bjurr.violations/violations-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/se.bjurr.violations/violations-maven-plugin)
[![Bintray](https://api.bintray.com/packages/tomasbjerre/tomasbjerre/se.bjurr.violations%3Aviolations-maven-plugin/images/download.svg)](https://bintray.com/tomasbjerre/tomasbjerre/se.bjurr.violations%3Aviolations-maven-plugin/_latestVersion)

This is a Maven plugin for [Violations Lib](https://github.com/tomasbjerre/violations-lib). There is also a [Gradle plugin](https://github.com/tomasbjerre/violations-gradle-plugin) for this.

It can parse results from static code analysis and:

 * Report violations in the build log.
 * Optionally fail the build depending on violations found.

A snippet of the output may look like this:
```
se/bjurr/violations/lib/example/OtherClass.java
╔══════════╤════════════╤══════════╤══════╤════════════════════════════════════════════════════╗
║ Reporter │ Rule       │ Severity │ Line │ Message                                            ║
╠══════════╪════════════╪══════════╪══════╪════════════════════════════════════════════════════╣
║ Findbugs │ MS_SHOULD_ │ INFO     │ 7    │ Field isn't final but should be                    ║
║          │ BE_FINAL   │          │      │                                                    ║
║          │            │          │      │                                                    ║
║          │            │          │      │    <p>                                             ║
║          │            │          │      │ This static field public but not final, and        ║
║          │            │          │      │ could be changed by malicious code or              ║
║          │            │          │      │         by accident from another package.          ║
║          │            │          │      │         The field could be made final to avoid     ║
║          │            │          │      │         this vulnerability.</p>                    ║
╟──────────┼────────────┼──────────┼──────┼────────────────────────────────────────────────────╢
║ Findbugs │ NM_FIELD_N │ INFO     │ 6    │ Field names should start with a lower case letter  ║
║          │ AMING_CONV │          │      │                                                    ║
║          │ ENTION     │          │      │                                                    ║
║          │            │          │      │   <p>                                              ║
║          │            │          │      │ Names of fields that are not final should be in mi ║
║          │            │          │      │ xed case with a lowercase first letter and the fir ║
║          │            │          │      │ st letters of subsequent words capitalized.        ║
║          │            │          │      │ </p>                                               ║
╚══════════╧════════════╧══════════╧══════╧════════════════════════════════════════════════════╝

Summary of se/bjurr/violations/lib/example/OtherClass.java
╔══════════╤══════╤══════╤═══════╤═══════╗
║ Reporter │ INFO │ WARN │ ERROR │ Total ║
╠══════════╪══════╪══════╪═══════╪═══════╣
║ Findbugs │ 2    │ 0    │ 0     │ 2     ║
╟──────────┼──────┼──────┼───────┼───────╢
║          │ 2    │ 0    │ 0     │ 2     ║
╚══════════╧══════╧══════╧═══════╧═══════╝


Summary
╔════════════╤══════╤══════╤═══════╤═══════╗
║ Reporter   │ INFO │ WARN │ ERROR │ Total ║
╠════════════╪══════╪══════╪═══════╪═══════╣
║ Checkstyle │ 4    │ 1    │ 1     │ 6     ║
╟────────────┼──────┼──────┼───────┼───────╢
║ Findbugs   │ 2    │ 2    │ 5     │ 9     ║
╟────────────┼──────┼──────┼───────┼───────╢
║            │ 6    │ 3    │ 6     │ 15    ║
╚════════════╧══════╧══════╧═══════╧═══════╝
```

Example of supported reports are available [here](https://github.com/tomasbjerre/violations-lib/tree/master/src/test/resources).

A number of **parsers** have been implemented. Some **parsers** can parse output from several **reporters**.

| Reporter | Parser | Notes
| --- | --- | ---
| [_ARM-GCC_](https://developer.arm.com/open-source/gnu-toolchain/gnu-rm)               | `CLANG`              | 
| [_AndroidLint_](http://developer.android.com/tools/help/lint.html)                    | `ANDROIDLINT`        | 
| [_AnsibleLint_](https://github.com/willthames/ansible-lint)                           | `FLAKE8`             | With `-p`
| [_CLang_](https://clang-analyzer.llvm.org/)                                           | `CLANG`              | 
| [_CPD_](http://pmd.sourceforge.net/pmd-4.3.0/cpd.html)                                | `CPD`                | 
| [_CPPCheck_](http://cppcheck.sourceforge.net/)                                        | `CPPCHECK`           | 
| [_CPPLint_](https://github.com/theandrewdavis/cpplint)                                | `CPPLINT`            | 
| [_CSSLint_](https://github.com/CSSLint/csslint)                                       | `CSSLINT`            | 
| [_Checkstyle_](http://checkstyle.sourceforge.net/)                                    | `CHECKSTYLE`         | 
| [_CodeNarc_](http://codenarc.sourceforge.net/)                                        | `CODENARC`           | 
| [_Detekt_](https://github.com/arturbosch/detekt)                                      | `CHECKSTYLE`         | With `--output-format xml`.
| [_DocFX_](http://dotnet.github.io/docfx/)                                             | `DOCFX`              | 
| [_Doxygen_](https://www.stack.nl/~dimitri/doxygen/)                                   | `CLANG`              | 
| [_ERB_](https://www.puppetcookbook.com/posts/erb-template-validation.html)            | `CLANG`              | With `erb -P -x -T '-' "${it}" \| ruby -c 2>&1 >/dev/null \| grep '^-' \| sed -E 's/^-([a-zA-Z0-9:]+)/${filename}\1 ERROR:/p' > erbfiles.out`.
| [_ESLint_](https://github.com/sindresorhus/grunt-eslint)                              | `CHECKSTYLE`         | With `format: 'checkstyle'`.
| [_Findbugs_](http://findbugs.sourceforge.net/)                                        | `FINDBUGS`           | 
| [_Flake8_](http://flake8.readthedocs.org/en/latest/)                                  | `FLAKE8`             | 
| [_FxCop_](https://en.wikipedia.org/wiki/FxCop)                                        | `FXCOP`              | 
| [_GCC_](https://gcc.gnu.org/)                                                         | `CLANG`              | 
| [_Gendarme_](http://www.mono-project.com/docs/tools+libraries/tools/gendarme/)        | `GENDARME`           | 
| [_GoLint_](https://github.com/golang/lint)                                            | `GOLINT`             | 
| [_GoVet_](https://golang.org/cmd/vet/)                                                | `GOLINT`             | Same format as GoLint.
| [_GoogleErrorProne_](https://github.com/google/error-prone)                           | `GOOGLEERRORPRONE`   | 
| [_IAR_](https://www.iar.com/iar-embedded-workbench/)                                  | `IAR`                | With `--no_wrap_diagnostics`
| [_Infer_](http://fbinfer.com/)                                                        | `PMD`                | Facebook Infer. With `--pmd-xml`.
| [_JCReport_](https://github.com/jCoderZ/fawkez/wiki/JcReport)                         | `JCREPORT`           | 
| [_JSHint_](http://jshint.com/)                                                        | `JSHINT`             | 
| [_KTLint_](https://github.com/shyiko/ktlint)                                          | `CHECKSTYLE`         | 
| [_Klocwork_](http://www.klocwork.com/products-services/klocwork/static-code-analysis)  | `KLOCWORK`           | 
| [_KotlinGradle_](https://github.com/JetBrains/kotlin)                                 | `KOTLINGRADLE`       | Output from Kotlin Gradle Plugin.
| [_KotlinMaven_](https://github.com/JetBrains/kotlin)                                  | `KOTLINMAVEN`        | Output from Kotlin Maven Plugin.
| [_Lint_]()                                                                            | `LINT`               | A common XML format, used by different linters.
| [_MSCpp_](https://visualstudio.microsoft.com/vs/features/cplusplus/)                  | `MSCPP`              | 
| [_Mccabe_](https://pypi.python.org/pypi/mccabe)                                       | `FLAKE8`             | 
| [_MyPy_](https://pypi.python.org/pypi/mypy-lang)                                      | `MYPY`               | 
| [_NullAway_](https://github.com/uber/NullAway)                                        | `GOOGLEERRORPRONE`   | Same format as Google Error Prone.
| [_PCLint_](http://www.gimpel.com/html/pcl.htm)                                        | `PCLINT`             | PC-Lint using the same output format as the Jenkins warnings plugin, [_details here_](https://wiki.jenkins.io/display/JENKINS/PcLint+options)
| [_PHPCS_](https://github.com/squizlabs/PHP_CodeSniffer)                               | `CHECKSTYLE`         | With `phpcs api.php --report=checkstyle`.
| [_PHPPMD_](https://phpmd.org/)                                                        | `PMD`                | With `phpmd api.php xml ruleset.xml`.
| [_PMD_](https://pmd.github.io/)                                                       | `PMD`                | 
| [_Pep8_](https://github.com/PyCQA/pycodestyle)                                        | `FLAKE8`             | 
| [_PerlCritic_](https://github.com/Perl-Critic)                                        | `PERLCRITIC`         | 
| [_PiTest_](http://pitest.org/)                                                        | `PITEST`             | 
| [_Puppet-Lint_](http://puppet-lint.com/)                                              | `CLANG`              | With `-log-format %{fullpath}:%{line}:%{column}: %{kind}: %{message}`
| [_PyDocStyle_](https://pypi.python.org/pypi/pydocstyle)                               | `PYDOCSTYLE`         | 
| [_PyFlakes_](https://pypi.python.org/pypi/pyflakes)                                   | `FLAKE8`             | 
| [_PyLint_](https://www.pylint.org/)                                                   | `PYLINT`             | With `pylint --output-format=parseable`.
| [_ReSharper_](https://www.jetbrains.com/resharper/)                                   | `RESHARPER`          | 
| [_RubyCop_](http://rubocop.readthedocs.io/en/latest/formatters/)                      | `CLANG`              | With `rubycop -f clang file.rb`
| [_SbtScalac_](http://www.scala-sbt.org/)                                              | `SBTSCALAC`          | 
| [_Scalastyle_](http://www.scalastyle.org/)                                            | `CHECKSTYLE`         | 
| [_Simian_](http://www.harukizaemon.com/simian/)                                       | `SIMIAN`             | 
| [_Spotbugs_](https://spotbugs.github.io/)                                             | `FINDBUGS`           | 
| [_StyleCop_](https://stylecop.codeplex.com/)                                          | `STYLECOP`           | 
| [_SwiftLint_](https://github.com/realm/SwiftLint)                                     | `CHECKSTYLE`         | With `--reporter checkstyle`.
| [_TSLint_](https://palantir.github.io/tslint/usage/cli/)                              | `CHECKSTYLE`         | With `-t checkstyle`
| [_XMLLint_](http://xmlsoft.org/xmllint.html)                                          | `XMLLINT`            | 
| [_YAMLLint_](https://yamllint.readthedocs.io/en/stable/index.html)                    | `YAMLLINT`           | With `-f parsable`
| [_ZPTLint_](https://pypi.python.org/pypi/zptlint)                                     | `ZPTLINT`            |

Missing a format? Open an issue [here](https://github.com/tomasbjerre/violations-lib/issues)!

## Usage ##
There is a running example [here](https://github.com/tomasbjerre/violations-maven-plugin/tree/master/violations-maven-plugin-example).

The plugin needs to run after any static code analysis tools, so put it after them in the pom. Having the following in the pom will make the plugin run with `mvn verify`: 

```
  <plugins>
   <plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>findbugs-maven-plugin</artifactId>
    <version>3.0.5</version>
    <executions>
     <execution>
      <goals>
       <goal>check</goal>
      </goals>
     </execution>
    </executions>
   </plugin>
   <plugin>
    <groupId>se.bjurr.violations</groupId>
    <artifactId>violations-maven-plugin</artifactId>
    <version>X</version>
    <executions>
     <execution>
      <phase>verify</phase>
      <goals>
       <goal>violations</goal>
      </goals>
      <configuration>
       <!-- Optional config -->
       <!-- 0 is disabled -->
       <maxReporterColumnWidth>0</maxReporterColumnWidth>
       <maxRuleColumnWidth>10</maxRuleColumnWidth>
       <maxSeverityColumnWidth>0</maxSeverityColumnWidth>
       <maxLineColumnWidth>0</maxLineColumnWidth>
       <maxMessageColumnWidth>50</maxMessageColumnWidth>
     
       <!-- Global configuration, remove if you dont want to report violations 
            for the entire repo. -->
       <!-- INFO, WARN or ERROR -->
       <minSeverity>INFO</minSeverity>
       <!-- PER_FILE_COMPACT, COMPACT or VERBOSE -->
       <detailLevel>VERBOSE</detailLevel>
       <!-- Will fail the build if total number of found violations is higher -->
       <maxViolations>99999999</maxViolations>
       <!-- Will print violations found in diff -->
       <printViolations>true</printViolations>
     
     
       <!-- Diff configuration, remove if you dont want to report violations 
            for files changed between specific revisions. -->
       <!-- Can be empty (ignored), Git-commit or any Git-reference -->
       <diffFrom></diffFrom>
       <!-- Same as above -->
       <diffTo></diffTo>
       <!-- INFO, WARN or ERROR -->
       <diffMinSeverity>INFO</diffMinSeverity>
       <!-- PER_FILE_COMPACT, COMPACT or VERBOSE -->
       <diffDetailLevel>VERBOSE</diffDetailLevel>
       <!-- Will fail the build if number of violations, in the diff within from/to, is higher -->
       <diffMaxViolations>99</diffMaxViolations>
       <!-- Will print violations found in diff -->
       <diffPrintViolations>true</diffPrintViolations>
       <!-- Where to look for Git -->
       <gitRepo>.</gitRepo>
     
     
       <!-- This is mandatory regardless of if you want to report violations 
            between revisions or the entire repo. -->
       <violations>
        <violation>
         <!-- Many more formats available, see:
              https://github.com/tomasbjerre/violations-lib
         -->
         <parser>FINDBUGS</parser>
         <reporter>Findbugs</reporter>
         <folder>.</folder>
         <pattern>.*/findbugsXml\.xml$</pattern>
        </violation>
       </violations>
      </configuration>
     </execution>
    </executions>
   </plugin>
  </plugins>
```
