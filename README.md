# Daymark

一个轻量的 Android 倒数日应用。它把一个固定的时间点编译进 APK，安装后只展示倒数，不提供修改入口。

## 使用 GitHub Actions 自定义开始日期和主页图片

在 GitHub 的 Actions 页面手动运行 **Build Android APK**，填写：

- `start_date`：可以填写 `20260321`、`2026-12-31`，也可以填写带时区的 ISO-8601 时间。
- `home_image`：填写可访问的 PNG/JPEG/WEBP 图片 URL，或仓库内已有图片路径；留空使用默认主页图片。

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

## 本地构建

需要 JDK 17、Android SDK 35 和 Gradle 8.7+：

```text
gradle assembleDebug
gradle assembleRelease
gradle assembleRelease -PcountdownStart="2026-12-31T23:59:59+08:00"
```

Actions 只上传一个可直接安装的 debug 签名 APK，Artifact 名称为 `daymark-installer`。下载后先解压 ZIP，再安装里面的 `daymark-installer.apk`；不要把 ZIP 或未签名 APK 当作安装包。

如果设备已经安装过其他签名版本的 Daymark，需要先卸载旧版本再安装新构建；应用包名是 `com.example.daymark`。
默认时间是 `2026-10-01T00:00:00+08:00`。
