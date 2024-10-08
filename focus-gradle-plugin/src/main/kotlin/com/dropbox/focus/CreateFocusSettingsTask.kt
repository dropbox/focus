package com.dropbox.focus

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Not worth caching")
public abstract class CreateFocusSettingsTask : DefaultTask() {

  @get:OutputFile
  public abstract val settingsFile: RegularFileProperty

  @get:OutputFile
  public abstract val modulesToDirMapFile: RegularFileProperty

  init {
    outputs.upToDateWhen { false }
  }

  @TaskAction
  public fun createFocusSettings() {
    val dependencies = project.collectDependencies().sortedBy { it.path }

    // generate CSV mapping from module name to its absolute path
    modulesToDirMapFile.get().asFile.writer().use { writer ->

      dependencies
        .forEach { dep ->
          val projectPath = dep.path
          val projectDir = dep.projectDir
          writer.appendLine("$projectPath,$projectDir")
        }
    }

    settingsFile.get().asFile.writer().use { writer ->
      writer.write("// ${project.path} specific settings\n")
      writer.appendLine("//")
      writer.appendLine("// This file is autogenerated by the focus task. Changes will be overwritten.")
      writer.appendLine()

      // language=groovy
      writer.append(
        """
        File f = new File('${modulesToDirMapFile.get().asFile.absolutePath}')
        if (f.exists()) {
          f.eachLine { line ->
            var values = line.split(",")
            var module = values[0]
            var path = values[1]
            include(module)
            project(module).projectDir = new File(path)
          }
        }
        """.trimIndent()
      )
    }
  }

  private fun Project.collectDependencies(): Set<Project> {
    val result = mutableSetOf<Project>()
    fun addDependent(project: Project) {
      val configuredProject = this.evaluationDependsOn(project.path)
      if (result.add(configuredProject)) {
        configuredProject.configurations.forEach { config ->
          config.dependencies
            .filterIsInstance<ProjectDependency>()
            .map { it.dependencyProject }
            .forEach(::addDependent)
        }
      }
    }

    addDependent(this)
    return result
  }

  public companion object {
    public operator fun invoke(subExtension: FocusSubExtension): CreateFocusSettingsTask.() -> Unit = {
      group = FOCUS_TASK_GROUP
      settingsFile.set(subExtension.focusSettingsFile)
      modulesToDirMapFile.set(subExtension.moduleToDirMapFile)
      notCompatibleWithConfigurationCache("This reads configurations from the project at action-time.")
    }
  }
}
