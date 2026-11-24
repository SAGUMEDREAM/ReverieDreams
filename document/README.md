# ReverieDreams 玩家 Wiki

VitePress + Vue，面向当前仓库的 **Minecraft 26.1.2 / 0.6.9+26.1.2** 模组内容。中文和英文攻略、物品/方块/实体图鉴、配方和用途查询共用同一份事实数据。不要手工维护第二套物品 ID、名称、配方数量或属性。

## 预览与构建

```sh
cd document
npm ci
npm run docs:dev
npm run docs:build
npm run docs:preview
```

重建静态站点后请重启已运行的 preview 服务，避免其保留上一版资源文件清单。开发服务绑定本机；生产文件在 `docs/.vitepress/dist/`。项目子路径部署时使用 `WIKI_BASE=/your-project/`（须以 `/` 结尾）再构建。PowerShell 示例：`$env:WIKI_BASE='/wiki/'; npm run docs:build`。不设置则部署在站点根目录。

`docs:dev` / `docs:build` 会先同步资源。需要本地对应版本的 Minecraft 客户端 JAR：自动查找 Gradle 缓存，或设置 `MINECRAFT_CLIENT_JAR`。同步不会联网下载游戏资源。

Gradle 的 `:document:docsDev`、`:document:docsBuild` 继续调用上述 npm 脚本。

## 模组改动后的更新流程

1. 使用项目要求的 Java 25 和 Gradle 环境。
2. `npm run sync:runtime`：启动临时 Fabric 专用服务器，导出真实注册表、组件、创造栏款式、实体基础属性、标签及模组配方，然后自动关闭。
3. `npm run sync`：合并 main/generated 资源、游戏资源与运行时快照，更新模型、名称、所有配方和反向用途索引。
4. 核对受改动影响的中英文攻略，再运行 `npm test`、`npm run verify`、`npm run docs:build`。
5. 构建后运行 `node scripts/browser-qa.mjs` 做浏览器回归；默认自动启动并关闭独立本机静态服务，避免旧 preview 的文件缓存。Windows 默认使用本机 Edge。可设置 `EDGE_PATH` 和 `WIKI_QA_URL`。其他系统提供支持 Chrome DevTools Protocol 的 Chromium 路径。

`sync:runtime` 的临时世界、EULA、编译探针和日志都在忽略的 `.runtime/` 下，固定绑定 `127.0.0.1:25629`，不打开玩家世界。端口被占用时先关闭上次导出，不要改用真实服务器目录。导出成功必须包含 `WIKI_EXPORT_PASS`；脚本同时检查 Gradle 退出码，并记录源码在导出期间是否稳定。可把额外 Gradle 参数附在命令后。

提交需要保留 `data/runtime.json`、`data/runtime-source.json`、`data/coverage.json` 和 Wiki 使用的生成资源。`verify` 对比源码/数据包摘要，过期或并行修改会给出明确提示，便于多人开发时继续编辑和预览 Wiki。发布前使用 `npm run verify -- --strict-source`；严格模式会拒绝过期或导出期间不稳定的源码快照。应在源码稳定后重跑导出，不能手工改摘要假装同步完成。

## 数据来源与边界

- `scripts/runtime/WikiExportProbe.java` 从真正运行的模组读取物品、默认组件、方块默认状态、实体构造基础属性、创造栏和自定义配方。实体属性不包含装备或难度加成。
- `scripts/sync-wiki.mjs` 按 **main 覆盖 generated** 的资源优先级读取数据；Minecraft 命名空间从匹配客户端 JAR 读取。标签以服务器实际加载结果为准。
- 当前快照：907 个模组物品、495 个方块、149 个实体、13 个创造栏；366 条原版格式模组配方和 725 条自定义/生成配方，合计 1091 条。内部界面/调试物品有标注，注册数量不等于生存物品数量。
- 食品/饮料默认属性从实际加载后的运行时缓存读取（包含代码回调追加），交易从实际数据文件解析；成品的显式属性优先。所有战利品表只存一份，物品持引用，不把随机记录说成必掉。
- 创造栏及配方中的组件款式会保留；角色卡 ID、染色、形状和轨迹不能仅按基础物品 ID 混在一起。
- 客户端 JAR 内置英文。`data/minecraft-zh_cn.json` 来自本地官方 26.1.2 资产索引 `26.1.2-30` 的 `minecraft/lang/zh_cn.json`，SHA-1 `bd3b3df108c1eaf78b6b8904c9f0842a7b110d5c`，用于原版材料准确中文名；更新游戏版本时必须一并刷新。同步会检查其版本元数据和原始 SHA-1。游戏资源归其原权利人。
- Wiki 描述仓库内置内容，服务器数据包、配置和客户端资源包覆盖不在快照内。没有配方不能直接推断为创造专属。

## 模型展示

解析 MC 26 的 `assets/<namespace>/items/` 定义、组合/条件分支、模型父级、纹理变量、GUI 变换、元素旋转、UV、染色及动画元数据。真实长方体模型使用 Canvas 纹理面渲染；generated 模型保留图层。方块按实际默认状态选模型，料理可分别查看背包与摆盘外观。不会根据模型文件名创造物品，也不会拿 particle 纹理替代完整模型。

预览是 Wiki 的静态/可旋转模型查看器，不运行 Minecraft 的实体、手持动画、方块实体或 YSM 专用渲染器。原版特殊渲染项有明确的不可预览状态；角色实体图鉴显示真实属性和装备，不伪造其动态模型。生成贴图目前展示正反面，没有按透明轮廓生成所有侧缘挤出面。

水田的原模组译名缺失，Wiki 使用经方块源码核对的说明性名称并在详情标明；不修改游戏语言文件。

当前模组资源本身有四类缺失引用：

- `minecraft:keyboard`、`minecraft:table` 父模型不存在；模型自身已有几何时保留自身数据，不猜测一个替代父级。
- `minecraft:missingno`、`minecraft:all` 贴图引用不存在；实际用到的面显示与游戏含义一致的紫黑缺失纹理，不静默换成其他图。

`coverage.json` 记录这些来源问题，`verify` 只允许这份已核对清单，出现新缺失就报错。修改模组源资源解决后，重新同步即可移除相应记录；Wiki 不改游戏资源掩盖问题。

## 组件

- `<ItemCatalog category="food" />`：真实注册物品，可按名称/ID/分类/创造栏检索。
- `<BlockCatalog />`、`<EntityCatalog />`：真实方块和实体清单。
- `<AltarStructure />`：从祭坛源码读取柱位、层高与材料，显示真实方块搭建场景，支持旋转、俯视、分层、坐标点选和手机缩放。网格仅用于定位；`npm run sync:altar` 可使用现有注册模型独立刷新，全量 `sync` 也会更新。
- `<RecipeCatalog type="reverie_dreams:barrel" />`：指定类型的完整配方查询。
- `<McItem id="reverie_dreams:power" :count="3" :show-name="true" />`：同步名称、模型和详情入口。
- `<AltarRecipe recipe="namespace:recipe_id" />`、`<BarrelRecipe recipe="namespace:recipe_id" />`：按真实配方 ID 展示；不支持编造材料覆盖。
- `<SourceNote :files='["common/src/..."]' />`：攻略底部的核对依据。

测试覆盖注册覆盖率、配方引用、祭坛槽位、计时单位、组件款式、中文材料名、交易/属性、模型面与贴图完整性、几何/动画帧和攻略来源。浏览器截图与报告在 `.runtime/qa/`，不随网站发布。
