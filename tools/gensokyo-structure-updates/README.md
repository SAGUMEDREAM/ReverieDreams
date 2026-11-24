# 幻想乡固定建筑：独立版本与维护

建筑版本独立于模组发行版本。已探索区块内的新增建筑和旧建筑更新，通过服务器维护作业完成；不会删除 region/entities/poi 文件，也不会重新运行整块地形、矿物或其他模组的世界生成。

## 管理员操作

命令需要游戏管理员权限。地标使用完整 ID；`*` 只展开当前注册的模板地标，逐栋排队。

```mcfunction
/touhou gensokyo structure status *
/touhou gensokyo structure preview reverie_dreams:scarlet_devil_mansion
/touhou gensokyo structure update reverie_dreams:scarlet_devil_mansion
/touhou gensokyo structure rebuild reverie_dreams:scarlet_devil_mansion
/touhou gensokyo structure job <作业UUID>
/touhou gensokyo structure cancel <作业UUID>
/touhou gensokyo structure rollback <作业UUID>
/touhou gensokyo structure resume <作业UUID>
```

- `update`：补建缺失建筑或升级低版本，按保存的旧基线、当前方块和新模板合并，保留玩家修改；同版本同内容直接结束。
- `rebuild`：按目标模板恢复几何，包括玩家改变的普通方块；仍保护箱子、机器、现有装备和 NPC 动态状态，不补领宝藏。
- `preview`：只显示版本、范围、选区方块数、实体数和已加载支撑区块数，不加载区块或改变地形。实际冲突在执行前扫描，不应把预览当作完整冲突检测。
- `cancel`：尚未修改世界时取消；已开始写入时转入有条件回滚。
- `rollback`：使用该作业的写前差异恢复结构，只接受当前仍等于写后结果的方块，不回档库存或奖励。后来出现的容器、物品变动或 NPC 进展可阻止回滚，保留作业交人工处理。
- `resume`：排除具体冲突后继续 `NEEDS_REVIEW` 作业。仍逐项核对期望状态，不跳过冲突。
- 原 `/touhou gensokyo repair <ID>` 已接入持久 `rebuild`，不再使用接近玩家时分批重铺的内存队列。

执行后离开目标建筑及其可视范围，等待自然卸载。外部强制加载、玩家或模组票据仍占用区块时，任务会等候或以 `BLOCKED` 结束，不移除别人的票据。写入失败后的 `NEEDS_REVIEW` 会维持维护区，管理员可先查看日志，再继续或回滚。

## 配置

在现有 `ReverieDreamsConfiguration` 配置中使用以下字段；由服务器配置决定：

| 字段 | 默认值 | 用途 |
| --- | --- | --- |
| `gensokyoStructureAutoUpgrade` | `false` | 开启后定期排队缺失或低版本地标 |
| `gensokyoStructureUpdateBudgetMs` | `2` | 方块扫描和写入的每 tick 时间预算，限制为 1–20ms |
| `gensokyoStructureUpdateMaxChunks` | `1024` | 目标和旧范围加支撑缓冲后的区块上限 |
| `gensokyoStructureUnloadTimeoutTicks` | `1200` | 等待自然卸载的超时 |
| `gensokyoStructureUpdateExcluded` | `[]` | 排除自动升级的地标 ID；不影响手动命令 |

预算不包含最后的引擎同步保存屏障。大建筑保存时可能出现一次较长 tick，这是发布持久版本前的保存操作。模板编译和压缩快照写盘使用后台任务，不在每个 tick 重建模板。

## 内容作者的数据链

1. 修改 `common/src/main/resources/data/reverie_dreams/structure/gensokyo/scarlet_devil_mansion.nbt` 及其内置配置 compound，提升 content_version，然后运行 Fabric datagen。
2. `scarlet_structure.json` 是 provider 输入。`content_version` 单调递增；每个实体的 `entity_slots[].id` 永久保留，移动实体只更新源坐标，不能根据新的实体数组顺序重新编号。新增实体使用新 ID。
3. `scarlet_adventure.json` 中的宝藏和遇敌点同样保留稳定 ID，移动房间不得通过改名重置奖励。蓝图保存这些功能点，以迁移工作位置及原有遇敌实体。
4. 由 Fabric `GensokyoStructureTemplateProvider` 生成结构 NBT 和 `gensokyo_structure_metadata`。内容哈希包含实际 NBT 和配套源清单；不要手写生成文件。`actors_version` 是预留元数据，目前维护版本统一由 `content_version` 控制。
5. 重跑 datagen 前备份生成树；并行工作时使用隔离输出，只同步当前 provider 的实际资源。

同版本但不同哈希会阻塞，资源版本低于存档也会阻塞。资源重载会刷新后续任务的目录，已经开始的任务继续使用持久化的目标蓝图。未知旧存档不冒充版本零：无可信基线时 `update` 报 `LEGACY_UNKNOWN`，可明确使用 `rebuild`。旧建筑中已缺失的陈设按已取走处理，避免补发装备。

## 保存与恢复

每个世界保存自己的维护资料：

```text
<世界>/data/reverie_dreams/structure_updates/
  ledger.json             已安装版本、稳定实体身份、待还原冻结标志
  baselines/<hash>.nbt     压缩、使用注册表名称的可迁移基线
  jobs/<UUID>/job.json     持久阶段和区块进度
  jobs/<UUID>/gate.json    维护范围的独立恢复记录
  jobs/<UUID>/target.nbt   固定目标蓝图
  jobs/<UUID>/actors.nbt   自有实体的写前快照
  jobs/<UUID>/chunks/      按区块压缩的 before/after 方块差异
```

元数据使用刷盘和原子替换。重开存档会重新核对所有已写差异，防止把单独保存的 receipt 当成区块已经落盘的证明。真实区块、实体保存后才发布安装版本。NPC 身份以地标和固定 actor ID 关联 UUID，死亡、卸载、被玩家带走、跨维度都不因简单查不到实体而克隆。

不删除既有容器或把它改成其他方块，不覆盖同类容器现有库存，不重置红魔馆宝藏/获救/冷却账本。移动或删除旧宝箱需要库存迁移适配，当前会阻塞。移除有物品的陈设、替换实体类型、交叠建筑的联合更新和未知机器迁移同样需要明确适配。

`StructureUpdateProtection.register` 可接领地/机器保护的维护否决检查。不自动假设任意第三方领地模组已经适配。没有旧基线的新建筑，也无法完美区分玩家堆砌的原版自然材料与自然地形；自动更新默认关闭。

## LOD、并发与验证

维护只通过普通加载 API 申请自有票据，等待 FULL 和实体加载完成，在服务器线程修改现存区块；不修改 C2ME 的内部调度器。维护区会拦截区块发布和玩家进入，自有挂墙实体与 NPC 暂停 tick。

DH 通过可选兼容层逐块提交实际区块更新，不删除数据库。作业中的 `QUEUED` 表示已提交异步请求，不能理解为远景数据库或客户端画面已同步完成；失败信息另记为 `lod_warning`。没有安装 DH 时为 `NOT_PRESENT`。

本目录的 `native` 探针测试真实区块/容器/实体、自然卸载、跨版本合并、回滚、存档重开、退役 NPC、保护否决和补全。运行实例必须位于 `E:/Temp/reverie-dreams-tests/structure-version-20261120-*`，结束后删除。结果放 `data/structure-version-20261120`。安装包只走项目正式 `build/dist`，本任务不生成发行包。
