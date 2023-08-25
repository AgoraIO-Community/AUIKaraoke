# KaraokeUIKit

*English | [中文](KaraokeUIKit_zh.md)*

KaraokeUIKit is a Karaoke scenario component that provides room management and the ability to pull up room pages. Developers can use this component to quickly build a Karaoke application。


## Quick Started
> Please make sure you have successfully run the project according to this [tutorial](../Example/AUIKitKaraoke/README.md) before integrating。

### 1. Add Source Code

**Copy the following source code into your own project：**

- [AUIKit](https://github.com/AgoraIO-Community/AUIKit/blob/main/iOS/README.md)
- [AScenesKit](../AScenesKit)
- [KaraokeUIKit.swift](../Example/AUIKitKaraoke/KaraokeUIKit.swift)
- [KeyCenter.swift](../Example/AUIKitKaraoke/KeyCenter.swift)

**Add dependencies on AScenesKit and AUIKit in the Podfile file (for example, when AUIKit and AScenesKit are placed in the same level directory as the Podfile)**

```
  pod 'AScenesKit', :path => './AScenesKit'
  pod 'AUIKit', :path => './AUIKit'
```

**Drag KaraokeUIKit.swift into the project**

![](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_keycenter_ios.png) 

**Configure microphone and camera permissions**

![](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_app_privacy_ios.png)


### 2. Initialize KaraokeUIKit
```swift
//Set basic information to KaraokeUIKit
let commonConfig = AUICommonConfig()
commonConfig.host = KeyCenter.HostUrl
commonConfig.userId = userInfo.userId
commonConfig.userName = userInfo.userName
commonConfig.userAvatar = userInfo.userAvatar
KaraokeUIKit.shared.setup(roomConfig: commonConfig,
                          ktvApi: nil,      //If there is an externally initialized KTV API
                          rtcEngine: nil,   //If there is an externally initialized rtc engine
                          rtmClient: nil)   //If there is an externally initialized rtm client
```

### 3. Get room list
```swift
KaraokeUIKit.shared.getRoomInfoList(lastCreateTime: nil, 
                                    pageSize: kListCountPerPage, 
                                    callback: { error, list in
    //Update UI
})
```

### 4. Create room
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
    //error handler
}
```

### 5. Launch room
```swift
let uid = KaraokeUIKit.shared.roomConfig?.userId ?? ""
//Creating Room Containers
let karaokeView = AUIKaraokeRoomView(frame: self.view.bounds)
//Obtain the necessary token and appid through the generateToken method
generateToken { roomConfig, appId in
    KaraokeUIKit.shared.launchRoom(roomInfo: self.roomInfo!,
                                   appId: appId,
                                   config: roomConfig,
                                   karaokeView: karaokeView) {_ in
    }
}
```

### 6. Exit the room
#### 6.1 Proactively exiting
```swift
//AUIKaraokeRoomView provides a closure for onClickOffButton
karaokeView.onClickOffButton = { [weak self] in
    self.navigationController?.popViewController(animated: true)
    KaraokeUIKit.shared.destoryRoom(roomId: self.roomInfo?.roomId ?? "") 
}
```

#### 6.2 Room destruction passive exit
Please refer to [Room Destruction] (# 7.2-Room-Destruction)


### 7. Exception handling
#### 7.1 Token expiration processing
```swift
//Subscribe to the callback for AUIRtmErrorProxyDelegate after KaraokeUIKit.shared.launchRoom
KaraokeUIKit.shared.subscribeError(roomId: self.roomInfo?.roomId ?? "", delegate: self)

//Unsubscribe when exiting the room
KaraokeUIKit.shared.unsubscribeError(roomId: self.roomInfo?.roomId ?? "", delegate: self)

//Then use the onTokenPrivilegeWillExpire callback method in the AUIRtmErrorProxyDelegate callback to renew all tokens
@objc func onTokenPrivilegeWillExpire(channelName: String?) {
    generatorToken { config, _ in
        KaraokeUIKit.shared.renew(config: config)
    }
}
```

#### 7.2 Room destruction
```swift
//Subscribe to the callback for AUIRoomManagerRespDelegate after KaraokeUIKit. shared. launchRoom
KaraokeUIKit.shared.bindRespDelegate(delegate: self)

//Unsubscribe when exiting the room
KaraokeUIKit.shared.unbindRespDelegate(delegate: self)

//Process room destruction through onRoomDestroy in the AUIRoomManagerRespDelegate callback method
func onRoomDestroy(roomId: String) {
    //Processing room was destroyed
}
```

### 8 Skin changing
- AUIKit supports one click skin changing, and you can set the skin using the following methods
```swift
//Reset to default theme
AUIRoomContext.shared.resetTheme()
```
```swift
//Switch to the next theme
AUIRoomContext.shared.switchThemeToNext()
```

```swift
//Specify a theme
AUIRoomContext.shared.switchTheme(themeName: "UIKit")
```

- You can also change the skin of the component by modifying the [configuration file](../AUIKit/AUIKit/Resource/auiTheme.bundle/UIKit/theme) or replacing the [resource file](../AUIKit/AUIKit/Resource/auiTheme.bundle/UIKit/resource)
- For more skin changing issues, please refer to [Skin Settings](./KaraokeTheme.md)

#API reference
##setup
Initialization
```swift
func setup(roomConfig: AUICommonConfig,
           ktvApi: KTVApiDelegate? = nil,
           rtcEngine: AgoraRtcEngineKit? = nil,
           rtmClient: AgoraRtmClientKit? = nil) 
```
The parameters are shown in the table below:
| parameter   | type            | meaning     |
| ----------- | --------------- | ------------------------------------------------------------ |
| config      | AUICommonConfig | General configuration, including user information and appId, etc.                              |
| ktvApi      | KTVApi          | (Optional) Agora KTV scene API instance. When the KTVApi has been integrated in the project, it can be imported, otherwise the empty transmission will be created internally. |
| rtcEngine | AgoraRtcEngineKit     | (Optional) Agora RTC engine. When Agora RTC has been integrated in the project, it can be passed in, otherwise it will be automatically created internally. |
| rtmClient   | AgoraRtmClientKit       | (Optional) Agora RTM engine. When Agora RTM has been integrated in the project, it can be passed in, otherwise it will be automatically created internally.|

##createRoom
Create a Room

```swift
func createRoom(roomInfo: AUICreateRoomInfo,
                success: ((AUIRoomInfo?)->())?,
                failure: ((Error)->())?)
```

The parameters are shown in the table below:
| parameter   | type            | meaning     |
| -------------- | ----------------- | -------------------------------- |
| roomInfo       | AUICreateRoomInfo | Information needed to create a room          |
| success        | Closure          | Success callback, success will return a room information |
| failure        | Closure          | Failure callback                         |



### getRoomInfoList
Get room list

```swift
func getRoomInfoList(lastCreateTime: Int64?, 
                     pageSize: Int, 
                     callback: @escaping AUIRoomListCallback)
```

The parameters are shown in the table below:
| parameter   | type            | meaning     |
| --------- | -------- | ------------------------------------ |
| lastCreateTime | Int64     | The page start time
                         |
| pageSize  | Int      | The page size                                 |
| callback   | Closure | Completion callback|

### launchRoom
Launch Room
```swift
func launchRoom(roomInfo: AUIRoomInfo,
                appId: String? = nil,
                config: AUIRoomConfig,
                karaokeView: AUIKaraokeRoomView) 
```

The parameters are shown in the table below:

| parameter   | type            | meaning     |
| ----------- | --------------- | ------------------------------------- |
| roomInfo    | AUIRoomInfo     | Room information                     |
| appId    | String     | (Optional) Set the current AppId. If it is not set during initialization, it must be set here, otherwise it can be ignored  |
| config      | AUIRoomConfig   | Related configuration in the room, including sub-channel name and token |
| karaokeView | KaraokeRoomView | Room UI View                    |

### destroyRoom
Destroy Room

```swift
func destoryRoom(roomId: String)
```

The parameters are shown in the table below:

| parameter   | type            | meaning     |
| ------ | ------ | -------------- |
| roomId | String | The ID of the room to destroy |


## Data Model

### AUICommonConfig

| parameter   | type            | meaning     |
| ---------- | ------- | -------------------- |
| appId      | String  | Agora AppID          |
| host       | String  | Backend service domain name     |
| userId     | String  | User ID              |
| userName   | String  | User name            |
| userAvatar | String  | User avatar url      |

### AUIRoomInfo
| parameter   | type            | meaning     |
| ----------- | -------------------- | ------------ |
| roomId      | String               | Room id       |
| roomOwner   | AUIUserThumbnailInfo | Room information   |
| memberCount | Int                  | Online user count  |
| createTime  | Int64                | Room create time |

### AUIUserThumbnailInfo

| parameter   | type            | meaning     |
| ---------- | ------ | -------- |
| userId     | String | Room id   |
| userName   | String | User name   |
| userAvatar | String | User avatar url |

### AUIRoomConfig
| parameter   | type            | meaning     |
| -------------------- | ------ | ------------------------------------------------------------ |
| channelName          | String | Main channel name, usually roomId                                       |
| rtmToken007             | String | The rtm token of the main channel whose uid is setup in AUICommonConfig     |
| rtcToken007             | String | The rtc token of the main channel whose uid is setup in AUICommonConfig     |
| rtcChannelName       | String | Video channel name, usually {roomId}_rtc                             |
| rtcRtcToken          | String | The rtc token of the video channel whose uid is setup in AUICommonConfig |
| rtcRtmToken          | String | The rtm token of the video channel whose uid is setup in AUICommonConfig |
| rtcChorusChannelName | String | Chorus channel name, usually {roomId}_rtc_ex                            |
| rtcChorusRtcToken    | String | The rtc token of the chorus channel whose uid is setup in AUICommonConfig
   |


## License
Copyright © Agora Corporation. All rights reserved.
Licensed under the [MIT license](../LICENSE).
