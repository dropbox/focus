# 🧘 Focus

A Gradle plugin that generates module-specific `settings.gradle` files, allowing you to focus on a specific feature or module without needing to sync the rest of your monorepo.

The Focus plugin evaluates your project setup and creates a unique `settings.gradle` file for the module you want to focus on, which only includes the dependencies required by that module.  It then creates a `.focus` file that references the currently focused module.

With these files in place only the modules that you need will be configured by Gradle when you sync your project.  Deleting the `.focus` file, which can be done using the `clearFocus` task, will revert to using the includes file to configure your entire project.

### Setup

Apply the plugin in your `settings.gradle` file.

```groovy
// settings.gradle(.kts)
pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins {
  id("com.dropbox.focus") version "0.4.0"
}
```

Note that the plugin is currently published to Maven Central, so you need to add it to the repositories list in the `pluginsManagement` block.

Move all non-required `include` statements into `settings-all.gradle`. Projects that are always included can remain in your main `settings.gradle` file.

```groovy
// settings-all.gradle(.kts)
include ':sample:app2'
include ':sample:lib2c'
include ':sample:lib-shared'

include ':sample:moved'
project(':sample:moved').projectDir = new File("sample/lib-moved")
```

Optionally configure the plugin if you'd like to use different settings files than the defaults:

```groovy
// settings.gradle
focus {
  // The name of the settings file
  allSettingsFileName = "settings-all.gradle" // Default
  focusFileName = ".focus"  // Default
}
```

```kotlin
// settings.gradle.kts
configure<com.dropbox.focus.FocusExtension> {
  // The name of the settings file
  allSettingsFileName.set("settings-all.gradle") // Default
  focusFileName.set(".focus") // Default
}
```

Whether or not you configure a custom focus file, it should be added to your `.gitignore` file as it's meant for a specific developer's workflow.

## Usage

The Focus plugin adds a few tasks for you to interact with in your Gradle builds. Using these tasks you can create module specific settings files that will be automatically used by Gradle to configure only the modules which are required.

For example, say you're currently working on the app module `:sample:app2` and only need to run that module and its dependencies. You can use the following flow to reduce the number of modules that are loaded and synced into your IDE to speed up development.

```shell
# When you start work on the app2 module, bring it into focus
./gradlew :sample:app2:focus

# Click the Sync Elephant to have your IDE reload the gradle config, and you'll only have
# :sample:app2 and it's dependencies loaded by the IDE, allowing you to build and run the sample app
# and it's tests without having to sync the rest of the project.

# If you want to spend time in a specific dependency, you can bring that into focus and sync your
# IDE for even more fine grained development
./gradlew :sample:lib2b:focus

# When you want to clear focus and get back to the entire project, simply use the clearFocus task.
./gradlew clearFocus
```

## Tasks

### focus

A `focus` task is added to all subprojects, and allows you to focus on just that module.

### createFocusSettings

A `createFocusSettings` task is created for each subproject, and is responsible for finding a
module's dependencies and creating a module-specific settings file. This is a dependency of the
`focus` task and likely not necessary to call on its own.

### clearFocus

A `clearFocus` task is added to the root project, and allows you to remove any previously focused
modules.

## License

    Copyright (c) 2022 Dropbox, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


