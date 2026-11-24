# 装备与防具

防具放在对应装备槽才生效；主副手道具按各自说明使用。点击下方装备可查看真实耐久、默认组件和制作配方，不用材料名字推断未实现的套装效果。

| 装备 | 实际属性与生效逻辑 |
|---|---|
| **神子耳机** | 戴上后在屏幕上显示附近声源的方向指示 |
| **恋恋钢盔** | 戴上后，处于潜行缓步移动状态时获得完全隐身效果 |
| **鬼面** | 耐久 198，散发鬼之威压：周围半径 **8 格** 内的敌对怪物每 2 秒陷入 **4 秒虚弱 I**；同时鬼族（Oni）视佩戴者为同族，**不会主动发起敌对攻击**。可在铁砧使用**鬼角**修复 |
| **低重力靴子** | 穿戴后获得跳跃提升与缓降效果；适合高低落差地形与探索跳跃 |
| **冥界之冠** | 普通亡灵生物不主动攻击佩戴者，且治疗与伤害药水效果对佩戴者发生逆转（伤害药水回血、治疗药水扣血）。祭坛合成需消耗幽灵残影与凋灵骷髅头颅等材料 |
| **速度羽毛** | 放在主手或副手提供移动速度加成；也是强化台中弹幕速度的核心升级材料 |

天狗手盾属于举盾使用物品，见[武器操作](./weapons)。不要把月球维度中的缓降与低重力靴子的穿戴效果混为一项属性。

## 生命上限

使用**残机**增加一颗心的生命上限，受服务器 maxUpgradedHealthValue 配置限制。残机碎片与残机分别是不同物品，需要按实际配方转换；背包里放着碎片不会自动加血。

<ItemCatalog category="equipment" />

<SourceNote :files='["common/src/main/java/cc/thonly/reverie_dreams/registry/content/item/RDItems.java","common/src/main/java/cc/thonly/reverie_dreams/item/armor/OniMaskItem.java","common/src/generated/resources/assets/reverie_dreams/lang/zh_cn.json"]' />