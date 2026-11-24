# 设置与常见问题

以下是源码默认值；服务器管理员可以修改，联机时以服务端为准。配置文件位于实例 config 目录，名称以 reverie_dreams 为前缀。修改后重启可确保所有设置生效。

| 设置键 | 默认值 | 影响 |
|---|---|---|
| debugMode | false | 开发/调试行为，正常游玩不必开启 |
| maxCustomerTickTime | 1200 tick（60 秒） | 顾客非进食期间的换单倒计时 |
| maxCustomerCooldownTickTime | 400 tick（20 秒） | 交单后的进食时长，期间不能继续交单 |
| npcCustomerResetTimerPerOrder | false | 是否每一单重置换单倒计时 |
| npcWorkRange / npcWorkVerticalRange | 8 / 8 | NPC 工作点水平/垂直范围 |
| npcWorkAutoLogistics | true | 工作补给与卸货 |
| npcWorkUnloadThreshold | 14 | 18 格货物区达到此占用量时尝试卸货 |
| npcBreedMaxAnimals / npcBreedFoodReserve | 32 / 2 | 繁殖区域上限和食物保留量 |
| npcHoneyProduct / npcHoneyRequireSmoke | auto / true | 蜜蜂产物选择和烟的要求 |
| chestSortClaimEmptyChests | true | 允许分类占用无规则的空箱 |
| enableYouseiSpawn / enableGhostSpawn | true / false | 妖精/幽灵的生成开关 |
| enableGensokyoChunkZstd | false | 可选[幻想乡存储压缩](./world/chunk-compression) |
| aiProxyUrl | ""（留空） | AI 请求代理地址（支持 SOCKS5、HTTP、HTTPS） |

## 顾客两个时间为什么不同

maxCustomerTickTime 是非进食期间的换单倒计时，归零会清空相关状态并生成新订单；maxCustomerCooldownTickTime 是交单后顾客进食的时长，结束后再生成新单，进食期间不能交单。它们都不是“离店后多久再来”。单位都是 tick，正常运行时 20 tick = 1 秒。

## NPC 不工作

检查状态是否为“工作”，工作点是否正确，然后检查工具、对应消耗品、18 格货物空间、容器规则和寻路通路。顾客模式还必须满足夜雀钟、菜单本和营业条件。切换模式不会自动补充缺失物资。

## 配方或名称与 Wiki 不同

先确认 Minecraft 与模组版本，再排查服务器数据包及资源包。带组件的模板、角色卡或成品属性可能不同；请在详情中选正确款式。Wiki 对应仓库内置内容，不会覆盖服务器自行修改的配方。

## AI 对话与代理配置

AI 接口、模型与密钥在配置界面中设置。配置项 `aiProxyUrl` 专用于 AI 请求（支持 SOCKS5、HTTP、HTTPS，如 `socks5://127.0.0.1:1080` 或 `http://127.0.0.1:7890`），留空即直连。该代理只作用于 AI 会话与工具分析请求，不改变游戏皮肤下载或更新检测等其他网络流量。

<SourceNote :files='["common/src/main/java/cc/thonly/reverie_dreams/ReverieDreamsConfiguration.java"]' />
