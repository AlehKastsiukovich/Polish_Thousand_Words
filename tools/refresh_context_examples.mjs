#!/usr/bin/env node

import { readFile, writeFile } from "node:fs/promises";
import path from "node:path";
import process from "node:process";

const DEFAULT_SOURCE = "polish_thousand_b1_ru_content_1000_FINAL_NO_DUPLICATES_uk.csv";
const DEFAULT_MODEL = "gemini-2.5-flash";
const DEFAULT_BATCH_SIZE = 10;
const EXAMPLE_FIELDS = [5, 6, 7, 8, 10, 11];
const TEMPLATE_MARKERS = ["Pojęcie", "Warto umieć", "Warto dobrze", "To wyrażenie często", "To słowo pojawia się często"];
const TEMPLATE_EXACT_SENTENCES = new Set([
    "To przydatne słowo w pracy, rozmowach i oficjalnych tekstach."
]);

function parseArgs(argv) {
    const args = {
        source: DEFAULT_SOURCE,
        model: DEFAULT_MODEL,
        apiKeyProperty: "gemini.api.key",
        batchSize: DEFAULT_BATCH_SIZE,
        ranks: null,
        templateOnly: false,
        checkpoint: false,
        dryRun: false
    };

    for (let index = 0; index < argv.length; index += 1) {
        const arg = argv[index];
        const value = argv[index + 1];
        switch (arg) {
            case "--source":
                args.source = value;
                index += 1;
                break;
            case "--model":
                args.model = value;
                index += 1;
                break;
            case "--api-key-property":
                args.apiKeyProperty = value;
                index += 1;
                break;
            case "--batch-size":
                args.batchSize = Number(value);
                index += 1;
                break;
            case "--ranks":
                args.ranks = new Set(value.split(",").map(Number));
                index += 1;
                break;
            case "--template-only":
                args.templateOnly = true;
                break;
            case "--checkpoint":
                args.checkpoint = true;
                break;
            case "--dry-run":
                args.dryRun = true;
                break;
            case "--help":
                console.log("Usage: node tools/refresh_context_examples.mjs (--ranks 341,343 | --template-only) [--batch-size 10] [--checkpoint] [--dry-run]");
                process.exit(0);
                break;
            default:
                throw new Error(`Unknown argument: ${arg}`);
        }
    }

    if (!args.ranks?.size && !args.templateOnly) throw new Error("--ranks or --template-only is required");
    if (!Number.isInteger(args.batchSize) || args.batchSize <= 0) throw new Error("--batch-size must be a positive integer");
    return args;
}

function parseCsvLine(line) {
    const values = [];
    let value = "";
    let quoted = false;

    for (let index = 0; index < line.length; index += 1) {
        const char = line[index];
        if (char === '"') {
            if (quoted && line[index + 1] === '"') {
                value += '"';
                index += 1;
            } else {
                quoted = !quoted;
            }
        } else if (char === "," && !quoted) {
            values.push(value);
            value = "";
        } else {
            value += char;
        }
    }
    values.push(value);
    return values;
}

function encodeCsvLine(values) {
    return values.map((value) => {
        const text = String(value ?? "");
        return /[",\n]/.test(text) ? `"${text.replaceAll('"', '""')}"` : text;
    }).join(",");
}

function containsTemplate(row) {
    const polishExamples = [row[5], row[7]];
    return polishExamples.some((example) =>
        TEMPLATE_MARKERS.some((marker) => example.startsWith(marker)) ||
        example.startsWith("Słowo „") ||
        example.startsWith("Wyrażenie „") ||
        example.startsWith("Warto znać „") ||
        TEMPLATE_EXACT_SENTENCES.has(example)
    );
}

function applyExamples(lines, generated) {
    return lines.map((line, index) => {
        if (index === 0 || !line) return line;
        const row = parseCsvLine(line);
        const example = generated.get(Number(row[1]));
        if (!example) return line;
        for (const [fieldIndex, field] of EXAMPLE_FIELDS.map((fieldIndex, position) => [fieldIndex, ["example_polish_1", "example_russian_1", "example_polish_2", "example_russian_2", "example_ukrainian_1", "example_ukrainian_2"][position]])) {
            row[fieldIndex] = example[field];
        }
        return encodeCsvLine(row);
    });
}

function parseProperties(text) {
    return Object.fromEntries(text.split(/\r?\n/)
        .map((line) => line.trim())
        .filter((line) => line && !line.startsWith("#"))
        .map((line) => {
            const separator = line.indexOf("=");
            return separator < 0 ? [line, ""] : [line.slice(0, separator).trim(), line.slice(separator + 1).trim()];
        }));
}

async function resolveApiKey(property) {
    if (process.env.GEMINI_API_KEY && property === "gemini.api.key") return process.env.GEMINI_API_KEY;
    const properties = parseProperties(await readFile("local.properties", "utf8"));
    return properties[property] ?? null;
}

function chunk(values, size) {
    return Array.from({ length: Math.ceil(values.length / size) }, (_, index) => values.slice(index * size, (index + 1) * size));
}

function buildPrompt(items) {
    return [
        "You are editing a Polish B1 vocabulary course for adult migrants in Poland.",
        "For every item, create exactly two distinct, short, natural Polish example sentences in practical contexts (work, administration, housing, health, transport, everyday life).",
        "Each Polish sentence must use the target word or an appropriate inflected form of it. Do not use generic dictionary commentary or mention the word as a concept.",
        "Provide precise, natural Russian and Ukrainian translations of each sentence.",
        "Return valid JSON only: an array of objects with rank, example_polish_1, example_russian_1, example_polish_2, example_russian_2, example_ukrainian_1, example_ukrainian_2.",
        "Items:",
        JSON.stringify(items)
    ].join("\n");
}

async function requestExamples(apiKey, model, items) {
    const response = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent`, {
        method: "POST",
        headers: { "Content-Type": "application/json", "x-goog-api-key": apiKey },
        body: JSON.stringify({
            contents: [{ parts: [{ text: buildPrompt(items) }] }],
            generationConfig: {
                responseMimeType: "application/json",
                temperature: 0.35
            }
        })
    });

    if (!response.ok) throw new Error(`Gemini request failed: ${response.status} ${await response.text()}`);
    const payload = await response.json();
    const text = payload?.candidates?.[0]?.content?.parts?.[0]?.text;
    if (!text) throw new Error("Gemini response did not contain text");
    const result = JSON.parse(text);
    if (!Array.isArray(result)) throw new Error("Gemini response is not an array");
    return result;
}

function validateBatch(items, examples) {
    const expected = new Set(items.map((item) => item.rank));
    const fields = ["example_polish_1", "example_russian_1", "example_polish_2", "example_russian_2", "example_ukrainian_1", "example_ukrainian_2"];
    const result = new Map();

    for (const example of examples) {
        if (!expected.has(example.rank) || result.has(example.rank)) throw new Error(`Unexpected or duplicated rank in response: ${example.rank}`);
        for (const field of fields) {
            if (typeof example[field] !== "string" || !example[field].trim()) throw new Error(`Missing ${field} for rank ${example.rank}`);
        }
        result.set(example.rank, example);
    }

    if (result.size !== expected.size) throw new Error("Gemini response did not cover every requested rank");
    return result;
}

async function main() {
    const args = parseArgs(process.argv.slice(2));
    const apiKey = await resolveApiKey(args.apiKeyProperty);
    if (!apiKey) throw new Error(`Gemini API key missing: ${args.apiKeyProperty}`);

    const sourcePath = path.resolve(args.source);
    const source = await readFile(sourcePath, "utf8");
    const lines = source.split(/\r?\n/);
    const selected = [];

    for (const line of lines.slice(1)) {
        if (!line) continue;
        const row = parseCsvLine(line);
        const rank = Number(row[1]);
        if (row.length !== 12 || (args.ranks && !args.ranks.has(rank)) || (args.templateOnly && !containsTemplate(row))) continue;
        selected.push({ rank: Number(row[1]), polish: row[2], russian: row[3], ukrainian: row[9], part_of_speech: row[4] });
    }

    if (args.ranks && selected.length !== args.ranks.size) throw new Error(`Expected ${args.ranks.size} source rows, found ${selected.length}`);
    const generated = new Map();
    let updatedCount = 0;
    for (const batch of chunk(selected, args.batchSize)) {
        console.log(`Generating examples for ranks ${batch.map((item) => item.rank).join(", ")}`);
        const response = await requestExamples(apiKey, args.model, batch);
        for (const [rank, example] of validateBatch(batch, response)) generated.set(rank, example);
        if (args.checkpoint && !args.dryRun) {
            const nextLines = applyExamples(lines, generated);
            await writeFile(sourcePath, nextLines.join("\n"), "utf8");
            lines.splice(0, lines.length, ...nextLines);
            updatedCount += generated.size;
            generated.clear();
        }
    }

    if (args.dryRun) {
        console.log(JSON.stringify(Array.from(generated.values()), null, 2));
        return;
    }

    const nextLines = applyExamples(lines, generated);
    await writeFile(sourcePath, nextLines.join("\n"), "utf8");
    console.log(`Updated ${updatedCount + generated.size} rows in ${args.source}`);
}

main().catch((error) => {
    console.error(error.message);
    process.exit(1);
});
