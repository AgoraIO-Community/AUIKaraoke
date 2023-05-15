# AUIKitKaraoke-iOS Quick Start

*English | [中文](README_zh.md)

This document mainly introduces how to quickly run through the AUIKitKaraoke example  and experience online karaoke scenarios, including micseat service, user service, music service, song player service, etc. For a more detailed introduction, please refer to [AUiScenesKit](../../AScenesKit/README.md) and [AUiKit](../../../backend/README.md)

## Architecture
![](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/uikit_structure_chart.png)

## Directory
```
┌─ Example                     // Demo Code Integration Catalog
│  └─ AUIKitKaraoke            // Provide Karaoke's integrated page
├─ AUiScenesKit                // Scenario business assembly module, currently only including Karaoke
│  ├─ AUiKaraokeRoomView       // Karaoke room container view, used to splice various basic components and bind them to services
│  └─ AUiKaraokeRoomService    // Karaoke Room Service, used to create various basic services and initialize RTC/RTM/KTVAPi, etc
└─ AUiKit                      // Including basic components and services
   ├─ Service                  // Related basic component services, including micseat, jukebox, user, choir, etc
   ├─ UI Widgets               // Basic UI component, supporting one click skin changing through configuration files
   ├─ UI Components            // Related basic business UI modules, including micseat, jukebox, song playback, etc. These UI modules do not contain any business logic and are pure UI modules
   └─ Binder                   // Business binding module that associates UI Components with Service
```

## Requirements

- Xcode 13.0 and later

- Minimum OS version: iOS 13.0

- Please ensure that your project has a valid developer signature set up


## Getting Started

### 1. Deployment backend services

[How to deploy Karaoke backend services](https://bitbucket.agoralab.co/projects/ADUC/repos/uikit-backend/browse/README_zh.md?at=refs%2Fheads%2Fdevelop)  

### 2. Build
- Clone or download  source code

- Fill in the HostUrl for step 1 in the [KeyCenter. swift] (AUIKitKaraoke/KeyCenter. swift) of the project
```
static var HostUrl: String = <#Your HostUrl#>
```
If you do not intend to deploy backend services temporarily, you can use the default domain name in KeyCenter.swift

- Download the [ latest version of the RTC SDK containing RTM 2.0](https://download.agora.io/sdk/release/Agora_Native_SDK_for_iOS_hyf_63842_FULL_20230428_1607_263060.zip), extract it, and then copy the files from libs to [libs](libs)
![](https://download.agora.io/null/3.jpg)

- Open the terminal and enter the [Podfile](Podfile) directory, run `pod install`

- Finally, open AUIKitKaraoke. xcworkspace and run it to start your experience


## FAQ

- [FAQ](../../doc/KaraokeFAQ.md)

- If you have any other questions, please feedback to the [developer community](https://www.rtcdeveloper.cn/cn/community/discussion/0)


## License

Copyright © Agora Corporation. All rights reserved.
Licensed under the [MIT license](LICENSE).
