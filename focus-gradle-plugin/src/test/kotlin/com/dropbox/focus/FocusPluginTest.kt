package com.dropbox.focus

import com.google.common.truth.Truth.assertThat
import java.io.File
import java.time.temporal.Temporal
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
  fun configurationCache() {
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
  fun singleQuotePath() {
    val fixtureRoot = File("src/test/projects/single-quote-path")

    gradleRunner
      .withArguments(":module:focus")
      .runFixture(fixtureRoot) { build() }

    val focusFileContent = File("src/test/projects/single-quote-path/build/notnowhere/build/focus.settings.gradle").readText()
    val absoluteFilePath = fixtureRoot.resolve("build/notnowhere").absolutePath.replace("\\", "\\\\")
    // language=groovy
    assertThat(focusFileContent).contains("""project(":module").projectDir = new File('$absoluteFilePath')""")
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
