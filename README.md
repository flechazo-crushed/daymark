# Daymark

一个轻量的 Android 倒数日应用。它把一个固定的时间点编译进 APK，安装后只展示倒数，不提供修改入口。

## 使用 GitHub Actions 自定义起始时间

在 GitHub 的 Actions 页面手动运行 **Build Android APK**，填写 `start_time`，格式为 ISO-8601，例如：

```text
2026-12-31T23:59:59+08:00
```

Action 会把这个值作为 Gradle 参数传入：

```text
gradle assembleRelease -PcountdownStart="2026-12-31T23:59:59+08:00"
```

`app/build.gradle` 将其转换为 epoch 秒并写入 `BuildConfig`。运行时没有编辑页面；要更换时间，需要重新运行 Action 并安装新的 APK。

## 本地构建

需要 JDK 17、Android SDK 35 和 Gradle 8.7+：

```text
gradle assembleDebug
gradle assembleRelease
gradle assembleRelease -PcountdownStart="2026-12-31T23:59:59+08:00"
```

Actions 会同时上传 debug APK（可直接安装）和 release unsigned APK（用于后续接入正式签名）。
默认时间是 `2026-10-01T00:00:00+08:00`。
