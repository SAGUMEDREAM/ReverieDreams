# 幻想乡区块压缩

**enableGensokyoChunkZstd 默认 false**。这是可选的存储策略，不是进入幻想乡的要求，也不会改变地图内容或月球地形。

开启后，符合条件的幻想乡地形区块改用 Zstd 写入；关闭后恢复默认写入方式。已有 Zstd 区块在关闭后仍可读取，不会因关掉开关立即重新压缩或转换整个世界。

该策略只针对幻想乡的地形 chunk 存储；不要据此推断所有维度、实体、兴趣点或其他模组文件都改用 Zstd。压缩等级默认为 8，允许范围为 1～19；设置变化在重启后用于后续写入。

压缩速度、CPU 开销和磁盘收益取决于机器与负载。本项目保持默认关闭；只有自己比较实际结果后再决定是否开启。需要调整时，在游戏正常关闭或服务器停服后编辑配置并重启。

<SourceNote :files='["common/src/main/java/cc/thonly/reverie_dreams/world/gensokyo/storage/GensokyoChunkCompression.java","common/src/main/java/cc/thonly/reverie_dreams/ReverieDreamsConfiguration.java"]' />
