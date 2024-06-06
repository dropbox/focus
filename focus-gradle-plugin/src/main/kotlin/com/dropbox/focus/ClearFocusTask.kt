package com.dropbox.focus

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
public abstract class ClearFocusTask : DefaultTask() {

  @get:PathSensitive(PathSensitivity.RELATIVE)
  @get:InputFile
  public abstract val focusFile: RegularFileProperty

  @TaskAction
  public fun clearFocus() {
    if (focusFile.isPresent) {
      focusFile.asFile.get().delete()
    }
  }

  public companion object {
    public operator fun invoke(
      focusFileName: Provider<String>
    ): ClearFocusTask.() -> Unit = {
      group = FOCUS_TASK_GROUP
      this.focusFile.set(project.rootProject.layout.file(focusFileName.map { File(it) }))
    }
  }
}
