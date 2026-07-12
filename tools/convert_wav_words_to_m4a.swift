#!/usr/bin/env swift

import AVFoundation
import Foundation

struct Arguments {
    let audioDir: URL
    let overwrite: Bool
    let keepWav: Bool
    let dryRun: Bool
}

enum ScriptError: Error, CustomStringConvertible {
    case invalidArguments(String)
    case conversionFailed(String)
    case exportFailed(String)
    case manifestMissing

    var description: String {
        switch self {
        case .invalidArguments(let message): return message
        case .conversionFailed(let message): return message
        case .exportFailed(let message): return message
        case .manifestMissing: return "manifest.json not found"
        }
    }
}

func parseArguments() throws -> Arguments {
    let args = Array(CommandLine.arguments.dropFirst())
    var audioDir = URL(fileURLWithPath: FileManager.default.currentDirectoryPath)
        .appendingPathComponent("shared/src/commonMain/composeResources/files/audio")
    var overwrite = false
    var keepWav = true
    var dryRun = false

    var index = 0
    while index < args.count {
        let arg = args[index]
        switch arg {
        case "--audio-dir":
            index += 1
            guard index < args.count else {
                throw ScriptError.invalidArguments("--audio-dir requires a value")
            }
            audioDir = URL(fileURLWithPath: args[index], relativeTo: URL(fileURLWithPath: FileManager.default.currentDirectoryPath)).standardizedFileURL
        case "--overwrite":
            overwrite = true
        case "--delete-wav":
            keepWav = false
        case "--dry-run":
            dryRun = true
        case "--help":
            print("Usage: swift tools/convert_wav_words_to_m4a.swift [--audio-dir <path>] [--overwrite] [--delete-wav] [--dry-run]")
            exit(0)
        default:
            throw ScriptError.invalidArguments("Unknown argument: \(arg)")
        }
        index += 1
    }

    return Arguments(audioDir: audioDir, overwrite: overwrite, keepWav: keepWav, dryRun: dryRun)
}

func listWavFiles(in wordsDir: URL) throws -> [URL] {
    let fileManager = FileManager.default
    let entries = try fileManager.contentsOfDirectory(at: wordsDir, includingPropertiesForKeys: nil)
    return entries
        .filter { $0.pathExtension.lowercased() == "wav" }
        .sorted { $0.lastPathComponent < $1.lastPathComponent }
}

func convertWavToM4a(inputURL: URL, outputURL: URL) throws {
    let asset = AVURLAsset(url: inputURL)
    guard let audioTrack = asset.tracks(withMediaType: .audio).first else {
        throw ScriptError.conversionFailed("No audio track found in \(inputURL.lastPathComponent)")
    }

    let reader = try AVAssetReader(asset: asset)
    let readerOutput = AVAssetReaderTrackOutput(track: audioTrack, outputSettings: nil)
    guard reader.canAdd(readerOutput) else {
        throw ScriptError.conversionFailed("Cannot add reader output for \(inputURL.lastPathComponent)")
    }
    reader.add(readerOutput)

    try? FileManager.default.removeItem(at: outputURL)
    let writer = try AVAssetWriter(outputURL: outputURL, fileType: .m4a)
    guard let outputSettings = audioTrack.recommendedAudioSettingsForAssetWriter(writingTo: .m4a) as? [String: Any] else {
        throw ScriptError.conversionFailed("Cannot create AAC output settings for \(inputURL.lastPathComponent)")
    }
    let writerInput = AVAssetWriterInput(
        mediaType: .audio,
        outputSettings: outputSettings
    )
    writerInput.expectsMediaDataInRealTime = false
    guard writer.canAdd(writerInput) else {
        throw ScriptError.conversionFailed("Cannot add writer input for \(inputURL.lastPathComponent)")
    }
    writer.add(writerInput)

    guard reader.startReading() else {
        throw ScriptError.conversionFailed("Reader failed to start for \(inputURL.lastPathComponent)")
    }
    guard writer.startWriting() else {
        throw ScriptError.conversionFailed("Writer failed to start for \(inputURL.lastPathComponent)")
    }

    writer.startSession(atSourceTime: .zero)

    let semaphore = DispatchSemaphore(value: 0)
    let queue = DispatchQueue(label: "wav-to-m4a-writer")

    writerInput.requestMediaDataWhenReady(on: queue) {
        while writerInput.isReadyForMoreMediaData {
            if let sampleBuffer = readerOutput.copyNextSampleBuffer() {
                if !writerInput.append(sampleBuffer) {
                    writerInput.markAsFinished()
                    reader.cancelReading()
                    semaphore.signal()
                    return
                }
            } else {
                writerInput.markAsFinished()
                writer.finishWriting {
                    semaphore.signal()
                }
                return
            }
        }
    }

    semaphore.wait()

    if reader.status == .failed {
        throw ScriptError.conversionFailed("\(inputURL.lastPathComponent): \(reader.error?.localizedDescription ?? "Reader failed")")
    }
    if writer.status == .failed {
        throw ScriptError.exportFailed("\(inputURL.lastPathComponent): \(writer.error?.localizedDescription ?? "Writer failed")")
    }
}

func updateManifest(at manifestURL: URL) throws {
    let data = try Data(contentsOf: manifestURL)
    guard var manifest = try JSONSerialization.jsonObject(with: data) as? [String: Any] else {
        throw ScriptError.manifestMissing
    }

    manifest["generatedAt"] = ISO8601DateFormatter().string(from: Date())
    manifest["format"] = [
        "container": "m4a",
        "codec": "aac-lc",
        "sampleRateHz": 24000,
        "channels": 1,
        "bitrateKbps": 48
    ]

    if var items = manifest["items"] as? [[String: Any]] {
        items = items.map { item in
            var next = item
            if let wordAudio = item["wordAudio"] as? String {
                next["wordAudio"] = wordAudio.replacingOccurrences(of: ".wav", with: ".m4a")
            }
            if let examples = item["examples"] as? [[String: Any]] {
                next["examples"] = examples.map { example in
                    var nextExample = example
                    if let audio = example["audio"] as? String {
                        nextExample["audio"] = audio.replacingOccurrences(of: ".wav", with: ".m4a")
                    }
                    return nextExample
                }
            }
            return next
        }
        manifest["items"] = items
    }

    let outputData = try JSONSerialization.data(withJSONObject: manifest, options: [.prettyPrinted, .sortedKeys])
    try outputData.write(to: manifestURL)
    if let handle = try? FileHandle(forWritingTo: manifestURL) {
        try handle.seekToEnd()
        try handle.write(contentsOf: Data([0x0A]))
        try handle.close()
    }
}

do {
    let arguments = try parseArguments()
    let wordsDir = arguments.audioDir.appendingPathComponent("words")
    let manifestURL = arguments.audioDir.appendingPathComponent("manifest.json")

    guard FileManager.default.fileExists(atPath: manifestURL.path) else {
        throw ScriptError.manifestMissing
    }

    let wavFiles = try listWavFiles(in: wordsDir)
    if arguments.dryRun {
        let sample = wavFiles.prefix(5).map(\.lastPathComponent)
        let payload: [String: Any] = [
            "audioDir": arguments.audioDir.path,
            "wavFiles": wavFiles.count,
            "sample": sample
        ]
        let data = try JSONSerialization.data(withJSONObject: payload, options: [.prettyPrinted, .sortedKeys])
        print(String(decoding: data, as: UTF8.self))
        exit(0)
    }

    var converted = 0
    for wavURL in wavFiles {
        let m4aURL = wavURL.deletingPathExtension().appendingPathExtension("m4a")
        if FileManager.default.fileExists(atPath: m4aURL.path), !arguments.overwrite {
            continue
        }

        try convertWavToM4a(inputURL: wavURL, outputURL: m4aURL)
        if !arguments.keepWav {
            try? FileManager.default.removeItem(at: wavURL)
        }
        converted += 1
        if converted % 50 == 0 {
            print("Converted \(converted)/\(wavFiles.count)")
        }
    }

    try updateManifest(at: manifestURL)
    print("Converted \(converted) files")
    print("Updated \(manifestURL.path)")
} catch {
    fputs("\(error)\n", stderr)
    exit(1)
}
