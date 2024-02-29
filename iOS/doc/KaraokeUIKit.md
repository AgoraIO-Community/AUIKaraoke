# KaraokeUIKit

KaraokeUIKit是一个Karaoke场景组件，提供房间管理以及拉起房间页面的功能，开发者可以凭借该组件快速搭起一个Karaoke应用。


## 快速集成

### 1. 准备后台环境
KaraokeUIKit依赖后台服务做以下功能：
- 房间管理
- Rtc/Rtm Token生成
- 环信IM聊天房创建

后台服务首先需要获取一个后台服务域名，其获取方式有以下两种：
- 直接使用声网提供的测试域名：https://service.shengwang.cn/uikit
> 测试域名仅供测试使用，不能商用！
- 自己部署后台代码，详见[部署教程](https://github.com/AgoraIO-Community/AUIKit/tree/main/backend)

### 2. 添加源码

**将以下源码复制到自己项目里：**

- [AScenesKit](../AScenesKit)
- [KeyCenter.swift](../Example/AUIKaraoke/KeyCenter.swift)
- [KaraokeUIKit.swift](../Example/AUIKaraoke/KaraokeUIKit.swift)

**在Podfile文件里添加依赖AScenesKit(例如AScenesKit放置在Podfile同一级目录下时)**

```
  pod 'AScenesKit', :path => './AScenesKit'
```

**把 KeyCenter.swift 和 KaraokeUIKit.swift 拖进工程里**

![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/karaoke/add_keycenter_to_karaoke.jpg) 

**在Info.plist里配置麦克风和摄像头权限**

![](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_app_privacy_ios.png)


### 3. 初始化KaraokeUIKit
```swift
//为KaraokeUIKit设置基本信息
let commonConfig = AUICommonConfig()
commonConfig.appId = KeyCenter.AppId
commonConfig.appCert = KeyCenter.AppCertificate
commonConfig.basicAuth = KeyCenter.AppBasicAuth
commonConfig.imAppKey = KeyCenter.IMAppKey
commonConfig.imClientId = KeyCenter.IMClientId
commonConfig.imClientSecret = KeyCenter.IMClientSecret
commonConfig.host = KeyCenter.HostUrl
let ownerInfo = AUIUserThumbnailInfo()
ownerInfo.userId = userInfo.userId
ownerInfo.userName = userInfo.userName
ownerInfo.userAvatar = userInfo.userAvatar
commonConfig.owner = ownerInfo
KaraokeUIKit.shared.setup(commonConfig: commonConfig,
                          apiConfig: nil)
```

### 4. 获取房间列表
```swift
KaraokeUIKit.shared.getRoomInfoList(lastCreateTime: nil, 
                                    pageSize: 20, 
                                    callback: { error, list in
    //更新UI
})
```

### 5. 创建房间
```swift
let room = AUICreateRoomInfo()
room.roomName = text
room.thumbnail = self.userInfo.userAvatar
KaraokeUIKit.shared.createRoom(roomInfo: room) { roomInfo in
    let vc = RoomViewController()
    vc.roomInfo = roomInfo
    self.navigationController?.pushViewController(vc, animated: true)
} failure: { error in
    //错误提示
}
```

### 6. 拉起房间
```swift
//创建房间容器
let karaokeView = AUIKaraokeRoomView(frame: self.view.bounds)
karaokeView.onClickOffButton = { [weak self] in
    //退出房间的回调
}
self.view.addSubview(karaokeView)

//进入房间
KaraokeUIKit.shared.launchRoom(roomInfo: self.roomInfo!,
                               karaokeView: karaokeView) {[weak self] error in
    guard let self = self else {return}
    if let _ = error { return }
    //订阅房间被销毁回调
    KaraokeUIKit.shared.bindRespDelegate(delegate: self)
}
```

### 7. 退出房间
#### 7.1 主动退出
```swift
//AUIKaraokeRoomView提供了onClickOffButton点击返回的clousure
karaokeView.onClickOffButton = { [weak self] in
    self.navigationController?.popViewController(animated: true)
    KaraokeUIKit.shared.destoryRoom(roomId: self.roomInfo?.roomId ?? "") 
}
```

#### 7.2 房间销毁被动退出
详见[房间销毁](#81-房间销毁)


### 8. 异常处理
#### 8.1 房间销毁
```swift
//在KaraokeUIKit.shared.launchRoom之后订阅AUIRoomManagerRespDelegate的回调
KaraokeUIKit.shared.bindRespDelegate(delegate: self)

//在退出房间时取消订阅
KaraokeUIKit.shared.unbindRespDelegate(delegate: self)

//然后通过AUIRoomManagerRespDelegate回调方法中的onRoomDestroy来处理房间销毁
func onRoomDestroy(roomId: String) {
    //处理房间被销毁
}
```

### 9 换肤
- AUIKit支持一键换肤，您可以通过下列方法设置皮肤
```swift
//重置成默认主题
AUIRoomContext.shared.resetTheme()
```
```swift
//切换到下一个主题
AUIRoomContext.shared.switchThemeToNext()
```

```swift
//指定一个主题
AUIRoomContext.shared.switchTheme(themeName: "Light")
```
- 也可通过修改[配置文件](../AUIKit/AUIKit/Resource/auiTheme.bundle/UIKit/theme)或者替换[资源文件](../AUIKit/AUIKit/Resource/auiTheme.bundle/UIKit/resource)来更换组件的皮肤
- 更多换肤问题可以参考[皮肤设置](./KaraokeTheme.md)

# API参考
## setup
初始化
```swift
func setup(commonConfig: AUICommonConfig,
           apiConfig: AUIAPIConfig? = nil)
```

| 参数        | 类型            | 含义                                                         |
| ----------- | --------------- | ------------------------------------------------------------ |
| commonConfig      | AUICommonConfig | 通用配置，包含用户信息和域名等                              |
| apiConfig | AUIAPIConfig     | （可选）声网相关SDK的引擎设置，为nil表示内部隐式创建该实例。 |

## createRoom
创建房间

```swift
func createRoom(roomInfo: AUIRoomInfo,
                roomConfig: AUIRoomConfig,
                karaokeView: AUIKaraokeRoomView,
                completion: @escaping (NSError?) -> Void)
```


参数如下表所示：

| 参数           | 类型              | 含义                             |
| -------------- | ----------------- | -------------------------------- |
| roomInfo | AUICreateRoomInfo | 创建房间所需的信息               |
| roomConfig        | AUIRoomConfig          | 房间token配置 |
| karaokeView        | AUIKaraokeRoomView          | K歌房容器View                    |
| completion        | Closure          | 完成回调                         |





### getRoomInfoList

获取房间列表

```swift
func getRoomInfoList(lastCreateTime: Int64, 
                     pageSize: Int, 
                     callback: @escaping AUIRoomListCallback)
```

参数如下表所示：

| 参数      | 类型     | 含义                                 |
| --------- | -------- | ------------------------------------ |
| lastCreateTime | Int64     | 起始时间，与1970-01-01:00:00:00的差值，单位：毫秒，例如:1681879844085，为0则使用服务器当前时间                    |
| pageSize  | Int      | 页数                                 |
| callback   | Closure | 完成回调 |

### enterRoom

```swift
func enterRoom(roomId: String,
               roomConfig: AUIRoomConfig,
               karaokeView: AUIKaraokeRoomView,
               completion: @escaping (AUIRoomInfo?, NSError?) -> Void) 
```

参数如下表所示：

| 参数        | 类型            | 含义                                  |
| ----------- | --------------- | ------------------------------------- |
| roomId    | String     | 房间id                              |
| roomConfig        | AUIRoomConfig          | 房间token配置 |
| karaokeView | AUIKaraokeRoomView | K歌房容器View                          |
| completion | Closure | 加入房间完成回调                          |

### leaveRoom

离开房间

```swift
func leaveRoom(roomId: String)
```

参数如下表所示：

| 参数   | 类型   | 含义           |
| ------ | ------ | -------------- |
| roomId | String | 要离开的房间ID |



## 数据模型

### AUICommonConfig

| 参数       | 类型    | 含义                 |
| ---------- | ------- | -------------------- |
| host       | String  | 后端服务域名          |
| appId     | String  | 声网AppId               |
| appCert   | String  | (可选)声网App证书，没有使用后端token生成服务可不填               |
| basicAuth   | String  | (可选)声网basicAuth，没有用到后端踢人服务可以不设置             |
| imAppKey   | String  | (可选)环信AppKey，没有用到后端IM服务可以不设置              |
| imClientId   | String  | (可选)环信ClientId，没有用到后端IM服务可以不设置              |
| imClientSecret   | String  | (可选)环信ClientSecret，没有用到后端IM服务可以不设置               |
| owner | AUIUserThumbnailInfo  | 用户信息             |

### AUIAPIConfig
| 参数       | 类型    | 含义                 |
| ---------- | ------- | -------------------- |
| rtcEngine       | AgoraRtcEngineKit  | (可选)rtc实例对象,为nil内部在使用到时会自行创建         |
| rtmClient       | AgoraRtmClientKit  | (可选)rtm实例对象,为nil内部在使用到时会自行创建          |
| ktvApi       | KTVApiDelegate  | (可选)KTVApi实例，用户KTV场景，如果非K歌场景可以不设置，  为nil内部在使用到时会自行创建        |

### AUIRoomInfo

| 参数        | 类型                 | 含义         |
| ----------- | -------------------- | ------------ |
| roomId      | String               | 房间id       |
| roomName      | String               | 房间名称       |
| owner   | AUIUserThumbnailInfo | 房主信息     |
| micSeatCount | UInt                  | 麦位个数，默认为8     |
| micSeatStyle | UInt                  | 麦位类型     |
| customPayload  | [String: Any]       | 扩展信息 |

### AUIUserThumbnailInfo

| 参数       | 类型   | 含义     |
| ---------- | ------ | -------- |
| userId     | String | 用户Id   |
| userName   | String | 用户名   |
| userAvatar | String | 用户头像 |

### AUIKaraokeRoomServiceRespDelegate
```AUIKaraokeRoomServiceRespDelegate``` 协议用于处理与房间操作相关的各种响应事件。它提供了以下方法，可以由遵循此协议的类来实现，以响应特定的事件。

#### 方法
  - `func onTokenPrivilegeWillExpire(roomId: String?)`
    房间token即将过期的回调方法
    - 参数：
      - ```roomId```: 房间ID。
    >
  - ```func onRoomDestroy(roomId: String)```
    房间被销毁时调用的回调方法。
    - 参数：
      - ```roomId```: 房间ID。
    >
  - ```func onRoomInfoChange(roomId: String, roomInfo: AUIRoomInfo)```
    房间信息发生变更时调用的回调方法。
    - 参数：
      - ```roomId```:房间ID。
      - ```roomInfo```:房间信息。
    >
- ```func onRoomUserBeKicked(roomId: String, userId: String)```
    房间用户被踢出房间时调用的方法。
    - 参数：
      - ```roomId```: 房间ID。
      - ```userId```: 用户ID。

---
## 功能定制化
KaraokeUIKit支持对UI及业务功能做定制化修改，并且由于是依赖AUIKit这个开源组件，不仅能对AScenesKit做基础定制，而且能对AUIKit做深入定制。

代码结构如下图所示，其中可以修改AScenesKit和AUIKit源码来定制功能：

<img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/karaoke/ios/karaoke_architecture_diagram_1.0.0.png" width="800" />


### 1. 基础定制

基础定制主要是修改AScenesKit库实现，下面分别从UI和逻辑介绍如何定制。
另外，房间管理定制对于已有后台房间管理功能的用户来说也至关重要，为此也会介绍下如何修改。

#### 1.1 定制UI
> KaraokeUIKit的UI是基于AUIKit的UI组件进行实现，而AIKit提供了一套UI主题样式，因此KaraokeUIKit UI样式是通过扩展AUIKit组件主题来实现的。
> AUIKit组件的主题样式说明见[AUIKit属性](https://github.com/AgoraIO-Community/AUIKit/blob/main/iOS/README.md#widget)。
>
>
> 另外，KaraokeUIKit提供了两套默认主题，[暗色(Dark)](https://github.com/AgoraIO-Community/AUIKit/tree/main/iOS/AUIKitCore/Resource/auiTheme.bundle/Dark) 和 [亮色(Light)](https://github.com/AgoraIO-Community/AUIKit/tree/main/iOS/AUIKitCore/Resource/auiTheme.bundle/Light)，
>
>
> 下面介绍 `Light` 是如何定制主题的，然后再进阶介绍如何自定义新的主题属性
>
> 下面以麦位背景图为例来介绍如何做定制：
>
> - 定位打开对应的[micSeat.json](https://github.com/AgoraIO-Community/AUIKit/blob/main/iOS/AUIKitCore/Resource/auiTheme.bundle/Dark/theme/micSeat.json)文件
>
>    <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/ios/voicechat_custom_theme_01.png" width="800" />
>
> - 拷贝该麦位主题json文件到项目里，例如拷贝到scenekit的对应主题bundle里
>
>    <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/karaoke/ios/karaoke_custom_theme_02.png" width="800" />
>
> - 修改麦位属性，例如
>
> ~~~json
> "SeatItem": {
>   ...
>   "backgroundColor": "#ff0000",
>   ...
> }
> ~~~
>
> - 配置好运行项目即可看到效果

#### 1.2 定制业务逻辑
  > 在做自定义前，需要知道几点：
  >   1. 组件通过[Binder](../AScenesKit/AScenesKit/Classes/ViewBinder/)将AUIKit提供的UI组件及Service组件绑定起来以实现业务交互
  >   2. [AUIKaraokeRoomService](../AScenesKit/AScenesKit/Classes/Karaoke/RoomContainer/AUIKaraokeRoomService.swift)管理着所有业务service
  >   3. [AUIKaraokeRoomView](../AScenesKit/AScenesKit/Classes/Karaoke/RoomContainer/AUIKaraokeRoomView.swift)作为房间总ui入口，管理所有Binder及AUIKaraokeRoomService
  >
  > 自定义功能核心是修改Binder及AUIKaraokeRoomView。
  >
  > 下面是自定义麦位的参考步骤：
  >
  >- 查看 AUIKaraokeRoomView 找到麦位控件
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/karaoke/ios/karaoke_custom_03.png" width="800" />
  >
  > - 在 AUIKaraokeRoomView 里找到[对应的Binder实现](../AScenesKit/AScenesKit/Classes/ViewBinder/AUIMicSeatViewBinder.swift)。
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/karaoke/ios/karaoke_custom_04.png" width="800" />
  >
  > - 将麦位相关的AUIKit ui组件实例及service组件实例通过与麦位Binder进行绑定
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/karaoke/ios/karaoke_custom_05.png" width="800" />
  >
  > - 在麦位Binder的bind方法里设置service事件监听、获取service数据及初始化ui等初始化操作
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/karaoke/ios/karaoke_custom_06.png" width="800" />


#### 1.3修改房间管理

  > 在后台服务里提供了一个房间管理，这个房间管理在移动端是由[RoomManager](https://github.com/AgoraIO-Community/AUIKit/blob/main/iOS/AUIKitCore/Sources/Service/Impl/AUIRoomManagerImpl.swift)进行管理。
  > RoomManager提供了创建房间、销毁房间、获取房间列表这三个api，但是这仅能满足简单的房间管理需求，如果有更复杂的需求就需要自行开发房间管理服务。或者您已经有自己的房间管理服务，您也可以使用自己的房间管理服务。
  >
   > 下面说明如何修改房间管理（下面的修改方法只做参考，具体项目里修改方式可以根据您现有的房间接口做出最合适的选择）：
  >
  >- 确认后台有独立的三个后台接口：创建房间、销毁房间 以及 获取房间列表。
     >   并且房间信息里必须包含房主的用户信息：用户名、用户ID 和 用户头像。
  >
  > - 实现您的RoomManager，并包含以下三个接口
  >
  > ~~~swift
  > // 创建房间
  > public func createRoom(room: AUIRoomInfo,
  >                        callback: @escaping (NSError?, AUIRoomInfo?) -> ())
  >  
  > // 销毁房间
  > public func destroyRoom(roomId: String,
  >                         callback: @escaping (NSError?) -> ())
  > 
  > // 获取房间列表
  > public func getRoomInfoList(lastCreateTime: Int64,
  >                             pageSize: Int,
  >                             callback: @escaping AUIRoomListCallback)
  > ~~~
  >
  > - 将[KaraokeUIKit](../Example/AUIKaraoke/KaraokeUIKit.swift)中的RoomManager替换成自己的RoomManager
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/karaoke/ios/karaoke_custom_07.png" width="800" />


### 2. 高级定制
高级定制主要是修改AUIKit源码。由于AUKit默认是以CocoaPods方式引入到AScenesKit库里，需要先引入源码。
AUIKit主要提供了UI和Service组件，下面介绍如何做定制。

#### 2.1 引入AUIKit源码
  > 本项目默认使用CocoaPods引入AUIKit库，但是可以在[Podfile](../AUIVoiceRoom/Podfile)里配置AUIKit源码路径。
  > 当AUIKit源码路径存在时，使用Xcode编译时会将源码导到项目里并能直接修改。
  > 配置方法如下：
  >
  > - 克隆或者直接下载AUIKit源码
  > ```
  > git clone https://github.com/AgoraIO-Community/AUIKit.git
  > ```
  > - 在Podfile里配置AUIKit源码路径，该路径可以是相对于Podfile所在目录的相对路径
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/karaoke/ios/karaoke_custom_dependency.png" width="800" />
  >
  > - 执行`pod install`后，即可看到AUIKit源码
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/karaoke/ios/karaoke_custom_02.png" width="800" />
  >
  > 
#### 2.2 定制UI
  > 
  > 如何为一组新UI扩展主题请参考[Karaoke Theme](./KaraokeTheme.md)。
  >
  > 下面以麦位背景色来介绍如何添加新属性，以及如何在代码里获取到主题属性值并调整ui:
  >
  > - 找到麦位的json文件，在里面添加背景色属性
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/ios/voicechat_custom_theme_03.png" width="800" />
  >
  > - 找到麦位自定义View，在麦位view里使用上面定义的背景色属性
  >
  >    <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/ios/voicechat_custom_theme_04.png" width="800" />
  >
  > - 主题属性自定义完成后即可按基础定制的步骤来使用这个新增属性
  > 

#### 2.3 定制业务功能

  > 高级定制业务功能是基于AUIKit提供service进行修改，具体service的说明见[AUIKit-Service文档](https://github.com/AgoraIO-Community/AUIKit/tree/main/iOS#service)


## 许可证
版权所有 Agora, Inc. 保留所有权利。
使用 [MIT 许可证](../LICENSE)
