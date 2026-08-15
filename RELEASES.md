# Releases

## v1.0.0 — 2026-08-15

First release candidate — feature-complete for the same scope the web app
shipped its own v1.0 with, plus a genuinely offline-capable client the web
app doesn't have. This is the build going into Play Console closed
testing, not a pre-1.0 preview: if testing surfaces bugs, the fix ships as
a new upload against this same version, not a "wait for 1.0" milestone.

### What's in v1.0

**Core todo functionality**
- Create, rename, and delete lists; add, rename, remove, complete, and
  reopen items.
- Per-item priority (Low/Medium/High), due date, and category
  (Work/Personal/Health) — tap-to-cycle for priority/category, a Material 3
  date picker for due date.
- Same account, same data as the web app — one shared backend and
  database (see the web repo's `CLAUDE.md`, "Multi-client architecture").

**Works with no connection at all**
- Every screen reads from an on-device cache, not the network directly —
  usable offline, not just tolerant of a flaky connection.
- Every mutation applies locally immediately and queues to sync
  automatically once a connection is back; **remote wins** on conflict.
- The one deliberate exception: registering a new account, logging in
  with fresh credentials, and deleting your account still require a live
  connection. See `CLAUDE.md`'s "Offline support" section for the full
  design.

**Accounts & security**
- Email/password accounts (JWT Bearer, 60-minute access tokens), scoped
  per owner the same way the backend enforces everywhere else.
- Cloudflare Turnstile CAPTCHA on registration — no Android SDK exists for
  it, so this loads the web app's own Turnstile page in a `WebView` and
  gets the token back through a JS bridge. No backend change was needed.
- Account deletion, in-app — required by Google Play policy for any app
  that supports account creation. Cascades to the account's lists/items on
  the backend, verified live (not just unit-tested).
- Terms of Service and Privacy Policy, reused from the web app's actual
  pages via `WebView` rather than duplicated.

**Architecture**
- Jetpack Compose + Material 3, Clean Architecture (domain/data/
  presentation/di), Hilt DI, Retrofit + OkHttp + kotlinx.serialization,
  Room for the local cache, DataStore for the access token.
- Consumes the web repo's versioned `/api/v1` REST API — same backend,
  same database, no mobile-specific backend surface except what's
  documented as needing client-type-aware handling (CAPTCHA, push).

**Release engineering**
- Signed release build (R8-minified AAB), a real upload keystore
  generated for this app specifically.
- CI (`Android CI` GitHub Actions workflow) builds and tests every push.
- First unit tests this repo has (`SyncManagerTest`, covering the offline
  sync engine's riskiest logic).

### Known limitations (by design, not oversights)

- No double-submit guard on delete (same open item the web frontend has
  for its own delete button).
- Priority/category editing is tap-to-cycle, not a dropdown/segmented
  control.
- Mutations no longer surface a synchronous per-action error (a duplicate
  name, etc.) now that they're local-first — a sync failure shows up later
  via the small status line instead. Deliberate trade-off of going
  offline-first, not an oversight — see `CLAUDE.md`.
- `dueDateState` (Overdue/Today/Upcoming) is a local, device-clock
  approximation until the next successful sync — the backend's real,
  timezone-correct computation is what actually gets stored.
- No push notifications yet — Firebase Cloud Messaging is wired into the
  build but not connected to any real backend event.
- Dark mode is forced off (an incomplete first attempt was worse than no
  dark mode at all — see `CLAUDE.md`).

### How it was built

Built by Claude end to end, in a separate repo/session from the web app,
using that repo as the architectural reference (Clean Architecture, the
same API contract) rather than a template to copy. Full build history is
in `docs/daily-notes/`.
