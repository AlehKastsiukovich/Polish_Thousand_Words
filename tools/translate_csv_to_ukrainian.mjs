#!/usr/bin/env node

import fs from "node:fs/promises";
import path from "node:path";

const DEFAULT_MODEL = "gemini-2.5-flash";
const DEFAULT_BATCH_SIZE = 20;

function parseArgs(argv) {
    const args = {
        source: "",
        output: "",
        model: DEFAULT_MODEL,
        batchSize: DEFAULT_BATCH_SIZE,
        overwrite: false
    };

    for (let index = 0; index < argv.length; index += 1) {
        const token = argv[index];
        switch (token) {
            case "--source":
                args.source = argv[++index] ?? "";
                break;
            case "--output":
                args.output = argv[++index] ?? "";
                break;
            case "--model":
                args.model = argv[++index] ?? DEFAULT_MODEL;
                break;
            case "--batch-size":
                args.batchSize = Number(argv[++index] ?? DEFAULT_BATCH_SIZE);
                break;
            case "--overwrite":
                args.overwrite = true;
                break;
            case "--help":
                printHelp();
                process.exit(0);
            default:
                throw new Error(`Unknown argument: ${token}`);
        }
    }

    if (!args.source) {
        throw new Error("--source is required");
    }

    if (!args.output) {
        const parsed = path.parse(args.source);
        args.output = path.join(parsed.dir, `${parsed.name}_uk${parsed.ext}`);
    }

    if (!Number.isInteger(args.batchSize) || args.batchSize <= 0) {
        throw new Error("--batch-size must be a positive integer");
    }

    return args;
}

function printHelp() {
    console.log(`Usage:
  node tools/translate_csv_to_ukrainian.mjs --source <path> [--output <path>] [--model <name>] [--batch-size <n>] [--overwrite]
`);
}

function parseCsv(text) {
    const rows = [];
    let row = [];
    let current = "";
    let inQuotes = false;

    for (let i = 0; i < text.length; i += 1) {
        const char = text[i];
        const next = text[i + 1];

        if (char === "\"") {
            if (inQuotes && next === "\"") {
                current += "\"";
                i += 1;
            } else {
                inQuotes = !inQuotes;
            }
            continue;
        }

        if (char === "," && !inQuotes) {
            row.push(current);
            current = "";
            continue;
        }

        if ((char === "\n" || char === "\r") && !inQuotes) {
            if (char === "\r" && next === "\n") {
                i += 1;
            }
            row.push(current);
            current = "";
            if (row.some((value) => value.length > 0)) {
                rows.push(row);
            }
            row = [];
            continue;
        }

        current += char;
    }

    if (current.length > 0 || row.length > 0) {
        row.push(current);
        rows.push(row);
    }

    if (rows.length === 0) {
        return { header: [], records: [] };
    }

    const [header, ...body] = rows;
    return {
        header,
        records: body.map((values) => Object.fromEntries(header.map((key, index) => [key, values[index] ?? ""])))
    };
}

function escapeCsvValue(value) {
    if (value.includes("\"") || value.includes(",") || value.includes("\n") || value.includes("\r")) {
        return `"${value.replaceAll("\"", "\"\"")}"`;
    }
    return value;
}

function stringifyCsv(header, records) {
    const lines = [header.map(escapeCsvValue).join(",")];
    for (const record of records) {
        lines.push(header.map((column) => escapeCsvValue(record[column] ?? "")).join(","));
    }
    return `${lines.join("\n")}\n`;
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

async function resolveApiKey(projectRoot) {
    if (process.env.GEMINI_API_KEY) return process.env.GEMINI_API_KEY;

    const localPropertiesPath = path.join(projectRoot, "local.properties");
    const text = await fs.readFile(localPropertiesPath, "utf8");
    const properties = parseProperties(text);
    const localKey = properties["gemini.api.key"];
    if (!localKey) {
        throw new Error("Gemini API key not found in GEMINI_API_KEY or local.properties");
    }
    return localKey;
}

function buildPrompt(batch) {
    return [
        "Translate each record into natural Ukrainian for learners.",
        "Return only strict JSON with this shape:",
        "{\"items\":[{\"id\":\"...\",\"ukrainian\":\"...\",\"example_ukrainian_1\":\"...\",\"example_ukrainian_2\":\"...\"}]}",
        "Rules:",
        "- Keep meanings aligned with the Polish word and existing Russian translation.",
        "- Translate example sentences from Polish into natural Ukrainian.",
        "- Preserve sentence punctuation.",
        "- Do not add explanations.",
        `Input: ${JSON.stringify(batch)}`
    ].join("\n");
}

async function translateBatch({ apiKey, model, batch }) {
    const response = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${apiKey}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            generationConfig: {
                temperature: 0.2,
                responseMimeType: "application/json"
            },
            contents: [
                {
                    role: "user",
                    parts: [
                        {
                            text: buildPrompt(batch)
                        }
                    ]
                }
            ]
        })
    });

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Gemini request failed: ${response.status} ${response.statusText}\n${errorText}`);
    }

    const payload = await response.json();
    const text = payload.candidates?.[0]?.content?.parts?.map((part) => part.text ?? "").join("") ?? "";
    if (!text) {
        throw new Error("Gemini response did not contain text");
    }

    const parsed = JSON.parse(text);
    if (!Array.isArray(parsed.items)) {
        throw new Error("Gemini response JSON does not contain items array");
    }
    return parsed.items;
}

async function main() {
    const args = parseArgs(process.argv.slice(2));
    const projectRoot = process.cwd();
    const apiKey = await resolveApiKey(projectRoot);
    const sourceText = await fs.readFile(args.source, "utf8");
    const { header, records } = parseCsv(sourceText);

    const outputExists = await fs.access(args.output).then(() => true).catch(() => false);
    if (outputExists && !args.overwrite) {
        throw new Error(`Output already exists: ${args.output}. Pass --overwrite to replace it.`);
    }

    const extraColumns = ["ukrainian", "example_ukrainian_1", "example_ukrainian_2"];
    const outputHeader = [...header];
    for (const column of extraColumns) {
        if (!outputHeader.includes(column)) {
            outputHeader.push(column);
        }
    }

    for (let start = 0; start < records.length; start += args.batchSize) {
        const slice = records.slice(start, start + args.batchSize);
        const batch = slice.map((record) => ({
            id: record.id,
            polish: record.polish,
            russian: record.russian,
            example_polish_1: record.example_polish_1,
            example_polish_2: record.example_polish_2
        }));
        const translated = await translateBatch({ apiKey, model: args.model, batch });
        const translatedById = new Map(translated.map((item) => [String(item.id), item]));

        for (const record of slice) {
            const item = translatedById.get(String(record.id));
            if (!item) {
                throw new Error(`Missing translation for id=${record.id}`);
            }
            record.ukrainian = String(item.ukrainian ?? "").trim();
            record.example_ukrainian_1 = String(item.example_ukrainian_1 ?? "").trim();
            record.example_ukrainian_2 = String(item.example_ukrainian_2 ?? "").trim();
        }

        console.log(`Translated ${Math.min(start + slice.length, records.length)}/${records.length}`);
    }

    await fs.writeFile(args.output, stringifyCsv(outputHeader, records), "utf8");
    console.log(`Wrote ${args.output}`);
}

main().catch((error) => {
    console.error(error.message);
    process.exit(1);
});
