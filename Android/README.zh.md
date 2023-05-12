# UiKit-Android

*[English](README.md) | 中文*

UiKit是一套场景化应用的脚手架，提供Ui组件以及Service组件，方便开发者快速搭建起自己的场景化应用。


## 项目结构
![图片](https://download.agora.io/null/UiKit.png)


## 特性
- [KaraokeUiKit](examples/AUIKitKaraoke/src/main/java/io/agora/app/karaoke/kit) **([使用指南](doc/KaraokeUiKit.zh.md))**
- [AScenesKit](asceneskit)
    - [Karaoke](asceneskit/src/main/java/io/agora/asceneskit/karaoke)
        - [KaraokeRoomView](asceneskit/src/main/java/io/agora/asceneskit/karaoke/KaraokeRoomView.kt)
        - [KaraokeRoomService](asceneskit/src/main/java/io/agora/asceneskit/karaoke/IKaraokeRoomService.kt)
- [AUiKit](auikit)
    - [Service](auikit/src/main/java/io/agora/auikit/service)**([使用指南](doc/AUiKit-Service.zh.md))**
        - [AUiRoomManager](auikit/src/main/java/io/agora/auikit/service/IAUiRoomManager.java)
        - [AUiUserService](auikit/src/main/java/io/agora/auikit/service/IAUiUserService.java)
        - [AUiMicSeatService](auikit/src/main/java/io/agora/auikit/service/IAUiMicSeatService.java)
        - [AUiMusicPlayerService](auikit/src/main/java/io/agora/auikit/service/IAUiMusicPlayerService.java)
        - [AUiChorusService](auikit/src/main/java/io/agora/auikit/service/IAUiChorusService.java)
        - [AUiJukeboxService](auikit/src/main/java/io/agora/auikit/service/IAUiJukeboxService.java)
    - [Ui](auikit/src/main/java/io/agora/auikit/ui)**([使用指南](doc/AUiKit-Ui.zh.md))**
        - [Feature Ui Widgets](auikit/src/main/java/io/agora/auikit/ui)
            - [AUiMicSeatsView](auikit/src/main/java/io/agora/auikit/ui/micseats/impl/AUIMicSeatsView.java)
            - [AUiJukeboxView](auikit/src/main/java/io/agora/auikit/ui/jukebox/impl/AUiJukeboxView.java)
            - [AUiMusicPlayerView](auikit/src/main/java/io/agora/auikit/ui/musicplayer/impl/AUiMusicPlayerView.java)
            - [AUiMemberView](auikit/src/main/java/io/agora/auikit/ui/member/impl/AUiRoomMemberListView.kt)
        - [Basic Ui Widgets](auikit/src/main/java/io/agora/auikit/ui/basic)
            - [AUiButton](auikit/src/main/java/io/agora/auikit/ui/basic/AUiButton.java)
            - [AUiBottomDialog](auikit/src/main/java/io/agora/auikit/ui/basic/AUiBottomDialog.java)
            - [AUiAlertDialog](auikit/src/main/java/io/agora/auikit/ui/basic/AUiAlertDialog.java)
            - [AUiTabLayout](auikit/src/main/java/io/agora/auikit/ui/basic/AUiTabLayout.java)
            - [AUiEditText](auikit/src/main/java/io/agora/auikit/ui/basic/AUiEditText.java)
            - ...

## 快速跑通

### 1. 环境准备

- <mark>最低兼容 Android 5.0</mark>（SDK API Level 21）
- Android Studio 3.5及以上版本。
- Android 5.0 及以上的手机设备。

---

### 2. 运行示例
- 获取声网sdk
  下载[包含RTM 2.0的RTC SDK最新版本](https://download.agora.io/null/Agora_Native_SDK_for_Android_rel.v4.1.1.30_49294_FULL_20230512_1606_264137.zip)并将文件解压到以下目录
  [auikit/libs](auikit/libs) : agora-rtc-sdk.jar
  [auikit/src/main/jniLibs](uikit/src/main/jniLibs) : so(arm64-v8a/armeabi-v7a/x86/x86_64)

- 在项目的[**local.properties**](/local.properties)里配置业务服务器域名

  ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_serverhost_android.png)

``` 
SERVER_HOST= （业务服务器域名）
```

> 声网测试域名： https://service.agora.io/uikit-karaoke，


- 用 Android Studio 运行项目即可开始您的体验

## 许可证
版权所有 Agora, Inc. 保留所有权利。
使用 [MIT 许可证](LICENSE)
