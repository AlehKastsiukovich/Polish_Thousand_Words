#!/usr/bin/env node

import { mkdir, readFile, readdir, rm, stat, writeFile } from "node:fs/promises";
import path from "node:path";
import { spawn } from "node:child_process";

const DEFAULT_AUDIO_DIR = "shared/src/commonMain/composeResources/files/audio";
const DEFAULT_BITRATE_KBPS = 48;

function parseArgs(argv) {
    const args = {
        audioDir: DEFAULT_AUDIO_DIR,
        overwrite: false,
        keepWav: true,
        dryRun: false
    };

    for (let index = 0; index < argv.length; index += 1) {
        const arg = argv[index];
        const next = argv[index + 1];

        switch (arg) {
            case "--audio-dir":
                args.audioDir = next;
                index += 1;
                break;
            case "--overwrite":
                args.overwrite = true;
                break;
            case "--delete-wav":
                args.keepWav = false;
                break;
            case "--dry-run":
                args.dryRun = true;
                break;
            case "--help":
                printHelp();
                process.exit(0);
            default:
                throw new Error(`Unknown argument: ${arg}`);
        }
    }

    return args;
}

function printHelp() {
    console.log(`Usage:
  node tools/convert_wav_words_to_m4a.mjs [--audio-dir <path>] [--overwrite] [--delete-wav] [--dry-run]
`);
}

async function convertWavToM4a(inputPath, outputPath) {
    await new Promise((resolve, reject) => {
        const child = spawn("/usr/bin/afconvert", [
            "-f", "m4af",
            "-d", "aac",
            "-u", "vbrq", "27",
            "-u", "src", "c=1,r=24000",
            inputPath,
            outputPath
        ], {
            stdio: ["ignore", "pipe", "pipe"]
        });

        let stderr = "";
        child.stderr.on("data", (chunk) => {
            stderr += chunk.toString();
        });
        child.on("error", reject);
        child.on("close", (code) => {
            if (code === 0) {
                resolve();
                return;
            }
            reject(new Error(`afconvert failed for ${path.basename(inputPath)}: ${stderr.trim()}`));
        });
    });
}

async function listWordWavFiles(wordsDir) {
    const entries = await readdir(wordsDir, { withFileTypes: true });
    return entries
        .filter((entry) => entry.isFile() && entry.name.endsWith(".wav"))
        .map((entry) => path.join(wordsDir, entry.name))
        .sort();
}

async function updateManifest(manifestPath) {
    const manifest = JSON.parse(await readFile(manifestPath, "utf8"));
    manifest.generatedAt = new Date().toISOString();
    manifest.format = {
        container: "m4a",
        codec: "aac-lc",
        sampleRateHz: 24000,
        channels: 1,
        bitrateKbps: DEFAULT_BITRATE_KBPS
    };

    if (Array.isArray(manifest.items)) {
        manifest.items = manifest.items.map((item) => ({
            ...item,
            wordAudio: typeof item.wordAudio === "string"
                ? item.wordAudio.replace(/\.wav$/i, ".m4a")
                : item.wordAudio,
            examples: Array.isArray(item.examples)
                ? item.examples.map((example) => ({
                    ...example,
                    audio: typeof example.audio === "string"
                        ? example.audio.replace(/\.wav$/i, ".m4a")
                        : example.audio
                }))
                : []
        }));
    }

    await writeFile(`${manifestPath}`, `${JSON.stringify(manifest, null, 2)}\n`, "utf8");
}

async function main() {
    const args = parseArgs(process.argv.slice(2));
    const audioDir = path.resolve(process.cwd(), args.audioDir);
    const wordsDir = path.join(audioDir, "words");
    const manifestPath = path.join(audioDir, "manifest.json");

    await mkdir(wordsDir, { recursive: true });
    await stat(manifestPath);

    const wavFiles = await listWordWavFiles(wordsDir);
    if (args.dryRun) {
        console.log(JSON.stringify({
            audioDir,
            wavFiles: wavFiles.length,
            sample: wavFiles.slice(0, 5).map((file) => path.basename(file))
        }, null, 2));
        return;
    }

    let converted = 0;
    for (const wavPath of wavFiles) {
        const m4aPath = wavPath.replace(/\.wav$/i, ".m4a");
        try {
            if (!args.overwrite) {
                await stat(m4aPath);
                continue;
            }
        } catch {
            // output does not exist
        }

        await convertWavToM4a(wavPath, m4aPath);
        if (!args.keepWav) {
            await rm(wavPath, { force: true });
        }
        converted += 1;
        if (converted % 50 === 0) {
            console.log(`Converted ${converted}/${wavFiles.length}`);
        }
    }

    await updateManifest(manifestPath);
    console.log(`Converted ${converted} files`);
    console.log(`Updated ${manifestPath}`);
}

main().catch((error) => {
    console.error(error.message);
    process.exit(1);
});
