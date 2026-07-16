# Audio Generation

This project can generate Polish lesson audio with Gemini TTS using the same model you test in Google AI Studio:

- `gemini-3.1-flash-tts-preview`

The generator script reads the CSV source, creates one `wav` file for each Polish word, and writes a manifest for app integration.

Current default:

- generate only the Polish word
- do not generate example sentence audio unless explicitly requested

## Source

Default source CSV:

- `polish_thousand_b1_ru_content_1000_FINAL_NO_DUPLICATES_uk.csv`

Expected columns:

- `id`
- `rank`
- `polish`
- `russian`
- `example_polish_1`
- `example_russian_1`
- `example_polish_2`
- `example_russian_2`

## Output

Default output directory:

- `shared/src/commonMain/composeResources/files/audio`

Generated structure:

- `shared/src/commonMain/composeResources/files/audio/words/*.wav`
- `shared/src/commonMain/composeResources/files/audio/examples/*.wav`
- `shared/src/commonMain/composeResources/files/audio/manifest.json`

The generated audio format is:

- `wav`
- `24 kHz`
- `mono`
- `16-bit PCM`

## Authentication

Export your Gemini API key before running:

```bash
export GEMINI_API_KEY='your-key-here'
```

Or store it in `local.properties`:

```properties
gemini.api.key=your-key-here
```

The key should be the same Google AI Studio / Gemini API key for the project you are using in AI Studio.

## Commands

Preview what will be generated:

```bash
node tools/generate_gemini_tts_audio.mjs --dry-run --start 1 --end 3
```

Generate the first 10 words:

```bash
node tools/generate_gemini_tts_audio.mjs --start 1 --end 10
```

Generate words only:

```bash
node tools/generate_gemini_tts_audio.mjs --start 1 --end 100 --types words
```

Generate examples only:

```bash
node tools/generate_gemini_tts_audio.mjs --start 1 --end 100 --types examples
```

Overwrite existing files:

```bash
node tools/generate_gemini_tts_audio.mjs --start 1 --end 10 --overwrite
```

Use another voice:

```bash
node tools/generate_gemini_tts_audio.mjs --voice Puck --start 1 --end 10
```

## Notes

- The script sends one request per word or example sentence.
- By default it generates only word audio for the Polish text.
- Example sentence audio is optional and must be requested with `--types examples` or `--types all`.
- It writes a manifest that can later be consumed by shared app code.
- AI Studio is useful for trying voices manually, but bulk generation for the app is better through the API script.
