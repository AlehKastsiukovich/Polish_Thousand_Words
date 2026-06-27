#!/usr/bin/env node

import { mkdir, readFile, writeFile, access } from "node:fs/promises";
import path from "node:path";
import process from "node:process";

const DEFAULT_MODEL = "gemini-3.1-flash-tts-preview";
const DEFAULT_VOICE = "Kore";
const DEFAULT_OUTPUT_DIR = "shared/src/commonMain/composeResources/files/audio";
const DEFAULT_SOURCE_CSV = "/Users/alehkastsiukovich/Work/1000 slow/polish_thousand_b1_ru_content_1000_FINAL_NO_DUPLICATES.csv";
const DEFAULT_LOCAL_PROPERTIES = "local.properties";
const DEFAULT_RETRIES = 5;
const SAMPLE_RATE = 24_000;
const CHANNELS = 1;
const BITS_PER_SAMPLE = 16;

function parseArgs(argv) {
    const args = {
        source: DEFAULT_SOURCE_CSV,
        outDir: DEFAULT_OUTPUT_DIR,
        model: DEFAULT_MODEL,
        fallbackModels: [],
        apiKeyProperty: "gemini.api.key",
        voice: DEFAULT_VOICE,
        retries: DEFAULT_RETRIES,
        start: 1,
        end: Number.POSITIVE_INFINITY,
        types: "words",
        maxJobs: Number.POSITIVE_INFINITY,
        delayMs: 0,
        overwrite: false,
        dryRun: false
    };

    for (let index = 0; index < argv.length; index += 1) {
        const arg = argv[index];
        const next = argv[index + 1];

        switch (arg) {
            case "--source":
                args.source = next;
                index += 1;
                break;
            case "--out-dir":
                args.outDir = next;
                index += 1;
                break;
            case "--model":
                args.model = next;
                index += 1;
                break;
            case "--voice":
                args.voice = next;
                index += 1;
                break;
            case "--api-key-property":
                args.apiKeyProperty = next;
                index += 1;
                break;
            case "--fallback-models":
                args.fallbackModels = next
                    .split(",")
                    .map((value) => value.trim())
                    .filter(Boolean);
                index += 1;
                break;
            case "--retries":
                args.retries = Number(next);
                index += 1;
                break;
            case "--start":
                args.start = Number(next);
                index += 1;
                break;
            case "--end":
                args.end = Number(next);
                index += 1;
                break;
            case "--types":
                args.types = next;
                index += 1;
                break;
            case "--max-jobs":
                args.maxJobs = Number(next);
                index += 1;
                break;
            case "--delay-ms":
                args.delayMs = Number(next);
                index += 1;
                break;
            case "--overwrite":
                args.overwrite = true;
                break;
            case "--dry-run":
                args.dryRun = true;
                break;
            case "--help":
                printHelp();
                process.exit(0);
                break;
            default:
                throw new Error(`Unknown argument: ${arg}`);
        }
    }

    if (!["all", "words", "examples"].includes(args.types)) {
        throw new Error("--types must be one of: all, words, examples");
    }

    if (!Number.isFinite(args.start) || !Number.isFinite(args.end)) {
        throw new Error("--start and --end must be numbers");
    }

    if (!Number.isFinite(args.maxJobs) || args.maxJobs <= 0) {
        throw new Error("--max-jobs must be a positive number");
    }

    if (!Number.isFinite(args.delayMs) || args.delayMs < 0) {
        throw new Error("--delay-ms must be zero or a positive number");
    }

    return args;
}

function printHelp() {
    console.log(`Usage:
  node tools/generate_gemini_tts_audio.mjs [options]

Options:
  --source <path>      CSV source file
  --out-dir <path>     Output audio directory
  --model <id>         Gemini TTS model
  --fallback-models    Comma-separated fallback TTS models
  --api-key-property   local.properties key name, e.g. gemini.api.key2
  --voice <name>       Gemini prebuilt voice
  --retries <count>    Retry count for 429/503
  --start <rank>       Start rank (inclusive)
  --end <rank>         End rank (inclusive)
  --types <mode>       all | words | examples
  --max-jobs <count>   Process only the next missing jobs
  --delay-ms <ms>      Wait between generated jobs to stay under RPM
  --overwrite          Regenerate existing files
  --dry-run            Print planned files only
  --help               Show help

Environment:
  GEMINI_API_KEY       Required Gemini API key
`);
}

function parseCsv(text) {
    const rows = [];
    let current = "";
    let row = [];
    let insideQuotes = false;

    for (let index = 0; index < text.length; index += 1) {
        const char = text[index];
        const next = text[index + 1];

        if (char === "\"") {
            if (insideQuotes && next === "\"") {
                current += "\"";
                index += 1;
            } else {
                insideQuotes = !insideQuotes;
            }
            continue;
        }

        if (char === "," && !insideQuotes) {
            row.push(current);
            current = "";
            continue;
        }

        if ((char === "\n" || char === "\r") && !insideQuotes) {
            if (char === "\r" && next === "\n") index += 1;
            row.push(current);
            current = "";
            if (row.some((value) => value.length > 0)) rows.push(row);
            row = [];
            continue;
        }

        current += char;
    }

    if (current.length > 0 || row.length > 0) {
        row.push(current);
        rows.push(row);
    }

    if (rows.length === 0) return [];

    const [header, ...body] = rows;
    return body.map((values) =>
        Object.fromEntries(header.map((key, index) => [key, values[index] ?? ""]))
    );
}

function normalizeSourceRow(row) {
    const rank = Number(row.rank);
    const id = Number(row.id);

    if (Number.isFinite(rank) || !Number.isFinite(id)) {
        return row;
    }

    // Some tail rows in the source CSV omit the dedicated rank column and shift
    // the remaining values left by one position. Recover them here instead of
    // editing the upstream file manually.
    return {
        ...row,
        rank: String(id),
        polish: row.rank,
        russian: row.polish,
        part_of_speech: row.russian
    };
}

function parseProperties(text) {
    const result = {};
    for (const line of text.split(/\r?\n/)) {
        const trimmed = line.trim();
        if (!trimmed || trimmed.startsWith("#") || trimmed.startsWith("!")) continue;
        const separatorIndex = trimmed.search(/[:=]/);
        if (separatorIndex < 0) continue;
        const key = trimmed.slice(0, separatorIndex).trim();
        const value = trimmed.slice(separatorIndex + 1).trim();
        result[key] = value.replace(/\\:/g, ":").replace(/\\=/g, "=");
    }
    return result;
}

function normalizeFileStem(value) {
    const polishMap = new Map([
        ["ą", "a"],
        ["ć", "c"],
        ["ę", "e"],
        ["ł", "l"],
        ["ń", "n"],
        ["ó", "o"],
        ["ś", "s"],
        ["ż", "z"],
        ["ź", "z"],
        ["Ą", "a"],
        ["Ć", "c"],
        ["Ę", "e"],
        ["Ł", "l"],
        ["Ń", "n"],
        ["Ó", "o"],
        ["Ś", "s"],
        ["Ż", "z"],
        ["Ź", "z"]
    ]);

    return value
        .split("")
        .map((char) => polishMap.get(char) ?? char)
        .join("")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .replace(/[^a-zA-Z0-9]+/g, "-")
        .replace(/^-+|-+$/g, "")
        .toLowerCase();
}

function buildWordPrompt(word) {
    return [
        "Generate speech audio for the exact Polish transcript below.",
        "Return audio only.",
        "Do not add explanations, labels, or extra words.",
        `Transcript: ${word}`
    ].join("\n");
}

function buildExamplePrompt(sentence) {
    return [
        "Generate speech audio for the exact Polish transcript below.",
        "Return audio only.",
        "Use clear standard Polish pronunciation.",
        "Do not add explanations, labels, or extra words.",
        `Transcript: ${sentence}`
    ].join("\n");
}

async function generatePcmAudio({ apiKey, model, voice, prompt, retries = DEFAULT_RETRIES }) {
    let attempt = 0;

    while (true) {
        attempt += 1;
        const response = await fetchTts({
            apiKey,
            model,
            voice,
            prompt,
            retries
        });
        const json = await response.json();
        const encoded = json?.candidates?.[0]?.content?.parts?.[0]?.inlineData?.data;

        if (encoded) {
            return Buffer.from(encoded, "base64");
        }

        if (attempt > retries) {
            throw new Error(`Gemini TTS response did not contain audio data: ${JSON.stringify(json)}`);
        }

        const delayMs = Math.min(2000 * (2 ** (attempt - 1)), 15000);
        console.log(`retry ${attempt}/${retries} after ${delayMs}ms due to empty audio response`);
        await sleep(delayMs);
    }
}

async function fetchTts({ apiKey, model, voice, prompt, retries = DEFAULT_RETRIES }) {
    let attempt = 0;

    while (true) {
        attempt += 1;
        const response = await fetch(
            `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "x-goog-api-key": apiKey
                },
                body: JSON.stringify({
                    contents: [
                        {
                            parts: [{ text: prompt }]
                        }
                    ],
                    generationConfig: {
                        responseModalities: ["AUDIO"],
                        speechConfig: {
                            voiceConfig: {
                                prebuiltVoiceConfig: {
                                    voiceName: voice
                                }
                            }
                        }
                    }
                })
            }
        );

        if (response.ok) return response;

        const body = await response.text();
        const retryable = response.status === 429 || response.status === 503;
        if (!retryable || attempt > retries) {
            throw new Error(`Gemini TTS request failed: ${response.status} ${body}`);
        }

        const delayMs = Math.min(2000 * (2 ** (attempt - 1)), 15000);
        console.log(`retry ${attempt}/${retries} after ${delayMs}ms due to ${response.status}`);
        await sleep(delayMs);
    }
}

function sleep(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}

function pcmToWavBuffer(pcmBuffer) {
    const header = Buffer.alloc(44);
    const byteRate = SAMPLE_RATE * CHANNELS * (BITS_PER_SAMPLE / 8);
    const blockAlign = CHANNELS * (BITS_PER_SAMPLE / 8);

    header.write("RIFF", 0);
    header.writeUInt32LE(36 + pcmBuffer.length, 4);
    header.write("WAVE", 8);
    header.write("fmt ", 12);
    header.writeUInt32LE(16, 16);
    header.writeUInt16LE(1, 20);
    header.writeUInt16LE(CHANNELS, 22);
    header.writeUInt32LE(SAMPLE_RATE, 24);
    header.writeUInt32LE(byteRate, 28);
    header.writeUInt16LE(blockAlign, 32);
    header.writeUInt16LE(BITS_PER_SAMPLE, 34);
    header.write("data", 36);
    header.writeUInt32LE(pcmBuffer.length, 40);

    return Buffer.concat([header, pcmBuffer]);
}

async function fileExists(filePath) {
    try {
        await access(filePath);
        return true;
    } catch {
        return false;
    }
}

async function ensureDirectory(dirPath) {
    await mkdir(dirPath, { recursive: true });
}

async function resolveApiKey(propertyName) {
    if (process.env.GEMINI_API_KEY && propertyName === "gemini.api.key") {
        return process.env.GEMINI_API_KEY;
    }

    try {
        const localPropertiesText = await readFile(
            path.resolve(process.cwd(), DEFAULT_LOCAL_PROPERTIES),
            "utf8"
        );
        const properties = parseProperties(localPropertiesText);
        if (properties[propertyName]) return properties[propertyName];
        if (propertyName === "gemini.api.key") {
            return properties.geminiApiKey ?? null;
        }
        return null;
    } catch {
        return null;
    }
}

function buildJobs(rows, args) {
    const filtered = rows.filter((row) => {
        const rank = Number(row.rank);
        return rank >= args.start && rank <= args.end;
    });

    const jobs = [];

    for (const row of filtered) {
        const rank = Number(row.rank);
        const rankLabel = String(rank).padStart(4, "0");
        const stem = normalizeFileStem(row.polish);

        if (args.types === "all" || args.types === "words") {
            jobs.push({
                kind: "word",
                id: row.id,
                rank,
                text: row.polish,
                output: path.join("words", `${rankLabel}-${stem}.wav`),
                prompt: buildWordPrompt(row.polish)
            });
        }

        if (args.types === "all" || args.types === "examples") {
            for (const index of [1, 2]) {
                const example = row[`example_polish_${index}`]?.trim();
                if (!example) continue;
                jobs.push({
                    kind: "example",
                    id: row.id,
                    rank,
                    exampleIndex: index,
                    text: example,
                    output: path.join("examples", `${rankLabel}-${stem}-${index}.wav`),
                    prompt: buildExamplePrompt(example)
                });
            }
        }
    }

    return jobs;
}

function buildManifestEntries(rows, args) {
    return rows.map((row) => {
        const rankLabel = String(Number(row.rank)).padStart(4, "0");
        const stem = normalizeFileStem(row.polish);
        const includeWordAudio = args.types === "all" || args.types === "words";
        const includeExamples = args.types === "all" || args.types === "examples";

        return {
            id: row.id,
            rank: Number(row.rank),
            polish: row.polish,
            russian: row.russian,
            wordAudio: includeWordAudio
                ? path.join("words", `${rankLabel}-${stem}.wav`)
                : null,
            examples: includeExamples
                ? [1, 2]
                    .map((index) => row[`example_polish_${index}`]?.trim() ? {
                        index,
                        polish: row[`example_polish_${index}`],
                        russian: row[`example_russian_${index}`],
                        audio: path.join("examples", `${rankLabel}-${stem}-${index}.wav`)
                    } : null)
                    .filter(Boolean)
                : []
        };
    });
}

async function loadExistingManifest(manifestPath) {
    try {
        const text = await readFile(manifestPath, "utf8");
        const parsed = JSON.parse(text);
        return Array.isArray(parsed.items) ? parsed : null;
    } catch {
        return null;
    }
}

function mergeManifestItems(existingItems, nextItems, types) {
    const byId = new Map(existingItems.map((item) => [item.id, item]));
    const forceWords = types === "all" || types === "words";
    const forceExamples = types === "all" || types === "examples";

    for (const item of nextItems) {
        const existing = byId.get(item.id);
        if (!existing) {
            byId.set(item.id, item);
            continue;
        }

        byId.set(item.id, {
            ...existing,
            ...item,
            wordAudio: forceWords ? item.wordAudio : (existing.wordAudio ?? null),
            examples: forceExamples ? item.examples : []
        });
    }

    return Array.from(byId.values()).sort((left, right) => left.rank - right.rank);
}

function buildManifest(args, items) {
    return {
        schemaVersion: 1,
        generatedAt: new Date().toISOString(),
        sourceCsv: args.source,
        model: [args.model, ...args.fallbackModels].join(", "),
        voice: args.voice,
        format: {
            container: "wav",
            sampleRateHz: SAMPLE_RATE,
            channels: CHANNELS,
            bitsPerSample: BITS_PER_SAMPLE
        },
        items
    };
}

async function main() {
    const args = parseArgs(process.argv.slice(2));
    const apiKey = await resolveApiKey(args.apiKeyProperty);

    if (!args.dryRun && !apiKey) {
        throw new Error(`Gemini API key is required. Set ${args.apiKeyProperty} in local.properties or GEMINI_API_KEY for the default key.`);
    }

    const sourceText = await readFile(args.source, "utf8");
    const rows = parseCsv(sourceText).map(normalizeSourceRow);
    const selectedRows = rows.filter((row) => {
        const rank = Number(row.rank);
        return rank >= args.start && rank <= args.end;
    });
    const jobs = buildJobs(rows, args);
    const outputRoot = path.resolve(process.cwd(), args.outDir);
    const manifestPath = path.join(outputRoot, "manifest.json");

    if (args.dryRun) {
        console.log(JSON.stringify({
            outputRoot,
            selectedItems: selectedRows.length,
            jobs: jobs.slice(0, 10),
            totalJobs: jobs.length
        }, null, 2));
        return;
    }

    await ensureDirectory(path.join(outputRoot, "words"));
    await ensureDirectory(path.join(outputRoot, "examples"));

    const failures = [];
    const models = [args.model, ...args.fallbackModels];
    const pendingJobs = [];

    for (const job of jobs) {
        const targetPath = path.join(outputRoot, job.output);
        const exists = await fileExists(targetPath);
        if (exists && !args.overwrite) continue;
        pendingJobs.push(job);
        if (pendingJobs.length >= args.maxJobs) break;
    }

    for (const job of pendingJobs) {
        const targetPath = path.join(outputRoot, job.output);
        let generated = false;
        let lastError = null;

        for (const model of models) {
            try {
                console.log(`generate ${job.output} via ${model}`);
                const pcm = await generatePcmAudio({
                    apiKey,
                    model,
                    voice: args.voice,
                    prompt: job.prompt,
                    retries: args.retries
                });
                const wav = pcmToWavBuffer(pcm);
                await writeFile(targetPath, wav);
                generated = true;
                break;
            } catch (error) {
                lastError = error;
                console.log(`model failed for ${job.output}: ${model}`);
            }
        }

        if (!generated) {
            failures.push({
                output: job.output,
                error: lastError?.message ?? "Unknown error"
            });
        }

        if (args.delayMs > 0) {
            await sleep(args.delayMs);
        }
    }

    const existingManifest = await loadExistingManifest(manifestPath);
    const nextManifestItems = buildManifestEntries(selectedRows, args);
    const mergedItems = mergeManifestItems(
        existingManifest?.items ?? [],
        nextManifestItems,
        args.types
    );
    const manifest = buildManifest(args, mergedItems);

    await writeFile(
        manifestPath,
        `${JSON.stringify(manifest, null, 2)}\n`
    );

    console.log(`done ${selectedRows.length} items, ${pendingJobs.length} audio files`);

    if (failures.length > 0) {
        console.error(`failed ${failures.length} jobs`);
        for (const failure of failures.slice(0, 20)) {
            console.error(`${failure.output}: ${failure.error}`);
        }
        process.exit(1);
    }
}

main().catch((error) => {
    console.error(error.message);
    process.exit(1);
});
