"""Build the independent Paper26.1.2 world probe without touching shared Gradle state."""
from pathlib import Path
import argparse
import hashlib
import json
import os
import subprocess
import sys
import uuid
import zipfile

ROOT = Path(__file__).resolve().parents[2]
PAPER = ROOT / 'paper'
SERVER = PAPER / 'run/versions/26.1.2/paper-26.1.2.jar'
API = PAPER / 'run/libraries/io/papermc/paper/paper-api/26.1.2.build.74-stable/paper-api-26.1.2.build.74-stable.jar'
DATA = ROOT / 'common/src/main/resources/data/reverie_dreams/gensokyo'
DATAPACK = PAPER / 'src/worldBootstrap/datapack'
RESOURCES = PAPER / 'src/worldBootstrap/resources'


def sha(path):
    result = hashlib.sha256()
    with path.open('rb') as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b''):
            result.update(chunk)
    return result.hexdigest()


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf8')


def author_datapack():
    with zipfile.ZipFile(SERVER) as archive:
        version = json.loads(archive.read('version.json'))
        if version['id'] != '26.1.2' or version['pack_version']['data_major'] != 101 or version['pack_version']['data_minor'] != 1:
            raise RuntimeError('Unexpected target version; re-audit the exact runtime before authoring')
        vanilla = json.loads(archive.read('data/minecraft/worldgen/noise_settings/overworld.json'))
    source_type = ROOT / 'common/src/generated/resources/data/reverie_dreams/dimension_type/gensokyo.json'
    dimension_type = json.loads(source_type.read_text(encoding='utf8'))
    assert dimension_type['min_y'] == 0 and dimension_type['height'] == dimension_type['logical_height'] == 1536
    write_json(DATAPACK / 'pack.mcmeta', {'pack': {
        'description': 'ReverieDreams Gensokyo1536-height metadata; independent Paper map generator required',
        'min_format': [101, 1], 'max_format': [101, 1]}})
    write_json(DATAPACK / 'data/reverie_dreams/dimension_type/gensokyo.json', dimension_type)
    # A correctly sized native generator is required even while Paper delegates
    # actual terrain writes to the source-map generator. Without the plugin this
    # intentionally produces void, never a fake flat/vanilla Gensokyo map.
    native = {
        'aquifers_enabled': False, 'default_block': {'Name': 'minecraft:stone'},
        'default_fluid': {'Name': 'minecraft:air'}, 'disable_mob_generation': True,
        'legacy_random_source': False,
        'noise': {'min_y': 0, 'height': 1536, 'size_horizontal': 1, 'size_vertical': 2},
        'noise_router': {key: (-1.0 if key == 'final_density' else 0.0) for key in vanilla['noise_router']},
        'ore_veins_enabled': False, 'sea_level': 1024, 'spawn_target': [],
        'surface_rule': {'type': 'minecraft:block', 'result_state': {'Name': 'minecraft:air'}},
    }
    write_json(DATAPACK / 'data/reverie_dreams/worldgen/noise_settings/gensokyo_delegate.json', native)
    write_json(DATAPACK / 'data/reverie_dreams/dimension/gensokyo.json', {
        'type': 'reverie_dreams:gensokyo', 'generator': {
            'type': 'minecraft:noise', 'settings': 'reverie_dreams:gensokyo_delegate',
            'biome_source': {'type': 'minecraft:fixed', 'biome': 'minecraft:plains'}}})
    write_json(DATAPACK / 'world-version-lock.json', {
        'minecraft': '26.1.2', 'paper_api': '26.1.2.build.74-stable', 'data_format': [101, 1],
        'paper_server_sha256': sha(SERVER), 'paper_api_sha256': sha(API),
        'original_dimension_type_sha256': sha(source_type),
        'source_files': {p.name: sha(p) for p in sorted(DATA.iterdir()) if p.is_file()},
        'scope': 'terrain-only; custom paddy binding and native/custom decorations/ore pipeline remain incomplete',
    })


def zip_entries(target, entries):
    with zipfile.ZipFile(target, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
        for name, value in sorted(entries.items()):
            info = zipfile.ZipInfo(name, (1980, 1, 1, 0, 0, 0))
            info.compress_type = zipfile.ZIP_STORED if name.endswith('.gz') else zipfile.ZIP_DEFLATED
            info.external_attr = 0o100644 << 16
            archive.writestr(info, value)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--draft', action='store_true', help='Compile isolated world-draft Java instead of published sourceSet')
    parser.add_argument('--author-datapack', action='store_true', help='Explicitly regenerate reviewable target-version datapack sources')
    parser.add_argument('--version', default='1.0-SNAPSHOT')
    options = parser.parse_args()
    if options.author_datapack:
        author_datapack()
    java_root = PAPER / ('build/world-draft/java' if options.draft else 'src/worldBootstrap/java')
    java_files = sorted(java_root.rglob('*.java'))
    if not java_files:
        raise RuntimeError('World Java sourceSet is not published; use --draft during authoring')
    forbidden = ['import net.minecraft.', 'import net.fabricmc.', 'import net.neoforged.', 'import net.momirealms.']
    for path in java_files:
        if any(token in path.read_text(encoding='utf8') for token in forbidden):
            raise RuntimeError('Non-Paper engine/Mod/CE dependency in world bootstrap: ' + str(path))
    workspace = PAPER / 'build/world-artifacts' / str(uuid.uuid4())
    classes = workspace / 'classes'
    classes.mkdir(parents=True)
    libraries = sorted((PAPER / 'run/libraries').rglob('*.jar'))
    if not API.is_file():
        raise RuntimeError('Exact Paper74 API JAR missing')
    arguments = ['--release', '25', '-encoding', 'UTF-8', '-classpath', os.pathsep.join(p.resolve().as_posix() for p in libraries),
                 '-d', classes.resolve().as_posix()] + [p.resolve().as_posix() for p in java_files]
    argfile = workspace / 'javac.args'
    argfile.write_text('\n'.join('"' + item + '"' for item in arguments), encoding='utf8')
    result = subprocess.run(['javac', '@' + str(argfile)], capture_output=True)
    if result.returncode:
        sys.stderr.buffer.write(result.stderr)
        raise RuntimeError('World bootstrap compilation failed')
    output = PAPER / ('build/world-draft/distributions' if options.draft else 'build/distributions')
    output.mkdir(parents=True, exist_ok=True)
    bootstrap = output / ('reverie-dreams-world-bootstrap-' + options.version + '.jar')
    datapack = output / ('reverie-dreams-world-height-' + options.version + '.zip')
    data = output / ('reverie-dreams-world-map-data-' + options.version + '.zip')
    jar = {p.relative_to(classes).as_posix(): p.read_bytes() for p in classes.rglob('*.class')}
    jar['LICENSE'] = (ROOT / 'LICENSE').read_bytes()
    for path in RESOURCES.rglob('*'):
        if path.is_file():
            jar[path.relative_to(RESOURCES).as_posix()] = path.read_text(encoding='utf8').replace('${version}', options.version).encode('utf8')
    zip_entries(bootstrap, jar)
    attribution = ROOT / 'authorized/gensokyo-map-dimensions.md'
    shared = {'LICENSE': (ROOT / 'LICENSE').read_bytes(), 'META-INF/authorized/' + attribution.name: attribution.read_bytes()}
    entries = {p.relative_to(DATAPACK).as_posix(): p.read_bytes() for p in DATAPACK.rglob('*') if p.is_file()}
    zip_entries(datapack, {**entries, **shared})
    zip_entries(data, {**{'map/' + p.name: p.read_bytes() for p in DATA.iterdir() if p.is_file()}, **shared})
    for artifact in [bootstrap, datapack, data]:
        print(json.dumps({'artifact': str(artifact.relative_to(ROOT)), 'bytes': artifact.stat().st_size, 'sha256': sha(artifact)}))


if __name__ == '__main__':
    main()
