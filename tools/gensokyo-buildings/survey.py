"""Read-only Litematica and current-map survey; never publishes game data."""
import gzip
import hashlib
import io
import json
import struct
from pathlib import Path

import numpy as np

ROOT = Path(__file__).resolve().parents[2]


def read_nbt(path, block_callback=None):
    data = Path(path).read_bytes()
    stream = io.BytesIO(gzip.decompress(data) if data[:2] == b'\x1f\x8b' else data)

    def number(fmt):
        return struct.unpack('>' + fmt, stream.read(struct.calcsize('>' + fmt)))[0]

    def string():
        return stream.read(number('H')).decode('utf8')

    def payload(tag, root=False):
        if tag in range(1, 7):
            return number({1: 'b', 2: 'h', 3: 'i', 4: 'q', 5: 'f', 6: 'd'}[tag])
        if tag == 7:
            return stream.read(number('i'))
        if tag == 8:
            return string()
        if tag == 9:
            child, size = number('B'), number('i')
            return [payload(child) for _ in range(size)]
        if tag == 10:
            result = {}
            while True:
                child = number('B')
                if not child:
                    return result
                key = string()
                if root and key == 'blocks' and block_callback is not None:
                    assert child == 9 and number('B') == 10
                    length = number('i')
                    for _ in range(length):block_callback(payload(10))
                    result[key] = length
                else:
                    result[key] = payload(child)
        if tag in (11, 12):
            size = number('i')
            return np.frombuffer(stream.read(size * (4 if tag == 11 else 8)), '>i4' if tag == 11 else '>i8').copy()
        raise ValueError('Unknown NBT tag: ' + str(tag))

    assert number('B') == 10
    string()
    return payload(10, True)


def unpack_region(region):
    """Litematica packs contiguous YZX indices, including values crossing longs."""
    dims = np.array([region['Size'][k] for k in ('x', 'y', 'z')], dtype=int)
    size = np.abs(dims)
    palette = region['BlockStatePalette']
    bits = max(2, (len(palette) - 1).bit_length())
    words = region['BlockStates'].astype('uint64')
    i = np.arange(int(np.prod(size)), dtype='uint64') * bits
    word, shift = i // 64, i % 64
    values = words[word] >> shift
    crosses = shift + bits > 64
    values[crosses] |= words[word[crosses] + 1] << (64 - shift[crosses])
    values = (values & ((1 << bits) - 1)).astype('uint16')
    assert int(values.max()) < len(palette)
    # Negative selection sizes extend from Position towards the negative axis.
    minimum = np.array([region['Position'][k] for k in ('x', 'y', 'z')]) + np.minimum(dims + 1, 0)
    return minimum, values.reshape((size[1], size[2], size[0])), palette


def load_litematic(path):
    root = read_nbt(path)
    regions = [(name, *unpack_region(r), r) for name, r in root['Regions'].items()]
    low = np.min([r[1] for r in regions], axis=0)
    high = np.max([r[1] + np.array([r[2].shape[2], r[2].shape[0], r[2].shape[1]]) for r in regions], axis=0)
    size = high - low
    states = np.zeros((size[1], size[2], size[0]), 'uint16')
    palette = [{'Name': 'minecraft:air'}]
    lookup = {json.dumps(palette[0], sort_keys=True): 0}
    stats = []
    # Preserve region insertion order, including explicit air in overlaps.
    for name, origin, arr, local_palette, raw in regions:
        mapping = []
        for state in local_palette:
            key = json.dumps(state, sort_keys=True)
            if key not in lookup:
                lookup[key] = len(palette)
                palette.append(state)
            mapping.append(lookup[key])
        x, y, z = origin - low
        mapped = np.asarray(mapping, 'uint16')[arr]
        states[y:y+arr.shape[0], z:z+arr.shape[1], x:x+arr.shape[2]] = mapped
        counts = np.bincount(arr.ravel(), minlength=len(local_palette))
        stats.append({'name': name, 'minimum': origin.tolist(), 'size': [arr.shape[2], arr.shape[0], arr.shape[1]],
                      'non_air': int(sum(n for s, n in zip(local_palette, counts) if s['Name'] not in ('minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'))),
                      'block_entities': len(raw.get('TileEntities', [])), 'entities': len(raw.get('Entities', []))})
    return root, low, states, palette, stats


def write_survey(path, output):
    from PIL import Image, ImageDraw, ImageFont
    output = Path(output)
    output.mkdir(parents=True, exist_ok=True)
    root, origin, states, palette, stats = load_litematic(path)
    solid = ~np.isin(states, [i for i, state in enumerate(palette) if state['Name'] in ('minecraft:air', 'minecraft:cave_air', 'minecraft:void_air')])
    counts = np.sum(solid, axis=(1, 2))
    top = np.max(np.where(solid, np.arange(states.shape[0])[:, None, None], -1), axis=0)
    colors = []
    for state in palette:
        name = state['Name']
        color = (185, 183, 171)
        if any(k in name for k in ('deepslate', 'blackstone', 'dark_oak')): color = (65, 60, 61)
        if any(k in name for k in ('quartz', 'diorite', 'white', 'smooth_stone')): color = (217, 213, 200)
        if any(k in name for k in ('red_', 'crimson', 'nether_brick')): color = (151, 45, 50)
        if any(k in name for k in ('grass', 'leaves', 'moss', 'fern')): color = (81, 135, 65)
        if 'water' in name: color = (47, 111, 181)
        if 'air' in name: color = (28, 38, 48)
        colors.append(color)
    z, x = np.indices(top.shape)
    image = np.asarray(colors, 'uint8')[states[np.maximum(top, 0), z, x]]
    Image.fromarray(image).resize((top.shape[1]*5, top.shape[0]*5), Image.NEAREST).save(output/'mansion-top.png')
    np.savez_compressed(output/'mansion-survey.npz', states=states, top=top)
    result = {'source': str(path), 'source_sha256': hashlib.sha256(Path(path).read_bytes()).hexdigest(),
              'metadata': root['Metadata'], 'source_data_version': root['MinecraftDataVersion'],
              'minimum': origin.tolist(), 'size': [states.shape[2], states.shape[0], states.shape[1]],
              'regions': stats, 'blocks_per_y': counts.tolist(), 'combined_non_air': int(solid.sum()),
              'palette': palette}
    (output/'mansion-survey.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf8')
    print(json.dumps({k: v for k,v in result.items() if k not in ('palette','blocks_per_y','metadata')}, ensure_ascii=False))


if __name__ == '__main__':
    import sys
    write_survey(sys.argv[1], sys.argv[2])
