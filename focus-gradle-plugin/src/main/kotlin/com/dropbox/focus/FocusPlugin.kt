package com.dropbox.focus

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

const val FOCUS_TASK_GROUP = "focus mode"
const val CREATE_FOCUS_SETTINGS_TASK_NAME = "createFocusSettings"
const val FOCUS_TASK_NAME = "focus"
const val CLEAR_FOCUS_TASK_NAME = "clearFocus"
val TASK_NAMES = setOf(
  CREATE_FOCUS_SETTINGS_TASK_NAME,
  FOCUS_TASK_NAME,
  CLEAR_FOCUS_TASK_NAME,
)

class FocusPlugin : Plugin<Settings> {
  override fun apply(target: Settings) = target.run {
    val extension = extensions.create<FocusExtension>("focus")

    gradle.settingsEvaluated {
      val requestingFocusTask = startParameter.taskNames
        .map { it.substringAfterLast(":") }
        .any { it in TASK_NAMES }

      val focusFile = rootDir.resolve(extension.focusFileName.get())
      if (!requestingFocusTask && focusFile.exists()) {
        apply(from = rootDir.resolve(focusFile.readText()))
      } else {
        apply(from = extension.allSettingsFileName)
      }

      gradle.rootProject {
        tasks.register(CLEAR_FOCUS_TASK_NAME, ClearFocusTask(
          focusFileName = extension.focusFileName
        ))

        subprojects {
          val createFocusSettingsTask = tasks
            .register(CREATE_FOCUS_SETTINGS_TASK_NAME, CreateFocusSettingsTask.invoke())

          tasks.register(FOCUS_TASK_NAME, FocusTask(
            moduleSettingsFile = createFocusSettingsTask.flatMap { it.settingsFile },
            rootFocusFileName = extension.focusFileName,
          ))
        }
      }
    }
  }
}
