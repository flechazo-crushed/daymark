# Daymark

一个轻量的 Android 倒数日应用。它把一个固定的时间点编译进 APK，安装后只展示倒数，不提供修改入口。

## 使用 GitHub Actions 自定义开始日期和主页图片

在 GitHub 的 Actions 页面手动运行 **Build Android APK**，填写：

- `start_date`：可以填写 `20260321`、`2026-12-31`，也可以填写带时区的 ISO-8601 时间。
- `home_image`：填写可访问的 PNG/JPEG/WEBP 图片 URL，或仓库内已有图片路径；默认使用 `mmexport1789272863975.jpg`。

例如：

```text
start_date: 2026-12-31T23:59:59+08:00
home_image: https://example.com/my-home-image.jpg
```

Action 会把这个值作为 Gradle 参数传入：

```text
gradle assembleRelease -PcountdownStart="2026-12-31T23:59:59+08:00"
```

`app/build.gradle` 将日期转换为 epoch 秒并写入 `BuildConfig`。私有 GitHub blob 图片会通过 Action 的只读 `GITHUB_TOKEN` 读取，然后在构建阶段复制到 Android drawable 资源中。运行时没有编辑页面，也没有网络权限；要更换日期或主页图片，需要重新运行 Action 并安装新的 APK。

主页采用全屏背景图、暗色遮罩和中央模糊毛玻璃卡片。背景图只在构建时写入 APK，应用内不能替换。

## 本地构建

需要 JDK 17、Android SDK 35 和 Gradle 8.7+：

```text
gradle assembleDebug
gradle assembleRelease
gradle assembleRelease -PcountdownStart="2026-12-31T23:59:59+08:00"
```

Actions 只上传一个已签名的 Release APK，Artifact 名称为 `daymark-release`。下载后先解压 ZIP，再安装里面的 `daymark-release.apk`；不要直接安装 ZIP。

当前 workflow 每次构建都会生成临时 Release 签名，因此如果设备已经安装过旧构建，需要先卸载旧版本再安装新构建；应用包名是 `com.example.daymark`。若需要跨版本覆盖安装，应改用固定的 GitHub Secrets 签名密钥。
默认时间是 `20260321`，也可以在运行 Action 时改成其他日期或时间。
