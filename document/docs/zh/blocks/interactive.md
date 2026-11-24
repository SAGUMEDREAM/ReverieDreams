# 功能方块与装饰

功能方块需要按各自的交互方式使用。下面的全量方块表同时保留没有独立物品的作物和摆盘方块，避免把模型文件名误当作可获取物品。

| 方块 | 用途与入口 |
|---|---|
| 幻想乡祭坛 | 配合八根柱子制作特殊物品；见[搭建与启动](./altar) |
| 弹幕制作台 | 染料、核心、Power、Point 与形状制作弹幕 |
| 强化台 | 调整弹幕轨迹、速度、伤害或次数；见[弹幕指南](../items/danmaku) |
| 五类厨具 | 用指定食材点选料理；见[烹饪](./cooking) |
| 酿酒桶 | 配料、启动、等待、玻璃瓶取饮料；见[酿造](./brewing) |
| 夜雀钟与菜单本 | 设置菜品、绑定设施并控制营业；见[餐厅](./restaurant) |
| Fumo、桌椅与建筑方块 | 装饰、家具或相应交互；制作方式逐项查询 |

不同木种、染色和方块形态的配方分别保留。外观相似不代表库存、结构判定或配方里可以互换。没有对应物品的方块会直接显示方块 ID，不提供虚假的物品合成入口。


## 餐具、座位和储物

**椅子**普通空手右键坐下，潜行空手右键可让座位上的乘客离座；已经被占用的座位不能再坐一人。餐厅要预留可到达的座位和过道。

**盘子**可以展示一个物品：持物右键空盘放入一个，普通右键已有内容的盘子会将内容弹出；潜行右键可食用其中满足当前进食条件的食物。盘中的容器余物按实际食物消费规则处理。

**橱柜**右键打开分页储物界面，允许放入带食品/饮料属性或相关食物、饮料、食材标签的物品。它不是不加筛选的通用杂物箱。银箱、木箱等储物容器则按自己的库存界面使用，是否能被 NPC 物流识别还取决于容器支持。

**茶具、屏风、挂轴、暖帘、榻榻米和灯具**属于建筑装饰系列；不能把茶具模型中的茶水当成酿造机或无限饮料来源。对应木种、颜色和变体的材料可从配方查询追溯。

## 制冰机

手持**水桶右键**空闲、没有残留成品的制冰机，消耗水桶并返还空桶。标准效率下运行 **8 秒**，从实际制冰产物标签中随机产生 **8 份**物品；取完本批才能开始下一批。完成后右键逐份取出，背包满时留意掉落物。

自动化可让发射器朝向制冰机：水桶用于启动，镐子用于逐份取出成品并扣耐久，成品从机器对应出口掉出。它不会把投入的水桶直接变成固定种类的八块冰，具体候选以标签为准。

## 祈愿箱（塞钱箱）与音乐

**祈愿箱**（原**塞钱箱**，`cash_box`）手持铜币、银币或金币使用可供奉，增加信仰；同一玩家每游戏日最多成功一次，成功会随机消耗 1～3 枚所持钱币。没有持钱币时打开钱币储物界面。供奉不等同于把等值钱币存入支票。

**音乐盒**读取实例里的 `config/reverie_dreams/nota` 目录中的 NBS 文件。潜行右键选择曲目，普通右键快速切换；接入红石后播放。先让服务器管理者放好有效的 .nbs 文件，MP3 或普通唱片文件不能直接作为 NBS 使用。NPC 演奏也使用这套本地曲目来源。

## 红石、矿车与广播

| 方块 | 操作和实际逻辑 |
|---|---|
| 停车控制轨 | 没有红石输入时停住靠近中心的矿车，给信号后放行；可通过比较器读取有车状态 |
| 标识检测轨 | 用改过名的命名牌设置轨道标识，再给矿车设置标识；符合匹配规则的矿车经过时触发信号 |
| 信号延迟器 | 右键设置正数 tick 延迟；输入持续到计时结束才输出，撤去输入后复位 |
| 远程发送端/接收端 | 分别右键设置相同的信号名和 token；发送端读取红石，匹配的接收端输出信号。两项均需非空，设施需要保持加载 |
| 广播器 | 普通右键设置广播文字，潜行右键开关铃声；红石由无到有时向周围玩家发送文字 |

标识检测轨支持相同标识，也支持双方非空时一方包含另一方的匹配，设计站点名时避免意外重叠。远程信号的 token 是配对内容，别只配置看起来相同的显示名称。

<BlockCatalog />

<SourceNote :files='["common/src/main/java/cc/thonly/reverie_dreams/registry/content/block/RDBlocks.java","common/src/main/java/cc/thonly/reverie_dreams/creative_tab/content/DecorationCreativeTab.java","common/src/main/java/cc/thonly/reverie_dreams/block/kitchen/ChairBlock.java","common/src/main/java/cc/thonly/reverie_dreams/block/kitchen/PlateBlock.java","common/src/main/java/cc/thonly/reverie_dreams/block/kitchen/IceMakingMachine.java","common/src/main/java/cc/thonly/reverie_dreams/block/entity/IceMakingMachineBlockEntity.java","common/src/main/java/cc/thonly/reverie_dreams/gui/block/CupboardGui.java","common/src/main/java/cc/thonly/reverie_dreams/block/props/CashBoxBlock.java","common/src/main/java/cc/thonly/reverie_dreams/block/props/MusicBlock.java","common/src/main/java/cc/thonly/reverie_dreams/block/redstone/RailControllerBlock.java","common/src/main/java/cc/thonly/reverie_dreams/block/redstone/SignalRailBlock.java","common/src/main/java/cc/thonly/reverie_dreams/block/redstone/SignalDelayerBlock.java","common/src/main/java/cc/thonly/reverie_dreams/block/redstone/RemoteServerBlock.java","common/src/main/java/cc/thonly/reverie_dreams/block/redstone/RemoteClientBlock.java","common/src/main/java/cc/thonly/reverie_dreams/block/redstone/SpeakerBlock.java"]' />
