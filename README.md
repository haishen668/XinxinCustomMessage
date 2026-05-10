# XinxinCustomMessage

Minecraft 服务器 QQ 群自定义消息回复插件。玩家在 QQ 群发送关键词，机器人自动回复文字、图片或执行服务器命令。

## 前置要求

| 依赖 | 说明 | 必装 |
|------|------|------|
| [XinxinBotApi](https://github.com/haishen668/XinxinBotApi) | QQ 机器人接口 | 是 |
| [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) | 变量解析 | 是 |
| [PokemonBag](https://github.com/haishen668/PokemonBag) | 精灵背包（可选） | 否 |
| [SkullAPI](https://github.com/haishen668/SkullAPI) | 头像获取（可选） | 否 |

- 服务端: Spigot / Paper 1.13+
- Java: 8+

## 安装

1. 下载插件 jar 文件，放入服务器 `plugins` 文件夹
2. 安装前置插件 XinxinBotApi 和 PlaceholderAPI
3. 启动服务器，插件会自动生成配置文件
4. 编辑 `plugins/XinxinCustomMessage/config.yml` 配置你的消息模板
5. 在游戏内或控制台执行 `/xxcm reload` 重载配置

## 快速开始

### 最简单的例子：关键词回复文字

打开 `plugins/XinxinCustomMessage/config.yml`，在 `custom_messages` 下添加：

```yaml
custom_messages:
  我的帮助:                    # 模板名称（随便起，用于区分）
    trigger: "help"           # 触发词：玩家在群里发送 "help" 就会触发
    responses:                # 回复内容（可以写多条）
      - "这是帮助信息"
      - "输入 help 查看帮助"
      - "输入 info 查看信息"
```

保存后执行 `/xxcm reload`，玩家在群里发送 `help` 就会收到回复。

### 只在指定群生效

```yaml
custom_messages:
  我的帮助:
    trigger: "help"
    groups:                    # 只在这些群号生效（不填则所有群生效）
      - "123456789"
      - "987654321"
    responses:
      - "这是帮助信息"
```

### 未绑定玩家的提示

如果玩家没有进入过服务器（未绑定），可以显示不同的消息：

```yaml
custom_messages:
  我的信息:
    trigger: "info"
    unbind_messages:           # 未绑定玩家看到的消息
      - "您还没有进入过服务器，无法使用该功能"
    responses:                 # 已绑定玩家看到的消息
      - "玩家名：%player_name%"
      - "金币：%vault_eco_balance%"
```

## 触发方式

### 普通触发词

玩家发送的消息和触发词完全一致时触发（不区分大小写）。

```yaml
trigger: "help"       # 玩家发送 "help" 触发
```

### 带参数的触发词

用 `{extra}` 代表玩家发送的额外内容：

```yaml
trigger: "kick {extra}"    # 玩家发送 "kick 张三" 时，{extra} = "张三"
responses:
  - "[command] kick {extra}"   # 执行命令 kick 张三
```

### 群成员加入触发

```yaml
trigger: "[join]"      # 有人加入群时自动触发
```

### 群成员退出触发

```yaml
trigger: "[leave]"     # 有人退出群时自动触发
trigger: "[quit]"      # 同上，另一种写法
```

### 正则表达式触发

```yaml
trigger: "[regex]\\d+"    # 匹配纯数字消息
```

## 回复类型

### 文字回复

直接写文字内容，支持 PlaceholderAPI 变量：

```yaml
responses:
  - "你好，%player_name%！"
  - "你的金币：%vault_eco_balance%"
```

### @用户

使用 CQ 码 @ 发消息的人：

```yaml
responses:
  - "[CQ:at,qq={qq}],你好！"
```

### 发送图片

先在 `custom_images` 中定义图片模板，然后引用：

```yaml
responses:
  - "[image] 在线人数"     # 引用名为"在线人数"的图片模板
```

### 执行服务器命令

```yaml
responses:
  - "[command] list"                    # 执行单条命令
  - "[command] [say 你好, gamemode 1 {extra}]"   # 执行多条命令（用逗号分隔）
```

## 图片模板

图片模板可以在一张底图上叠加文字和子图片，生成自定义图片发送到群里。

### 基本结构

```yaml
custom_images:
  在线人数:                        # 图片模板名称
    source: "在线人数.png"          # 底图文件（放在 plugins/XinxinCustomMessage/images/ 下）
    texts:                         # 叠加的文字
      在线人数文字:
        text: "%server_online%"    # 文字内容（支持变量）
        x: 290                     # X 坐标（像素）
        z: 270                     # Y 坐标（像素）
        center: false              # 是否居中
        font: 站酷高端黑体          # 字体名称
        size: 120                  # 字体大小
        style: 0                   # 字体样式：0=普通 1=加粗 2=斜体 3=加粗斜体
    images:                        # 叠加的子图片（可选）
      头像:
        path: "头像.png"           # 子图片文件
        width: 100                 # 显示宽度
        height: 100                # 显示高度
        x: 50                      # X 坐标
        z: 50                      # Y 坐标
        center: false              # 是否居中
```

### 颜色代码

文字支持 Minecraft 颜色代码和十六进制颜色：

| 格式 | 示例 | 说明 |
|------|------|------|
| `&0`-`&f` | `&c` | Minecraft 标准颜色 |
| `&#RRGGBB` | `&#3380ff` | 十六进制颜色（蓝色） |

示例：`"&#3380ff%player_name%"` 显示蓝色的玩家名。

### 自定义字体

将 `.ttf` 字体文件放入 `plugins/XinxinCustomMessage/fonts/` 文件夹，然后在配置中使用文件名（不含 .ttf）作为字体名。

## 权限控制

### 管理员限定

只有指定 QQ 号才能触发该消息：

```yaml
custom_messages:
  执行命令:
    trigger: "执行 {extra}"
    admins:                      # 只有这些 QQ 号可以触发
      - "2821396723"
    responses:
      - "[command] {extra}"
```

### 全局黑名单

在 `global-setting.black_list` 中的 QQ 号无法触发任何消息：

```yaml
global-setting:
  black_list:
    - "1740023584"
```

### 全局管理员

全局管理员可以通过 @其他用户 来代替该用户执行命令：

```yaml
global-setting:
  admins:
    - "2821396723"
```

## 脚本功能

脚本可以实现条件判断，根据条件执行不同操作。

### 基本格式

```yaml
custom_messages:
  示例:
    trigger: "test"
    scripts:
      - "条件表达式 -> 不满足条件时执行的操作"
    responses:
      - "满足条件时的回复"
```

### 脚本类型

| 类型 | 格式 | 说明 |
|------|------|------|
| 发送消息 | `msg:消息内容` | 向群内发送一条消息 |
| 执行命令 | `cmd:命令` | 执行服务器命令 |

### 示例：根据条件执行不同命令

```yaml
scripts:
  - "%vault_eco_balance% < 100 -> msg:你的金币不足100"
  - "%player_level% < 10 -> cmd:gamemode survival {extra}"
```

- 如果玩家金币 < 100，发送"你的金币不足100"
- 如果玩家等级 < 10，执行 gamemode survival 命令
- 条件都满足则正常发送 responses 中的回复

## 命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/xxcm reload` | `xinxincustommessages.admin` | 重载配置文件 |
| `/xxcm fonts` | `xinxincustommessages.admin` | 查看可用字体列表 |
| `/xxcm log` | `xinxincustommessages.admin` | 开关消息日志输出 |
| `/xxcm listmessages` | `xinxincustommessages.admin` | 列出已加载的消息模板 |
| `/xxcm listimages` | `xinxincustommessages.admin` | 列出已加载的图片模板 |
| `/xxcm send <群号> <玩家> <消息ID> [extra]` | `xinxincustommessages.send` | 主动向群内发送消息 |

## PlaceholderAPI 变量

本插件提供以下 PlaceholderAPI 变量（需安装 PlaceholderAPI）：

| 变量 | 说明 |
|------|------|
| `%bot_invokeCounts_total%` | 消息模板总调用次数 |
| `%bot_invokeCounts_images%` | 图片模板调用次数 |
| `%bot_invokeCounts_texts%` | 普通模板调用次数 |

同时，你可以在 responses 和 scripts 中使用任何其他 PlaceholderAPI 变量（如 `%player_name%`、`%vault_eco_balance%` 等）。

## 配置文件完整示例

```yaml
# 是否开启调试模式（出错时显示详细信息）
debug: false

# 消息模板存放的文件夹（可以配置多个）
messagefolders:
  - messages

# 全局设置
global-setting:
  admins:
    - "2821396723"           # 全局管理员 QQ 号
  black_list:
    - "1740023584"           # 全局黑名单 QQ 号

# ========== 消息模板 ==========
custom_messages:
  帮助:
    trigger: "help"
    responses:
      - "[CQ:at,qq={qq}],你好"
      - "帮助菜单"
      - "  - info  个人信息"
      - "  - list  玩家列表"
      - "  - online 在线人数"

  我的信息:
    trigger: "info"
    unbind_messages:
      - "您还没有进入过服务器，无法使用该功能"
    responses:
      - "玩家：%player_name%"
      - "金币：%vault_eco_balance%"

  在线人数:
    trigger: "online"
    responses:
      - "[image] 在线人数"

  加群欢迎:
    trigger: "[join]"
    responses:
      - "[CQ:at,qq={qq}]欢迎加入！"

  踢人:
    trigger: "kick {extra}"
    admins:
      - "2821396723"
    responses:
      - "[command] kick {extra}"

  执行命令:
    trigger: "执行 {extra}"
    admins:
      - "2821396723"
    responses:
      - "[command] {extra}"

# ========== 图片模板 ==========
custom_images:
  在线人数:
    source: "在线人数.png"
    texts:
      在线人数:
        text: "%server_online%"
        x: 290
        z: 270
        center: false
        font: 站酷高端黑体
        size: 120
        style: 0
```

## 文件目录结构

```
plugins/XinxinCustomMessage/
├── config.yml              # 主配置文件
├── counts.yml              # 调用次数统计（自动生成）
├── images/                 # 图片资源目录
│   ├── 个人信息.png         # 默认底图
│   ├── 在线人数.png         # 默认底图
│   └── pokemonImg/         # 精灵背包图片（自动下载）
├── fonts/                  # 自定义字体目录
│   └── *.ttf               # TTF 字体文件
└── messages/               # 额外消息模板文件夹
    └── *.yml               # 额外的消息模板文件
```

## 常见问题

**Q: 发送关键词没有反应？**
- 检查是否执行了 `/xxcm reload`
- 检查触发词是否拼写正确（不区分大小写）
- 检查是否配置了 `groups` 限制了群号
- 检查是否在 `black_list` 黑名单中
- 开启 `debug: true` 查看详细日志

**Q: 变量没有解析？**
- 确保已安装 PlaceholderAPI 和对应的变量扩展
- 某些变量（如玩家信息）需要玩家已绑定 QQ

**Q: 图片发送失败？**
- 检查底图文件是否存在于 `images/` 目录
- 检查图片模板名称是否和 `[image]` 后面的名称一致
- 开启 `debug: true` 查看详细错误信息

**Q: 执行命令没有效果？**
- 命令是以控制台身份执行的，确保该命令控制台有权执行
- 检查是否配置了 `admins` 限制了执行权限

**Q: 如何添加多个消息模板文件？**
- 在 `messagefolders` 中配置多个文件夹，或在一个文件夹中放多个 `.yml` 文件
- 每个 `.yml` 文件的格式和 `config.yml` 中的 `custom_messages` / `custom_images` 相同

## 语雀文档

更详细的文档请查看: https://www.yuque.com/haishen668/xinxinbot/qxc0wpq762aycagd
