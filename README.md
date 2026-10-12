# SmartSubscription for Android

A Jetpack Compose port of the iOS app's refined UI (`smart-subscriptions/design/ui-refinement-2026-09-20.html`),
using the same demo seed as `DemoDataSeeder.swift`.

Screens: Home (the renewal rail), Add, Reading an invoice, Enter by hand / Review / Edit, Detail,
Analytics, Settings, Categories, Archive, and the one-time Unlock. The Upcoming renewals
home screen widget lists what renews in the next 7 days.

## Build

```
./gradlew :app:assembleDebug        # app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:testDebugUnitTest    # ledger math, money formatting, amount parsing
```

## Layout

- `data/` models, the demo seed, `Ledger` (every total and grouping the screens show), CSV export
- `AppViewModel.kt` app state and actions, in memory
- `ui/theme/` the design's colour tokens (light and dark) and Bricolage Grotesque for figures
- `ui/components/` ink block, cards, rows, segmented control, pull-downs, glyphs
- `ui/screens/` one file per screen
- `widget/` the home screen widget, fed a snapshot the app writes on every change

## Not wired yet

- Persistence: state lives in memory and resets on restart.
- Invoice scanning plays a sample invoice; no OCR yet.
- The $2.99 unlock simulates Google Play Billing.
- Reminders are stored settings; nothing is scheduled.
- Currency conversion: totals count only the default currency.
- iCloud sync has no Android version and is left out.
- The widget's window is fixed at 7 days; Settings has no 7/14/30 choice yet.
