# 固定建筑流式生成回归验证

针对幻想乡固定 `template` 锚点，测试低视距接近建筑、依附实体支撑、保存卸载重载和旧建筑定向修复。不会读取或修改用户真实存档，也不会改地图层或发布 revision。

## 数据链路

`GensokyoStructureTemplateProvider` 从保留的原始 Litematic 生成标准结构 NBT 和 `gensokyo_structure_metadata` 小型目录。保留选区内全部空气；选区外的地下空白不清除。运行时从目录取得包围盒，首次在后台把模板编译成不可变的区块索引。

`GensokyoChunkGenerator.applyBiomeDecoration` 在 FEATURES 最后写入当前区块的结构方块、原稿空气和方块实体。自然植被等通过 `WorldGenRegion` 写入时避让建筑范围。建筑本体在光照、FULL 和客户端发送之前完成，无须整栋强制加载，也没有在玩家已看到后继续拼墙的 tick 队列。

绘画、展示框等普通实体在服务端收尾：等待相邻 FULL 区块内的建筑和实体实际包围盒覆盖的支撑区块就绪，检查原版 `survives()` 后创建。实体使用稳定 UUID；区块 NBT 保存独立的方块/实体标记，正常保存重载不再重铺建筑。

## 已生成旧建筑

新生成区块自动使用新流程。旧区块不会在加载时自动覆盖玩家装修；需要修复时由管理员执行：

```mcfunction
/touhou gensokyo repair reverie_dreams:scarlet_devil_mansion
```

命令按原稿恢复已加载的结构选区，走近时继续处理其余区块，保留已经存在的同类容器及其中物品。它会恢复该建筑中的其他方块，因此在修改过建筑的存档上执行前应备份。重复执行会重新发起修复，已有实体不会重复创建。

## 测试入口

1. `python -B tools/gensokyo-structure-streaming/probe_points.py` 从未修改的原稿创建客户端首次可见抽查点和楼梯空气坐标。
2. `python -B tools/gensokyo-buildings/validate_template.py data/structure-streaming-20261119` 独立流式比较全部模板条目、选区覆盖、方块实体 NBT 和普通实体坐标。
3. Fabric client game test 源码在 `native/java`，独立测试 Mod 清单在 `native/resources`。通过隔离 Gradle initializer 注册测试 source set，`runClientGameTest` 临时实例必须位于 `E:/Temp/reverie-dreams-tests/<唯一任务>/`，启动脚本必须在 finally 核对进程和路径后清理，不得输出安装包或运行 Polymer。

原生测试不强制加载整栋建筑；视距4，从建筑外逐段接近，在每个客户端区块首次出现时核对原稿，保留硬性失败。测试等待周边区块实际抵达客户端后再推进，避免仅因异步传输尚未完成就结束观察。保存后完整退出集成服并重新打开同一存档，确认磁盘重载得到新对象且标记/实体 UUID 保持。组合模式复用项目已有 DH GameTest 的关闭辅助流程，避开测试框架的渲染/服务端相位门互等，不改变游戏生命周期实现。

2026-11-19 验证：原生396项通过、DH3.3.3+C2ME0.4.0-alpha.0.65组合795项通过；组合覆盖141个首次可见区块、132个墙附着方块（其中34个位于区块边缘）、57个挂墙实体、真实存档重开、楼梯清障、容器物品保留与重复修复。最终共享源码通过 Fabric 和 NeoForge 编译。结果只代表该定向回归，不是全图逐栋游玩验收；截图为坐标附近走廊，也不替代完整楼梯通行验证。

结果、必要截图保留在 `data/structure-streaming-20261119`；构建与测试输出目录均应隔离，不要把临时实例、测试 JAR 或完整 ZIP 保存在本工具目录。
