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

It supports:
 * [_AndroidLint_](http://developer.android.com/tools/help/lint.html)
 * [_Checkstyle_](http://checkstyle.sourceforge.net/)
   * [_Detekt_](https://github.com/arturbosch/detekt) with `--output-format xml`.
   * [_ESLint_](https://github.com/sindresorhus/grunt-eslint) with `format: 'checkstyle'`.
   * [_KTLint_](https://github.com/shyiko/ktlint)
   * [_SwiftLint_](https://github.com/realm/SwiftLint) with `--reporter checkstyle`.
   * [_PHPCS_](https://github.com/squizlabs/PHP_CodeSniffer) with `phpcs api.php --report=checkstyle`.
 * [_CLang_](https://clang-analyzer.llvm.org/)
   * [_RubyCop_](http://rubocop.readthedocs.io/en/latest/formatters/) with `rubycop -f clang file.rb`
 * [_CodeNarc_](http://codenarc.sourceforge.net/)
 * [_CPD_](http://pmd.sourceforge.net/pmd-4.3.0/cpd.html)
 * [_CPPLint_](https://github.com/theandrewdavis/cpplint)
 * [_CPPCheck_](http://cppcheck.sourceforge.net/)
 * [_CSSLint_](https://github.com/CSSLint/csslint)
 * [_DocFX_](http://dotnet.github.io/docfx/)
 * [_Findbugs_](http://findbugs.sourceforge.net/)
 * [_Flake8_](http://flake8.readthedocs.org/en/latest/)
   * [_AnsibleLint_](https://github.com/willthames/ansible-lint) with `-p`
   * [_Mccabe_](https://pypi.python.org/pypi/mccabe)
   * [_Pep8_](https://github.com/PyCQA/pycodestyle)
   * [_PyFlakes_](https://pypi.python.org/pypi/pyflakes)
 * [_FxCop_](https://en.wikipedia.org/wiki/FxCop)
 * [_Gendarme_](http://www.mono-project.com/docs/tools+libraries/tools/gendarme/)
 * [_GoLint_](https://github.com/golang/lint)
   * [_GoVet_](https://golang.org/cmd/vet/) Same format as GoLint.
 * [_GoogleErrorProne_](https://github.com/google/error-prone)
 * [_JSHint_](http://jshint.com/)
 * _Lint_ A common XML format, used by different linters.
 * [_JCReport_](https://github.com/jCoderZ/fawkez/wiki/JcReport)
 * [_Klocwork_](http://www.klocwork.com/products-services/klocwork/static-code-analysis)
 * [_MyPy_](https://pypi.python.org/pypi/mypy-lang)
 * [_PCLint_](http://www.gimpel.com/html/pcl.htm) PC-Lint using the same output format as the Jenkins warnings plugin, [_details here_](https://wiki.jenkins.io/display/JENKINS/PcLint+options)
 * [_PerlCritic_](https://github.com/Perl-Critic)
 * [_PiTest_](http://pitest.org/)
 * [_PyDocStyle_](https://pypi.python.org/pypi/pydocstyle)
 * [_PyLint_](https://www.pylint.org/)
 * [_PMD_](https://pmd.github.io/)
   * [_Infer_](http://fbinfer.com/) Facebook Infer. With `--pmd-xml`.
   * [_PHPPMD_](https://phpmd.org/) with `phpmd api.php xml ruleset.xml`.
 * [_ReSharper_](https://www.jetbrains.com/resharper/)
 * [_SbtScalac_](http://www.scala-sbt.org/)
 * [_Simian_](http://www.harukizaemon.com/simian/)
 * [_StyleCop_](https://stylecop.codeplex.com/)
 * [_XMLLint_](http://xmlsoft.org/xmllint.html)
 * [_ZPTLint_](https://pypi.python.org/pypi/zptlint)


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
       <parser>FINDBUGS</parser>
       <reporter>Findbugs</reporter>
       <folder>.</folder>
       <pattern>.*/findbugsXml.*\.xml$</pattern>
      </violation>
     </violations>
    </configuration>
    <executions>
     <execution>
      <phase>verify</phase>
      <goals>
       <goal>violations</goal>
      </goals>
     </execution>
    </executions>
   </plugin>
  </plugins>
```
