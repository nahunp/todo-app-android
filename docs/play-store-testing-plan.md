# Play Store closed-testing plan

Google requires a new personal developer account to run a **closed
testing track** with enough opted-in testers, for a continuous minimum
number of days, before it can apply for production access. Diego's
Play Console identity verification is still the hard blocker on
starting that clock at all — nothing below can begin until it clears.

**Don't take the exact tester-count/day numbers below as gospel** — Google
has changed these requirements more than once, and Diego's own Play
Console shows the live, account-specific checklist with the real current
numbers (Console → your app → Testing → Closed testing → the
requirements panel). Historically this has been "N testers, opted in
continuously, for 14 days" — confirm the actual N and day count there,
not from anything written here or from Claude's training data, before
counting on it.

## Already done (this repo)

- Signed release build — real upload keystore, R8-minified AAB verified
  to build clean (`./gradlew :app:bundleRelease`).
- App icon (in-app launcher) and the separate 512×512 Play Store listing
  icon (`play-store-assets/icon-512.png`).
- Privacy Policy / Terms of Service — reused live from the web app,
  linked in-app.
- Data safety form answers, content rating questionnaire expectations,
  short/full descriptions, and the v1.0.0 release notes text — all
  drafted in `play-store-listing.md`, ready to paste in.
- Account deletion (Play Store policy requirement for any app supporting
  account creation) — built and verified live, both client and backend.
- `RELEASES.md`'s v1.0.0 entry — the human-readable version of what's
  shipping, if a description beyond the listing copy is ever needed.

## Still needed before submitting to the closed testing track

**Diego's turn — needs his Play Console login, can't be done from here:**
1. Identity verification clearing (blocking everything else).
2. Create the app in Play Console (package `com.nahunp.todoapp`).
3. Paste in the drafted listing content, data safety answers, and content
   rating questionnaire from `play-store-listing.md`.
4. Set up the closed testing track and a tester list — either individual
   tester email addresses or a Google Group. Needs at least a small group
   of real people willing to install and actually use the app for the
   full test window, not just opt in and never open it (Google looks at
   *active* testers, not just opted-in count, for some of these
   requirements — worth over-recruiting a little rather than exactly
   hitting the minimum).
5. Upload the release AAB (`./gradlew :app:bundleRelease`, or have Claude
   build it) and submit the track for review.
6. Once the closed test's minimum window has genuinely elapsed with
   enough active testers, apply for production access from Console.

**My turn — can do without Play Console access:**
- [x] 1024×500 feature graphic (`play-store-assets/feature-graphic.png`)
      — same technique as the listing icon.
- [ ] A few real screenshots of the running app (emulator), not mockups.
- [x] A genuine on-device functional smoke test, including the offline
      flow — done 2026-08-15 on the Pixel_6 emulator against the real
      production backend (register through the real Turnstile widget,
      login, create a list, go genuinely offline via `svc wifi/data
      disable`, add/edit an item, watch the status line, reconnect,
      confirm sync, then force a fresh cold pull to prove the offline
      changes actually reached the server rather than just looking right
      from cache). No bugs found. Full writeup in CLAUDE.md's "Offline
      support" section and today's daily note. Test account deleted
      afterward, verified gone via a failed re-login.

## Sequencing

The two "my turn" items don't block Diego starting his side (account
creation, listing content, tester recruitment can all happen in
parallel) — but the on-device smoke test specifically should happen
*before* the AAB that goes to testers is the final one, since finding an
offline-sync bug after testers are already 5 days into their 14 stalls
the whole clock, not just the fix.
