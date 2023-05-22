# UiKit-Android

*English | [中文](README.zh.md)*

UiKit is a set of scaffolding for scenario-based applications. It provides Ui components and Service components to facilitate developers to quickly build their own scenario-based applications.

## Project Construction
![图片](https://download.agora.io/null/UiKit.png)

## Features
- [KaraokeUiKit](examples/AUIKitKaraoke/main/java/io/agora/app/karaoke/kit) **([Document](doc/KaraokeUiKit.md))**
- [AScenesKit](asceneskit)
  - [Karaoke](asceneskit/src/main/java/io/agora/asceneskit/karaoke)
    - [KaraokeRoomView](asceneskit/src/main/java/io/agora/asceneskit/karaoke/KaraokeRoomView.kt)
    - [KaraokeRoomService](asceneskit/src/main/java/io/agora/asceneskit/karaoke/KaraokeRoomService.kt)
- [AUiKit](auikit)
  - [Service](auikit/src/main/java/io/agora/auikit/service)**([Document](doc/AUiKit-Service.md))**
    - [AUiRoomManager](auikit/src/main/java/io/agora/auikit/service/imp/AUiRoomServiceImpl.kt)
    - [AUiUserService](auikit/src/main/java/io/agora/auikit/service/imp/AUiUserServiceImpl.kt)
    - [AUiMicSeatService](auikit/src/main/java/io/agora/auikit/service/imp/AUiMicSeatServiceImpl.kt)
    - [AUiMusicPlayerService](auikit/src/main/java/io/agora/auikit/service/imp/AUiMusicPlayerServiceImpl.kt)
    - [AUiChorusService](auikit/src/main/java/io/agora/auikit/service/imp/AUiChorusServiceImpl.kt)
    - [AUiJukeboxService](auikit/src/main/java/io/agora/auikit/service/imp/AUiJukeboxServiceImpl.kt)
  - [Ui](auikit/src/main/java/io/agora/auikit/ui)**([Document](doc/AUiKit-Ui.md))**
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

## Quick Start

### 1. Environment Setup

- <mark>Minimum Compatibility with Android 5.0</mark>（SDK API Level 21）
- Android Studio 3.5 and above versions.
- Mobile devices with Android 5.0 and above.

---

### 2. Running the Example
- Obtain Agora SDK
  Download [the rtc sdk with rtm 2.0](https://download.agora.io/null/Agora_Native_SDK_for_Android_rel.v4.1.1.30_49294_FULL_20230512_1606_264137.zip) and then unzip it to the directions belows:
  [auikit/libs](auikit/libs) : agora-rtc-sdk.jar
  [auikit/src/main/jniLibs](uikit/src/main/jniLibs) : so(arm64-v8a/armeabi-v7a/x86/x86_64)

- Please fill in the domain name of the business server in the [**local.properties**](/local.properties) file of the project

  ![PIC](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_serverhost_android.png)

``` 
SERVER_HOST= （Domain name of the business server）
```

> Agora Test Domain: https://service.agora.io/uikit-karaoke

- Execute in the AUIKitKaraoke directory:
```
git submodule update --init
```

- Run the project with Android Studio to begin your experience.

## License
Copyright © Agora Corporation. All rights reserved.
Licensed under the [MIT license](LICENSE).