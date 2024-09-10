package com.dropbox.focus

import com.google.common.truth.Truth.assertThat
import java.io.File
import java.util.regex.Pattern
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FocusPluginTest {
  @get:Rule val buildDir = TemporaryFolder()
  private lateinit var gradleRunner: GradleRunner

  @Before
  fun setup() {
    gradleRunner = GradleRunner.create()
      .withTestKitDir(buildDir.root)
      .withPluginClasspath()
  }

  @Test
  fun configurationCache_focus() {
    val fixtureRoot = File("src/test/projects/configuration-cache-compatible")

    gradleRunner
      .withArguments("--configuration-cache", "focus")
      .withProjectDir(fixtureRoot)
      .build()

    val result = gradleRunner
      .withArguments("--configuration-cache", "focus")
      .withProjectDir(fixtureRoot)
      .build()

    assertThat(result.output).contains("Reusing configuration cache.")
  }

  @Test
  fun configurationCache_clear() {
    val fixtureRoot = File("src/test/projects/configuration-cache-compatible")

    gradleRunner
      .withArguments("--configuration-cache", "clearFocus")
      .withProjectDir(fixtureRoot)
      .build()

    val result = gradleRunner
      .withArguments("--configuration-cache", "clearFocus")
      .withProjectDir(fixtureRoot)
      .build()

    assertThat(result.output).contains("Reusing configuration cache.")
  }

  @Test
  fun missingSettingsAll() {
    val fixtureRoot = File("src/test/projects/missing-settings-all")

    val firstRun = gradleRunner
      .withArguments("clearFocus")
      .runFixture(fixtureRoot) { buildAndFail() }

    assertThat(firstRun.output).contains("Could not read script")
    assertThat(firstRun.output).contains("as it does not exist.")
  }

  @Test
  fun allPathLiteralsAreSingleQuote() {
    val fixtureRoot = File("src/test/projects/happy-path")
    gradleRunner
      .withArguments(":module:focus")
      .runFixture(fixtureRoot) { build() }

    val focusFileContent =
      File("src/test/projects/happy-path/build/notnowhere/build/focus.settings.gradle").readText()
    // has at least one single-quote path literal
    assertThat(focusFileContent).matches(Pattern.compile(""".*new File\('.*'\).*""", Pattern.DOTALL))
    // no double-quote path literals
    assertThat(focusFileContent).doesNotMatch(Pattern.compile(""".*new File\(".*"\).*""", Pattern.DOTALL))
  }

  @Test
  fun happyPath_CsvCreated() {
    val fixtureRoot = File("src/test/projects/happy-path")

    gradleRunner
      .withArguments(":module:focus")
      .runFixture(fixtureRoot) { build() }

    val csvFileContents = File("src/test/projects/happy-path/build/notnowhere/build/moduleToDirMap.csv").readText()
    val absoluteFilePath = fixtureRoot.resolve("build/notnowhere").absolutePath.replace("\\", "\\\\")
    // language=csv
    assertThat(csvFileContents).contains(""":module,$absoluteFilePath""")
  }

  @Test
  fun happyPath_CsvRead() {
    val fixtureRoot = File("src/test/projects/happy-path")

    gradleRunner
      .withArguments(":module:focus")
      .runFixture(fixtureRoot) { build() }

    val csvFilePath = File("src/test/projects/happy-path/build/notnowhere/build/moduleToDirMap.csv").absolutePath
    val focusFileContent = File("src/test/projects/happy-path/build/notnowhere/build/focus.settings.gradle").readText()
    // language=groovy
    assertThat(focusFileContent).contains("""File f = new File('$csvFilePath')""")
  }

  private fun GradleRunner.runFixture(
    projectRoot: File,
    action: GradleRunner.() -> BuildResult,
  ): BuildResult {
    val settings = File(projectRoot, "settings.gradle")
    if (!settings.exists()) {
      settings.createNewFile()
      settings.deleteOnExit()
    }

    return withProjectDir(projectRoot).action()
  }
}
