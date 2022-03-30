package com.dropbox.focus

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

public abstract class FocusTask : DefaultTask() {

  @get:InputDirectory
  public abstract val rootProjectDir: DirectoryProperty

  @get:InputFile
  public abstract val moduleSettingsFile: RegularFileProperty

  @get:Input
  public abstract val rootFocusFileName: Property<String>

  @get:OutputFile
  public abstract val focusFile: RegularFileProperty

  @TaskAction
  public fun focus() {
    focusFile.get().asFile.writer().use { writer ->
      writer.write(moduleSettingsFile.get().asFile.toRelativeString(rootProjectDir.get().asFile))
    }
  }

  public companion object {
    public operator fun invoke(
      moduleSettingsFile: Provider<RegularFile>,
      rootFocusFileName: Provider<String>,
    ): FocusTask.() -> Unit = {
      group = FOCUS_TASK_GROUP

      this.rootProjectDir.set(project.rootProject.layout.projectDirectory)
      this.moduleSettingsFile.set(moduleSettingsFile)
      this.rootFocusFileName.set(rootFocusFileName)
      focusFile.set(project.rootProject.layout.projectDirectory.file(rootFocusFileName))
    }
  }
}
