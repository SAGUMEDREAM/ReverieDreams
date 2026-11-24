# ReverieDreams Paper + CraftEngine 迁移

目标固定为 Minecraft/Paper 26.1.2 build74、CraftEngine26.8.2、Java25。完整移植仍在进行，原 common、Fabric、NeoForge、Polymer 和原世界保留。当前已验证内容包有933个物品定义、213个方块、70个家具、466条配方；这些包含渲染与颜色适配项，不等于原Mod玩法完成度。

功能对应、真实测试结果及未完成项见[完整移植进度](../migration/full-port-progress.md)。矿物早期记录见[历史批次](../migration/phase-c-minerals.md)，不能把历史25物品快照当作当前范围。

## 构建

在仓库根目录运行独立Paper入口，避免配置其他加载器或读取其并发JAR：

```powershell
.\gradlew.bat -p paper/standalone :paper:build --offline --no-daemon --console=plain --max-workers=2 --project-cache-dir paper/build/standalone-gradle-cache
```

它复用现有paper/build.gradle和原资产目录，保留同名产物，不运行Mod数据生成或服务器。离线构建需要已初始化的Gradle、Paper与CE缓存。普通根入口`:paper:build`仍保留，但根工程会配置Fabric/NeoForge/Polymer，可能受其他加载器构建影响。

产物：

- 插件：`paper/build/libs/paper-1.0-SNAPSHOT.jar`。
- CE内容：`paper/build/craftengine/reverie_dreams/`，ZIP位于`paper/build/distributions/reverie-dreams-craftengine-1.0-SNAPSHOT.zip`。
- 可选世界插件：`paper/build/distributions/reverie-dreams-world-bootstrap-1.0-SNAPSHOT.jar`。
- 可选世界高度/地图数据：同目录`reverie-dreams-world-height-1.0-SNAPSHOT.zip`、`reverie-dreams-world-map-data-1.0-SNAPSHOT.zip`。
- 回归报告：`paper/build/reports/tests/test/`、`paper/build/reports/tests/worldTest/`。
- `paper-1.0-SNAPSHOT-smoke-probe.jar`仅供隔离验证，禁止部署到正式服。

## 隔离验证

用户接受测试Minecraft EULA后可运行：

```powershell
python paper/scripts/run_smoke.py --accept-eula
```

运行器只在paper/build新目录创建127.0.0.1测试服，保留日志、逐项结果与smoke-result.json，完成初次检查、资源包ZIP校验、保存和真实重启；原paper/run/eula.txt与原世界不变。配置issue、服务端ERROR、非零退出码均判FAIL，不关闭看门狗。配方检查每tick分批，世界异步准备，释放自身票据后正常stop并允许600秒保存。

新版源地图测试必须先离线准备缓存：

```powershell
.\gradlew.bat -p paper/standalone :paper:prepareWorldMapCache --offline --no-daemon --console=plain --max-workers=2 --project-cache-dir paper/build/standalone-gradle-cache
python paper/scripts/run_smoke.py --accept-eula --world --world-cache paper/build/world-draft/source-map-cache-v2 --world-feature-mode all --world-forest-trees
```

运行器在启动前验证缓存与夹具的源指纹，源数据改变后必须重新审计夹具。森林开关表示已公开差异的后置适配，默认关闭；真实群系/树形/矿带和边界见[世界](../migration/full-world.md)、[森林](../migration/full-forests.md)。它不代表梦世界、月球或原世界转换已经完成。

## 部署与边界

在独立测试服先初始化精确版本CraftEngine，再安装正式插件和CE内容包。配置`block.serverside-blocks: 6000`，按[建筑说明](../migration/full-buildings.md)处理官方示例包占用的视觉槽。保存CraftEngine/cache/custom_block_states.json等状态分配缓存，不能把旧世界原生编号当作可随意重建的内容映射。

执行`/ce reload all`检查配置、模型与资源包工作流，并设置托管/分发让玩家接受资源包。构建ZIP是CE内容包；客户端资源包由CE生成，仅复制内容ZIP不会自动分发。世界插件/高度数据包需按世界文档单独配置，不应直接覆盖原世界。

首轮客户端仍需验收模型、碰撞预测、坐席、声音、玩家物品使用/铁砧与GUI；API探针没有客户端画面。尚未宣称Folia兼容。剩余武器、NPC工作、连接装饰、其他维度、客户端专有能力等均在进度表逐项记录，不删除原实现来冒充已迁移。

原模型、贴图、声音和授权信息按各共享资产manifest选入；LICENSE/CREDIT随内容包提供。构建不会向原paper/run安装插件或修改其世界。
