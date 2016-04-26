# Gradle External Build Plugin

Android Studio Plugin for gradle building on external tools

This plugin can run tools (e.g. shell, python, perl, etc) with Gradle arguments and tasks, instead of the default gradle build before run.

## Requirements

Android Studio >= 1.5

## Why use this plugin?

This plugin was created to build on Google Compute Engine instead of my slow machine :)

## How to use

### Install

1. Download latest `GradleExternalBuildPlugin.zip` from "Releases".

2. Open Android Studio's Preferences and go to "Plugins".

3. Select "Install plugin from disk..." and install this plugin from `GradleExternalBuildPlugin.zip`.

4. Restart Android Studio.

### Set up and Run

1. Open Preferences and choose "Gradle External Build" in "Build, Execution, Deployment".

2. Set your build program path and Parameters in "Program" and "Parameters".

    You can insert "Macro"s, then macro texts will be replaced with build environment variables.

    In addition to default "Macro"s, you can replace `$GRADLE_ARGS$` and `$GRADLE_TASKS$` with gradle arguments (e.g. `-Pandroid.optional.compilation=INSTANT_DEV,-Pandroid.injected.build.api=21,`) and gradle tasks (e.g. `:app:assembleDebug,:app:assemble,:app:assembleRelease,`) in "Parameters".

3. Select "Edit Configurations..." on configurations button or in "Run" menu.

4. Add new "Android Configuration" and add "Gradle Make on External Tool" to "Before launch" section.

5. Remove "Gradle-aware Make" and set other configuration preferences.

6. Choose created Build Configuration on configurations button.

7. Run.

### Tips

#### Escape

Tasks and arguments text is joined with commas(`,`). If joined variables contain commas, back slashes (`\`) will be added before them.

#### Remote build example Shell script

See `example.sh`.

# License

See `LICENSE.md`.

