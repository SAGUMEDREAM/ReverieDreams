# BetterModel 实体开发依赖

已接入 **BetterModel 3.6.0 正式版** 的 Paper 编译 API，目标 Minecraft **26.1.2 / Java 25**。版本及官方运行插件 SHA-512 固定在 [bettermodel.lock.json](bettermodel.lock.json)。[Modrinth 正式版](https://modrinth.com/plugin/bettermodel/version/fToyVqvi) 的兼容列表明确包含 Paper 26.1.2。

`paper/bettermodel.gradle` 通过 Maven Central 引入 `compileOnly("io.github.toxicity188:bettermodel-bukkit-api:3.6.0")`，引擎类不打进 ReverieDreams JAR。插件描述符加入可选 BetterModel 依赖，安排其先加载并提供 classpath。目前没有调用引擎或改变任何实体、AI、模型和游戏功能。

在仓库根目录准备并校验独立运行插件：

```powershell
.\gradlew.bat -p paper/standalone :paper:prepareBetterModel --no-daemon --console=plain --max-workers=2 --project-cache-dir paper/build/standalone-gradle-cache
```

产物：`paper/build/dependencies/bettermodel-3.6.0-paper.jar`。这个任务只下载并校验，不安装到现有服务端、不启动测试服、不生成资源包；普通 `build` 也不会自动下载运行插件。实际部署时把该 JAR 与 CraftEngine、ReverieDreams 放入目标 Paper 服的 `plugins/`。BetterModel 自己所需的运行库由其加载器处理。

后续实体开发使用项目 Skill **`$bettermodel-entity-dev`**，入口位于 `.agents/skills/bettermodel-entity-dev/SKILL.md`，包含固定版本 API、tracker 归属与生命周期、模型/动画、CraftEngine 包合并、可选依赖隔离及可编译示例。

本次验证范围为依赖解析、Paper 编译、插件 JAR 和 API 示例编译、Skill 格式检查。没有启动服务端或客户端；模型渲染、动画与资源包合并留待具体实体开发时验证。Polymer 未涉及、未运行。
