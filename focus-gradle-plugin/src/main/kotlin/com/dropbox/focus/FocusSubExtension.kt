package com.dropbox.focus

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty

public abstract class FocusSubExtension @Inject constructor(
  layout: ProjectLayout,
  objects: ObjectFactory
) {

  public val focusSettingsFile: RegularFileProperty = objects.fileProperty().convention(
    layout.buildDirectory.file("focus.settings.gradle")
  )

  public val moduleToDirMapFile: RegularFileProperty = objects.fileProperty().convention(
    layout.buildDirectory.file("moduleToDirMap.csv")
  )
}
