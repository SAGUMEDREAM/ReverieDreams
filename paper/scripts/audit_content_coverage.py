"""Compare generated Mod resource IDs with configured CE IDs, not runtime functionality.

Requires PyYAML. A matching ID means a definition exists, never that its behavior,
appearance or survival acquisition has passed acceptance.
"""
from pathlib import Path
import json
import yaml


def main():
    root = Path(__file__).resolve().parents[2]
    assets = root / "common/src/generated/resources/assets/reverie_dreams"
    configured = {"items": set(), "blocks": set(), "furniture": set(), "recipes": set()}
    for path in sorted((root / "paper/src/main/craftengine/reverie_dreams/configuration").glob("*.yml")):
        document = yaml.safe_load(path.read_text(encoding="utf-8")) or {}
        for category in ("items", "blocks", "recipes"):
            incoming = set(document.get(category, {}))
            duplicate = configured[category].intersection(incoming)
            if duplicate:
                raise ValueError("Duplicate " + category + ": " + str(sorted(duplicate)))
            configured[category].update(incoming)
        for item_id, definition in document.get("items", {}).items():
            if definition.get("behavior", {}).get("type") == "furniture_item":
                configured["furniture"].add(item_id)
    source = {}
    for category, folder in (("items", "items"), ("blocks", "blockstates")):
        directory = assets / folder
        source[category] = {"reverie_dreams:" + path.relative_to(directory).with_suffix("").as_posix(): path.relative_to(root).as_posix()
                            for path in sorted(directory.rglob("*.json"))}
    items_covered = set(source["items"]).intersection(configured["items"])
    blocks_covered = set(source["blocks"]).intersection(configured["blocks"] | configured["furniture"])
    report = {
        "basis": "Generated item-model and blockstate resource IDs; not a runtime registry or functionality census",
        "configured": {category: len(values) for category, values in configured.items()},
        "sourceItemModelFiles": len(source["items"]), "matchingItemIds": len(items_covered),
        "sourceBlockstateFiles": len(source["blocks"]), "matchingBlockOrFurnitureIds": len(blocks_covered),
        "missingItemDefinitions": [{"id": key, "source": source["items"][key]} for key in sorted(set(source["items"]) - items_covered)],
        "missingBlockOrFurnitureDefinitions": [{"id": key, "source": source["blocks"][key]} for key in sorted(set(source["blocks"]) - blocks_covered)],
        "extraConfiguredItemIds": sorted(configured["items"] - set(source["items"])),
    }
    output = root / "migration/content-definition-coverage.json"
    output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({key: value for key, value in report.items() if not isinstance(value, list)}))


if __name__ == "__main__":
    main()
