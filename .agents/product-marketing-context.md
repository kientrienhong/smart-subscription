# Product Marketing Context (Android)

*Last updated: 2026-10-12 (on-device translation added to scanning). Android competitors and ML Kit data collection checked 2026-10-09.*

> Adapted from the iOS repo's `smart-subscriptions/.agents/product-marketing-context.md` (2026-09-20), which is still the source for anything shared across platforms. Items marked **[Android]** differ from the iOS doc. **[inferred]** means read from the code, not confirmed positioning. **[TBD]** needs your input. **[verify]** is a claim to check before it goes into public copy.

## Product Overview
**One-liner:** A private subscription tracker for Android phones that shows what you pay for, when each one renews, and what it has cost you so far.
**What it does:** SmartSubscription (`com.timeskip.smartsubscription`) lets you add recurring subscriptions by hand or by scanning an invoice or receipt. Home lists what renews over the next six weeks, with a "still to come this month" total. Analytics splits paid from expected spend by month, and an Upcoming renewals home screen widget lists what renews in the next 7 days. Paused and cancelled subscriptions move to an Archive that keeps their lifetime cost. **[Android]** There is no account and no bank link.
**Product category:** Subscription tracker / recurring-bill manager (Play Store shelf: Finance, "subscription manager"). **[inferred]**
**Product type:** **[Android]** Native Android app (Kotlin, Jetpack Compose), minSdk 26 (Android 8.0), version 0.2.0 (build 5). It ports the iOS app's 2026-09-20 UI refinement.
**Business model:** **[Android]** A one-time $2.99 unlock, following the design doc (`ui-refinement-2026-09-20.html`): "An app that tracks subscriptions should not sell you one." The proposed split puts scanning, more than 5 subscriptions and 12-month Analytics behind the unlock. Entering by hand, reminders, the Home rail, the Archive and the widget stay free. Google Play Billing is simulated in the app today, and nothing is gated yet. The iOS tip jar has no Android equivalent.

**Not shippable yet [Android]:**
- Subscriptions live in memory and reset when the app restarts.
- Reminders are stored as settings; nothing is scheduled.
- Totals count only the default currency; conversion isn't ported.
- There is no sync (iCloud has no Android equivalent).

## Target Audience
**Target companies:** N/A, consumer (B2C).
**Decision-makers:** **[Android]** The person paying for the subscriptions, on an Android phone. The iOS doc's "Apple ecosystem, iPhone + Mac" angle doesn't apply here.
**Primary use case:** Knowing your real recurring spend across streaming, software and services, and not getting surprised by a renewal.
**Jobs to be done:**
- "Tell me how much I'm really spending per month and per year on subscriptions."
- "Warn me before something renews so I can cancel it or budget for it."
- "Keep a record of what I paid and when, without giving a third party my bank login."
**Use cases:**
- **[Android]** Scanning an invoice photo to add a subscription. ML Kit reads the text on the phone. On phones that have Gemini Nano, the on-device model fills in provider, amount, currency, date and cycle. Other phones fall back to a pattern parser, and you check the result on the review form. Invoices in other languages are translated to English on the phone (ML Kit Translation) so the parser can read them. Each language's ~30 MB model downloads on Wi-Fi after the first scan in that language, so the first scan only gets amount, currency and numeric dates from the parser.
- Confirming a renewal when it happens, so paid history builds up next to expected spend.
- Pausing or cancelling a service and still seeing what it cost over its lifetime (Archive).
- **[Android]** Glancing at the home screen widget to see what renews this week.
- Subscriptions in several currencies (VND, JPY, USD, EUR and more). Zero-decimal currency handling suggests a Vietnam/SE Asia-aware audience. **[inferred, confirm]**

## Personas
Single-persona consumer product, skipped. **[Add one if you target, for example, freelancers tracking SaaS spend.]**

## Problems & Pain Points
**Core problem:** Subscriptions are scattered across app stores, card statements and email receipts, so nobody knows the real total or when the next charge lands. **[inferred]**
**Why alternatives fall short:**
- Bank-linked apps (Rocket Money and others) need your financial credentials and often miss non-card or foreign-currency charges.
- Spreadsheets don't remind you and go stale.
- **[Android]** Google Play's own Subscriptions screen only covers subscriptions billed through Google Play.
**What it costs them:** Money on forgotten renewals; time spent rebuilding spend from statements. **[TBD: any numbers?]**
**Emotional tension:** The nagging sense of "I'm probably paying for stuff I don't use," plus unease about giving a finance app bank access. **[inferred]**

## Competitive Landscape
*Checked 2026-10-09 from web sources, not from the Play Store itself. Most sources are the vendors' own sites or roundups written by competitors, and several disagree on price. Confirm each price on its Play listing before quoting it publicly. **[verify]***

**Direct on Android (no bank link):**
- **Tilla:** **[Android]** Android-only, built with Material Design 3, manual entry, calendar view and a home screen widget. A one-time Premium unlock, reported as $2.99 or $4.99 lifetime with 5 free subscriptions. It is the closest match to our shape on Android. It falls short with no scanning or import tools, and no iOS version. [subtracker.io roundup](https://subtracker.io/best/best-subscription-manager-apps-2026), [resubs.app roundup](https://resubs.app/resources/best-subscription-tracker-apps)
- **Subby:** **[Android]** iOS and Android, manual entry with AI extraction on the paid tier. Free with ads, or $2.99 one-time to remove ads (per ReSubs' comparison page). It falls short because the free tier shows ads in a finance app, and nothing in its marketing says the AI extraction runs on the phone. [subby.online guide](https://subby.online/guides/best-subscription-tracker-apps.html)
- **ReSubs:** iOS and Android (its FAQ points to Google Play). CSV import, Gmail receipt pulls, AI extraction from screenshots and billing emails, multi-currency, widgets. Free for 5 subscriptions. Price is unclear: its own pages now say Premium is $10/mo, while the iOS doc recorded $1.99–3.99/mo, $14.99–17.99/yr or $24.99 one-time on 2026-09-20. It falls short because scanning is cloud- and Gmail-based. [ReSubs FAQ](https://resubs.app/faq), [ReSubs vs Subby](https://resubs.app/resources/resubs-vs-subby)
- **Subsly:** Lists Android, manual entry only, no bank access. Little else is known. [subsly.app](https://subsly.app/blog/best-subscription-tracker-apps-2026)
- **SubTracker:** Plus $5.90/mo, Family $9.90/mo; reminders are paywalled. No Android app confirmed.
- **Bobby:** **[Android]** Not on Android; the Android version was abandoned. The privacy-first incumbent on iOS has no presence here. [lowermysubs.com test](https://www.lowermysubs.com/blog/best-subscription-trackers-2026)

**Secondary (bank-linked / budgeting):**
- **Rocket Money:** detects subscriptions by reading linked bank, card and investment accounts. Premium is $6–12/mo. For our buyer it falls short: it needs bank credentials, is US-centric and misses foreign-currency charges. [CNBC Select](https://www.cnbc.com/select/best-subscription-trackers/)
- Budgeting apps (YNAB, Monarch): subscriptions are just transactions, with no renewal-first view and no scanning.

**Indirect:** Spreadsheets, Google Keep notes, and Google Play's Subscriptions screen. They're free, but none has reminders or analytics, and Play only covers Play billing.

**Pricing map (Android) [verify]:**
| App | Free tier | Paid |
|---|---|---|
| Tilla | 5 subscriptions | $2.99–4.99 once |
| Subby | Unlimited, with ads | $2.99 once (removes ads) |
| ReSubs | 5 subscriptions | $10/mo per its site (conflicts with earlier figures) |
| Rocket Money | Limited, bank-linked | $6–12/mo |
| **SmartSubscription (proposed)** | 5 subscriptions, manual entry, reminders, widget | **$2.99 once** |

On Android, one-time prices around $3–5 are the norm among manual trackers. At $2.99 we match Subby and the low end of Tilla's reported price rather than undercut them. The price gap is only against ReSubs' recurring Premium.

## Differentiation
**Key differentiators [Android]:** *(ranked by what the 2026-10-09 competitor check found; confirm)*
- Invoice scanning that runs on the phone: ML Kit text recognition, plus Gemini Nano where the phone has it, and on-device translation for invoices that aren't in English. Tilla has no scanning; ReSubs and Subby extract in the cloud or don't say. This is the one feature none of the Android competitors match.
- Paid vs expected spend: Analytics separates what you actually paid from what's coming. None of the competitor pages describe this.
- An Archive that keeps the lifetime cost of cancelled and paused services.
- No account, no bank link, no ads.

**Not a differentiator on Android:** the home screen widget (Tilla and ReSubs have one), "no bank link" on its own (every direct competitor above says the same), and the $2.99 price (Subby charges the same, Tilla $2.99–4.99).

**Dropped from the iOS list for Android:** the native Mac app, iCloud sync, and a single converted multi-currency total (not ported yet).
**How we do it differently:** You enter or scan subscriptions instead of linking a bank, so your data stays with you.
**Why that's better:** It works for any subscription (cash, foreign card, family-shared), and there's no bank login to leak.
**Why customers choose us:** **[TBD: needs real user feedback]**

## Objections
| Objection | Response |
|-----------|----------|
| "I don't want to enter everything by hand." | Scan the invoice and check what the app filled in. |
| "Won't a bank-linked app find them automatically?" | Only the ones on that card, and only if you hand over your login. This catches everything and shares nothing. |
| "Why pay for a tracker when free ones exist?" | $2.99 once, less than one month of most of the subscriptions it tracks. Manual entry, reminders and the widget are free, with no ads. |
| **[Android]** "Tilla does this already." | Tilla is manual entry only. Here you can scan the invoice, it's read on your phone, and Analytics shows what you actually paid next to what's coming. |
| **[Android]** "Will scanning work on my phone?" | Text recognition works on any supported phone. The smarter field-filling needs Gemini Nano, which only some recent phones have; elsewhere a simpler parser fills what it can and you correct the rest. Say this plainly in the listing. |

**Anti-persona:** People who want automatic bank-transaction import and full budgeting; teams managing shared SaaS spend. **[inferred]**

## Switching Dynamics
**Push:** Surprise renewals; distrust of bank-linking apps. **[inferred]**
**Pull:** **[Android]** Scan-to-add with the reading done on the phone, paid vs expected spend, no ads, and a $2.99 one-time price.
**Habit:** "I sort of know what I pay," or an existing spreadsheet or notes list.
**Anxiety:** Manual upkeep; whether an indie app will be maintained. **[Android]** Losing data when switching phones, since there's no sync or backup yet (CSV export exists).

## Customer Language
**How they describe the problem:** **[TBD: collect from Play Store reviews of competitors, r/androidapps, r/personalfinance]**
- "…"
**How they describe us:** **[TBD]**
- "…"
**Words to use:** subscriptions, renewals, "what it cost you", on your phone, private, scan an invoice, upcoming, still to come, paid vs expected.
**Words to avoid:** "budgeting app", "link your bank", "AI-powered" (the on-device model is how scanning works, not the pitch). **[Android]** Don't say "nothing leaves your phone" or "nothing is sent anywhere": ML Kit sends usage diagnostics (see Proof Points). Say "your invoices are read on your phone" instead.
**Glossary:**
| Term | Meaning |
|------|---------|
| Renewal | One charge of a subscription; recording it builds paid history. |
| Still to come | What renews later this month (Home's headline figure). |
| Paid / expected | Analytics' split between recorded payments and projected charges. |
| Archive | Where paused and cancelled subscriptions live, with their lifetime cost. |
| Upcoming renewals widget | **[Android]** Home screen widget listing what renews in the next 7 days. |
| Unlock | **[Android]** The one-time $2.99 purchase. |
| Category | User-managed grouping (Entertainment, Productivity, Cloud Storage…). |

## Brand Voice
**Tone:** Plain, calm, reassuring. The in-app copy reads like a careful friend. **[inferred from UI strings]**
**Style:** Direct, short sentences, no hype; explains the "why" in one line.
**Personality:** **[Android]** Private, tidy, honest, understated. The iOS doc's "Apple-native" becomes "native Android": Material-aware, but with the app's own navy ink block and Bricolage Grotesque figures.

## Proof Points
**Metrics:** **[TBD: installs, ratings, scans processed]**
**Customers:** N/A (consumer).
**Testimonials:** **[TBD]**
**Value themes:**
| Theme | Proof |
|-------|-------|
| Privacy | No account, no bank link, no ads; OCR and parsing run on the phone (ML Kit, Gemini Nano). **[Android, checked 2026-10-09]** The `INTERNET` permission comes from the ML Kit dependencies. Google's [ML Kit Android data disclosure](https://developers.google.com/ml-kit/android-data-disclosure) says every ML Kit feature collects device and app info, per-installation identifiers, performance metrics, API configuration, input/output sizes and error codes "for diagnostics and usage analytics", sent over HTTPS and not shared with third parties. The GenAI features (`genai-prompt`) also collect "user, device or other identifiers". **[2026-10-12]** Language ID and Translation (`language-id`, `translate`) are ML Kit features too, so the same diagnostics collection covers them, and translation models download from Google's servers. Translation itself runs on the phone. **[verify against the disclosure page before updating the Data safety form]** The page names no opt-out and doesn't say whether image or prompt content is sent. So the honest claim is that invoices are read on the phone, not that nothing leaves it, and the Play Data safety form has to declare this diagnostics collection. |
| Speed of entry | Photo or camera scan leads to a pre-filled review form. |
| Truthful numbers | Paid vs expected in Analytics; the Archive keeps lifetime cost of cancelled services. |
| Fair price | One $2.99 payment, no ads. That is the same as Subby and the low end of Tilla on Android; the real gap is against ReSubs' recurring Premium. |

## Goals
**Business goal:** **[TBD: e.g. Play Store ranking for "subscription tracker", unlock revenue]**
**Conversion action:** Install the app and add the first subscription, ideally by scan; then the $2.99 unlock. **[inferred]**
**Current metrics:** **[TBD: not yet on the Play Store]**
