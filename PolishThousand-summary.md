# Mów 1000 Summary

## Product Promise

`Mów 1000` helps Russian- and Ukrainian-speaking adults learn the 1,000 most useful Polish words and short phrases needed for confident everyday communication and a practical B1 vocabulary base.

The product is not a traditional language course and is not organized around beginner topics. It is one clear path from `0` to `1,000`.

## Core Experience

- One support language selected during onboarding: Russian or Ukrainian.
- One linear sequence of short lessons with 10 learning units each.
- Every unit contains a Polish word or phrase, one translation, and one short Polish example with translation.
- One primary action per screen.
- The main screen always answers three questions: how much is complete, what is the next milestone, and what starts next.
- Categories may exist only as internal content tags for balance and analytics. They are not part of the main navigation.

## Localization Research

Research snapshot from July 16, 2026:

- Around 1.2 million foreign nationals were registered in Poland's social insurance system in mid-2025. Ukrainians and Belarusians were the two largest groups.
- Employment among citizens of Colombia, the Philippines, and India was growing particularly quickly in 2024.
- Sources: [ZUS](https://www.zus.pl/-/zus-fundusz-ubezpiecze%C5%84-spo%C5%82ecznych-w-znakomitej-formie-1), [Statistics Poland](https://stat.gov.pl/files/gfx/portalinformacyjny/pl/defaultaktualnosci/6329/15/25/1/cudzoziemcy_wykonujacy_prace_w_polsce_w_grudniu_w_2024_r..pdf), and [2025 migration overview](https://zielonalinia.gov.pl/migracje-w-polsce/).
- The preliminary localization priority after Russian and Ukrainian is: English, Spanish, Georgian, Belarusian, Vietnamese, and Romanian.
- This priority is a potential-reach hypothesis, not validated product demand. It must be checked against store-search data, conversion data, and interviews before all six localizations are commissioned.
- English has especially high leverage as a bridge language for users from India, the Philippines, and the wider international audience.
- Polish at B1 level is relevant to long-term integration because proof of at least B1 proficiency is required for a long-term EU resident permit and can be used in the procedure for recognition as a Polish citizen. Source: [Office for Foreigners](https://www.gov.pl/web/udsc/zezwolenie-na-pobyt-rezydenta-dlugoterminowego-ue2).

The language picker must therefore scale beyond Russian and Ukrainian without showing every available language as a permanent full-width option on the settings screen.

## Milestones

- `100` - Warm-up / `Розігрів` / `Разогрев`
- `250` - Base / `База`
- `500` - Confidence / `Впевненість` / `Уверенность`
- `750` - Good pace / `Добрий темп` / `Хороший темп`
- `1,000` - B1 ready / `B1 готовий` / `B1 готов`

Milestones must feel visible and attainable. The interface shows both total progress to 1,000 and progress to the nearest milestone.

## MVP Content

The technical MVP starts with 30-60 reviewed learning units and proves the complete learning loop before the full list is produced.

Content rules:

- Prefer high-frequency, high-utility B1 vocabulary and natural short phrases.
- Avoid obvious beginner filler such as basic greetings unless corpus validation proves that it belongs in the final list.
- Mix connectors, common verbs, practical actions, nouns, adjectives, and conversational patterns.
- Validate the final ordering against Polish frequency and spoken-language corpora before claiming an exact frequency rank.

## Exercises

The MVP uses only a small set of exercise types:

- choose the translation
- listen and choose when audio is available
- understand the word or phrase in context

## UX Principles

- No account required in the MVP.
- Offline-first.
- Five-to-ten-minute sessions.
- No topic browser in the core flow.
- No redundant introductory screens or explanatory blocks.
- Short, supportive messages may appear after lessons, but they must not interrupt learning.
- Progress is calculated from completed learning units, never from placeholder values.

## Monetization

Prefer a one-time unlock for the first commercial version.

- Free: the first milestone of 100 words.
- Paid: unlock the complete path to 1,000 words.

The paywall appears only after the user has experienced enough value to reach the first milestone.

## Technical Direction

- Kotlin Multiplatform and Compose Multiplatform for Android and iOS.
- Static reviewed content bundled with the app for the MVP.
- Audio files stored separately in app resources.
- Progress stored locally.
- No backend required for the MVP.

## Immediate Goal

Ship a small, understandable, paid-capable MVP by June 21, 2026. Validate whether users understand the 1,000-word promise, complete short lessons, notice their progress, and consider paying to unlock the full path.

## MVP Cut Line

Must be complete before calling the MVP usable:

- First launch: splash, app icon, one-time support-language selection.
- Main screen: total progress, next milestone, optional due review, next lesson, one main CTA.
- Lesson loop: learn cards with 2-3 examples, one focused practice step, completion result.
- Progress logic: count only correctly answered words as learned; missed words go to optional quick review.
- Review logic: due review appears later as optional reinforcement, not as a forced blocker before new words.
- Persistence: support language, learned words, completed lessons, review schedule, premium state.
- Monetization placeholder: free first 100 learned words, then soft paywall for the full 1,000-word path.
- Android and iOS builds must compile; Android runtime flow should be smoke-tested on a device/emulator before release.

Explicitly out of MVP unless needed for store submission:

- Account system.
- Backend.
- Topic browser.
- Full audio pipeline.
- Full 1,000-word content authoring before the external word file is provided.
