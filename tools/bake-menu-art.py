"""Bake Trevor's original authored pixels into one texture (requires Pillow)."""
from pathlib import Path
import json
from PIL import Image
root = Path(__file__).resolve().parents[1]
art = json.loads((root / 'tools/menu-art.json').read_text())
atlas = Image.new('RGBA', (288, 32))
for cell, sprite in enumerate(art['sprites']):
    for y, row in enumerate(sprite['rows']):
        for x, symbol in enumerate(row):
            ink = art['palette'].get(symbol, 0)
            atlas.putpixel((cell * 32 + x, y), ((ink >> 16) & 255, (ink >> 8) & 255, ink & 255, ink >> 24))
atlas.save(root / 'src/main/resources/assets/trevorroll/textures/gui/menu_art.png')
