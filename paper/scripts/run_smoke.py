"""Run the built plugin/pack twice on a loopback-only Paper 26.1.2 test server.

Requires Python 3.8+, PyYAML and the existing paper/run server/CE library cache.
The smoke probe is a separate test artifact, never part of the shipped plugin.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import socket
import subprocess
import time
import zipfile

import yaml


def write_json(path, data):
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def inside(path, parent):
    return os.path.normcase(os.path.commonpath([str(path.resolve()), str(parent.resolve())])) == os.path.normcase(str(parent.resolve()))


def verify_world_inputs(root, cache, feature_mode):
    source = root / "common/src/main/resources/data/reverie_dreams/gensokyo"
    metadata = json.loads((source / "map.json").read_text(encoding="utf8"))
    names = ["map.json"] + [metadata["layers"][key] for key in ("heightmap", "biomes", "surfaces")]
    if metadata.get("underground"):
        names.append(metadata["underground"])
    digest = hashlib.sha256()
    for name in names:
        path = source / name
        if not inside(path, source):
            raise ValueError("Source layer path escapes map directory")
        digest.update(name.encode("utf8") + b"\0")
        with path.open("rb") as stream:
            for chunk in iter(lambda: stream.read(1024 * 1024), b""):
                digest.update(chunk)
    stamp = digest.hexdigest()
    marker = json.loads((cache / "manifest.json").read_text(encoding="utf8"))
    if marker["source_sha256"] != stamp:
        raise ValueError("Source map changed: prepare a new cache before starting a world test")
    fixtures = ["gensokyo-native-points.json", "gensokyo-paddy-point.json"]
    if feature_mode != "terrain":
        fixtures += ["gensokyo-first-ore-veins-seed-123456789.json", "gensokyo-active-biome-points.json"]
    for name in fixtures:
        fixture = json.loads((root / "migration/fixtures" / name).read_text(encoding="utf8"))
        if fixture.get("source_sha256") != stamp:
            raise ValueError("Stale source fixture " + name + "; regenerate from the current audited source")


def prepare(root, run, runtime, agree, world=False, world_cache=None, feature_mode="terrain", forest=False,
            test_root=None):
    if not agree:
        raise ValueError("Pass --accept-eula only after accepting https://aka.ms/MinecraftEULA for this test server")
    allowed = test_root or root / "paper/build"
    if not inside(run, allowed) or run.exists():
        raise ValueError("Use a new test directory below " + str(allowed) + "; existing directories are never overwritten")
    if world:
        verify_world_inputs(root, (world_cache or root / "paper/build/world-draft/source-map-cache").resolve(), feature_mode)
    server = runtime / "versions/26.1.2/paper-26.1.2.jar"
    ce_jar = runtime / "plugins/craft-engine-paper-plugin-26.8.2.jar"
    main_jar = root / "paper/build/libs/paper-1.0-SNAPSHOT.jar"
    probe_jar = root / "paper/build/libs/paper-1.0-SNAPSHOT-smoke-probe.jar"
    pack = root / "paper/build/craftengine/reverie_dreams"
    libraries = sorted((runtime / "libraries").rglob("*.jar"))
    for path in (server, ce_jar, main_jar, probe_jar, pack, runtime / "plugins/CraftEngine/libs"):
        if not path.exists():
            raise FileNotFoundError(str(path) + "; build :paper:build and initialize the runtime cache first")
    if not libraries:
        raise ValueError("Paper library cache is empty")
    with zipfile.ZipFile(server) as archive:
        manifest = archive.read("META-INF/MANIFEST.MF").decode("utf-8")
        if "Implementation-Version: 26.1.2-74-" not in manifest:
            raise ValueError("Expected cached Paper build 74")
    run.mkdir(parents=True)
    (run / "tmp").mkdir()
    ce = run / "plugins/CraftEngine"
    ce.mkdir(parents=True)
    for source in (main_jar, probe_jar, ce_jar):
        shutil.copy2(source, run / "plugins" / source.name)
    shutil.copytree(runtime / "plugins/CraftEngine/libs", ce / "libs")
    shutil.copytree(pack, ce / "resources/reverie_dreams")
    with zipfile.ZipFile(ce_jar) as archive:
        if str(yaml.safe_load(archive.read("paper-plugin.yml"))["version"]) != "26.8.2":
            raise ValueError("CraftEngine runtime version mismatch")
        for entry in archive.infolist():
            if entry.is_dir() or not entry.filename.startswith(("resources/internal/", "resources/default_templates/")):
                continue
            target = ce / entry.filename
            if not inside(target, ce / "resources"):
                raise ValueError("Archive entry escapes resources directory")
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(archive.read(entry))
        config = yaml.safe_load(archive.read("config.yml"))
    config.update({"metrics": False, "update-checker": False, "forced-locale": "en"})
    config["block"]["serverside-blocks"] = 6000
    if world:
        config["misc"]["delay-configuration-load"] = False
    resource = config["resource-pack"]
    resource["supported-version"] = {"min": "server", "max": "server"}
    resource["merge-external-folders"] = []
    resource["merge-external-zip-files"] = []
    resource["delivery"].update({"hosting": [], "send-on-join": False, "auto-upload": False, "resend-on-upload": False})
    # Keep this diagnostic output readable by standard ZIP validators.
    for method in resource["protection"]["crash-tools"]:
        resource["protection"]["crash-tools"][method] = False
    (ce / "config.yml").write_text(yaml.safe_dump(config, allow_unicode=True, sort_keys=False), encoding="utf-8")
    (run / "eula.txt").write_text("# Explicitly accepted for this isolated test only.\neula=true\n", encoding="utf-8")
    with socket.socket() as sock:
        sock.bind(("127.0.0.1", 0))
        port = sock.getsockname()[1]
    properties = {"server-ip": "127.0.0.1", "server-port": port, "online-mode": "false",
                  "enable-rcon": "false", "enable-query": "false", "max-players": 2,
                  "view-distance": 2, "simulation-distance": 2, "level-name": "migration_test_world",
                  "level-type": "minecraft:flat", "generate-structures": "false", "spawn-protection": 0,
                  "gamemode": "creative", "difficulty": "peaceful", "pause-when-empty-seconds": -1}
    properties["level-seed"] = 123456789
    properties["generator-settings"] = json.dumps({"biome": "minecraft:plains", "layers": [
        {"block": "minecraft:bedrock", "height": 1}, {"block": "minecraft:dirt", "height": 2},
        {"block": "minecraft:grass_block", "height": 1}]}, separators=(",", ":"))
    bukkit = {"settings": {"allow-end": False}}
    if world:
        world_jar = root / "paper/build/distributions/reverie-dreams-world-bootstrap-1.0-SNAPSHOT.jar"
        datapack = root / "paper/build/distributions/reverie-dreams-world-height-1.0-SNAPSHOT.zip"
        if not datapack.is_file():
            raise FileNotFoundError(datapack)
        shutil.copy2(world_jar, run / "plugins" / world_jar.name)
        target = run / "migration_test_world/datapacks"
        target.mkdir(parents=True)
        shutil.copy2(datapack, target / "reverie-dreams-world.zip")
        properties["initial-enabled-packs"] = "vanilla,file/reverie-dreams-world.zip"
        world_name = "migration_test_world_reverie_dreams_gensokyo"
        bukkit["worlds"] = {world_name: {"generator": "ReverieDreamsWorld:gensokyo"}}
        world_data = run / "plugins/ReverieDreamsWorld"
        world_data.mkdir()
        cache = (world_cache or root / "paper/build/world-draft/source-map-cache").resolve()
        if not (cache / "manifest.json").is_file():
            raise FileNotFoundError("Prepare source map cache first: " + str(cache))
        (world_data / "config.yml").write_text(yaml.safe_dump({"enabled": True, "acknowledge-terrain-only-gaps": True,
            "world-name": world_name, "source-directory": (root / "common/src/main/resources/data/reverie_dreams/gensokyo").as_posix(),
            "cache-directory": cache.as_posix(), "features": {"native-biome-decoration": feature_mode == "all",
            "terrain-relative-ores": feature_mode != "terrain", "source-ore-placements": feature_mode == "all",
            "source-forest-trees": forest}}, sort_keys=False), encoding="utf-8")
        (run / "world-feature-mode.txt").write_text(feature_mode, encoding="ascii")
        for name in ("gensokyo-native-points.json", "gensokyo-paddy-point.json"):
            shutil.copy2(root / "migration/fixtures" / name, run / name)
        if feature_mode != "terrain":
            for name in ("gensokyo-first-ore-veins-seed-123456789.json", "gensokyo-active-biome-points.json"):
                shutil.copy2(root / "migration/fixtures" / name, run / name)
            shutil.copy2(root / "common/src/main/resources/data/reverie_dreams/gensokyo/underground.json", run / "gensokyo-authored-underground.json")
    (run / "server.properties").write_text("".join(str(k) + "=" + str(v) + "\n" for k, v in properties.items()), encoding="utf-8")
    (run / "bukkit.yml").write_text(yaml.safe_dump(bukkit, sort_keys=False), encoding="utf-8")
    classpath = os.pathsep.join(path.as_posix() for path in [server] + libraries)
    arguments = ["-XX:ActiveProcessorCount=2", "-Xms512M", "-Xmx2G", '"-Djava.io.tmpdir=' + (run / "tmp").as_posix() + '"', "-Dfile.encoding=UTF-8",
                 "-Dterminal.jline=false", "-Dterminal.ansi=false", "--enable-native-access=ALL-UNNAMED",
                 "-cp", '"' + classpath + '"', "org.bukkit.craftbukkit.Main", "nogui"]
    (run / "launch.args").write_text("\n".join(arguments) + "\n", encoding="utf-8")
    original_eula = runtime / "eula.txt"
    environment = {"paper": "26.1.2-74", "craftengine": "26.8.2", "bind": "127.0.0.1", "port": port,
                   "original_eula": str(original_eula), "original_eula_sha256": hashlib.sha256(original_eula.read_bytes()).hexdigest(),
                   "plugin_sha256": hashlib.sha256(main_jar.read_bytes()).hexdigest(), "directory": str(run), "world_probe": world,
                   "world_feature_mode": feature_mode, "world_forest_trees": forest, "level_seed": 123456789}
    write_json(run / "test-environment.json", environment)
    return environment


def run_stage(run, stage, startup_timeout, probe_timeout, shutdown_timeout):
    log = run / (stage + "-console.log")
    report = {"stage": stage, "ready": False, "probe": None, "commands": [], "error": None}
    started = time.monotonic()

    def text():
        return log.read_text(encoding="utf-8", errors="replace") if log.exists() else ""

    with log.open("w", encoding="utf-8") as output:
        process = subprocess.Popen(["java", "@launch.args"], cwd=str(run), stdin=subprocess.PIPE,
                                   stdout=output, stderr=subprocess.STDOUT, text=True, encoding="utf-8")
        (run / "java.pid").write_text(str(process.pid), encoding="ascii")

        def wait_for(predicate, timeout, step):
            beginning = time.monotonic()
            next_progress = beginning
            while process.poll() is None and time.monotonic() - beginning < timeout:
                if predicate():
                    return
                now = time.monotonic()
                if now >= next_progress:
                    print(json.dumps({"stage": stage, "step": step, "seconds": round(now - beginning),
                                      "latest": text().splitlines()[-1:]}, ensure_ascii=True), flush=True)
                    next_progress = now + 15
                time.sleep(1)
            raise TimeoutError(step + ": timed out or Java exited")

        try:
            wait_for(lambda: "Done (" in text(), startup_timeout, "server startup")
            report["ready"] = True
            probe = run / ("probe-" + stage + "-result.yml")
            wait_for(probe.is_file, probe_timeout, "API probe and real furnaces")
            report["probe"] = yaml.safe_load(probe.read_text(encoding="utf-8"))
            if report["probe"]["status"] != "PASS":
                raise AssertionError(report["probe"].get("failure", "Probe failed"))
            reload_started = time.time()
            for command in ("plugins", "version ReverieDreams", "ce reload all"):
                process.stdin.write(command + "\n")
                process.stdin.flush()
                report["commands"].append(command)
            diagnostic = run / "plugins/CraftEngine/generated/resource_pack.zip"
            wait_for(lambda: diagnostic.is_file() and diagnostic.stat().st_size > 100
                     and diagnostic.stat().st_mtime >= reload_started, 180, "resource pack generation")
            # Wait for the workflow to finish writing both archives.
            wait_for(lambda: "Resource pack generated" in text().split("Generating resource pack...")[-1], 30, "resource pack workflow")
            with zipfile.ZipFile(diagnostic) as archive:
                if archive.testzip() is not None:
                    raise AssertionError("Diagnostic client pack CRC check failed")
                report["client_pack_files"] = len([entry for entry in archive.infolist() if not entry.is_dir()])
                report["pack_metadata"] = json.loads(archive.read("pack.mcmeta"))
                report["custom_models"] = len([name for name in archive.namelist() if name.startswith("assets/reverie_dreams/models/") and name.endswith(".json")])
        except Exception as failure:
            report["error"] = str(failure)
        finally:
            if process.poll() is None:
                try:
                    # The probe releases its generation tickets before publishing its result.
                    # Let Paper cancel no-longer-requested neighbor work before entering stopServer.
                    settle_until = time.monotonic() + 5
                    while process.poll() is None and time.monotonic() < settle_until:
                        time.sleep(0.5)
                    # stopServer performs the authoritative world/player flush;
                    # an extra synchronous save-all first duplicates this large stress-world save.
                    process.stdin.write("stop\n")
                    process.stdin.flush()
                    shutdown_started = time.monotonic()
                    while process.poll() is None:
                        remaining = shutdown_timeout - (time.monotonic() - shutdown_started)
                        if remaining <= 0:
                            raise subprocess.TimeoutExpired(process.args, shutdown_timeout)
                        try:
                            process.wait(timeout=min(15, remaining))
                        except subprocess.TimeoutExpired:
                            print(json.dumps({"stage": stage, "step": "server save and shutdown",
                                "seconds": round(time.monotonic() - shutdown_started), "latest": text().splitlines()[-1:]}, ensure_ascii=True), flush=True)
                except (subprocess.TimeoutExpired, BrokenPipeError, OSError):
                    report["error"] = report["error"] or ("Server did not finish a clean save/shutdown within " + str(shutdown_timeout) + " seconds")
                    process.terminate()
                    try:
                        process.wait(timeout=10)
                    except subprocess.TimeoutExpired:
                        process.kill()
                        process.wait()
            report["exit_code"] = process.returncode
    lines = text().splitlines()
    report["server_errors"] = [line for line in lines if " ERROR]:" in line]
    report["config_issues"] = [line for line in lines if re.search(r"Found [1-9]\d* issue", line)]
    report["duration_seconds"] = round(time.monotonic() - started)
    report["status"] = "PASS" if not report["error"] and report["exit_code"] == 0 and not report["server_errors"] and not report["config_issues"] else "FAIL"
    write_json(run / (stage + "-result.json"), report)
    print(json.dumps({"stage": stage, "status": report["status"], "error": report["error"],
                      "checks": len((report["probe"] or {}).get("checks", []))}, ensure_ascii=True), flush=True)
    return report


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--accept-eula", action="store_true")
    parser.add_argument("--run-dir", type=Path)
    parser.add_argument("--runtime-root", type=Path)
    parser.add_argument("--startup-timeout", type=int, default=600)
    parser.add_argument("--probe-timeout", type=int, default=600)
    parser.add_argument("--shutdown-timeout", type=int, default=600)
    parser.add_argument("--world", action="store_true", help="Install the separate 1536-height datapack/world plugin and verify source terrain")
    parser.add_argument("--world-cache", type=Path)
    parser.add_argument("--world-feature-mode", choices=("terrain", "ore", "all"), default="terrain")
    parser.add_argument("--world-forest-trees", action="store_true", help="Enable the explicitly documented post-native source forest adaptation")
    parser.add_argument("--test-root", type=Path, help="Only run directories below this disposable root are accepted")
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[2]
    test_root = (args.test_root or root / "paper/build").resolve()
    run = (args.run_dir or test_root / ("smoke-" + time.strftime("%Y%m%d-%H%M%S"))).resolve()
    runtime = (args.runtime_root or root / "paper/run").resolve()
    if not args.world and args.world_feature_mode != "terrain": parser.error("--world-feature-mode requires --world")
    if args.world_forest_trees and not args.world: parser.error("--world-forest-trees requires --world")
    environment = prepare(root, run, runtime, args.accept_eula, args.world, args.world_cache, args.world_feature_mode, args.world_forest_trees, test_root)
    print(json.dumps(environment, ensure_ascii=True), flush=True)
    reports = [run_stage(run, "initial", args.startup_timeout, args.probe_timeout, args.shutdown_timeout)]
    if reports[0]["status"] == "PASS":
        reports.append(run_stage(run, "restart", args.startup_timeout, args.probe_timeout, args.shutdown_timeout))
    unchanged = hashlib.sha256(Path(environment["original_eula"]).read_bytes()).hexdigest() == environment["original_eula_sha256"]
    with socket.socket() as sock:
        sock.settimeout(2)
        closed = sock.connect_ex(("127.0.0.1", environment["port"])) != 0
    result = {"environment": environment, "stages": reports, "original_eula_unchanged": unchanged, "test_port_closed": closed}
    result["status"] = "PASS" if len(reports) == 2 and all(report["status"] == "PASS" for report in reports) and unchanged and closed else "FAIL"
    write_json(run / "smoke-result.json", result)
    print(json.dumps({"status": result["status"], "report": str(run / "smoke-result.json")}), flush=True)
    return 0 if result["status"] == "PASS" else 1


if __name__ == "__main__":
    raise SystemExit(main())
