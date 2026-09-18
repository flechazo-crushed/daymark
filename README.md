# Daymark

一个轻量的 Android 倒数日应用。它把一个固定的时间点编译进 APK，安装后只展示倒数，不提供修改入口。

## 使用 GitHub Actions 构建 Release

当前版本固定使用仓库内的 `app/src/main/res/drawable/home_image.jpg` 作为背景，日期固定为 `20260321`，不再通过 Action 输入自定义。

Action 会把固定日期作为 Gradle 参数传入：

```text
gradle assembleRelease -PcountdownStart="20260321"
```

`app/build.gradle` 将日期转换为 epoch 秒并写入 `BuildConfig`。运行时没有编辑页面，也没有网络权限；要更换日期或主页图片，需要修改源码后重新发布 APK。

主页采用全屏背景图、暗色遮罩和中央模糊毛玻璃卡片。背景图只在构建时写入 APK，应用内不能替换。

## 本地构建

需要 JDK 17、Android SDK 35 和 Gradle 8.7+：

```text
gradle assembleDebug
gradle assembleRelease
gradle assembleRelease -PcountdownStart="20260321"
```

Actions 会上传一个已签名的 Release APK，Artifact 名称为 `daymark-release`；推送 `v*` 标签时还会自动创建 GitHub Release。下载后先解压 ZIP，再安装里面的 `daymark-release.apk`；不要直接安装 ZIP。

Release 使用固定的 GitHub Secrets 签名密钥，Secrets 名称为 `DAYMARK_KEYSTORE_BASE64`、`DAYMARK_KEYSTORE_PASSWORD`、`DAYMARK_KEY_ALIAS` 和 `DAYMARK_KEY_PASSWORD`。应用包名是 `com.example.daymark`；固定密钥启用后，后续版本可以直接覆盖安装。此前由临时密钥签名的版本无法改签，首次切换固定密钥需要卸载一次旧版本。
默认时间是 `20260321`。
