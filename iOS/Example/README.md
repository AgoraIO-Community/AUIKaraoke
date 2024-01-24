# AUIKaraoke iOS 示例工程快速跑通

本文档主要介绍如何快速跑通 AUIKaraoke 示例工程，体验在线 K 歌场景，包括麦位管理、用户管理、歌曲管理、歌曲播放等，更详细的介绍，请参考[AUIScenesKit](../AScenesKit/README.md)和[AUIKit](https://github.com/AgoraIO-Community/AUIKit/blob/main/iOS/README.md)

## 架构图
![](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/uikit_structure_chart.png)


## 目录结构
```
┌─ Example                     // Demo代码集成目录
│  └─ AUIKaraoke               // 主要提供 Karaoke 的集成页面
├─ AUiScenesKit                // 场景业务组装模块，目前只包含Karaoke
│  ├─ AUIKaraokeRoomView       // Karaoke房间容器View，用于拼接各个基础组件以及基础组件与Service的绑定
│  ├─ AUIKaraokeRoomService    // Karaoke房间Service，用于创建各个基础Service以及RTC/RTM/KTVAPi等的初始化
│	 └─ Binder                   // 把UI Components和Service关联起来的业务绑定模块
└─ AUIKit                      // 包含基础组件和基础服务
   ├─ Service                  // 相关基础组件服务类，包括麦位、点歌器、用户管理、合唱等
   ├─ UI Widgets               // 基础UI组件，支持通过配置文件进行一键换肤
   └─ UI Components            // 相关基础业务UI模块，包括麦位、点歌、歌曲播放等，这些UI模块不包含任何业务逻辑，是纯UI模块
```

## 环境准备

- Xcode 13.0及以上版本
- 最低支持系统：iOS 13.0
- 请确保您的项目已设置有效的开发者签名

## 运行示例

## 运行示例

### 1. 获取声网AppID和证书

- 登录[声网控制台](https://console.shengwang.cn/)，如果没有账号则注册一个

- 创建项目

  <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/createaccount/uikit_agora_01.png" width="800" />

  <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/createaccount/uikit_agora_02.png" width="800" />

- 复制AppID和证书，并保存下来

  <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/createaccount/uikit_agora_03.png" width="800" />

- 开启RTM
  
  <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/createaccount/uikit_agora_04.png" width="800" />
  

  <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/createaccount/uikit_agora_05.png" width="800" />

### 2. 获取环信IM AppKey、ClientID和ClientSecret

- 登录[环信即时通讯控制台](https://console.easemob.com/)，如果没有账号则创建一个

- 点击添加并创建应用

  <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/createaccount/uikit_easemob_01.png" width="800" />

- 点击查看进入应用详情页，复制AppKey、ClientID和ClientSecret，并保存下来

  <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/createaccount/uikit_easemob_02.png" width="800" />

### 3. （可选）一键部署Karaoke后端服务

- [部署Karaoke后端服务](../../backend/README_zh.md) 
- 获取后端服务请求域名，并保存下来

### 4. 配置并运行项目
- 克隆或者直接下载项目源码
- 在项目的[KeyCenter.swift](AUIKaraoke/KeyCenter.swift) 中填入步骤1获取的声网证书、步骤2获取的环信证书，以及步骤3部署的HostUrl
```
static var AppId: String = <#Your AppId#>
static var AppCertificate: String = ""
static var IMAppKey: String = ""
static var IMClientId: String = ""
static var IMClientSecret: String = ""
static var HostUrl: String = <#Your HostUrl#>
```
> 如果暂无意部署后端服务，可以使用[KeyCenter.swift](AUIKaraoke/KeyCenter.swift)里默认的域名
> 
>如果想尝试运行项目，可以使用[KeyCenter.swift](AUIKaraoke/KeyCenter.swift)里默认的AppId，剩余配置保持`""`即可

- 打开终端，进入到[Podfile](Podfile)目录下，执行`pod update`命令
  - 建议cocoapods升级到1.12.0以上，如果您的cocoapods版本低于1.12.0，可能会遇到如下错误
  ```
  the version of cocoapods to generate the lockfile(1.12.0) is higher than the version of the current executable(1.11.2). 
  ```
  ```
  can't modify frozen string: "[Xcodeproject] unknown object version (56).
  ```
  请打开[AUIKaraoke.xcodeproj](AUIKaraoke.xcodeproj)并按照下图修改为"Xcode 13.0-compatible"
  ![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/1691738494762.jpg)
  
- 最后打开AUIKaraoke.xcworkspace，运行即可开始您的体验
  - 如果您的cocoapods版本低于1.12.0，会遇到如下错误，请在"Team"里手动设置签名
  ![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/1691739881708.jpg)

## 快速集成
请查看[KaraokeUIKit](../doc/KaraokeUIKit.md)

## 常见问题

- [常见问题](../doc/KaraokeFAQ.md)
- 如有其他问题请反馈至 [开发者社区](https://www.rtcdeveloper.cn/cn/community/discussion/0)

## 许可证

版权所有 Agora, Inc. 保留所有权利。 使用 [MIT 许可证](https://bitbucket.agoralab.co/projects/ADUC/repos/uikit/browse/Android/LICENSE?at=refs%2Fheads%2Fdev%2Fandroid%2Ftheme)

