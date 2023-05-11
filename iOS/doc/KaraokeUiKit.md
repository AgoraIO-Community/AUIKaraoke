# KaraokeUiKit

*English | [中文](KaraokeUiKit_zh.md)*

KaraokeUiKit is a Karaoke scenario component that provides room management and the ability to pull up room pages. Developers can use this component to quickly build a Karaoke application。


## Quick Started
> Please make sure you have successfully run the project according to this [tutorial](../Example/AUIKitKaraoke/README.md) before integrating.。

### 1. Add Source Code

**Copy the following source code into your own project：**

- [AUiKit](../AUiKit)
- [AScenesKit](../AScenesKit)
- [KaraokeUiKit.swift](../Example/AUIKitKaraoke/AUIKitKaraoke/KaraokeUIKit.swift)
- [KeyCenter.swift](../Example/AUIKitKaraoke/AUIKitKaraoke/KeyCenter.swift)

**Add dependencies on AScenesKit and AUiKit in the Podfile file (for example, when AUiKit and AScenesKit are placed in the same level directory as the Podfile)**

```
  pod 'AScenesKit', :path => './AScenesKit'
  pod 'AUiKit', :path => './AUiKit'
```

**Drag KaraokeUiKit.swift into the project**

![](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_keycenter_ios.png) 

**Configure microphone and camera permissions**

![](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_app_privacy_ios.png)


### 2. Initialize KaraokeUiKit
```swift
//Set basic information to KaraokeUIKit
let commonConfig = AUiCommonConfig()
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
let room = AUiCreateRoomInfo()
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
let karaokeView = AUiKaraokeRoomView(frame: self.view.bounds)
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
//AUiKaraokeRoomView provides a closure for onClickOffButton
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
//Subscribe to the callback for AUiRtmErrorProxyDelegate after KaraokeUIKit.shared.launchRoom
KaraokeUIKit.shared.subscribeError(roomId: self.roomInfo?.roomId ?? "", delegate: self)

//Unsubscribe when exiting the room
KaraokeUIKit.shared.unsubscribeError(roomId: self.roomInfo?.roomId ?? "", delegate: self)

//Then use the onTokenPrivilegeWillExpire callback method in the AUiRtmErrorProxyDelegate callback to renew all tokens
@objc func onTokenPrivilegeWillExpire(channelName: String?) {
    generatorToken { config, _ in
        KaraokeUIKit.shared.renew(config: config)
    }
}
```

#### 7.2 Room destruction
```swift
//Subscribe to the callback for AUiRoomManagerRespDelegate after KaraokeUIKit. shared. launchRoom
KaraokeUIKit.shared.bindRespDelegate(delegate: self)

//Unsubscribe when exiting the room
KaraokeUIKit.shared.unbindRespDelegate(delegate: self)

//Process room destruction through onRoomDestroy in the AUiRoomManagerRespDelegate callback method
func onRoomDestroy(roomId: String) {
    //Processing room was destroyed
}
```

### 8 Skin changing
- AUiKit supports one click skin changing, and you can set the skin using the following methods
```swift
//Reset to default theme
AUiRoomContext.shared.resetTheme()
```
```swift
//Switch to the next theme
AUiRoomContext.shared.switchThemeToNext()
```

```swift
//Specify a theme
AUiRoomContext.shared.switchTheme(themeName: "UIKit")
```
- You can also change the skin of the component by modifying the [configuration file](../AUiKit/AUiKit/Resource/auiTheme.bundle/UIKit/theme) or replacing the [resource file](../AUiKit/AUiKit/Resource/auiTheme.bundle/UIKit/resource)
- For more skin changing issues, please refer to [Skin Settings](./KaraokeTheme.md)

## License
Copyright © Agora Corporation. All rights reserved.
Licensed under the [MIT license](../LICENSE).
