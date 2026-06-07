import AppKit
import Foundation

private struct Palette {
    let card = NSColor(calibratedRed: 0.988, green: 0.988, blue: 0.980, alpha: 1.0)
    let primaryText = NSColor(calibratedRed: 0.106, green: 0.110, blue: 0.114, alpha: 1.0)
    let accent = NSColor(calibratedRed: 0.729, green: 0.247, blue: 0.188, alpha: 1.0)
    let badge = NSColor(calibratedRed: 0.729, green: 0.247, blue: 0.188, alpha: 1.0)
    let badgeText = NSColor.white
}

private let palette = Palette()

private func makeBitmap(
    size: Int,
    clear: Bool = false,
    drawing: (CGRect) -> Void
) -> NSBitmapImageRep {
    let bitmap = NSBitmapImageRep(
        bitmapDataPlanes: nil,
        pixelsWide: size,
        pixelsHigh: size,
        bitsPerSample: 8,
        samplesPerPixel: 4,
        hasAlpha: true,
        isPlanar: false,
        colorSpaceName: .deviceRGB,
        bytesPerRow: 0,
        bitsPerPixel: 0
    )!

    NSGraphicsContext.saveGraphicsState()
    NSGraphicsContext.current = NSGraphicsContext(bitmapImageRep: bitmap)

    let canvas = CGRect(x: 0, y: 0, width: size, height: size)
    if clear {
        NSColor.clear.setFill()
        canvas.fill()
    } else {
        palette.card.setFill()
        canvas.fill()
    }
    drawing(canvas)

    NSGraphicsContext.restoreGraphicsState()
    return bitmap
}

private func roundedRect(_ rect: CGRect, radius: CGFloat) -> NSBezierPath {
    NSBezierPath(roundedRect: rect, xRadius: radius, yRadius: radius)
}

private func drawAppIcon(
    in rect: CGRect,
    platform: PlatformSpec
) {
    let size = min(rect.width, rect.height)
    let centerX = rect.midX
    let centerY = rect.midY

    let numberFont = NSFont.systemFont(ofSize: size * platform.numberFontScale, weight: .heavy)
    let wordFont = NSFont.systemFont(ofSize: size * platform.wordFontScale, weight: .regular)
    let badgeFont = NSFont.systemFont(ofSize: size * platform.badgeFontScale, weight: .bold)

    let numberStyle = NSMutableParagraphStyle()
    numberStyle.alignment = .center

    let numberAttributes: [NSAttributedString.Key: Any] = [
        .font: numberFont,
        .foregroundColor: palette.primaryText,
        .kern: -(size * platform.numberFontScale * 0.05),
        .paragraphStyle: numberStyle
    ]

    let wordAttributes: [NSAttributedString.Key: Any] = [
        .font: wordFont,
        .foregroundColor: palette.accent,
        .kern: size * platform.wordTrackingScale,
        .paragraphStyle: numberStyle
    ]

    let numberText = NSAttributedString(string: "1000", attributes: numberAttributes)
    let wordText = NSAttributedString(string: "SŁÓW", attributes: wordAttributes)

    let numberSize = numberText.size()
    let wordSize = wordText.size()
    let lineGap = size * platform.lineGapScale
    let totalHeight = numberSize.height + lineGap + wordSize.height
    let originY = centerY + totalHeight / 2 - numberSize.height + (size * platform.verticalOffsetScale)

    numberText.draw(at: CGPoint(x: centerX - numberSize.width / 2, y: originY))
    wordText.draw(at: CGPoint(x: centerX - wordSize.width / 2, y: originY - wordSize.height - lineGap))

    let badgePaddingH = size * platform.badgePaddingHScale
    let badgePaddingV = size * platform.badgePaddingVScale
    let badgeRadius = size * platform.badgeRadiusScale
    let badgeBottom = size * platform.badgeBottomScale
    let badgeRight = size * platform.badgeRightScale

    let badgeText = NSAttributedString(
        string: "PL",
        attributes: [
            .font: badgeFont,
            .foregroundColor: palette.badgeText,
            .kern: size * platform.badgeTrackingScale
        ]
    )
    let badgeTextSize = badgeText.size()
    let badgeRect = CGRect(
        x: rect.maxX - badgeRight - badgeTextSize.width - (badgePaddingH * 2),
        y: rect.minY + badgeBottom,
        width: badgeTextSize.width + (badgePaddingH * 2),
        height: badgeTextSize.height + (badgePaddingV * 2)
    )

    palette.badge.setFill()
    roundedRect(badgeRect, radius: badgeRadius).fill()
    badgeText.draw(
        at: CGPoint(
            x: badgeRect.midX - badgeTextSize.width / 2,
            y: badgeRect.midY - badgeTextSize.height / 2
        )
    )
}

private struct PlatformSpec {
    let numberFontScale: CGFloat
    let wordFontScale: CGFloat
    let wordTrackingScale: CGFloat
    let badgeFontScale: CGFloat
    let badgePaddingHScale: CGFloat
    let badgePaddingVScale: CGFloat
    let badgeRadiusScale: CGFloat
    let badgeBottomScale: CGFloat
    let badgeRightScale: CGFloat
    let badgeTrackingScale: CGFloat
    let lineGapScale: CGFloat
    let verticalOffsetScale: CGFloat
}

private let iosSpec = PlatformSpec(
    numberFontScale: 0.285,
    wordFontScale: 0.145,
    wordTrackingScale: 0.0116,
    badgeFontScale: 0.065,
    badgePaddingHScale: 0.028,
    badgePaddingVScale: 0.012,
    badgeRadiusScale: 0.028,
    badgeBottomScale: 0.11,
    badgeRightScale: 0.11,
    badgeTrackingScale: 0.0039,
    lineGapScale: 0.008,
    verticalOffsetScale: 0.0
)

private let androidSpec = PlatformSpec(
    numberFontScale: 0.285,
    wordFontScale: 0.145,
    wordTrackingScale: 0.0072,
    badgeFontScale: 0.065,
    badgePaddingHScale: 0.028,
    badgePaddingVScale: 0.012,
    badgeRadiusScale: 0.028,
    badgeBottomScale: 0.11,
    badgeRightScale: 0.11,
    badgeTrackingScale: 0.0039,
    lineGapScale: 0.008,
    verticalOffsetScale: 0.0
)

private func savePng(bitmap: NSBitmapImageRep, to path: String) throws {
    let url = URL(fileURLWithPath: path)
    try FileManager.default.createDirectory(
        at: url.deletingLastPathComponent(),
        withIntermediateDirectories: true
    )

    let data = bitmap.representation(using: .png, properties: [:])!
    try data.write(to: url)
}

private func generateAssets(at repoRoot: String) throws {
    let iosIcon = makeBitmap(size: 1024) { rect in
        drawAppIcon(in: rect, platform: iosSpec)
    }

    let androidLegacyIcon = makeBitmap(size: 1024) { rect in
        drawAppIcon(in: rect, platform: androidSpec)
    }

    let androidBrandMark = makeBitmap(size: 432, clear: true) { rect in
        let inset = rect.width * 0.10
        let iconRect = rect.insetBy(dx: inset, dy: inset)
        palette.card.setFill()
        roundedRect(iconRect, radius: iconRect.width * 0.22).fill()
        drawAppIcon(in: iconRect, platform: androidSpec)
    }

    try savePng(
        bitmap: iosIcon,
        to: "\(repoRoot)/iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png"
    )

    try savePng(
        bitmap: androidBrandMark,
        to: "\(repoRoot)/androidApp/src/main/res/drawable-nodpi/ic_brand_mark.png"
    )

    let launcherSizes = [
        ("mdpi", 48),
        ("hdpi", 72),
        ("xhdpi", 96),
        ("xxhdpi", 144),
        ("xxxhdpi", 192)
    ]

    for (density, size) in launcherSizes {
        let resized = NSBitmapImageRep(
            bitmapDataPlanes: nil,
            pixelsWide: size,
            pixelsHigh: size,
            bitsPerSample: 8,
            samplesPerPixel: 4,
            hasAlpha: true,
            isPlanar: false,
            colorSpaceName: .deviceRGB,
            bytesPerRow: 0,
            bitsPerPixel: 0
        )!

        NSGraphicsContext.saveGraphicsState()
        NSGraphicsContext.current = NSGraphicsContext(bitmapImageRep: resized)
        NSGraphicsContext.current?.imageInterpolation = .high
        let image = NSImage(size: NSSize(width: 1024, height: 1024))
        image.addRepresentation(androidLegacyIcon)
        image.draw(in: CGRect(x: 0, y: 0, width: size, height: size))
        NSGraphicsContext.restoreGraphicsState()

        let basePath = "\(repoRoot)/androidApp/src/main/res/mipmap-\(density)"
        try savePng(bitmap: resized, to: "\(basePath)/ic_launcher.png")
        try savePng(bitmap: resized, to: "\(basePath)/ic_launcher_round.png")
    }
}

guard CommandLine.arguments.count >= 2 else {
    fputs("Usage: swift tools/generate_brand_assets.swift <repo-root>\n", stderr)
    exit(1)
}

do {
    try generateAssets(at: CommandLine.arguments[1])
} catch {
    fputs("Failed to generate brand assets: \(error)\n", stderr)
    exit(1)
}
