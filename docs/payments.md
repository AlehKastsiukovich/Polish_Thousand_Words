# Payments and Monetization

## Decision

Use one purchase:

- Product id: `polish_1000_full_unlock`
- RevenueCat entitlement id: `full_unlock`
- Type: non-consumable / one-time product
- Price: start with `USD 4.99`, localized by App Store / Google Play price tiers
- Free gate: first 100 words are free
- Paid unlock: words 101-1000, all future review for unlocked words, and all future improvements inside the Mów 1000 product
- No account system in v1

This fits the product better than a subscription because Mów 1000 is a finite learning pack. The user understands the value quickly after the first 100 words, and a one-time purchase feels lower-risk than a recurring charge.

## Why not subscription first

- The app does not yet provide an ongoing service, teacher, cloud sync, or regularly expanding content library.
- Subscription adds cancellation anxiety and increases store review/support complexity.
- For the current promise, "pay once, unlock all 1000 words" is simpler and more trustworthy.

Add subscription only if the product becomes a continuously updated learning system: B2/C1 packs, cloud sync, personal review analytics, new audio packs, exams, or weekly content.

## Pricing

Start with `USD 4.99`.

Reasoning:

- `USD 2.99` is too low for paid acquisition and makes the product feel disposable.
- `USD 9.99` is possible later, but risky before we have reviews, retention, and conversion data.
- `USD 4.99` is a clean impulse price for a focused language utility.

After release, review:

- paywall view to purchase conversion
- lesson 1 completion rate
- 100-word completion rate
- refund rate
- store conversion by country

If 100-word users convert strongly and refunds stay low, test `USD 5.99` or `USD 6.99`.

## Store model

Apple:

- Create a non-consumable In-App Purchase in App Store Connect.
- Use product id `polish_1000_full_unlock`.
- App Store restores purchase by Apple ID.

Google:

- Create a one-time product in Play Console.
- Use product id `polish_1000_full_unlock`.
- It should be non-consumable: permanent account entitlement.

## Current implementation state

The app now has a shared `PaymentClient` contract and platform adapters:

- `shared/src/commonMain/kotlin/com/polish/thousand/payments/PaymentClient.kt`
- `shared/src/androidMain/kotlin/com/polish/thousand/payments/PlatformPaymentClient.android.kt`
- `shared/src/iosMain/kotlin/com/polish/thousand/payments/PlatformPaymentClient.ios.kt`

Because App Store Connect and Play Console products do not exist yet, both adapters intentionally return `PaymentResult.Unavailable`. This prevents accidentally shipping a fake local unlock.

The paywall UI is wired to:

- load product price
- sync an already active full-unlock entitlement on app start
- purchase full unlock
- restore purchases
- show store-not-configured state
- keep the user able to continue the free path

## Recommended implementation after store accounts exist

### Fastest reliable route: RevenueCat

Use RevenueCat if we want one entitlement across iOS and Android without building receipt validation backend now.

Library:

- Gradle dependency: `com.revenuecat.purchases:purchases-kmp-core`
- Android requires `Purchases.configure(apiKey, appUserID?)`.
- iOS requires `Purchases.configureWithAPIKey(apiKey, appUserID?)`; Kotlin/Native setup may require `@OptIn(ExperimentalForeignApi::class)`.
- Android `MainActivity` launch mode must remain `standard` or `singleTop` for purchase result delivery.

Recommended entitlement:

- Entitlement id: `full_unlock`
- Product id: `polish_1000_full_unlock`

Expected app behavior:

1. Load offerings.
2. Show localized store price on the paywall.
3. Purchase package.
4. If entitlement `full_unlock` is active, save `hasPremium = true`.
5. On app start and settings restore, sync purchaser info.
6. Map active entitlement to `PaymentClient.isFullUnlockActive()`.

This is the most pragmatic path until we have our own backend.

### Native route

Android:

- Use the current supported Google Play Billing Library version from Android Developers docs.
- Query product details for `polish_1000_full_unlock`.
- Launch purchase flow.
- Acknowledge purchase.
- Verify entitlement locally at minimum; backend verification is safer before scaling.

iOS:

- Use StoreKit 2.
- Query product `polish_1000_full_unlock`.
- Purchase and verify transaction.
- Finish transaction.
- Restore from current entitlements.

Native integrations are fine, but more duplicated work across Android and iOS.

## Required account setup

Before real purchases can work:

1. Enroll in Apple Developer Program. Expect an annual Apple Developer Program membership fee.
2. Create app record in App Store Connect.
3. Sign Paid Applications Agreement, add tax and banking details.
4. Create non-consumable IAP `polish_1000_full_unlock`.
5. Create sandbox testers.
6. Create Google Play Developer account. Expect a one-time Google Play Console registration fee.
7. Create app in Play Console.
8. Set up merchant/payment profile if required.
9. Create one-time product `polish_1000_full_unlock`.
10. Add license testers.

## Paywall flow

1. User learns first 100 words for free.
2. On next paid lesson attempt, show paywall.
3. Primary CTA: "Open full course · localized price".
4. Secondary action: "Restore purchase".
5. Tertiary action: continue free, which keeps review and already unlocked content available.
6. If purchase succeeds, unlock the next lesson immediately.
7. If restore succeeds, unlock immediately.
8. If store is unavailable, show a short explanation and keep free path usable.

Important: "continue free" must not open lessons after word 100. It may return the user to Home, review due words, or let them repeat already unlocked lessons.

## Release acceptance checklist

Do not submit store builds until all items are true:

- The paywall price comes from the store product, not from `RecommendedFullUnlockPrice`.
- `PaymentClient.purchaseFullUnlock()` grants premium only after verified store success.
- `PaymentClient.restoreFullUnlock()` grants premium only after active entitlement or active non-consumable purchase is verified.
- `PaymentClient.isFullUnlockActive()` syncs existing purchases on cold app start.
- Android purchase is acknowledged if native Google Play Billing is used.
- iOS StoreKit transactions are verified and finished if native StoreKit is used.
- Restore purchases is available from Settings.
- The free path still works when purchases are unavailable.
- A free user cannot start word 101+ by closing or skipping the paywall.
- Sandbox/license tester purchase and restore have been checked on a real iOS device and Android device/emulator.
- App Store / Play Console metadata clearly says the first 100 words are free and the full 1000-word path is a one-time purchase.

## Sources

- Apple App Store Connect: [In-App Purchase types](https://developer.apple.com/help/app-store-connect/reference/in-app-purchase-types/), non-consumable products are purchased once and do not expire.
- Android Developers: [One-time products](https://developer.android.com/google/play/billing/one-time-products), non-consumable products are purchased once and provide a permanent benefit.
- Android Developers: [Google Play Billing Library](https://developer.android.com/google/play/billing), the app-side integration for Play purchases and localized product offers.
- RevenueCat: [Kotlin Multiplatform SDK installation](https://www.revenuecat.com/docs/getting-started/installation/kotlin-multiplatform).
