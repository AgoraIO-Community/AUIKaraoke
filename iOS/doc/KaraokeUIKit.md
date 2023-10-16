# KaraokeUIKit

*English | [中文](KaraokeUIKit_zh.md)*

KaraokeUIKit is a Karaoke scenario component that provides room management and the ability to pull up room pages. Developers can use this component to quickly build a Karaoke application。


## Quick Started

### 1. Add Source Code

**Copy the following source code into your own project：**

- [AScenesKit](../AScenesKit)
- [KaraokeUIKit.swift](../Example/AUIKaraoke/KaraokeUIKit.swift)
- [KeyCenter.swift](../Example/AUIKaraoke/KeyCenter.swift)

**Add dependencies on AScenesKit in the Podfile file (for example, when AScenesKit are placed in the same level directory as the Podfile)**

```
  pod 'AScenesKit', :path => './AScenesKit'
  pod 'AUIKitCore'
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
//creating room containers
let karaokeView = AUIKaraokeRoomView(frame: self.view.bounds)
karaokeView.onClickOffButton = { [weak self] in
    //exit room callback
}
self.view.addSubview(karaokeView)

//enter room
KaraokeUIKit.shared.launchRoom(roomInfo: self.roomInfo!,
                               karaokeView: karaokeView) {[weak self] error in
    guard let self = self else {return}
    if let _ = error { return }
    //subscription room destroyed callback
    KaraokeUIKit.shared.bindRespDelegate(delegate: self)
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
Please refer to [Room Destruction] (# 7.1-Room-Destruction)


### 7. Exception handling
#### 7.1 Room destruction
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

# API reference
## setup
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
| config      | AUICommonConfig | General configuration, including user information and domain name, etc.                             |
| ktvApi      | KTVApi          | (Optional) Agora KTV scene API instance. When the KTVApi has been integrated in the project, it can be imported, otherwise the empty transmission will be created internally. |
| rtcEngine | AgoraRtcEngineKit     | (Optional) Agora RTC engine. When Agora RTC has been integrated in the project, it can be passed in, otherwise it will be automatically created internally. |
| rtmClient   | AgoraRtmClientKit       | (Optional) Agora RTM engine. When Agora RTM has been integrated in the project, it can be passed in, otherwise it will be automatically created internally.|

## createRoom
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
| lastCreateTime | Int64     | (Optional) The page start time, difference from 1970-01-01:00:00:00, in milliseconds, For example: 1681879844085                      |
| pageSize  | Int      | The page size                                 |
| callback   | Closure | Completion callback|

### launchRoom
Launch Room
```swift
func launchRoom(roomInfo: AUIRoomInfo,
                karaokeView: AUIKaraokeRoomView,
                completion: @escaping (NSError?)->()) 
```

The parameters are shown in the table below:

| parameter   | type            | meaning     |
| ----------- | --------------- | ------------------------------------- |
| roomInfo    | AUIRoomInfo     | Room information                     |
| karaokeView | KaraokeRoomView | Room UI View                    |
| completion | Closure | Join the room to complete the callback                          |

### destroyRoom
Destroy Room

```swift
func destoryRoom(roomId: String)
```

The parameters are shown in the table below:

| parameter   | type            | meaning     |
| ------ | ------ | -------------- |
| roomId | String | The ID of the room to destroy |



### renew

Update room token

```swift
func renew(config: AUIRoomConfig)
```

The parameters are shown in the table below:

| parameter   | type            | meaning     |
| ------ | ------ | -------------- |
| config | AUIRoomConfig | Configuration related to the room, including subchannel names and tokens |

### subscribeError

Abnormal callback of subscription rooms, such as token expiration, can be updated through the renew method

```swift
func subscribeError(delegate: AUIRtmErrorProxyDelegate)
```

The parameters are shown in the table below:

| parameter   | type            | meaning     |
| ------ | ------ | -------------- |
| delegate | AUIRtmErrorProxyDelegate | Error callback object |

### unsubscribeError

Exception callback for unsubscribing to a room

```swift
func unsubscribeError(delegate: AUIRtmErrorProxyDelegate)
```

The parameters are shown in the table below:

| parameter   | type            | meaning     |
| ------ | ------ | -------------- |
| delegate | AUIRtmErrorProxyDelegate | Error callback object |

### bindRespDelegate

Bind the response of the room, such as the room being destroyed, the user being kicked out, the room's information being updated, etc

```swift
func bindRespDelegate(delegate: AUIRoomManagerRespDelegate)
```

The parameters are shown in the table below:

| parameter   | type            | meaning     |
| ------ | ------ | -------------- |
| delegate | AUIRoomManagerRespDelegate | Response callback object |

### unbindRespDelegate

Response to unbinding the room

```swift
func unbindRespDelegate(delegate: AUIRoomManagerRespDelegate)
```

The parameters are shown in the table below:

| parameter   | type            | meaning     |
| ------ | ------ | -------------- |
| delegate | AUIRoomManagerRespDelegate | Response callback object |


## Data Model

### AUICommonConfig

| parameter   | type            | meaning     |
| ---------- | ------- | -------------------- |
| host       | String  | Backend service domain name     |
| userId     | String  | User ID              |
| userName   | String  | User name            |
| userAvatar | String  | User avatar url      |

### AUICreateRoomInfo
| parameter   | type            | meaning     |
| ---------- | ------- | -------------------- |
| roomName      | String  | Room name           |
| thumbnail       | String  | Thumbnails on Room List          |
| micSeatCount     | UInt  | Number of mic seat, default to 8             |
| micSeatStyle   | UInt  | mic seat style, default to 3, reserved attributes, currently KTV does not implement special wheat slot layout               |
| password | String?  | [Optional] Room password            |

### AUIRoomInfo
| parameter   | type            | meaning     |
| ----------- | -------------------- | ------------ |
| roomName      | String  | Room name           |
| thumbnail       | String  | Thumbnails on Room List          |
| micSeatCount     | UInt  | Number of mic seat, default to 8             |
| micSeatStyle   | UInt  | mic seat style, default to 3, reserved attributes, currently KTV does not implement special wheat slot layout               |
| password | String?  | [Optional] Room password            |
| roomId      | String               | Room id       |
| roomOwner   | AUIUserThumbnailInfo | Room information   |
| memberCount | Int                  | Online user count  |
| createTime  | Int64                | Room create time, difference from 1970-01-01:00:00:00, in milliseconds, For example: 1681879844085 |

### AUIUserThumbnailInfo

| parameter   | type            | meaning     |
| ---------- | ------ | -------- |
| userId     | String | Room id   |
| userName   | String | User name   |
| userAvatar | String | User avatar url |


### AUIRoomManagerRespDelegate
```AUIRoomManagerRespDelegate``` protocol is used to handle various response events related to room operations. It provides the following methods that can be implemented by classes following this protocol to respond to specific events.

#### Method
  - ```func onRoomDestroy(roomId: String)```
    The callback method called when the room is destroyed.
    - Parameter:
      - ```roomId```: Room ID.
    >
  - ```func onRoomInfoChange(roomId: String, roomInfo: AUIRoomInfo)```
    The callback method called when room information changes.
    - Parameter:
      - ```roomId```: Room ID.
      - ```roomInfo```: Room information.
    >
  - ```func onRoomAnnouncementChange(roomId: String, announcement: String)```
    The method called when a room announcement changes.
    - Parameter:
      - ```roomId```: Room ID.
      - ```announcement```: Announcement of changes.
    >
- ```func onRoomUserBeKicked(roomId: String, userId: String)```
    The method called when a room user is kicked out of the room.
    - Parameter:
      - ```roomId```: Room ID.
      - ```userId```: User ID.

## License
Copyright © Agora Corporation. All rights reserved.
Licensed under the [MIT license](../LICENSE).
