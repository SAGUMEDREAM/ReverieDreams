# STG 弹幕引擎（新版）设计说明

> 本文件由开发过程整理，描述 `reverie_dreams` 新版 STG 弹幕引擎的结构、数据格式与用法。
> 旧版 `DanmakuTrajectory` / `SpellcardRenderer` 仍然保留可用（兼容层），新内容建议使用新引擎。

## 1. 总览

```
DanmakuDirector（服务端唯一 tick 调度）
 └─ DanmakuPattern（图案/符卡 = 发射器时间线）
     └─ DanmakuEmitter（发射口）
         ├─ 计时：delay / duration / interval
         ├─ 位置：锚点 + offset，可绕锚点旋转（rotation 关键帧）
         ├─ 朝向：EmitAim（TARGET/FIXED/SOURCE_FACING/OUTWARD）+ pitch/yaw 与角度关键帧
         ├─ 形状：EmitShape（AIM/FAN/RING/MULTI_RING/MATRIX）
         └─ 子弹：DanmakuBulletSpec（类型/颜色/速度/缩放/伤害/寿命/运动模型）
 └─ DanmakuEntity（保留实体弹幕）
     └─ DanmakuMotion（运动模型，随实体同步，双端确定性步进）
         └─ MotionSegment：rotate / orbit / accelerate / homing
```

### 对应参考资料的核心思路

| 参考资料 | 新引擎表达 |
|---|---|
| 假追踪弹幕 | 发射器 `aim=target`，齐射瞬间解算朝向 |
| 圆形弹幕 | `shape=ring` + `face_outward=true` |
| 扇形弹幕 | `shape=fan` + `spread` |
| 螺旋弹幕 | 发射口 `offset` + `rotation` 匀速关键帧 + `aim=outward` |
| 匀加速旋转弹幕 | `rotation` 用 `quadratic` 关键帧（角度二次增长） |
| 波与粒子的境界 | `rotation` 用 `sine` 关键帧 |
| 花型弹幕 | 子弹出膛后 `motion.segments` 中延迟 `rotate` / `orbit` |

## 2. 数据格式（数据包）

路径：`data/<namespace>/danmaku_pattern/<name>.json`

```json
{
  "emitters": [
    {
      "delay": 0,
      "duration": 120,
      "interval": 4,
      "aim": "outward",
      "attached": true,
      "offset": [0.9, 0.0, 0.0],
      "rotation": { "key": "reverie_dreams:quadratic", "params": { "start": 0.0, "end": 1080.0 } },
      "shape": { "type": "aim" },
      "bullet": {
        "type": "reverie_dreams:rice",
        "random_color": true,
        "speed": 1.0,
        "lifetime": 160,
        "motion": {
          "spin": 4.0,
          "segments": [
            { "at": 60, "type": "rotate", "value": 4.0, "axis": [0.0, 1.0, 0.0] }
          ]
        }
      }
    }
  ]
}
```

### 发射器字段

| 字段 | 默认 | 说明 |
|---|---|---|
| `delay` | 0 | 距离图案开始多少 tick 开火 |
| `duration` | 100 | 持续开火 tick 数 |
| `interval` | 10 | 每多少 tick 一次齐射 |
| `aim` | `target` | `target`/`fixed`/`source_facing`/`outward` |
| `attached` | true | true 跟随源实体，false 固定在启动位置 |
| `offset` | `[0,0,0]` | 发射口相对锚点偏移（格） |
| `rotation` | 0→0 | 关键帧函数，输出角度（度），使 offset 随时间绕锚点旋转 |
| `pitch` / `yaw` | 0 | 固定朝向基准角 |
| `pitch_range` / `yaw_range` | 0→0 | 关键帧函数，输出角度偏移（叠加到基准朝向） |
| `shape` | `aim` | 发射形状，见下 |
| `bullet` | amulet 默认弹 | 子弹规格 |

### 形状字段

| 类型 | 参数 |
|---|---|
| `aim` | 无 |
| `fan` | `count`、`spread`（扇形散布角度） |
| `ring` | `count`、`radius`（出膛分布半径）、`face_outward`（是否朝外） |
| `multi_ring` | `rings`、`count`、`radius`、`ring_angle_step`、`ring_radius_step`、`ring_speed_step` |
| `matrix` | `pattern`（字符串数组，`#` 为子弹） |

### 子弹字段

| 字段 | 默认 | 说明 |
|---|---|---|
| `type` | 必填 | 弹幕类型 id，如 `reverie_dreams:star` |
| `color` | -1 | RGB 染色，-1 为类型默认 |
| `random_color` | false | 每发随机颜色 |
| `speed` | 类型默认 | 格/tick |
| `scale` | 1 | 缩放倍率 |
| `damage` | 类型默认 | 伤害覆盖 |
| `lifetime` | 200 | 寿命（tick） |
| `motion` | 直线 | 运动模型 |

### 运动模型字段

| 字段 | 说明 |
|---|---|
| `acceleration` | 沿自身方向的持续加速度（格/tick²） |
| `spin` | 视觉自转（度/tick） |
| `origin` | 出膛点（由生成器自动写入，`center_mode=origin` 的公转段使用） |
| `segments[].at` | 该行为段在第几 tick 生效 |
| `segments[].type` | `rotate` 绕轴转向 / `orbit` 绕点公转 / `accelerate` 变速 / `homing` 追踪 |
| `segments[].value` | rotate/orbit 为度/tick；accelerate 为加速度；homing 为转向速率（度/tick） |
| `segments[].axis` | 旋转轴，默认 `[0,1,0]` |
| `segments[].center` | orbit 的圆心偏移（相对生效瞬间自身位置） |
| `segments[].center_mode` | orbit 圆心基准：`self`（默认，激活瞬间自身位置）或 `origin`（子弹出膛点，即文档中绕 circle_O 旋转的花型弹幕） |
| `segments[].duration` | 持续时间（-1 无限），rotate/homing 可用 |

### 关键帧函数（`rotation` / `pitch_range` / `yaw_range`）

`key` 可选：`reverie_dreams:linear`（匀速）、`reverie_dreams:quadratic`（匀加速）、
`reverie_dreams:sine`、`reverie_dreams:cosine`、`reverie_dreams:triangle`、`reverie_dreams:saw`。

参数：`start`、`end` 必用；`sine`/`cosine` 额外支持 `frequency`、`phase`；`triangle`/`saw` 支持 `frequency`。

## 3. 内置图案

| id | 说明 |
|---|---|
| `reverie_dreams:stg/aimed_fan` | 假追踪扇形 |
| `reverie_dreams:stg/fan` | 扇形弹幕 |
| `reverie_dreams:stg/circle` | 圆形弹幕 |
| `reverie_dreams:stg/star_matrix` | 星形矩阵 |
| `reverie_dreams:stg/spiral` | 螺旋弹幕 |
| `reverie_dreams:stg/accelerating_rotation` | 匀加速旋转弹幕 |
| `reverie_dreams:stg/wave_particle` | 波与粒子的境界 |
| `reverie_dreams:stg/flower` | 花型弹幕 |

## 4. 使用方式

- 命令：
  - `/touhou get_pattern_card <pattern>` 获得图案符卡
  - `/touhou cast_pattern <pattern>` 直接以自己为中心施放图案
- 物品组件：符卡物品（`reverie_dreams:spellcard`）带 `danmaku_pattern` 组件时，右键施放对应图案；
  旧符卡（`spell_card_component`）仍然走旧 `SpellcardRenderer`。
- 创造模式物品栏「弹幕」页会列出全部图案卡。

## 5. 迁移与兼容

- 旧 `DanmakuTrajectory`（物品右键瞬发）与 `SpellcardRenderer`（旧符卡 JSON）保持原样可用。
- `LegacySpellcardAdapter.toPattern(...)` 可以把旧 `List<List<SpellCardFrameConfig>>` 近似转换为新图案。
- 旧弹幕物品的子弹（`DanmakuEntity`）现在也由新的 `DanmakuMotion` 驱动（默认直线匀速，不再受原版惯性影响）。
