# CLAUDE.md

Read this first, every session. Durable reference — architecture,
conventions, gotchas already learned the hard way — not a changelog. Keep
it about what's *true now* and *why*.

## What this repo is

The Android client for the same TodoApp product as
[`nahunp/todo-app`](https://github.com/nahunp/todo-app) (the web repo:
.NET backend + Angular frontend, deployed to Azure) — same features, same
backend API, same account system. Different repo on purpose: this is a
genuinely separate client with its own build system, its own
architecture decisions, and its own release cadence, not a folder that
happens to share a monorepo. The web repo's backend is the one and only
source of truth for the API contract (`docs/api/openapi.json` there);
this repo consumes it, never redefines it.

**This is a deliberate decision, not a default**: web, this Android app,
and the future iOS app all share **one** backend and **one** database —
same account, same data, regardless of which client you're using. See
the web repo's CLAUDE.md, "Multi-client architecture" section, for the
full reasoning (and for what sharing a backend does *not* mean — CAPTCHA
and push notifications still need client-type-aware handling, covered
in Open Questions below).

**Read the web repo's CLAUDE.md too, at least once.** Auth model,
ownership rules (404 not 403 for non-owners), the enums-serialize-as-
strings convention, the API versioning rationale, the secrets policy —
all inherited from there, not re-derived here. This file only covers what's
different or Android-specific.

## Environment reality (verified live, this machine)

- Android Studio + SDK already installed. SDK at
  `%LOCALAPPDATA%\Android\Sdk`. Only platform installed at scaffold time:
  **android-37**, build-tools **36.0.0** — `compileSdk`/`targetSdk` are set
  to 37 to match, not to some assumed "current" value. Check what's
  actually installed (`ls $SDK/platforms`) before bumping either.
- **No standalone JDK on this machine** — only the JBR (JetBrains Runtime)
  bundled inside Android Studio, at
  `C:\Program Files\Android\Android Studio\jbr`, currently JDK **25**.
  Running Gradle from a terminal (not Android Studio itself) needs
  `JAVA_HOME` pointed there explicitly.
- **That JDK 25 + Gradle/AGP version combo took real trial and error to
  get working** — worth recording in full since it cost three failed
  attempts:
  1. Gradle 8.9 (and 8.13) both threw `IllegalArgumentException: 25.0.2`
     from deep inside the Kotlin Gradle Plugin's vendored
     `JavaVersion.parse` — that utility didn't understand a JDK 25 version
     string. Not a Gradle-core issue; a Kotlin-plugin-bundled one.
  2. Bumped to Gradle **9.7.0** (real current stable at the time — checked
     live via `https://services.gradle.org/versions/current`, don't
     assume, that endpoint is free to hit again) and Kotlin **2.4.10**
     (real latest stable, checked via Maven Central's
     `maven-metadata.xml` for `org.jetbrains.kotlin:kotlin-gradle-plugin`)
     — fixed the JDK parsing, but then AGP **8.13.2** failed with: *"Plugin
     'com.android.internal.application' relies on
     'org.gradle.api.problems.internal.InternalProblems', a Gradle
     internal API that was removed in Gradle 9.6.0."* AGP 8.x is not
     compatible with Gradle 9.6+.
  3. Bumped AGP to **9.3.1** (real latest stable) to match — but AGP 9.x
     has **Kotlin support built in** and refuses to coexist with the
     separate `org.jetbrains.kotlin.android` plugin ("no longer required
     since AGP 9.0" — a hard error, not a warning). Removed that plugin
     from both `build.gradle.kts` files; `kotlin.serialization` and
     `kotlin.plugin.compose` are still separate plugins and still needed.
  4. Only after all three fixes did `./gradlew :app:assembleDebug`
     actually succeed — confirmed live, not assumed, including a real
     `kspDebugKotlin` run (Hilt codegen) and a produced
     `app-debug.apk`.
  - **The lesson, not just the fix**: don't trust remembered/trained
    version numbers for fast-moving tooling (Gradle, AGP, Kotlin) — check
    live (Maven Central `maven-metadata.xml`, `services.gradle.org`)
    before pinning, the same way the web repo's CLAUDE.md says to actually
    run code rather than assume a diff is correct.
- `local.properties` (gitignored) needs `sdk.dir=<path>` pointing at the
  SDK — not committed, machine-specific by design (same reasoning as the
  web repo never committing a real connection string).

## Stack

- **Jetpack Compose** (not Views/XML layouts) + **Material 3**.
- **Clean Architecture**, adapted from the web repo's backend layering,
  not copied 1:1 (there's no "no PackageReference" project-reference
  enforcement here — Gradle module boundaries would be the equivalent if
  this ever splits into multiple Gradle modules; it's single-module today,
  package-only separation):
  - `domain/` — models, repository *interfaces*, no Android framework
    dependencies.
  - `data/` — DTOs (`data/remote/dto/`), repository *implementations*,
    the Retrofit service interface (`core/network/`).
  - `presentation/` — one package per feature (`auth/login/`,
    `auth/register/`, `todolist/`), each with a `UiState` data class, a
    `@HiltViewModel`, and a `@Composable` screen — same three-file
    pattern every time, follow it for new features rather than inventing
    a new shape per screen.
  - `di/` — Hilt modules. `core/network/NetworkModule.kt` and
    `core/datastore/DataStoreModule.kt` provide infrastructure;
    `di/RepositoryModule.kt` is the only place interface->impl binding
    happens.
- **Hilt** for DI (not manual/Koin) — `@HiltAndroidApp` on
  `TodoApplication`, `@AndroidEntryPoint` on `MainActivity`,
  `@HiltViewModel` on every ViewModel, `hiltViewModel()` in Compose to
  obtain them.
- **Retrofit + OkHttp + kotlinx.serialization** (not Moshi/Gson) — one
  `TodoApiService` interface mirroring the backend's `/api/v1` routes
   route-for-route.
- **DataStore** (not SharedPreferences directly) for the access token —
  see `core/datastore/TokenStore.kt`'s doc comment for why.
- **Firebase**: Crashlytics + Cloud Messaging (notifications) + Analytics.
  See the dedicated section below — the Gradle wiring exists, a real
  project does not yet.

## Backend contract — inherited, not redesigned

- Base URL is build-time (`BuildConfig.API_BASE_URL`,
  `app/build.gradle.kts`), not runtime like the web frontend's
  `window.__appConfig` — there's no equivalent of "redeploy the static
  bundle without rebuilding" for an installed APK, so build-time
  `buildConfigField` per build type is the *correct* adaptation here, not
  a missed opportunity to copy the web pattern. Debug points at
  `10.0.2.2:5080` (emulator's host-loopback alias); release points at the
  real Azure origin.
- Enums arrive as **string names** (`"High"`, `"Overdue"`), matching the
  backend's `JsonStringEnumConverter` — `domain/model/TodoItem.kt`'s enum
  constant names are spelled to match exactly (case-sensitive) so
  `Priority.valueOf(dto.priority)` just works without a custom
  serializer. If the backend ever adds a new enum value, this needs a
  matching addition here — nothing enforces that automatically across
  repos the way a single shared type would.
- Error responses are `ProblemDetails`/`ValidationProblemDetails` JSON
  (see the web repo's `GlobalExceptionHandler.cs`) — `core/network/
  ApiError.kt` parses `detail`/`errors`/`title` out of them, same
  reasoning as the web repo's `shared/http-error.ts` and the bug it was
  written to fix (see that repo's `fix/auth-loading-and-error-states`
  PR) — never show a bare HTTP status if the backend sent a real reason.
- No refresh tokens on the backend (60-minute access token only,
  deliberate v1 scope) — this app doesn't do anything special about that
  yet either; a token going stale mid-session just starts failing
  requests with 401 until the user logs in again. Worth a real "session
  expired, please log in again" UX once this matters.

## Release signing — configured, first real signed build verified live

`app/build.gradle.kts` reads `keystore.properties` (project root,
gitignored, never committed) if it exists, and wires a `release`
`signingConfig` from it; CI and anyone without that file still build fine
(`release` just comes out unsigned — the point of a release build in CI
is catching R8/shrinking regressions, not producing something to
actually upload). Same conditional-application pattern as the Firebase
plugins above.

- Keystore lives at `keystore/todoapp-upload.jks`, also gitignored, a
  PKCS12 keystore generated with `keytool` (30-year validity — Google's
  own recommendation, to never need regenerating). **PKCS12 quirk hit
  live**: it forces the store password and key password to be the
  *same* value — `keytool` silently ignores a separately-specified
  `-keypass` and warns about it rather than erroring, which is exactly
  the kind of thing that looks like it worked until the resulting
  keystore fails to decrypt. Verified by actually opening the generated
  keystore with the password immediately after creating it, not assumed.
- This is an **upload key**, not the real Play Store signing key — Play
  App Signing (Google's default for new apps) re-signs with a key Google
  holds; losing this one is recoverable via Google's own upload-key-reset
  flow, not catastrophic, but it should still never end up in git history.
- **First real signed release build verified live**: `./gradlew
  :app:bundleRelease` — this exercises R8 minification/shrinking for the
  first time since the scaffold was created (`isMinifyEnabled = true` had
  never actually been run before). Succeeded clean, and the resulting
  `.aab`'s signature was independently verified with `jarsigner -verify`
  (`jar verified.` — the self-signed-cert/no-timestamp warnings jarsigner
  prints are normal and expected for any Android app signing key, not
  specific to this one).

## Firebase — wired, not configured

`app/build.gradle.kts` depends on the Crashlytics/Analytics/Messaging
SDKs, but **there is no real Firebase project behind any of it yet**, and
`app/google-services.json` is gitignored, never committed (same
secrets-never-in-source policy as the web repo). The `google-services`
and `firebase-crashlytics` *plugins* are only applied when that file
exists (`if (file("google-services.json").exists())` in
`app/build.gradle.kts`) — **this used to be a hard build failure instead**
(both plugins fail the *entire* build, not just Firebase features,
without that file — Diego hit this directly trying to open the project in
Android Studio right after the initial scaffold) — fixed once, don't
revert it back to unconditional `alias(...)` application. Firebase code
still compiles fine either way (the SDKs are just libraries on the
classpath); it just won't initialize without a real project. To make it
real:

1. Create a Firebase project (Firebase console — needs your own Google
   account; this is account-creation territory, not something an agent
   should do on your behalf).
2. Add an Android app to it with package name `com.nahunp.todoapp`.
3. Download the real `google-services.json`, drop it in `app/` — the
   plugins activate automatically next build, no other change needed.
4. `core/notifications/TodoFirebaseMessagingService.kt` is a stub —
   `onNewToken`/`onMessageReceived` just log, they don't do anything real
   yet. Needs: a backend endpoint to receive/store a device's FCM token
   per user (doesn't exist in the .NET backend yet — this is Android-side
   plumbing only so far), and an actual notification-building step.
5. The `POST_NOTIFICATIONS` runtime permission (Android 13+) is declared
   in the manifest but never requested at runtime — wire that into a
   real first-run flow once there's an actual notification worth asking
   permission for.

CI (`android-ci.yml`) doesn't need a placeholder file anymore either —
it builds the same way any fresh clone does, Firebase present but inert.

## Open questions (need a decision, not yet made)

- ~~CAPTCHA on registration~~ **Done** — went with option (a), a
  `WebView`-hosted Turnstile challenge. `TurnstileCaptchaView.kt` loads
  the web repo's `frontend/public/mobile-captcha.html` (a new static page
  there, outside the Angular app) via `BuildConfig.CAPTCHA_PAGE_URL`, and
  gets the token back through a `window.AndroidCaptchaBridge.onToken(...)`
  JS interface. No backend change needed at all —
  `TurnstileCaptchaService` already just verifies whatever token it's
  given, regardless of origin. Registration is now fully functional, not
  a placeholder. See the web repo's CLAUDE.md, "Multi-client
  architecture," for the full writeup (this was the one piece that
  decision still needed client-type-aware handling for).
- ~~No auth guard~~ **Done** — `AppEntryViewModel` checks
  `AuthRepository.isAuthenticated` once before `NavHost` is even composed
  (Compose Navigation needs a concrete `startDestination` up front, so
  this is a one-shot "what's the first screen" check, not a full
  per-navigation route guard like the web frontend's `authGuard` —
  a token going stale *mid-session* still just fails requests with 401
  until the user manually logs out and back in; no redirect-on-expiry
  yet). `TodoListScreen` also got a Logout action it didn't have before,
  since without one there was no way back to Login to test any of this.
- ~~No detail screen~~ **Done** — `TodoListDetailScreen` +
  `TodoListDetailViewModel`: add/remove items, toggle done, and
  priority/category (tap-to-cycle through values) and due date
  (Material3 `DatePickerDialog`) editing. Building this surfaced real
  bugs in the initial scaffold's assumed contract, now fixed and worth
  knowing about — see "Contract bugs found building the detail screen"
  below.
- **No offline story.** Every screen hits the network directly, no local
  cache/Room database. Fine for a template; a real app probably wants at
  least a "show the last-known list while refreshing" cache before this
  ships.
- **No double-submit guard** on delete (same open item as the web
  frontend's own daily notes flagged for its delete button, also still
  unfixed there as of this writing).
- **Priority/category editing is tap-to-cycle, not a picker.** Works, but
  a dropdown (category) and a segmented control (priority) would be a
  more honest UI than "tap repeatedly and hope." Deliberately simple for
  a first pass, not a final design.
- **Dark mode is forced off** (`TodoAppTheme` no longer reads
  `isSystemInDarkTheme()` at all — see `Theme.kt`'s doc comment).
  Confirmed live on a real device with system dark mode on: text and
  buttons were unreadable, because the first dark scheme only overrode
  `primary`/`error` and left everything else at Material3's generic dark
  defaults — internally inconsistent against Cloud Dancer's light-only
  palette, not a deliberate dark design. Needs a real Cloud-Dancer-dark
  palette (the web frontend doesn't have one either) before re-enabling,
  not just flipping the boolean back on.

## Contract bugs found building the detail screen

The initial scaffold's DTOs were written against an *assumed* shape of
the backend contract, not checked against it — building a feature that
actually exercises every field surfaced three real mismatches, found by
checking `docs/api/openapi.json` (the web repo's committed, authoritative
contract) directly rather than continuing to guess:

- **The "is this item done" field is `isDone`, not `isComplete`.**
  `TodoItem`/`TodoItemDto` had the wrong name from the start; also
  missing `notes` and `completedAt` entirely, both present on the
  backend since the beginning.
- **`complete`/`reopen` are `POST`, not `PATCH`.** Both were declared as
  `@PATCH` in `TodoApiService`, unverified, since nothing had exercised
  those specific calls yet.
- **`POST /todolists` and `POST /todolists/{id}/items` return only
  `{ "id": <int> }`**, not the full created object — `createTodoList`
  was declared to return a full `TodoListDto`, which would have failed
  to deserialize the moment it was actually called with a real response.
- **The lesson**: a DTO that compiles and "looks right" proves nothing
  about whether it matches the real contract — check the actual response
  shape (`docs/api/openapi.json`, or the live API) before trusting a
  guessed field name, the same reasoning as this repo's own "verify
  live, not just reads correctly" process rule, just not followed
  closely enough the first time around.

## Process (inherited from the web repo, unless noted)

- **Secrets**: never ask for one in chat, never hardcode one, never
  accept a pasted API key/token/connection-string-equivalent as a "fix."
  `google-services.json` follows this rule same as the web repo's
  connection strings — say where it goes (`app/`, gitignored), the human
  drops the real file in themselves.
- **Verify live, not just "reads correctly."** This repo's own Gradle/AGP
  saga above is the proof: guessed version numbers failed three times in
  a row; only checking live metadata and actually running
  `./gradlew assembleDebug` found the real, current, compatible set.
- **Branching/CI**: single `android-ci.yml` for now (build debug + unit
  test on every push and PR) — no multi-branch promotion pipeline yet
  like the web repo's `development`→`release`→`master`. Worth adopting
  once this repo has enough real activity to justify it, not before.
