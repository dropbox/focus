package com.dropbox.focus

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

public abstract class FocusTask : DefaultTask() {

  @get:Input
  public abstract val focusFileName: Property<String>

  @get:OutputFile
  public abstract val focusFile: RegularFileProperty

  @TaskAction
  fun focus() {
    focusFile.get().asFile.writer().use { writer ->
      writer.write(project.focusSettingsFileName)
    }
  }

  public companion object {
    public operator fun invoke(
      focusFileName: Provider<String>,
      createFocusSettingsTaskProvider: TaskProvider<CreateFocusSettingsTask>
    ): FocusTask.() -> Unit = {
      group = FOCUS_TASK_GROUP
      this.focusFileName.set(focusFileName)
      focusFile.set(project.rootProject.layout.projectDirectory.file(focusFileName))


      dependsOn(createFocusSettingsTaskProvider)
    }
  }
}
