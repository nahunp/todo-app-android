# Play Store listing — draft content

Not app source — reference material for the Play Console listing form,
ready to paste in once identity verification clears. Update this file if
any of it changes rather than letting the Console listing and this
drift apart.

## App details

**App name**: TodoApp

**Short description** (max 80 chars, currently 79):
```
Simple todo lists with priority, due dates, and categories — demo app.
```

**Full description** (max 4000 chars):
```
TodoApp is a personal project built to practice designing, building, and
deploying an enterprise-grade, cloud-ready application end to end —
architecture, auth, CI/CD, all of it — using AI coding agents as the
actual developers, not just autocomplete. It's not a company or a
commercial product.

Features:
• Create, rename, and delete todo lists
• Add, complete, reopen, rename, and remove items
• Set priority (Low/Medium/High), due dates, and categories
  (Work/Personal/Health) per item
• Overdue/due-today items are called out automatically
• Same account works on the web version of this app too — your lists
  stay in sync across both

This app is a learning project, not a security-audited production
service — please don't reuse a password here that you use anywhere
else. Full details in the in-app Terms of Service and Privacy Policy.

Source code (both this app and its backend) is public and MIT-licensed:
https://github.com/nahunp/todo-app-android
https://github.com/nahunp/todo-app
```

**App category**: Productivity

**Contact email**: (use whatever email you want publicly listed for
this app — Play Console requires one; it doesn't have to be the same
address you registered the Play Console account with)

**Privacy policy URL**:
```
https://zealous-meadow-0c73a9610.7.azurestaticapps.net/privacy
```

## Store listing graphics

**App icon (512×512)**: [`play-store-assets/icon-512.png`](play-store-assets/icon-512.png)
— solid `#4E76AC` background, bold white checkmark. Same mark as the
in-app launcher icon, just at the flat listing size/format Play Console
wants (no adaptive-icon layers). Feature graphic (1024×500) and
screenshots aren't produced yet — add before actual submission.

## Content rating questionnaire — expected answers

Google's questionnaire is asked interactively in Console, but every
answer for this app should be "No" — no violence, no sexual content, no
gambling, no user-generated content that's visible to other users (todo
lists are private per account), no ability to communicate with other
users. Should land on the lowest rating tier automatically (Everyone /
PEGI 3, depending on region).

## Data safety form — answers

Google requires declaring every data type collected, matched against
what the app (and its shared backend) actually does — not guessed:

| Data type | Collected? | Purpose | Shared with third parties? | Optional? |
|---|---|---|---|---|
| Email address | Yes | Account creation & login | No | No (required to use the app) |
| Password | Not applicable — Play Console's data types don't include a distinct "password" category; passwords are never stored in a readable form (see the web repo's Privacy Policy on hashing) and aren't "collected" in the sense this form means | | | |
| App activity — todo lists/items you create | Yes | Core app functionality (the todo list itself) | No | No |
| Device or other IDs | No | | | |
| Precise/approximate location | No | | | |
| Photos/videos/audio | No | | | |

- **Is data encrypted in transit?** Yes — all API calls use HTTPS/TLS.
- **Is data encrypted at rest?** Yes — Azure SQL Transparent Data
  Encryption (confirmed enabled on the production database).
- **Can users request data deletion?** Yes, once the account-deletion
  work below ships — in-app, not just by request.
- **Third-party data sharing**: Cloudflare Turnstile (CAPTCHA on
  registration) may process the device's IP address as part of bot
  verification — same disclosure already in the in-app Privacy Policy.
  No other third-party data sharing.
