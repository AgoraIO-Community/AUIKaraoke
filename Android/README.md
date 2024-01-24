# AUIKaraoke-Android Quick Start

*English | [中文](README.zh.md)*

This document mainly introduces how to quickly run through the AUIKaraoke example  and experience online karaoke scenarios, including micseat service, user service, music service, song player service, etc. For a more detailed introduction, please refer to  [AUIKit](https://github.com/AgoraIO-Community/AUIKit/blob/main/Android/README.md)

## Architecture

![auikitkaraoke-architecture](https://download.agora.io/demo/release/auikitkaraoke-architecture.png)

## Directory

```
.
├── app                                       				            // Demo
└── asceneskit																				
    └── src
        └── main
            ├── java
            │   └── io.agora.asceneskit.karaoke				
            │       ├── AUIKaraokeRoomService.kt	    // Room service, manage AUIKit services 
            │       ├── KaraokeRoomView.kt            // Room UI，manage AUIKit UI components
            │       ├── KaraokeUiKit.kt           	// Karaoke launch class
            │       └── binder
            │           ├── AUIChatBottomBarBinder.kt	// The binder of bottom bar UI and services
            │           ├── AUIChatListBinder.kt		// The binder of chat list UI and services
            │           ├── AUIGiftBarrageBinder.kt	// The binder of gift UI and services
            │           ├── AUIJukeboxBinder.java 	// The binder of jukebox UI and services
            │           ├── AUIMicSeatsBinder.java	// The binder of micseats UI and services
            │           ├── AUIMusicPlayerBinder.java	// The binder of music player UI and services
            │           └── IAUIBindable.java
            ├── res
            └── res-ktv
```



## Requirements

- <mark>Minimum Compatibility with Android 7.0</mark>（SDK API Level 24）
- Android Studio 3.5 and above versions.
- Mobile devices with Android 7.0 and above.
- JDK 17.

## Getting Started

### 1. Deployment backend services

[How to deploy Karaoke backend services](../backend)  

### 2. Build

- Please fill in the domain name of the business server in the local.properties(If the file does not exist, create one) file of the project
  
  ![PIC](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_serverhost_android.png)

``` 
SERVER_HOST= （Domain name of the business server）
```

> If you do not intend to deploy backend services temporarily, you can use the agora test server host:
>
>  https://service.agora.io/uikit-karaoke


- Open the project with Android Studio and begin your experience.

## Quick integration
Please review [KaraokeUIKit](./doc/KaraokeUIKit.md)

## FAQ

- [FAQ](./doc/KaraokeFAQ.md)

- If you have any other questions, please feedback to the [developer community](

## License

Copyright © Agora Corporation. All rights reserved.
Licensed under the [MIT license](LICENSE).