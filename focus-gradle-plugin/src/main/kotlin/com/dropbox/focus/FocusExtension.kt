package com.dropbox.focus

import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

const val DEFAULT_FOCUS_FILENAME = ".focus"
const val DEFAULT_ALL_SETTINGS_FILENAME = "settings-all.gradle"

public abstract class FocusExtension @Inject constructor(objects: ObjectFactory) {

  public val focusFileName: Property<String> = objects.property<String>().convention(DEFAULT_FOCUS_FILENAME)

  public val allSettingsFileName: Property<String> = objects.property<String>().convention(DEFAULT_ALL_SETTINGS_FILENAME)

}
