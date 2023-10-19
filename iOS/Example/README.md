# AUIKaraoke-iOS Quick Start

*English | [中文](README_zh.md)

This document mainly introduces how to quickly run through the AUIKaraoke example  and experience online karaoke scenarios, including micseat service, user service, music service, song player service, etc. For a more detailed introduction, please refer to [AUIScenesKit](../AScenesKit/README.md) and [AUIKit](https://github.com/AgoraIO-Community/AUIKit/tree/main/iOS)

## Architecture
![](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/uikit_structure_chart.png)

## Directory
```
┌─ Example                     // Demo Code Integration Catalog
│  └─ AUIKaraoke            // Provide Karaoke's integrated page
├─ AUIScenesKit                // Scenario business assembly module, currently only including Karaoke
│  ├─ AUIKaraokeRoomView       // Karaoke room container view, used to splice various basic components and bind them to services
│  ├─ AUIKaraokeRoomService    // Karaoke Room Service, used to create various basic services and initialize RTC/RTM/KTVAPi, etc
│  └─ Binder                   // Business binding module that associates UI Components with Service
└─ AUIKit                      // Including basic components and services
   ├─ Service                  // Related basic component services, including micseat, jukebox, user, choir, etc
   ├─ UI Widgets               // Basic UI component, supporting one click skin changing through configuration files
   └─ UI Components            // Related basic business UI modules, including micseat, jukebox, song playback, etc. These UI modules do not contain any business logic and are pure UI modules
   
```

## Requirements

- Xcode 13.0 and later

- Minimum OS version: iOS 13.0

- Please ensure that your project has a valid developer signature set up


## Getting Started

### 1. Deployment backend services

[How to deploy Karaoke backend services](../../backend)  

### 2. Build
- Clone or download  source code
- Fill in the HostUrl for step 1 in the [KeyCenter. swift] (AUIKaraoke/KeyCenter. swift) of the project
```
static var HostUrl: String = <#Your HostUrl#>
```
If you do not intend to deploy backend services temporarily, you can use the default domain name in KeyCenter.swift

- Open the terminal and enter the [Podfile](Podfile) directory, run `pod update`
  - It is recommended to upgrade cocoapods to 1.12.0 or higher. If your cocoapods version is lower than 1.12.0, you may encounter the following errors
  ```
  the version of cocoapods to generate the lockfile(1.12.0) is higher than the version of the current executable(1.11.2). 
  ```
  ```
  can't modify frozen string: "[Xcodeproject] unknown object version (56).
  ```
  Please open [AUIKaraoke.xcodeproj](AUIKaraoke.xcodeproj) and modify it to "Xcode 13.0 compatible" according to the following image
  ![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/1691738494762.jpg)

- Finally, open AUIKaraoke. xcworkspace and run it to start your experience
  - If your cocoapods version is lower than 1.12.0, you will encounter the following error. Please manually set the signature in 'Team'
  ![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/1691739881708.jpg)


## FAQ

- [FAQ](../doc/KaraokeFAQ.md)

- If you have any other questions, please feedback to the [developer community](https://www.rtcdeveloper.cn/cn/community/discussion/0)


## License

Copyright © Agora Corporation. All rights reserved.
Licensed under the [MIT license](LICENSE).
