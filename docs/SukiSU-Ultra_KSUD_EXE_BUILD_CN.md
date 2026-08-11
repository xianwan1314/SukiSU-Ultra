# SukiSU-Ultra `ksud.exe` 构建说明

本文档基于当前仓库实际状态，说明如何在 Windows 上编译可用的 `ksud.exe`，以及它依赖的内嵌资源、可用命令和限制范围。

## 1. 适用范围

本文档只描述 **PC 端非 Android 目标** 的 `ksud.exe`：

- `boot-patch`
- `boot-restore`
- `get-sign`
- `supported-kmis`

不包含以下内容：

- Android 设备侧完整 `ksud` 运行环境
- `manager` APK 打包
- `kernelsu.ko` 的完整 DDK 构建细节

相关源码入口：

- [userspace/ksud/src/main.rs](../userspace/ksud/src/main.rs)
- [userspace/ksud/src/cli_non_android.rs](../userspace/ksud/src/cli_non_android.rs)
- [userspace/ksud/src/assets.rs](../userspace/ksud/src/assets.rs)
- [userspace/ksud/src/boot_patch.rs](../userspace/ksud/src/boot_patch.rs)

## 2. 当前项目里的实际行为

### 2.1 非 Android 走哪套入口

当前 `ksud` 在非 Android 平台会进入 `cli_non_android`，也就是桌面端 CLI。

这意味着 Windows 下编出来的 `ksud.exe` 不是 Android 那套完整管理程序，而是用于：

- 补丁 `boot.img` / `init_boot.img`
- 恢复被 KernelSU 修改过的启动镜像
- 读取 APK 签名尺寸与哈希
- 列出当前内嵌的 KMI 列表

### 2.2 当前没有 `--arch`

当前仓库里的桌面端实现 **没有** `--arch` 参数。

也就是说：

- `supported-kmis` 只会枚举当前编译时嵌入的资源
- `boot-patch` 读取内嵌资源时，不会在运行时切换架构目录

### 2.3 非 Android 当前嵌入哪个目录

当前 [userspace/ksud/src/assets.rs](../userspace/ksud/src/assets.rs) 的实际实现是：

- Android `aarch64` 嵌入 `bin/aarch64`
- Android `arm` 嵌入 `bin/arm`
- Android `x86_64` 嵌入 `bin/x86_64`
- **非 Android 统一嵌入 `bin/aarch64`**

所以当前 Windows 下的 `ksud.exe`，内嵌资源来源固定是：

```txt
userspace/ksud/bin/aarch64/
```

这也是本文档所有示例都围绕 `aarch64` 目录展开的原因。

## 3. 什么叫“完整可用的 `ksud.exe`”

对当前仓库来说，能编出 `ksud.exe` 本体，不代表它已经具备完整的 `boot-patch` 能力。

桌面端 `boot-patch` 真正依赖的核心内嵌资源至少有：

- `ksuinit`
- 一个或多个 `*_kernelsu.ko`

当前仓库的 `userspace/ksud/bin/aarch64/` 还包含：

- `busybox`
- `bootctl`

但对非 Android 的 `boot-patch` 来说，最关键的是前两类。

## 4. 当前仓库中的资源状态

截至当前仓库状态，`userspace/ksud/bin/aarch64/` 已包含：

```txt
ksuinit
busybox
bootctl
android12-5.10_kernelsu.ko
android13-5.10_kernelsu.ko
android13-5.15_kernelsu.ko
android14-5.15_kernelsu.ko
android14-6.1_kernelsu.ko
android15-6.6_kernelsu.ko
android16-6.12_kernelsu.ko
```

因此，按当前仓库内容，Windows 下可以直接构建一个带这些 KMI 资源的 `ksud.exe`。

## 5. `ko` 文件命名规则

当前 `supported-kmis` 的识别规则非常直接：

- 扫描内嵌资源文件名
- 只识别以 `_kernelsu.ko` 结尾的文件
- 去掉这个后缀后作为 KMI 输出

因此文件名必须符合下面这种格式：

```txt
android12-5.10_kernelsu.ko
android13-5.10_kernelsu.ko
android13-5.15_kernelsu.ko
android14-5.15_kernelsu.ko
android14-6.1_kernelsu.ko
android15-6.6_kernelsu.ko
android16-6.12_kernelsu.ko
```

如果命名不符合这个格式：

- `supported-kmis` 不会列出来
- `boot-patch --kmi <kmi>` 也无法按默认逻辑命中对应资源

## 6. 资源从哪里来

### 6.1 `ksuinit`

`ksuinit` 来源通常有两种：

1. 本地自行构建
2. 使用项目 CI 产物

相关位置：

- [userspace/ksuinit](../userspace/ksuinit)
- [.github/workflows/ksuinit.yml](../.github/workflows/ksuinit.yml)

### 6.2 `*_kernelsu.ko`

`kernelsu.ko` 通常来自：

1. 项目 CI 产物
2. Linux / CI 环境下按 KMI 分别构建

相关工作流：

- [.github/workflows/build-lkm.yml](../.github/workflows/build-lkm.yml)
- [.github/workflows/ddk-lkm.yml](../.github/workflows/ddk-lkm.yml)

## 7. 如何补充或替换资源

如果你要替换当前内嵌的 `ksuinit` 或 `ko`，直接放到下面目录即可：

```txt
userspace/ksud/bin/aarch64/
```

例如：

```powershell
Copy-Item .\ksuinit userspace\ksud\bin\aarch64\ksuinit -Force
Copy-Item .\android13-5.10_kernelsu.ko userspace\ksud\bin\aarch64\android13-5.10_kernelsu.ko -Force
```

替换这类内嵌资源后，建议重新完整构建。

## 8. Windows 本地构建 `ksud.exe`

### 8.1 最直接的构建命令

在仓库根目录执行：

```powershell
cargo build -p ksud --release
```

当前仓库已实际验证这条命令可通过。

### 8.2 产物位置

构建成功后，产物位于：

- [target/release/ksud.exe](/D:/PhoneResources/vivo-manger-github/SukiSU-Ultra/target/release/ksud.exe)

注意：

- 不是 `userspace/ksud/target/...`
- 当前 workspace 的统一输出目录在仓库根目录 `target/`

### 8.3 什么时候建议先 `cargo clean`

如果你做过下面这些操作，建议先清理再重建：

- 替换了 `userspace/ksud/bin/aarch64/ksuinit`
- 替换了任意 `*_kernelsu.ko`
- 修改了 [userspace/ksud/src/assets.rs](../userspace/ksud/src/assets.rs)

命令：

```powershell
cargo clean -p ksud
cargo build -p ksud --release
```

如果你希望更彻底，也可以直接：

```powershell
cargo clean
cargo build -p ksud --release
```

## 9. 构建后的验证

### 9.1 查看帮助

```powershell
.\target\release\ksud.exe --help
```

当前实际可见的子命令应为：

- `boot-patch`
- `boot-restore`
- `get-sign`
- `supported-kmis`

### 9.2 查看当前内嵌 KMI

```powershell
.\target\release\ksud.exe supported-kmis
```

按当前仓库资源，预期会输出：

```txt
android12-5.10
android13-5.10
android13-5.15
android14-5.15
android14-6.1
android15-6.6
android16-6.12
```

### 9.3 `boot-patch` 示例

使用内嵌资源自动匹配：

```powershell
.\target\release\ksud.exe boot-patch -b .\boot.img --kmi android13-5.10
```

指定输出文件名：

```powershell
.\target\release\ksud.exe boot-patch -b .\boot.img --kmi android13-5.10 --out-name patched-boot.img
```

不依赖内嵌资源，手动指定 `ko` 与 `ksuinit`：

```powershell
.\target\release\ksud.exe boot-patch `
  -b .\boot.img `
  --kmi android13-5.10 `
  -m .\android13-5.10_kernelsu.ko `
  -i .\ksuinit `
  --out-name patched-boot.img
```

### 9.4 `boot-restore` 示例

```powershell
.\target\release\ksud.exe boot-restore -b .\patched-boot.img --out-name restored-boot.img
```

## 10. 当前项目与 CI 的关系

当前项目的 Android `ksud` 构建流程，会在 CI 中先准备资源，再编译：

- [.github/workflows/ksud.yml](../.github/workflows/ksud.yml)
- [.github/workflows/ksud-extra.yml](../.github/workflows/ksud-extra.yml)

其中关键动作是：

- 把 `*_kernelsu.ko` 放进 `userspace/ksud/bin/aarch64/`
- 把 `ksuinit` 放进 `userspace/ksud/bin/aarch64/`

这和当前 Windows 下可用的 `ksud.exe` 资源准备思路是一致的。

但要注意：

- 这些 CI 工作流主要是为 Android 目标服务
- 当前仓库**没有单独维护一条官方 Windows 发布工作流**
- Windows 本地 `ksud.exe` 是基于当前源码状态直接构建出来的桌面端工具

## 11. 常见问题

### 11.1 `supported-kmis` 没有输出

优先检查：

1. `userspace/ksud/bin/aarch64/` 下是否真的存在 `*_kernelsu.ko`
2. 文件名是否符合 `<kmi>_kernelsu.ko`
3. 替换资源后是否重新构建过
4. 当前运行的是否确实是最新的 `target/release/ksud.exe`

### 11.2 `boot-patch` 提示找不到内嵌资源

优先检查：

1. `--kmi` 是否填写为纯 KMI，例如 `android13-5.10`
2. `userspace/ksud/bin/aarch64/ksuinit` 是否存在
3. `userspace/ksud/bin/aarch64/<kmi>_kernelsu.ko` 是否存在
4. 资源更新后是否重新构建过 `ksud.exe`

### 11.3 替换资源后构建很快结束，但结果没变化

建议直接执行：

```powershell
cargo clean -p ksud
cargo build -p ksud --release
```

### 11.4 `cargo clean` 出现文件占用

这通常是 Windows 下有进程占用了 `target/` 内文件。可按下面顺序处理：

1. 关闭正在运行的 `ksud.exe`
2. 关闭可能占用 `target/` 的终端或工具
3. 再执行 `cargo clean`

### 11.5 是否必须用 nightly Rust

按当前仓库状态，**不需要**。

当前 Windows 构建 `ksud.exe` 可直接使用 stable Rust。

## 12. 当前文档对应的关键事实

本文档内容基于当前仓库已经验证的事实：

1. `userspace/ksud/bin/aarch64/` 已补齐 `ksuinit` 和多个 `*_kernelsu.ko`
2. 非 Android 构建当前从 `bin/aarch64` 嵌入资源
3. `cargo build -p ksud --release` 已在 Windows 本地构建成功
4. `ksud.exe --help` 与 `ksud.exe supported-kmis` 已实际验证通过

如果后续 `assets.rs`、CLI 参数或资源目录策略发生变化，本文档也需要同步更新。
