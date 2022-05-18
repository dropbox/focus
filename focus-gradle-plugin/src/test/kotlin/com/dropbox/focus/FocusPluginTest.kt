package com.dropbox.focus

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Test

class FocusPluginTest {
  private lateinit var gradleRunner: GradleRunner

  @Before
  fun setup() {
    gradleRunner = GradleRunner.create()
      .withPluginClasspath()
  }

  @Test
  fun configurationCache() {
    val fixtureRoot = File("src/test/projects/configuration-cache-compatible")

    gradleRunner
      .withArguments("clearFocus", "--configuration-cache", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
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
  fun singleQuotePath() {
    val fixtureRoot = File("src/test/projects/single-quote-path")

    gradleRunner
      .withArguments(":module:focus")
      .runFixture(fixtureRoot) { build() }

    val focusFileContent = File("src/test/projects/single-quote-path/build/notnowhere/build/focus.settings.gradle").readText()
    assertThat(focusFileContent)
      .containsMatch("""project\(\":module\"\).projectDir = new File\(\'.*/src/test/projects/single-quote-path/build/notnowhere\'\)""")
  }

  private fun GradleRunner.runFixture(
    projectRoot: File,
    moduleRoot: File = projectRoot,
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
