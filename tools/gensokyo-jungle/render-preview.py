"""Render the Java planner's actual voxels. Diagrams, not Minecraft screenshots."""
from pathlib import Path
import importlib.util, json
from PIL import Image, ImageDraw
import numpy as np

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT/'build/gensokyo-jungle-review'
spec = importlib.util.spec_from_file_location('forest_preview', ROOT/'tools/gensokyo-trees/render-preview.py')
renderer = importlib.util.module_from_spec(spec); spec.loader.exec_module(renderer)
renderer.PALETTE['JUNGLE'] = ((121, 99, 67), (77, 126, 59))
LABELS = ['\u677f\u6839\u9ad8\u51a0', '\u4f4e\u4f4d\u5206\u53c9', '\u591a\u5c42\u6811\u51a0', '\u504f\u51a0\u659c\u751f', '\u5bbd\u5c55\u4f1e\u51a0', '\u6797\u4e0b\u5e7c\u6811']

def main():
    all_samples = json.loads((OUT/'tree-geometry.json').read_text())
    samples = all_samples[::3]
    for wood in [False, True]:
        image = Image.new('RGB', (1920, 1100), '#e5ecdc'); draw = ImageDraw.Draw(image)
        title = '\u5e7b\u60f3\u4e61\u4e1b\u6797\uff1a\u516d\u79cd\u6811\u5f62' + (' / \u679d\u5e72\u7ed3\u6784' if wood else '')
        draw.text((30, 18), title, font=renderer.font(30, True), fill='#29412c')
        draw.text((30, 63), 'Exact Java geometry / same scale / three generated samples per profile available in tree-geometry.json', font=renderer.font(17), fill='#4b5a49')
        for i, sample in enumerate(samples):
            col, row = i % 3, i // 3
            x, top = col*640, 105+row*475; baseline = top+365
            draw.rounded_rectangle((x+12, top, x+628, top+465), radius=12, fill='#f5f6ec', outline='#b2c2a7')
            renderer.tree(draw, sample, x+320, baseline, 8.5, only_wood=wood)
            draw.text((x+30, top+380), LABELS[i], font=renderer.font(25, True), fill='#29412c')
            draw.text((x+30, top+418), '%s  |  H=%s  |  %d%%' % (sample['profile'], sample['height'], round(sample['weight']*100)), font=renderer.font(17), fill='#4b5a49')
        draw.text((30, 1070), 'Geometry review only. Existing Minecraft materials; this is not an in-game render or user acceptance.', font=renderer.font(17), fill='#4b5a49')
        image.save(OUT/('branches.png' if wood else 'six-tree-shapes.png'))
    metadata = json.loads((ROOT/'map_source/gensokyo/map.json').read_text())
    layer = np.memmap(ROOT/'map_source/gensokyo/biomes.bin', dtype='u1', mode='r', shape=(metadata['depth'], metadata['width']))
    small = layer[::8, ::8]; colors = np.full((*small.shape, 3), [57, 78, 55], dtype='u1')
    palette = metadata['biome_palette']
    for i, biome in enumerate(palette):
        if 'ocean' in biome or 'river' in biome: colors[small==i] = [57, 106, 160]
    colors[small==palette.index('reverie_dreams:gensokyo_bamboo_forest')] = [130, 159, 70]
    colors[small==palette.index('reverie_dreams:gensokyo_jungle')] = [62, 205, 161]
    image = Image.fromarray(colors); draw = ImageDraw.Draw(image)
    draw.rectangle((0, 0, 1600, 55), fill='#1e3428')
    draw.text((15, 10), '\u9752\u7eff\uff1a\u672c\u6b21\u66ff\u6362\u7684\u5e7b\u60f3\u4e61\u4e1b\u6797  /  \u9ec4\u7eff\uff1a\u4fdd\u7559\u7684\u8ff7\u9014\u7af9\u6797', font=renderer.font(24), fill='white')
    image.save(OUT/'replacement-range.png')

if __name__ == '__main__': main()
