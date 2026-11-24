# 幻想乡森林树苗系列

八种独立树苗：森林、白桦林、针叶林、樱花林、黑森林、魔法森林、枫林、丛林。ID为`gensokyo_<森林类型>_sapling`，例如`reverie_dreams:gensokyo_jungle_sapling`、`reverie_dreams:gensokyo_taiga_sapling`。

`GensokyoSaplings`复用`GensokyoForestType`和现有configured selector；`RDGensokyoSaplings`经RDWoodBlocks生命周期注册普通SaplingBlock及BlockItem。每次生长尝试从对应自然树池按原权重抽取一次，保留林龄/树形/木种混合；丛林包含六种树形，枫林保留原与新增两组及混生树。种植不经过自然树密度与BiomeFilter，因此可以在其他维度合适的土壤/空间上生长。未改变任何原版树苗或自然选树池。

自然随机刻、两阶段与骨粉成功率沿用原版。单株可选择宽干大树，需要足够的平整地面和完整树冠空间；失败由原版TreeGrower恢复树苗，下次可重新抽取。已接入种子创造页、自身掉落、block/item saplings标签、30%堆肥和100 tick燃料。不额外改变共用原版树叶的全局掉落，不增加配方。

外观只引用原版：森林→橡木、白桦林→白桦、针叶林→云杉、樱花林和枫林→樱花、黑森林→深色橡木、魔法森林→苍白橡木、丛林→丛林木。魔法森林树苗使用苍白橡木外观作物品区分，成年树仍是原有魔法森林树池。`blockstates`直接引用minecraft:block模型，`items`引用minecraft:item模型，贴图随原版模型继承；不复制/重画PNG或新建重复模型。原版资源包替换也会作用于这些引用。

ModelProvider、BlockLootTableProvider、Block/ItemTagProvider、BlockLang以及方块/状态清单provider负责输出；必须实际运行Fabric datagen。`sync-generated.py <隔离输出目录>`核对引用图、现有翻译、并行修改和HashCache后，只原样复制29份本任务provider输出。禁止手写或补丁生成JSON。

`inspect-assets.py`从当前MC依赖读取原版16×16贴图和模型并输出来源审计、浅深底参考图到`build/gensokyo-saplings-review`。它不生成游戏素材，也不代表实机验收。功能验证为定向TreeGrower键/selector放置与阻塞回归、datagen和Common/Fabric/NeoForge编译；正式dist是否更新另行报告。

获取示例：创造模式种子页搜索“幻想乡”或“魔法森林树苗”，或执行`/give @s reverie_dreams:gensokyo_jungle_sapling`。无缘塚沿用已有紫樱树苗；迷途竹林沿用已有特殊竹笋。
