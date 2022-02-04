package com.dropbox.focus

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

const val FOCUS_TASK_GROUP = "focus mode"

class FocusPlugin : Plugin<Settings> {
  override fun apply(target: Settings) = target.run {
    val extension = extensions.create<FocusExtension>("focus")

    val focusFile = rootDir.resolve(DEFAULT_FOCUS_FILENAME)
    if (focusFile.exists()) {
      apply(from = rootDir.resolve(focusFile.readText()))
    } else {
      apply(from = DEFAULT_ALL_SETTINGS_FILENAME)
    }

    gradle.settingsEvaluated {
      gradle.rootProject {
        tasks.register("clearFocus", ClearFocusTask(
          focusFileName = provider { DEFAULT_FOCUS_FILENAME }
        ))

        subprojects {
          plugins.withId("com.android.application") {
            val createFocusSettingsTask = tasks
              .register("createFocusSettings", CreateFocusSettingsTask.invoke())

            tasks.register("focus", FocusTask(
              focusFileName = provider { DEFAULT_FOCUS_FILENAME },
              createFocusSettingsTaskProvider = createFocusSettingsTask
            ))
          }
        }
      }
    }
  }
}
