# 远古铁系列生成稿

## 护甲物品图标返修 · 2026-11-20

用户指出第一版图标混素。第一版四张游戏 PNG 虽然已经是 16×16，但由大生成稿直接取样，分别有 65/119/84/62 种可见颜色，缺少原生像素色块组织，已被本次返修替代，不视为认可基准。

当前四件护甲物品的**最终可编辑母版**位于 `native_16x16/`，每张都是精确的 16×16 PNG。重新调用内置 ImageGen 得到的四张设计稿在 `generated-16x16-r2/`，同目录 `prompts.json` 保存实际提示词；它们仍是生成设计稿，不能直接投放为游戏贴图。

`import_native_armor_icons.py` 将重新组织过的板甲色块按原生格编码，保持原版透明轮廓，采用这一系列的七个冷灰铁色阶，无插值平滑、无抖色。最终头盔、胸甲、护腿各 7 色，靴子 6 色。此有限色阶只约束本次四张小图标，不成为全项目色数规则。`export_generated_textures.py` 对护甲图标只读取母版，检查尺寸、alpha 和色数，不再缩放第一版大稿；缺失母版会直接失败。

运行 `review_native_armor_icons.py` 可重建原版/旧版/新版的浅深物品槽对照，1倍及6倍最近邻显示。证据在 `data/ancient-iron-icons-16-20261120/`。本次不改变穿戴 UV、工具、模型、属性或冒险内容，没有新增游戏运行测试，也不更新正式 dist。

## 第一版生成稿归档

`generated/` 的 13 张 PNG 来自内置 ImageGen，每一张均以本机 Minecraft 26.1.2 对应原版下界合金图为编辑参考；没有通过亮度滤镜生成新外观。它们是生成原稿，不直接当作游戏 UV 使用。

以下是本系列最终采用的提示规格，整理为可复现的提示词组；不是对早期工具调用逐字转录的声称。

```text
Use case: precise-object-edit.
Asset: Minecraft Java 26 Ancient Iron equipment sprite or armor UV.
Reference: the matching vanilla netherite texture; exact silhouette / UV reference.
Repaint the material as brighter ancient iron, retaining Minecraft logical pixels:
dark graphite outlines, cool slate iron body, restrained brighter silver highlights;
tools retain a warm brown haft. Keep silhouette, orientation, occupied regions,
transparency, armor part placement and logical pixel scale unchanged.
Organized pixel clusters and crisp square edges. No smooth spray gradients,
glow, text, runes, random scratches, speckles, extra geometry or scene.
One asset per call, transparent background.
```

| 生成稿 | 对应原版参考 |
| --- | --- |
| helmet / chestplate / leggings / boots | `textures/item/netherite_<slot>.png` |
| sword / pickaxe / axe / shovel / hoe / spear | `textures/item/netherite_<tool>.png` |
| spear_in_hand | `textures/item/netherite_spear_in_hand.png` |
| humanoid | `textures/entity/equipment/humanoid/netherite.png` |
| humanoid_leggings | `textures/entity/equipment/humanoid_leggings/netherite.png` |

格式导出可执行：

```powershell
python -B tools/scarlet-adventure/export_generated_textures.py --minecraft-client "D:/Gradle/.gradle/caches/fabric-loom/26.1.2/minecraft-client.jar"
```

原稿从本目录读取；参考 alpha 从参数指定的原版客户端 JAR 读取，不依赖本地 `.agent` 协作目录。四件护甲物品使用上述新的原生母版；其他素材维持此前导出。最终游戏贴图在 `common/src/main/resources/assets/reverie_dreams/textures/`。

项目规范采用 `metal.iron` 的铁材质角色，保留用户指定的下界合金轮廓，以较亮的冷灰铁和银色高光形成系列扩展。技术导出、Agent 美术自查、实际游戏验证和用户认可分别记录；当前不将新稿标为用户已认可基准。
