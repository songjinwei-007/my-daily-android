My Daily 安卓项目（Android APK）

推荐方式：用 GitHub Actions 自动编译 APK，不要求你本机安装 Android SDK。

方法一：Android Studio
1. 安装 Android Studio（建议最新版），安装 Android SDK Platform 35 和 JDK 17。
2. Android Studio -> Open -> 选择解压后的 MyDailyAndroid 文件夹。
3. 等待 Gradle Sync 完成。
4. 菜单 Build -> Build Bundle(s) / APK(s) -> Build APK(s)。
5. APK 通常位于 app/build/outputs/apk/debug/app-debug.apk。
6. 把 APK 传到手机，允许安装此来源的应用后安装。

方法二：GitHub Actions 自动编译（推荐）
1. 登录 GitHub，创建一个空仓库，例如 my-daily-android。
2. 将本文件夹内所有文件上传到仓库根目录（包含 .github 文件夹）。
3. 打开仓库的 Actions 页面，选择 Build My Daily APK，点击 Run workflow；如果上传时 workflow 已启用，也可能自动运行。
4. 构建完成后进入该次运行页面，在 Artifacts 下载 MyDaily-debug-apk.zip。
5. 解压后得到 app-debug.apk，发送到手机安装。

功能：日历、每日打卡、每项备注、自由记录、月度统计、四套主题、JSON 导入导出。
数据保存在本机应用 WebView 的本地存储中。卸载应用或清除应用数据前，请先导出备份。
本项目目前生成的是 debug APK，适合个人安装测试；不是 Play 商店发布签名包。
