#!/usr/bin/env python3
from __future__ import annotations

import csv
import json
import re
from dataclasses import dataclass
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SOURCE_CSV = ROOT / "polish_thousand_b1_ru_content_1000_FINAL_NO_DUPLICATES_uk.csv"
TARGET_KT = ROOT / "shared/src/commonMain/kotlin/com/polish/thousand/content/GeneratedB1RuLessons.kt"


@dataclass
class CsvItem:
    rank: int
    polish: str
    russian: str
    note: str
    example_polish_1: str
    example_russian_1: str
    example_polish_2: str
    example_russian_2: str
    ukrainian: str
    example_ukrainian_1: str
    example_ukrainian_2: str


def quote(value: str) -> str:
    return json.dumps(value, ensure_ascii=False)


def parse_existing_metadata(text: str) -> tuple[list[str], list[tuple[str, int, int]]]:
    item_ids = re.findall(r'generatedItem\("([^"]+)"', text)
    lesson_matches = re.findall(
        r'generatedLesson\("([^"]+)",\s*(\d+),\s*(\d+),\s*listOf\(',
        text,
    )
    lesson_ids = [(lesson_id, int(start), int(end)) for lesson_id, start, end in lesson_matches]
    if len(item_ids) != 1000:
        raise ValueError(f"Expected 1000 item ids, got {len(item_ids)}")
    if len(lesson_ids) != 100:
        raise ValueError(f"Expected 100 lesson ids, got {len(lesson_ids)}")
    return item_ids, lesson_ids


def parse_csv_item(row: list[str], line_number: int) -> CsvItem:
    if len(row) == 12 and row[1].isdigit():
        return CsvItem(
            rank=int(row[1]),
            polish=row[2],
            russian=row[3],
            note=row[4],
            example_polish_1=row[5],
            example_russian_1=row[6],
            example_polish_2=row[7],
            example_russian_2=row[8],
            ukrainian=row[9],
            example_ukrainian_1=row[10],
            example_ukrainian_2=row[11],
        )

    if len(row) == 12 and not row[1].isdigit() and row[3] in {"noun", "verb", "adjective", "adverb", "phrase", "conjunction", "preposition"}:
        return CsvItem(
            rank=int(row[0]),
            polish=row[1],
            russian=row[2],
            note=row[3],
            example_polish_1=row[5],
            example_russian_1=row[6],
            example_polish_2=row[7],
            example_russian_2=row[8],
            ukrainian=row[9],
            example_ukrainian_1=row[10],
            example_ukrainian_2=row[11],
        )

    raise ValueError(f"Unsupported CSV shape at line {line_number}: {row}")


def load_csv_items() -> list[CsvItem]:
    with SOURCE_CSV.open(newline="", encoding="utf-8") as source:
        reader = csv.reader(source)
        next(reader)
        items = [parse_csv_item(row, index) for index, row in enumerate(reader, start=2)]
    if len(items) != 1000:
        raise ValueError(f"Expected 1000 CSV items, got {len(items)}")
    return items


def generate_kotlin(item_ids: list[str], lesson_ids: list[tuple[str, int, int]], items: list[CsvItem]) -> str:
    lines: list[str] = [
        "package com.polish.thousand.content",
        "",
        "internal val generatedB1RuLessons: List<LessonContent> = listOf(",
    ]

    for lesson_index, (lesson_id, start_rank, end_rank) in enumerate(lesson_ids):
        slice_start = lesson_index * 10
        slice_end = slice_start + 10
        lesson_items = items[slice_start:slice_end]
        lesson_item_ids = item_ids[slice_start:slice_end]

        lines.append(f'    generatedLesson("{lesson_id}", {start_rank}, {end_rank}, listOf(')
        for item_id, item in zip(lesson_item_ids, lesson_items):
            lines.append(
                "        generatedItem("
                f'{quote(item_id)}, {quote(item.polish)}, {quote(item.russian)}, {quote(item.ukrainian)}, '
                f'{quote(item.note)}, {quote(item.example_polish_1)}, {quote(item.example_russian_1)}, '
                f'{quote(item.example_ukrainian_1)}, {quote(item.example_polish_2)}, '
                f'{quote(item.example_russian_2)}, {quote(item.example_ukrainian_2)}'
                "),"
            )
        lines.append("    ))," if lesson_index < len(lesson_ids) - 1 else "    ))")

    lines.extend(
        [
            ")",
            "",
            "private fun generatedLesson(",
            "    id: String,",
            "    startRank: Int,",
            "    endRank: Int,",
            "    items: List<LessonItemContent>",
            ") = LessonContent(",
            "    id = id,",
            '    title = "Words $startRank-$endRank",',
            '    description = "Core Polish words $startRank-$endRank.",',
            "    estimatedMinutes = 6,",
            "    exerciseTypes = defaultExerciseTypes,",
            "    items = items",
            ")",
            "",
            "private fun generatedItem(",
            "    id: String,",
            "    polish: String,",
            "    russian: String,",
            "    ukrainian: String,",
            "    note: String,",
            "    examplePolish1: String,",
            "    exampleRussian1: String,",
            "    exampleUkrainian1: String,",
            "    examplePolish2: String,",
            "    exampleRussian2: String,",
            "    exampleUkrainian2: String",
            ") = LessonItemContent(",
            "    id = id,",
            "    polish = polish,",
            "    russian = russian,",
            "    ukrainian = ukrainian,",
            "    examples = listOf(",
            "        LessonExampleContent(examplePolish1, exampleRussian1, exampleUkrainian1),",
            "        LessonExampleContent(examplePolish2, exampleRussian2, exampleUkrainian2)",
            "    ),",
            "    note = note",
            ")",
            "",
        ]
    )

    return "\n".join(lines)


def main() -> None:
    existing_text = TARGET_KT.read_text(encoding="utf-8")
    item_ids, lesson_ids = parse_existing_metadata(existing_text)
    csv_items = load_csv_items()
    kotlin = generate_kotlin(item_ids, lesson_ids, csv_items)
    TARGET_KT.write_text(kotlin, encoding="utf-8")


if __name__ == "__main__":
    main()
