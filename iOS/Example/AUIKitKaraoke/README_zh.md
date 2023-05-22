# AUIKitKaraoke iOS 示例工程快速跑通

*[English](README.md) | 中文*

本文档主要介绍如何快速跑通 AUIKitKaraoke 示例工程，体验在线 K 歌场景，包括麦位管理、用户管理、歌曲管理、歌曲播放等，更详细的介绍，请参考[AUiScenesKit](../../AScenesKit/README_zh.md)和[AUiKit](../../AUiKit/README_zh.md)


## 目录结构
```
┌─ Example                     // Demo代码集成目录
│  └─ AUIKitKaraoke            // 主要提供 Karaoke 的集成页面
├─ AUiScenesKit                // 场景业务组装模块，目前只包含Karaoke
│  ├─ AUiKaraokeRoomView       // Karaoke房间容器View，用于拼接各个基础组件以及基础组件与Service的绑定
│  └─ AUiKaraokeRoomService    // Karaoke房间Service，用于创建各个基础Service以及RTC/RTM/KTVAPi等的初始化
└─ AUiKit                      // 包含基础组件和基础服务
   ├─ Service                  // 相关基础组件服务类，包括麦位、点歌器、用户管理、合唱等
   ├─ UI Widgets               // 基础UI组件，支持通过配置文件进行一键换肤
   ├─ UI Components            // 相关基础业务UI模块，包括麦位、点歌、歌曲播放等，这些UI模块不包含任何业务逻辑，是纯UI模块
   └─ Binder                   // 把UI Components和Service关联起来的业务绑定模块
```

## 环境准备

- Xcode 13.0及以上版本
- 最低支持系统：iOS 13.0
- 请确保您的项目已设置有效的开发者签名

## 运行示例


### 1. 一键部署Karaoke后端服务

[如何部署Karaoke后端服务](../../../backend/README_zh.md)  

### 2. 项目运行
- 克隆或者直接下载项目源码

- 在项目的[KeyCenter.swift](AUIKitKaraoke/KeyCenter.swift) 中填入步骤1的HostUrl
```
static var HostUrl: String = <#Your HostUrl#>
```
如果暂无意部署后端服务，可以使用KeyCenter.swift里默认的域名


- 下载[包含RTM 2.0的RTC SDK最新版本](https://download.agora.io/sdk/release/Agora_Native_SDK_for_iOS_hyf_63842_FULL_20230428_1607_263060.zip) , 解压之后把libs里的文件拷贝到[libs](libs) 里

- 打开终端进入到[Podfile](Podfile)目录下，执行`pod install`命令

- 最后打开AUIKitKaraoke.xcworkspace，运行即可开始您的体验

## 常见问题

- [常见问题](../../doc/KaraokeFAQ_zh.md)
- 如有其他问题请反馈至 [开发者社区](https://www.rtcdeveloper.cn/cn/community/discussion/0)

## 许可证

版权所有 Agora, Inc. 保留所有权利。 使用 [MIT 许可证](https://bitbucket.agoralab.co/projects/ADUC/repos/uikit/browse/Android/LICENSE?at=refs%2Fheads%2Fdev%2Fandroid%2Ftheme)

