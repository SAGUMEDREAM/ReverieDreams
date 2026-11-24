# Gensokyo Map Editor

P4 开发用地图编辑器。零依赖，只用 Node.js 内置模块。

## 启动

```powershell
node world-editor-web/server.mjs
```

或者：

```powershell
cd world-editor-web
npm start
```

打开：

```text
http://127.0.0.1:3210
```

如果 3210 也被占用，可以用 `PORT` 覆盖：

```powershell
$env:PORT=3211; node world-editor-web/server.mjs
```

默认只监听 `127.0.0.1`，不会暴露到局域网。

大地图默认单边上限 32768、总格数 134,217,728；可通过 `GENSOYO_EDITOR_MAX_MAP_SIZE`、`GENSOYO_EDITOR_MAX_MAP_CELLS`、`GENSOYO_EDITOR_MAX_BODY_MB` 调整。12800×10240 约需 655 MB 图层内存，浏览器/Java 都需要足够内存。

`public/mc_assets/` 是从当前 Minecraft client jar 与 `common/src/main/resources/assets/reverie_dreams` 提取的 blockstate / model / texture 静态资源；更换 Minecraft 版本后需要重新提取。

## 协作房间 / 密码 / 权限

鉴权默认关闭，不配置任何密码时与旧版完全一致，现有 3210 标签页不受影响。

需要房间密码时，在启动前设置环境变量：

```powershell
$env:GENSOYO_EDITOR_PASSWORD = "editor-pass"
$env:GENSOYO_EDITOR_VIEWER_PASSWORD = "viewer-pass"
$env:GENSOYO_EDITOR_ADMIN_PASSWORD = "admin-pass"
$env:GENSOYO_EDITOR_ROOM = "gensokyo"
$env:GENSOYO_EDITOR_SESSION_TTL_MS = "43200000"
node world-editor-web/server.mjs
```

- `GENSOYO_EDITOR_PASSWORD`：编辑者密码，必须配置才能真正开启鉴权。
- `GENSOYO_EDITOR_VIEWER_PASSWORD`：只读密码；只读角色只能查看地图/图层/结构/协作者，不能保存、绘制、修改结构或调用写接口。
- `GENSOYO_EDITOR_ADMIN_PASSWORD`：管理员密码；管理员可以编辑，也可以把协作者移出房间并使其登录会话失效。
- `GENSOYO_EDITOR_ROOM`：房间名，默认 `gensokyo`。
- `GENSOYO_EDITOR_SESSION_TTL_MS`：会话有效期，默认 12 小时。

登录后会写入 HttpOnly 会话 Cookie；服务重启后需要重新登录。登录尝试按 IP 限流（每分钟最多 10 次失败）。协作操作按客户端限流，默认每分钟 1200 次，可用 `GENSOYO_EDITOR_OP_LIMIT` 调整。访客可以读取地图和建立只读协作连接，所有写操作在服务端返回 403。管理员在协作者列表旁有“移出”按钮，踢出会撤销对方会话。

## 数据流

```text
map_source/gensokyo/{map.json, heightmap.bin, biomes.bin, surfaces.bin, underground.json}
        ↓ 编辑器「保存」
map_source/gensokyo/{map.json, *.bin, underground.json}
        ↓ 编辑器「同步到 resources」（编码压缩）
common/src/main/resources/data/reverie_dreams/gensokyo/{map.json, heightmap.delta.gz, biomes.pal4.gz, surfaces.pal4.gz, underground.json}
        ↓ 重启/重新构建客户端
GensokyoChunkGenerator
```

编辑器只修改 source；保存会更新 `map.json` 和 raw `.bin` 图层。「同步到 resources」会把 source 编码成游戏运行时使用的压缩图层（heightmap 差分 + palette4 gzip），不再复制 raw。「从 common 导入」会解码 runtime 的压缩图层回 source 的 raw `map.json` + `.bin`。

## 当前功能

- World Editor 式布局：顶部菜单与工具栏、左侧小地图、中央 2D/3D 视口、右侧 Minecraft 物品栏式 Tool Palette、底部热键栏/状态栏
- 右侧 5 个 Tab：地形 / 方块 / 结构 / 光照 / 地图
- Minecraft 风格：深板岩背景、方块物品栏格子、底部热键栏、像素边框和工具提示
- 底部 Minecraft 热键栏：快速切换笔刷/矩形/吸管/高度工具/当前方块/地下切片
- 高度层：抬升、降低、平滑、压平、设为、挖空（void），笔刷半径 0–512（可直接输入精确值）
- 笔刷强度 1–10：控制高度抬升/降低幅度，并影响平滑、压平从笔刷中心到边缘的渐变强度
- 群系/地表笔刷为幂等写入，使用按行圆盘扫描且不维护逐格 visited 集合，大半径（≥64）涂抹也不掉帧；调色板颜色只按索引解析一次，2D 视口逐像素渲染不再每像素重建颜色表或解析字符串
- 地表层：按 `surface_palette` 刷方块；支持全部已注册方块（16-bit surface 索引）
- 地表调色板：可上移/下移调整层，删除方块；删除使用中的层会替换为其他实体地表，并支持撤销
- 全部方块列表：Minecraft 物品栏式方块格子，逐方块渲染 Mojang 模型图标，内存 + IndexedDB 缓存，搜索/滚动按需补齐；方块清单由 datagen 的 `blocks.json` 生成
- 方块模型预览：读取 Mojang blockstate/model JSON + 纹理，显示选中方块的 3D 模型；3D 锚点结构也按 NBT 方块逐块渲染
- BlockState 编辑：方块属性面板读取 Mojang blockstate 的 variants/multipart，生成 `namespace:id[prop=value,...]`；`surface_palette` 与 `underground.json` 都支持完整状态，旧纯方块 ID 自动按 defaultBlockState 解析。属性修改可撤销/协作同步；3D 地形表面和地下方块仍用颜色/图集近似，方块预览显示精确模型。
- 方块属性元数据由 datagen 的 `block_states.json` 提供（id → properties/defaults），`blocks.json` 仍只含 id/颜色；两者分离，避免属性数据和颜色资源互相干扰。
- 游戏内测试命令：`/touhou gensokyo blockstate minecraft:oak_stairs[facing=east,half=top]`（dev + gamemaster），用于验证 Java 侧 blockstate 解析。
- 地下编辑：切片 Y + 笔刷 Y 下限/上限，支持一次落笔覆盖任意 Y 范围（单次最多 256 格）；2D 切片或 3D 悬空切片均可编辑；稀疏覆盖空气/方块，运行时会应用到区块生成。3D 地下模式 Shift+滚轮调整切片 Y；PageUp/PageDown 调切片，Shift+PageUp/PageDown 移动整段范围。
- 原版 feature 装饰：dev 环境区块生成时按群系运行原版 feature（树木、花草、矿石、湖泊等），随后重新应用稀疏地下覆盖，保证编辑器数据优先；结构仍由锚点安装器负责，生产环境不生成。
- 3D 地形分块网格：地形按 32×32 网格块缓存，笔刷只重建受影响的块；图层/调色板/全图参数变化仍全量重建，避免大地图每一笔都重建整张地形。3D 拾取使用本地格坐标，适配降采样的大地图。
- 3D 方向键：上下左右平移视角；右键旋转，中键平移，滚轮缩放
- 3D 笔刷光标使用射线落点并按周围高度贴合凹面/坡面，不再固定在单格顶部
- 3D 实际方块模式：关闭坡度平滑，顶面按高度图逐格显示为阶梯，并在高度差处生成竖直方块侧面
- 3D 起伏显示：垂直夸张与横向 XZ 缩放都只做预览，不修改高度、坐标或保存数据；横向缩放 0.25–4×，锚点、结构、地下切片与远程光标同步缩放
- 3D 结构拖拽手柄：结构 tab + 3D 视图中拖动锚点/结构框移动 X/Z，拖动绿色圆环旋转 90°，拖动蓝色竖杆调整 Y，拖动橙色方块缩放（0.25–4）；Esc 取消，拖拽进入撤销/协作同步。
- 结构缩放：锚点新增 `scale` 字段（0.25–4，缺省 1），编辑器平滑缩放预览和 footprint；Java 端按目标体素网格重采样模板（放大后为块状重复，缩小为采样），复制方块状态/方块实体 NBT，并按有效缩放同步缩放实体位置后正常生成实体（实体模型本身不放大）。编辑器用粉色八面体标记模板实体位置，随结构一起平滑缩放。超过约 150 万目标方块会自动限制有效缩放。缩放变化只会影响新安装；旧世界需要安全重新生成维度或清理已安装状态。
- 元数据冲突合并：客户端发送锚点/字段增量与基线值；服务端按锚点 ID 和字段合并不同修改。并发修改同一锚点/字段时保留服务器版本，回传 `conflicts` 并记录日志，不会整批拒绝其他编辑。带 `metaDelta=1` 的新客户端走增量，旧客户端自动收到全量 meta 快照。
- 大图层/资源传输：图层 GET/PUT 使用 gzip；服务端优先使用同目录 `.gz` 缓存，避免每次加载都现压缩；JSON 响应超过 1KB 自动 gzip；静态 JS/CSS/HTML/SVG 自动 gzip。`/api/map`、`/api/blocks`、`/api/biomes`、`/api/underground`、`/api/layers/*` 和静态资源支持 ETag + 304；客户端改用 `cache: no-cache` 重新验证，减少重复传输。图层读取直接用 `Int16Array/Uint16Array` 视图，不再逐格 `DataView` 解码。
- 群系层：按 `biome_palette` 刷群系，P2 已接入 `GensokyoBiomeSource`
- 生物群系过渡：`biome_transition` 0–64 方块，0 为硬边界；编辑器用确定性 hash 逐格预览，Java `GensokyoBiomeSource` 按 4×4 quart 采样。旧地图缺少该字段时按 0 处理；改动只影响新生成区块，需要安全重新生成维度。
- 群系库：通过 datagen 的 `biomes.json` 读取原版与模组注册的全部群系（当前 68 个），支持搜索；新群系会追加到地图 `biome_palette`
- 笔刷 / 矩形 / 选区 / 吸管模式；矩形适合大范围平整/刷群系
- 地形选区：框选后可复制、粘贴、清除；Ctrl+C / Ctrl+V 操作；支持保存/加载/删除本地地形模板（最多 65536 格）
- 小地图概览：2D 显示视口白框；3D 按相机目标/朝向/俯仰/缩放绘制旋转视角范围框，并裁剪到地图边界；点击/拖动快速定位
- 出生点：可直接用当前鼠标位置的地表高度设置
- 锚点：选择结构模板，鼠标移到目标格后点“添加”；点击锚点行可编辑坐标，或进入移动模式后点击目标格重新摆放
- 结构 tab 进入选择模式：地形笔刷暂停，拖动鼠标框选结构，Ctrl/Shift 追加选择；支持多选移动和删除
- 结构变换：可编辑旋转（0/90/180/270）、镜像（左右/前后）和完整性；2D 占地框和 3D 结构预览同步更新，支持撤销
- 装饰物 / 树林：原版橡树、白桦、云杉、丛林、金合欢、深色橡木、樱花、红树，以及花草、蘑菇、岩石装饰
- 森林笔刷：按当前笔刷半径和森林密度随机种植混合树木，批量操作支持撤销；可避让已有结构、水面和低洼
- 地形全局：可编辑海平面 Y 和泥土层厚度；海平面影响水体和运行时生成，泥土层影响地表下方地层
- 装饰树/花草模板位于 `common/src/main/resources/data/reverie_dreams/structure/tree_*.nbt` / `deco_*.nbt`，由 `tools/gensokyo-map/generate-decorations.mjs` 生成
- 群系清单由 `BiomePaletteProvider` 在 `fabric:runData` 时生成到 `common/src/generated/resources/data/reverie_dreams/gensokyo/biomes.json`
- 结构编辑支持撤销/重做；移动模式会显示 2D/3D 占地预览
- 结构放置模式：选中模板后点地图直接放置，连续放置，Esc 退出（World Editor 式）
- 结构模板列表会扫描 `common/src/main/resources/data/*/structure/*.nbt`，数据包命名空间也能出现在下拉框
- 模板尺寸从 NBT 读取，用于 2D/3D 锚点占地框和放置预览
- 3D 光照：太阳方位/高度/强度和软阴影开关，设置持久化
- 2D/3D 缩放范围：0.05×–64× / 0.05×–100×；3D 的“1:1 方块”精度只渲染视图中心 512×512 完整方块网格
- 2D / 3D 编辑切换
  - 2D：滚轮缩放、中键/右键平移
  - 3D：WebGL / Three.js 地形，左键编辑；右键拖动旋转；中键拖动平移；滚轮缩放；方向键平移；大图按采样网格拾取
  - 大屏下 2D/3D 画布自动铺满中间视口
- 3D 1:1 方块精度：只渲染视图中心 512×512 的完整方块网格（随视角移动，接近窗口边缘重新居中），下面垫一层整图低精度背景；远景/雾按地图范围计算，100× 缩放下远处不会发黑。用于检查方块比例；1:1 模式缩放下限 2×，相机最大 100×；整图请用标准/高/超高降采样
- Gensokyo 维度高度 1536（Y 0..1535），地图 `world_height` 固定为 1536
- 水：水面高度来自 TIFF；PNG 只有水面范围、没有河床深度。PNG 里落在水面的纯黑等高线会先还原成水（避免水中出现“陆地环”）。岸边只抬到与水面齐平（不再 +1），水格生成 **3 格深**；水仍是普通水，会沿河道和落差流动，玩家可以直接从水里走上岸，不会漫过河岸。地图外恢复高山/高原边界：第 1 格保留边缘高度并用地下+上方隐形屏障封闭，之后按地图尺寸生成外环（宽度 `min(width, depth) / 16`，限制 256–1024，当前为 640）。高度使用多 octave value noise + ridged noise 制造山脊/起伏，径向目标只占约 0.62，不再是平滑同心坡；边界 24 格内用原位面方块按噪声概率过渡，之后用坡度 + 噪声石线/雪线决定草/石/雪，避免水平条带。最外侧仍为 Y0–Y1535 全屏障。玩家（含创造/旁观）在边界 32 格内会看到 barrier 风格的红色线框（客户端 `Gizmos`，和结构方块显示屏障的 0.45–0.55 方框一致），不再生成屏障物品粒子。边界外的玩家破坏、放置方块、使用物品/对方块使用物品写入都会被拦截；爆炸（TNT/床/末影水晶/苦力怕等）在 `ServerExplosion` 计算爆炸格时就把边界外的坐标剔除，边界外的方块既不消失也不掉落、也不会在那里点火；活塞（粘性/非粘性、推/拉/压碎）在 `PistonStructureResolver.resolve()` 阶段整段拒绝，不会出现半移动的残影；紧贴边界朝外的发射器直接不工作（避免水/岩浆/火焰/骨粉写进边界外）。世界生成不经过 `Level.setBlock`，不受影响；命令 `/setblock`、WorldEdit 等管理员写入仍可穿过边界（如果要硬性禁止，可把 `LevelBoundaryWriteMixin` 改成无条件拦截）。修改边界/水岸后需要重新生成幻想乡维度。
- 地表语义修正：左侧彼岸红区 `#7f0019/#a3243f` → `minecraft:poppy` / `minecraft:red_tulip` + plains；中央棕色区 `#4b4431`（人类村落平原）→ `minecraft:grass_block` + plains；赛之河原的灰色低地（约 Y1050，源色 RGB≈120,124,118）→ `reverie_dreams:paddy_field` + plains，真正的高海拔灰色石山仍保留 stony_peaks；黄色小块（太阳花田）→ `minecraft:grass_block` + `minecraft:sunflower_plains`；竹林（迷途竹林）→ `minecraft:bamboo_jungle`；火山口红/橙 → `minecraft:lava`，深灰/黑火山岩 → `minecraft:basalt` + stony_peaks；右上 `#91495c` 仍为 cherry_grove。surface palette 由 9 项扩到 15 项（新增 `reverie_dreams:paddy_field`、`minecraft:pale_moss_block`）。彼岸红区内部的误判 `red_sand` 已按最大红彼岸花连通区清理为 poppy/red_tulip/泥土，彼岸红区之外的红沙保持不动。
- 右上角无缘塚（约 x12154–12797, z1409–2060）：整块改为 `minecraft:pale_garden` + `minecraft:pale_moss_block`，清理红沙/樱花网格；小水塘和高度保留，范围外不动。
- 沼泽区（手绘范围 x≈10187–12799, z≈1938–4261，共 4,755,400 格，无缘塚 pale_garden 一格不动）：整块群系改为 `minecraft:swamp`；只动 biome 层，水面/地表/高度保持原样。因为 `biomes.pal4.gz` 是 4-bit 索引（只能 0–15），把 `minecraft:swamp` 与未被使用的 `minecraft:basalt_deltas` 在 `biome_palette` 中对调：**第 11 项 = `minecraft:swamp`，第 55 项 = `minecraft:basalt_deltas`**（`dimension/gensokyo.json` 的 biome 列表同步）。
- 稻田方块 `reverie_dreams:paddy_field`：继承原版耕地，始终 moisture=7，不会干涸/踩成泥土；模型直接引用原版 `minecraft:block/farmland_moist` 材质；破坏掉落普通泥土。稻田中按稀疏哈希放入一格水源方块（约 14,151 格），用于视觉水田，不依赖它保湿。
- 枫树林 `reverie_dreams:maple_forest`：Gensokyo 地图当前 **0 格**（原红框区域已全部清回 `minecraft:forest`），`biome_palette` 仍保留第 9 项，供 web-world-editor 手绘。Iroha / Itaya 两种枫树只在 `maple_forest` 生成，原版 `forest / birch_forest / flower_forest / cherry_grove` 仍不注入。两种枫树生成时叶子 `distance=6`、`persistent=false`：随机 tick 不会清除外围叶子，但树干被挖掉后邻居更新会把失去支撑的叶子重新计算并正常掉落。
- 枫林密度/植被：两种枫树各 **4~5 棵/区块**（合计约 8~10 棵，接近原版森林）；枫林加入 `forest_flowers`、`flower_default`、`patch_grass_forest`、`patch_bush`、红/棕蘑菇、南瓜、甘蔗、萤火虫灌木，并额外加入自定义 `maple_grass`（2×24）、`maple_tall_grass`（6×16）、`maple_flowers` / `maple_forest_flowers`（各 rarity 8 / count 8）、`maple_leaf_litter`（2×24），避免花海式过密。
- 主世界枫林：通过 TerraBlender `MapleForestRegion` 注册到 `RegionType.OVERWORLD`（权重 4）；使用 `VanillaParameterOverlayBuilder` **纯新增**枫林参数点，不替换任何原版群系。参数点外的位置由 `DEFERRED_PLACEHOLDER` 回退到原版 Region，因此枫林是可大可小的新气候斑块，而不是覆盖 forest。当前为 4 个参数点（仅 `NEUTRAL` 温度），面积是之前 8 点版本的一半。
- 火山岩浆湖：主岩浆区统一液面 **Y1447**，内部台地/玄武岩/孔洞全部改为 lava；低侧加一圈 basalt 边（约 1,072 格）；生成器对 lava 地表向下填 **96 格**岩浆，不再是一格表层。
- 游戏运行时压缩：`heightmap.delta.gz`（向上差分 int16 + gzip）、`surfaces.pal4.gz` / `biomes.pal4.gz`（4-bit palette + gzip）；`map.json.layers` 直接指向压缩文件，`GensokyoMapLoader` 启动时解码。运行时从 **625 MiB 降到约 41 MiB**。编辑器 `map_source` 仍保持 raw，后续再迁移到 tiled/dirty 保存。
- 懒解压/缓存：游戏目录 `cache/reverie_dreams/gensokyo-map/<namespace>/<map>` 共用解压后的 raw 数组，默认地图为 `reverie_dreams/gensokyo`，约625 MiB，同一游戏实例的多个存档复用。地图元数据/压缩图层按内容SHA256识别，raw数组也校验SHA256；跨进程锁和最后提交的`source.stamp`避免并发与中断导致混读。缓存无效时后台重建，无法写缓存则直接使用解码数组。`createLevels`后后台预热，进入时若仍在加载则等待；下次打开旧存档时只清理其`gensokyo-map-cache`中的四个已知缓存文件，未知文件不动。共用缓存可手动删除，存档地形不受影响；内存数组占用不因共用磁盘缓存而减少。
- 地图项目：新建空白地图（单边 1–32768，总格数 ≤134,217,728，12800×10240 可用）、调整地图尺寸（裁剪/扩展，自动备份）、重新载入、60 秒自动保存开关。大地图会占用大量浏览器/Java 内存，建议先用小图验证。
- 大地图渐进加载：先读取 1/16 概览并立即显示（12800×10240 实测约 8 秒），随后后台顺序加载完整 heightmap/biomes/surfaces，完成后解锁编辑/保存（同图实测约 2 分 18 秒）。完整加载期间显示“加载细节”，禁止编辑，避免把概览数据写回地图。服务端提供 `/api/layers/:name/overview` 与 `/api/layers/:name/tile`；当前完整细节使用整层后台加载，因为测试环境逐瓦片请求反而更慢。
- 地图备份：保存/新建/恢复前自动备份到 `map_source/gensokyo/backups/`，保留最近 20 份；可手动创建和恢复
- 安全重新生成幻想乡维度：编辑器地图页的“安全重新生成维度”按钮或 `/touhou gensokyo regenerate confirm` 只写入待处理标记；下次启动时在创建维度之前把旧 `world/dimensions/reverie_dreams/gensokyo` 整体移动到 `world/backups/gensokyo-regenerate-*`，再由当前 map.json 重新生成。启动前会检查 `playerdata`，如果有玩家的存档位置仍在幻想乡，会暂停并保留标记，避免把玩家生成到新地形里。命令 `/touhou gensokyo status|cancel` 可查看或取消。该功能由 `GensokyoFeature.ENABLED` 控制（当前 true，所有环境注册），需要 gamemaster 权限。
- 多人协作（MVP）：SSE 在线列表、2D/3D 远程光标、地形和地下覆盖同步、结构/出生点/调色板/地形全局参数同步、事件回放、保存/重置通知。
- 地形协作锁：笔刷等待申请成功再绘制，拖动沿途申请，5 秒续租、20 秒过期；服务端整笔校验持锁状态，拒绝无锁、过期锁、越界提交。确认提交后才释放锁；失败会回滚本地预览。矩形、地形粘贴、地形撤销/重做也走此流程。
- 保存版本协议：保存前 `POST /api/save/begin` 携带 `baseRevision`；服务端校验当前 live revision，并用 60 秒保存会话把 `map.json`、三个 `.bin` 图层和 `underground.json` 先写入 `.save-staging/<token>`，`POST /api/save/commit` 时统一替换。提交期间拒绝新的地形/元数据操作；并发保存、过期 revision、保存期间被他人修改都会返回 409 并丢弃 staging。保存成功后 revision +1，并广播统一 checkpoint。
- 版本号：`map.json` 不写入编辑器 revision；已提交 revision 持久化在 `map_source/gensokyo/.editor-revision.json`。`GET /api/map` 同时返回 `revision`（磁盘检查点）和 `liveRevision`（含尚未保存的协作操作）。地形/地下/元数据操作都会推进 live revision；元数据使用增量 + 基线值合并，并发同项冲突时保留服务器版本。
- 旧版直接 `PUT /api/map` 仅保留给单机/脚本；只要有协作连接就会被拒绝。编辑器本身始终走保存会话。
- 协作连接丢失时暂停协作绘制；全局元数据撤销/重做在协作期间暂时禁用。结构/元数据现在按锚点/字段增量合并，同项并发冲突保留服务器版本并记录；保存仍纳入版本协议，但不能视为生产级多人系统。
- 自动测试：`node --test world-editor-web/collaboration-locks.test.mjs world-editor-web/save-protocol.test.mjs world-editor-web/regeneration.test.mjs`（锁、保存协议和重生成 API 测试都使用独立端口；保存/重生成测试使用临时地图目录和临时世界目录，不写仓库地图或世界）。
- 网格、水面覆盖开关
- 撤销/重做（Ctrl+Z / Ctrl+Y）：地形、地下覆盖、结构添加/删除/移动/坐标、出生点、地表 palette、群系/地表笔刷修改
- 保存（Ctrl+S）：元数据 + 二进制图层（通过保存会话，带 revision 校验）
- 从 common 导入：解码 runtime 的压缩图层（heightmap.delta.gz / *.pal4.gz）为 source 的 raw `map.json` + `.bin` + `underground.json` 并重新加载
- 服务端校验：dimensions、palette、高宽、spawn/anchor 范围、单边最大 32768、总格数最大 134,217,728

## 从合并地形图仿造地图（近似）

阴影 + 等高线 + 群系色块合并成一张图时，无法精确恢复原始高度/群系，只能按图像颜色和明暗仿造；不添加随机地形，也不放置图里没有的结构。

1. PNG 导出为原始 BGRA（不缩放、不压缩像素）：

```powershell
powershell -ExecutionPolicy Bypass -File world-editor-web/image-to-raw.ps1 `
  -Image "E:\Download\gensokyopreclassic0.0.4.png" `
  -Out "E:\TEMP\opencode\gensokyo_preclassic_native\image_native.rgba"
```

2. 原生逐像素导入（默认 1 像素 = 1 方块，origin `[0,0]`）：

```powershell
node world-editor-web/image-import-native.mjs `
  --raw "E:\TEMP\opencode\gensokyo_preclassic_native\image_native.rgba" `
  --width 12800 --height 10240 `
  --out "E:\TEMP\opencode\gensokyo_preclassic_native\map" `
  --id "reverie_dreams:gensokyo_preclassic" --sea-level 64
```

3. 如果已经有缩小后的原始 RGBA，也可以用 `image-import.mjs` 做一次性分类。

- 图层输出为未压缩的 `.bin`；HTTP gzip 只是传输压缩，不改变数据。
- 12800×10240 约需 655 MB 图层内存；实测编辑器加载约 107 秒、浏览器堆约 662 MB。
- 高度由图像明暗、颜色类别和模糊后的亮度推导，属于近似；需要精确高度请使用 WorldPainter `.world` 或原始高度图。

## 从真实 TIFF 高度图导入

TIFF 必须是灰度高度图；当前素材为 12800×10240、**32-bit 浮点**，像素值约 8.0155–11.7990。它不是 16-bit 整数；用 `-depth 16` 直接导出会把小数截成 8–12 的 5 个整数层，地形会变得像平板。

WorldPainter 对这张图的解释是约 **×128**：
- 坐标 (6326,4520) 的原始浮点值是 8.4253
- 8.4253 × 128 = 1078.4，WorldPainter 状态栏显示 1078
- 最高点约 11.799 × 128 ≈ 1510；1535 是 WorldPainter 的世界高度上限

1. 用 ImageMagick 保留小数并放大到整数高度（这里直接乘 128）：

```powershell
magick "E:\Download\gensokyopreclassic0.0_high-res-heightmap.tiff" `
  -evaluate multiply 128 -depth 16 gray:"E:\TEMP\opencode\heightmap_x128.raw"
```

2. 以已有的图像分类结果为群系/地表底图，替换高度层：

```powershell
node world-editor-web/heightmap-import.mjs `
  --heightmap "E:\TEMP\opencode\heightmap_x128.raw" `
  --width 12800 --depth 10240 `
  --scale 1 --world-height 1536 --sea-level 1024 `
  --base "E:\TEMP\opencode\gensokyo_preclassic_native\map" `
  --out "E:\TEMP\opencode\gensokyo_preclassic_worldpainter_absolute\map" `
  --id "reverie_dreams:gensokyo"
```

- 高度：原始浮点 ×128，约 Y1026–1510，保留 485 个高度层。
- `world_height`：1536；Java 维度/校验也必须同步为 1536。
- `sea_level`：1024。
- 高度像素与 PNG 分类图必须同尺寸、同原点；当前两者都是 12800×10240、origin `[0,0]`。
- 输出高度为未压缩 int16 `.bin`，并生成 1/16 概览。
- **不要**再使用 `--scale 1` 直接喂 16-bit 截断值；那是之前的错误版本。

### TIFF 与彩色图对比结论（12800×10240，5% 采样）

- 两图尺寸完全一致、同坐标。
- TIFF 是 32-bit 浮点高度，不是 16-bit 整数；原始范围约 **8.0155–11.7990**。
- 按 WorldPainter 的 ×128 映射后，高度约 **1026–1510**，共 **485** 个整数高度层。
- 黑色等高线与 TIFF 高度跃变的统计只能作为风格化参考，不能替代真实浮点高度。
- 最终合并规则：
  - 高度：只使用 TIFF。
  - 群系/地表：只使用彩色图像的颜色分类。
  - 不用等高线补高度，不随机生成地形，不放置图中不存在的结构。
- 颜色到群系 ID 仍属于语义映射（图中没有图例），这部分是推断，不是 TIFF 数据。

## 注意

改完地图后，旧区块不会更新。安全流程：

1. 在编辑器里保存地图（Ctrl+S）
2. 同步到 resources
3. 重新构建/重启客户端
4. 让所有玩家离开幻想乡
5. 点“安全重新生成维度”（世界目录默认 `fabric/runs/world`），或游戏内执行
   `/touhou gensokyo regenerate confirm`
6. 完全重启服务器/客户端

下次启动时，日志会出现类似：

```text
Gensokyo dimension moved to .../world/backups/gensokyo-regenerate-...; it will regenerate from the current map data.
```

旧维度会保留在 `world/backups/`，不会直接删除。需要回滚时，先停服，把当前
`world/dimensions/reverie_dreams/gensokyo` 移走，再把对应备份目录移回该位置，然后重启；不要手动修改其他世界数据。

## 快捷键

- T / D / V：地形 / 结构 / 光照
- E / M：方块物品栏 / 地图信息
- J：森林笔刷
- 1 / 2 / 3：高度 / 群系 / 地表
- B / R / S / I：笔刷 / 矩形 / 选区 / 吸管
- X：挖空；F：适应地图
- [ / ]：笔刷大小
- G：网格；W：水面
- PageUp / PageDown：地下切片 Y；Shift+PageUp/PageDown：移动整段 Y 范围
- 3D 地下模式 Shift+滚轮：调整切片 Y
- Esc：退出结构放置 / 移动 / 框选 / 森林笔刷
- Ctrl+Z / Ctrl+Y / Ctrl+S：撤销 / 重做 / 保存
