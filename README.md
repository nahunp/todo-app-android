# TodoApp — Android

The Android client for [TodoApp](https://github.com/nahunp/todo-app) — same
account, same lists, same backend API, native this time. Personal project
for practicing enterprise-grade, cloud-ready app development end to end
(architecture, CI, the works) with an AI coding agent as the actual
developer, same purpose as the web repo, different platform.

[![Android CI](https://img.shields.io/github/actions/workflow/status/nahunp/todo-app-android/android-ci.yml?branch=main&label=build)](https://github.com/nahunp/todo-app-android/actions/workflows/android-ci.yml)

## Status

Early template / scaffold, not a working app yet. What exists:

- A real, verified-building (`./gradlew :app:assembleDebug` succeeds,
  produces an installable APK) Jetpack Compose + Clean Architecture +
  Hilt project.
- Login screen, fully wired end to end against the backend (network
  layer, token storage, error handling, loading states).
- Todo list screen: view/create/delete lists.
- Firebase (Crashlytics, Cloud Messaging, Analytics) — Gradle-wired, no
  real Firebase project behind it yet.

What doesn't exist yet: registration (blocked on a CAPTCHA-for-mobile
decision), list detail / item editing (priority, due date, category),
an auth guard, real push notifications, offline support. See
[`CLAUDE.md`](CLAUDE.md)'s "Open questions" for the full list and why
each one isn't done yet.

## Stack

- Kotlin, Jetpack Compose, Material 3
- Clean Architecture (`domain` / `data` / `presentation` / `di`)
- Hilt (DI), Retrofit + OkHttp + kotlinx.serialization (networking),
  DataStore (token storage), Navigation Compose
- Firebase: Crashlytics, Cloud Messaging, Analytics
- Gradle 9.7, AGP 9.3, Kotlin 2.4 — see `CLAUDE.md`'s "Environment
  reality" section for why these specific versions and what it took to
  get them working together

## Building

Requires Android Studio (or a standalone JDK 17+ and the Android SDK) and
a `local.properties` with `sdk.dir=<path-to-your-Android-SDK>` (gitignored,
machine-specific — Android Studio creates this for you automatically the
first time you open the project).

```powershell
./gradlew :app:assembleDebug
```

The build works fine without a real Firebase project — Firebase just
stays inert (no Crashlytics/Analytics/Messaging) until you add your own
`app/google-services.json` (gitignored, never committed). See
`CLAUDE.md`'s Firebase section for how to get one; nothing else needs to
change once you do.

By default the debug build points at `10.0.2.2:5080` (the Android
emulator's alias for your host machine's localhost) — run the
[backend](https://github.com/nahunp/todo-app) locally alongside this app
for it to have anything to talk to. Release builds point at the real
deployed Azure backend.

## License

[MIT](LICENSE) — same as the web repo.
