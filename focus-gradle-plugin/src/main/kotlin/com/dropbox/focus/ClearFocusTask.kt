package com.dropbox.focus

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

@CacheableTask
public abstract class ClearFocusTask : DefaultTask() {

  @get:Input
  public abstract val focusFilePath: Property<String>

  @TaskAction
  public fun clearFocus() {
    val focusFile = File(focusFilePath.get())
    if (focusFile.exists()) {
      focusFile.delete()
    }
  }

  public companion object {
    public operator fun invoke(
      focusFileName: Provider<String>
    ): ClearFocusTask.() -> Unit = {
      group = FOCUS_TASK_GROUP
      this.focusFilePath.set(
        project.rootProject.layout.file(focusFileName.map { File(it) }).map { it.asFile.absolutePath }
      )
    }
  }
}
