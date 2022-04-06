package com.dropbox.focus

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

internal const val FOCUS_TASK_GROUP = "focus mode"
internal const val CREATE_FOCUS_SETTINGS_TASK_NAME = "createFocusSettings"
internal const val FOCUS_TASK_NAME = "focus"
internal const val CLEAR_FOCUS_TASK_NAME = "clearFocus"
internal val TASK_NAMES = setOf(
  CREATE_FOCUS_SETTINGS_TASK_NAME,
  FOCUS_TASK_NAME,
  CLEAR_FOCUS_TASK_NAME,
)

/**
 * A Gradle plugin that generates module-specific `settings.gradle` files, allowing you to focus on a specific
 * feature or module without needing to sync the rest of your monorepo.
 *
 * It works by evaluating your project setup and creating a unique `settings.gradle` file for the module you want
 * to focus on, which only includes the dependencies required by that module. It then creates a `.focus` file that
 * references the currently focused module.
 *
 * With these files in place only the modules that you need will be configured by Gradle when you sync your project.
 * Deleting the `.focus` file, which can be done using the `clearFocus` task, will revert to using the includes file
 * to configure your entire project.
 *
 * This is optionally configurable vai the [focus][FocusExtension] extension.
 *
 * ```kotlin
 * focus {
 *   // The name of the settings file that contains all of your standard `include` statements.
 *   allSettingsFileName = "settings-all.gradle" // Default
 *
 *   // The name of the file in your root project that identifies the generated settings file to use.
 *   // This should be added to your .gitignore file.
 *   focusFileName = ".focus"  // Default
 * }
 * ```
 *
 * The general workflow is:
 *
 * 1. Focus on the module you'd like to work in by running it's `focus` task.
 * 2. Repeat step 1 as you work in other modules.
 * 3. Either delete the `.focus` file, or call the `clearFocus` task to stop focusing.
 */
public class FocusPlugin : Plugin<Settings> {
  override fun apply(target: Settings): Unit = target.run {
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
