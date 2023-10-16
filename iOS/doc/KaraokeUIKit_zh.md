# KaraokeUIKit

*[English](KaraokeUIKit.md) | 中文*

KaraokeUIKit是一个Karaoke场景组件，提供房间管理以及拉起房间页面的功能，开发者可以凭借该组件快速搭起一个Karaoke应用。


## 快速集成

### 1. 添加源码

**将以下源码复制到自己项目里：**

- [AScenesKit](../AScenesKit)
- [KaraokeUIKit.swift](../Example/AUIKaraoke/KaraokeUIKit.swift)
- [KeyCenter.swift](../Example/AUIKaraoke/KeyCenter.swift)

**在Podfile文件里添加依赖AScenesKit(例如AScenesKit放置在Podfile同一级目录下时)**

```
  pod 'AScenesKit', :path => './AScenesKit'
  pod 'AUIKitCore'
```

**把KaraokeUIKit.swift拖进工程里**

![](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_keycenter_ios.png) 

**在Info.plist里配置麦克风和摄像头权限**

![](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_app_privacy_ios.png)


### 2. 初始化KaraokeUIKit
```swift
//设置基础信息到KaraokeUIKit里
let commonConfig = AUICommonConfig()
commonConfig.host = KeyCenter.HostUrl
commonConfig.userId = userInfo.userId
commonConfig.userName = userInfo.userName
commonConfig.userAvatar = userInfo.userAvatar
KaraokeUIKit.shared.setup(roomConfig: commonConfig,
                          ktvApi: nil,      //如果有外部初始化的ktv api
                          rtcEngine: nil,   //如果有外部初始化的rtc engine
                          rtmClient: nil)   //如果有外部初始化的rtm client
```

### 3. 获取房间列表
```swift
KaraokeUIKit.shared.getRoomInfoList(lastCreateTime: nil, 
                                    pageSize: kListCountPerPage, 
                                    callback: { error, list in
    //更新UI
})
```

### 4. 创建房间
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

### 5. 拉起房间
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

### 6. 退出房间
#### 6.1 主动退出
```swift
//AUIKaraokeRoomView提供了onClickOffButton点击返回的clousure
karaokeView.onClickOffButton = { [weak self] in
    self.navigationController?.popViewController(animated: true)
    KaraokeUIKit.shared.destoryRoom(roomId: self.roomInfo?.roomId ?? "") 
}
```

#### 6.2 房间销毁被动退出
详见[房间销毁](#7.1%20房间销毁)


### 7. 异常处理
#### 7.1 房间销毁
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

### 8 换肤
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
AUIRoomContext.shared.switchTheme(themeName: "UIKit")
```
- 也可通过修改[配置文件](../AUIKit/AUIKit/Resource/auiTheme.bundle/UIKit/theme)或者替换[资源文件](../AUIKit/AUIKit/Resource/auiTheme.bundle/UIKit/resource)来更换组件的皮肤
- 更多换肤问题可以参考[皮肤设置](./KaraokeTheme_zh.md)

# API参考
## setup
初始化
```swift
func setup(roomConfig: AUICommonConfig,
           ktvApi: KTVApiDelegate? = nil,
           rtcEngine: AgoraRtcEngineKit? = nil,
           rtmClient: AgoraRtmClientKit? = nil) 
```

| 参数        | 类型            | 含义                                                         |
| ----------- | --------------- | ------------------------------------------------------------ |
| config      | AUICommonConfig | 通用配置，包含用户信息和appId等                              |
| ktvApi      | KTVApi          | （可选）声网KTV场景化API实例。当项目里已经集成了KTV场景化可以传入，否则传空由内部自行创建。 |
| rtcEngineEx | AgoraRtcEngineKit     | （可选）声网RTC引擎。当项目里已集成Agora RTC可以传入，否则传空由内部自动创建。 |
| rtmClient   | AgoraRtmClientKit       | （可选）声网RTM引擎。当项目里已集成Agora RTM可以传入，否则传空由内部自动创建。 |

## createRoom
创建房间

```swift
func createRoom(roomInfo: AUICreateRoomInfo,
                success: ((AUIRoomInfo?)->())?,
                failure: ((Error)->())?)
```


参数如下表所示：

| 参数           | 类型              | 含义                             |
| -------------- | ----------------- | -------------------------------- |
| roomInfo | AUICreateRoomInfo | 创建房间所需的信息               |
| success        | Closure          | 成功回调，成功会返回一个房间信息 |
| failure        | Closure          | 失败回调                         |



### getRoomInfoList

获取房间列表

```swift
func getRoomInfoList(lastCreateTime: Int64?, 
                     pageSize: Int, 
                     callback: @escaping AUIRoomListCallback)
```

参数如下表所示：

| 参数      | 类型     | 含义                                 |
| --------- | -------- | ------------------------------------ |
| lastCreateTime | Int64?     | [可选]起始时间，与1970-01-01:00:00:00的差值，单位：毫秒，例如:1681879844085                    |
| pageSize  | Int      | 页数                                 |
| callback   | Closure | 完成回调 |

### launchRoom

```swift
func launchRoom(roomInfo: AUIRoomInfo,
                karaokeView: AUIKaraokeRoomView,
                completion: @escaping (NSError?)->()) 
```

参数如下表所示：

| 参数        | 类型            | 含义                                  |
| ----------- | --------------- | ------------------------------------- |
| roomInfo    | AUIRoomInfo     | 房间信息                              |
| karaokeView | KaraokeRoomView | 房间UI View                           |
| completion | Closure | 加入房间完成回调                           |

### destroyRoom

销毁房间

```swift
func destoryRoom(roomId: String)
```

### bindRespDelegate

绑定房间响应，比如房间被销毁、用户被踢出、房间的信息更新等

```swift
func bindRespDelegate(delegate: AUIRoomManagerRespDelegate)
```

参数如下表所示：

| 参数   | 类型   | 含义           |
| ------ | ------ | -------------- |
| delegate | AUIRoomManagerRespDelegate | 响应回调对象 |

### unbindRespDelegate

解除绑定房间的响应

```swift
func unbindRespDelegate(delegate: AUIRoomManagerRespDelegate)
```

参数如下表所示：

| 参数   | 类型   | 含义           |
| ------ | ------ | -------------- |
| delegate | AUIRoomManagerRespDelegate | 响应回调对象 |


## 数据模型

### AUICommonConfig

| 参数       | 类型    | 含义                 |
| ---------- | ------- | -------------------- |
| host       | String  | 后端服务域名          |
| userId     | String  | 用户ID               |
| userName   | String  | 用户名               |
| userAvatar | String  | 用户头像             |

### AUICreateRoomInfo
| 参数       | 类型    | 含义                 |
| ---------- | ------- | -------------------- |
| roomName      | String  | 房间名称            |
| thumbnail       | String  | 房间列表上的缩略图          |
| micSeatCount     | UInt  | 麦位个数，默认为8              |
| micSeatStyle   | UInt  | 麦位样式，默认为3， 预留属性，目前K歌未实现特殊麦位布局               |
| password | String?  | [可选]房间密码            |

### AUIRoomInfo

| 参数        | 类型                 | 含义         |
| ----------- | -------------------- | ------------ |
| roomName      | String  | 房间名称            |
| thumbnail       | String  | 房间列表上的缩略图          |
| micSeatCount     | UInt  | 麦位个数，默认为8              |
| micSeatStyle   | UInt  | 麦位样式，默认为3， 预留属性，目前K歌未实现特殊麦位布局               |
| password | String?  | [可选]房间密码            |
| roomId      | String               | 房间id       |
| roomOwner   | AUIUserThumbnailInfo | 房主信息     |
| memberCount | Int                  | 房间人数     |
| createTime  | Int64                 | 房间创建时间，与1970-01-01:00:00:00的差值，单位：毫秒，例如:1681879844085 |

### AUIUserThumbnailInfo

| 参数       | 类型   | 含义     |
| ---------- | ------ | -------- |
| userId     | String | 用户Id   |
| userName   | String | 用户名   |
| userAvatar | String | 用户头像 |

### AUIRoomManagerRespDelegate
```AUIRoomManagerRespDelegate``` 协议用于处理与房间操作相关的各种响应事件。它提供了以下方法，可以由遵循此协议的类来实现，以响应特定的事件。

#### 方法
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
  - ```func onRoomAnnouncementChange(roomId: String, announcement: String)```
    房间公告发生变更时调用的方法。
    - 参数：
      - ```roomId```: 房间ID。
      - ```announcement```: 公告变更内容。
    >
- ```func onRoomUserBeKicked(roomId: String, userId: String)```
    房间用户被踢出房间时调用的方法。
    - 参数：
      - ```roomId```: 房间ID。
      - ```userId```: 用户ID。

## 许可证
版权所有 Agora, Inc. 保留所有权利。
使用 [MIT 许可证](../LICENSE)
