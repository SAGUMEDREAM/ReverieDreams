# 酿酒桶与饮料

本版本的饮料由酿酒桶配方生产，包括茶、果汁、酒类等。它们同样具有餐厅顾客会关心的饮料属性；“酒名”与是否含酒精应以实际属性为准。

## 酿造步骤

1. 放置酿酒桶，普通右键打开材料界面。每个输入格只放一个材料。
2. 按配方备齐材料，关闭界面，**空手潜行右键**启动匹配的酿造。
3. 酿造过程中等待酒桶完成；区块需要持续运行。
4. 完成后手持**玻璃瓶右键酒桶**取出饮料。一次酿造提供 **9 次装瓶**，每次消耗一个玻璃瓶。
5. 将成品取完后，再放材料开始下一批；有剩余成品时不能直接覆盖酿造。

放入多余材料也会被启动过程按非空槽消耗；建议只放目标配方需要的内容。

## 时间与属性

配方中的 cost_time 使用 tick，Wiki 已除以 20 转为秒。例如梅酒的 **1120 tick = 56 秒**，不是 1120 秒。服务器低于正常 tick 速度或区块停止运行时，实际等候时间会更长。

饮料详情列出默认属性；酿造成品可带有实际组件，服务顾客时以拿到的那一瓶为准。菜单本只是允许点单的清单，仍需准备真正的库存。

<RecipeCatalog type="reverie_dreams:barrel" />

<SourceNote :files='["common/src/main/java/cc/thonly/reverie_dreams/block/kitchen/BrewingBarrelBlock.java","common/src/main/java/cc/thonly/reverie_dreams/block/entity/BrewingBarrelBlockEntity.java"]' />
