"""Subsets the bundled icon fonts to the glyphs KeepOn actually renders.

The app only ever draws two things with these fonts: the generated timeout text (digits,
the s/m/h unit letters and the infinity sign) and the Style screen's font list (the "Aa"
preview chip and the family display names). The full Google Fonts TTFs cover thousands of
glyphs (~5 MB in res/font); subsetting them to printable ASCII + U+221E keeps every glyph
the app can show while dropping ~90% of the size.

Per the SIL OFL, a modified font may not be distributed under its Reserved Font Name (Lora
declares one), so the internal family name of every subset font gets a " Subset" suffix.
This is invisible in the app: fonts are loaded by resource id and labelled by
IconFontFamily.displayName. The embedded copyright/license name records are kept.

Usage — the originals are NOT kept in the tree; download them from fonts.google.com (links
in README.md) or take them from git history (last full versions: v2.2.0), then:

    pip install fonttools
    python scripts/subset_fonts.py <originals_dir> app/src/main/res/font
"""

import sys
from pathlib import Path

from fontTools import subset
from fontTools.ttLib import TTFont

# Printable ASCII + the infinity sign (qs_short_infinite).
KEPT_UNICODES = list(range(0x20, 0x7F)) + [0x221E]

# Sanity floor: every glyph the app is known to render today.
REQUIRED_CHARS = "0123456789smh∞Aa"

FAMILY_NAME_SUFFIX = " Subset"

# Name records carrying the family name: family, unique id, full name, typographic family.
FAMILY_NAME_IDS = (1, 3, 4, 16)
POSTSCRIPT_NAME_ID = 6


def subset_font(src: Path, dst: Path) -> tuple[int, int]:
    font = TTFont(src)

    options = subset.Options()
    options.name_IDs = ["*"]  # keep the whole name table (copyright + license records)
    options.name_languages = ["*"]

    subsetter = subset.Subsetter(options=options)
    subsetter.populate(unicodes=KEPT_UNICODES)
    subsetter.subset(font)

    rename_family(font)

    cmap = font.getBestCmap()
    missing = [c for c in REQUIRED_CHARS if ord(c) not in cmap]
    if missing:
        raise RuntimeError(f"{src.name}: missing required glyphs after subset: {missing}")

    font.save(dst)
    return src.stat().st_size, dst.stat().st_size


def rename_family(font: TTFont) -> None:
    name = font["name"]
    family = name.getDebugName(16) or name.getDebugName(1)
    new_family = family + FAMILY_NAME_SUFFIX
    ps_old = family.replace(" ", "")
    ps_new = new_family.replace(" ", "")
    for record in name.names:
        if record.nameID in FAMILY_NAME_IDS:
            record.string = record.toUnicode().replace(family, new_family)
        elif record.nameID == POSTSCRIPT_NAME_ID:
            record.string = record.toUnicode().replace(ps_old, ps_new)


def main() -> None:
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    src_dir, dst_dir = Path(sys.argv[1]), Path(sys.argv[2])
    dst_dir.mkdir(parents=True, exist_ok=True)

    total_before = total_after = 0
    for src in sorted(src_dir.glob("*.ttf")):
        before, after = subset_font(src, dst_dir / src.name)
        total_before += before
        total_after += after
        print(f"{src.name}: {before / 1024:.0f} KB -> {after / 1024:.0f} KB")

    print(f"TOTAL: {total_before / 1024 / 1024:.2f} MB -> {total_after / 1024:.0f} KB")


if __name__ == "__main__":
    main()
