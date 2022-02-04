package com.dropbox.focus

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

@CacheableTask
public abstract class ClearFocusTask : DefaultTask() {

  @get:Input
  public abstract val focusFileName: Property<String>

  @TaskAction
  public fun clearFocus() {
    val focusFile = project.rootDir.resolve(focusFileName.get())
    focusFile.delete()
  }

  public companion object {
    public operator fun invoke(
      focusFileName: Provider<String>
    ): ClearFocusTask.() -> Unit = {
      group = FOCUS_TASK_GROUP
      this.focusFileName.set(focusFileName)
    }
  }
}
