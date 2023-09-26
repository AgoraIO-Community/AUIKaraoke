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
room.seatCount = 8
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
let uid = KaraokeUIKit.shared.roomConfig?.userId ?? ""
//创建房间容器
let karaokeView = AUIKaraokeRoomView(frame: self.view.bounds)
//通过generateToken方法获取到必须的token和appid
generateToken { roomConfig, appId in
    KaraokeUIKit.shared.launchRoom(roomInfo: self.roomInfo!,
                                   appId: appId,
                                   config: roomConfig,
                                   karaokeView: karaokeView) 
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
详见[房间销毁](#7.2 房间销毁)


### 7. 异常处理
#### 7.1 token过期处理
```swift
//在KaraokeUIKit.shared.launchRoom之后订阅AUIRtmErrorProxyDelegate的回调
KaraokeUIKit.shared.subscribeError(roomId: self.roomInfo?.roomId ?? "", delegate: self)

//在退出房间时取消订阅
KaraokeUIKit.shared.unsubscribeError(roomId: self.roomInfo?.roomId ?? "", delegate: self)

//然后通过AUIRtmErrorProxyDelegate回调方法中的onTokenPrivilegeWillExpire来renew所有的token
@objc func onTokenPrivilegeWillExpire(channelName: String?) {
    generatorToken { config, _ in
        KaraokeUIKit.shared.renew(config: config)
    }
}
```

#### 7.2 房间销毁
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

#API参考
##setup
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

##createRoom
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
| lastCreateTime | Int64     | 起始时间                         |
| pageSize  | Int      | 页数                                 |
| callback   | Closure | 完成回调 |

### launchRoom

```swift
func launchRoom(roomInfo: AUIRoomInfo,
                appId: String? = nil,
                config: AUIRoomConfig,
                karaokeView: AUIKaraokeRoomView) 
```

参数如下表所示：

| 参数        | 类型            | 含义                                  |
| ----------- | --------------- | ------------------------------------- |
| roomInfo    | AUIRoomInfo     | 房间信息                              |
| appId    | String     | (可选)设置当前AppId，如果初始化时未设置，这里必须要设置否则可以忽略                              |
| config      | AUIRoomConfig   | 房间里相关的配置，包含子频道名和token |
| karaokeView | KaraokeRoomView | 房间UI View                           |

### destroyRoom

销毁房间

```swift
func destoryRoom(roomId: String)
```

参数如下表所示：

| 参数   | 类型   | 含义           |
| ------ | ------ | -------------- |
| roomId | String | 要销毁的房间ID |


## 数据模型

### AUICommonConfig

| 参数       | 类型    | 含义                 |
| ---------- | ------- | -------------------- |
| appId      | String  | 声网AppID            |
| host       | String  | 后端服务域名          |
| userId     | String  | 用户ID               |
| userName   | String  | 用户名               |
| userAvatar | String  | 用户头像             |

### AUIRoomInfo

| 参数        | 类型                 | 含义         |
| ----------- | -------------------- | ------------ |
| roomId      | String               | 房间id       |
| roomOwner   | AUIUserThumbnailInfo | 房主信息     |
| memberCount | Int                  | 房间人数     |
| createTime  | Int64                 | 房间创建时间 |

### AUIUserThumbnailInfo

| 参数       | 类型   | 含义     |
| ---------- | ------ | -------- |
| userId     | String | 用户Id   |
| userName   | String | 用户名   |
| userAvatar | String | 用户头像 |

### AUIRoomConfig

| 参数                 | 类型   | 含义                                                         |
| -------------------- | ------ | ------------------------------------------------------------ |
| channelName          | String | 主频道名，一般为roomId                                       |
| rtmToken007             | String | 主频道的rtm token，uid为setup时AUICommonConfig里的userId     |
| rtcToken007             | String | 主频道的rtc token，uid为setup时AUICommonConfig里的userId     |
| rtcChannelName       | String | 音视频频道名，一般为{roomId}_rtc                             |
| rtcRtcToken          | String | 音视频频道的rtc token，uid为setup时AUICommonConfig里的userId |
| rtcRtmToken          | String | 音视频频道的rtm token，uid为setup时AUICommonConfig里的userId |
| rtcChorusChannelName | String | 合唱频道名，一般为{roomId}_rtc_ex                            |
| rtcChorusRtcToken    | String | 合唱频道的rtc token，uid为setup时AUICommonConfig里的userId   |

## 许可证
版权所有 Agora, Inc. 保留所有权利。
使用 [MIT 许可证](../LICENSE)
