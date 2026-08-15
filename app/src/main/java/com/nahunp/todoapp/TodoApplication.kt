package com.nahunp.todoapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * @HiltAndroidApp generates the Hilt component tree rooted here — every
 * @AndroidEntryPoint Activity/Service/Fragment in this app depends on this
 * class existing and being declared as android:name in the manifest.
 *
 * Firebase Crashlytics/Analytics/Messaging initialize themselves off
 * google-services.json via the Google Services Gradle plugin — nothing
 * needs to happen here for them specifically. See CLAUDE.md's Firebase
 * section for what still needs a real Firebase project before this builds.
 */
@HiltAndroidApp
class TodoApplication : Application()
