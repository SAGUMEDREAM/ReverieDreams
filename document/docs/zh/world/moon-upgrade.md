# 月球更新与旧存档

当前月球生成器包含高地、玄武岩月海、阴影冰坑等区域，并扩展了地下高度。新地形与已有月球存档需要兼容处理。

## 玩家会看到什么

旧月球区块会保留原有地表、建筑、实体和容器数据；转换会补足更低的地下区段并重建必要的高度与光照信息。未探索地区由新生成器创建，不能保证已经探索过的地方重新长出新地形或新建筑。

## 升级流程

1. 关闭世界或服务器，备份完整世界目录。备份应包含主世界元数据及 dimensions 中的月球，不要只复制 region。
2. 使用匹配的模组与加载器版本启动，等待转换和服务器启动完成。不要在转换中途删除临时目录或替换地图文件。
3. 查看日志中的 Moon upgrade complete，再检查原有建筑、箱子和重要实体。

自动迁移使用世界内的 **backups/moon-terrain-v2** 保存原始月球与有关元数据，并用版本标记避免重复转换。这个备份不能代替升级前的完整世界备份。

## 使用自定义数据包的世界

如果月球生成器或维度高度被其他数据包改过，内置迁移可能拒绝不支持的组合并在日志说明。不要删除版本标记或强行清空月球来绕过；应在世界副本上核对自定义生成器和高度，再制定迁移方式。

月球迁移与[幻想乡区块压缩](./chunk-compression)是独立机制，开关 Zstd 不能控制月球地形升级。

<SourceNote :files='["common/src/main/java/cc/thonly/reverie_dreams/world/moon/compat/MoonSaveUpgrade.java","common/src/main/java/cc/thonly/reverie_dreams/world/moon/compat/MoonChunkUpgrade.java"]' />
