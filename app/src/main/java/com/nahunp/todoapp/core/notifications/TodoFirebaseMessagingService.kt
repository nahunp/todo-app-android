package com.nahunp.todoapp.core.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Stub — needs three things before it does anything real:
 *  1. A Firebase project with Cloud Messaging enabled (Firebase console),
 *     and app/google-services.json from it (see CLAUDE.md's Firebase
 *     section).
 *  2. A backend endpoint to receive/store this device's FCM token
 *     (onNewToken below) per user, plus the backend actually sending
 *     messages on relevant events (e.g. a due-today reminder) — none of
 *     that exists in the .NET backend yet. This is Android-side plumbing
 *     only.
 *  3. The POST_NOTIFICATIONS runtime permission request (Android 13+),
 *     not just the manifest declaration — wire that into the app's
 *     first-run flow once there's an actual notification to show.
 */
class TodoFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: send this token to the backend, associated with the
        // logged-in user, once that endpoint exists server-side.
        Log.d(TAG, "New FCM token generated (not yet sent anywhere)")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // TODO: build and show an actual notification (NotificationCompat)
        // once there's a real message payload shape to handle.
        Log.d(TAG, "FCM message received: ${message.data}")
    }

    private companion object {
        const val TAG = "TodoFcmService"
    }
}
