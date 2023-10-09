# AUIKaraoke-Android

*English | [中文](README.zh.md)*

AUIKaraoke is an open source audio and video UI component. By integrating the AUIKitKaraoke component in the project, you only need to write a few lines of code to add online karaoke scenes to your application, experience karaoke, microphone management, send and receive gifts, and text chat Wait for the relevant capabilities of Agora RTC in the KTV scenario.

## Quick Start

### 1. Environment Setup

- <mark>Minimum Compatibility with Android 7.0</mark>（SDK API Level 24）
- Android Studio 3.5 and above versions.
- Mobile devices with Android 7.0 and above.
- JDK 17 and above.

---

### 2. Running the Example
- (Optional)Execute in the AUIKitKaraoke directory:
```
git submodule update --init --remote
```

- Please fill in the domain name of the business server in the [**local.properties**](/local.properties) file of the project
  
  ![PIC](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_serverhost_android.png)

``` 
SERVER_HOST= （Domain name of the business server）
```

> For business server setup, please refer to [backend README](../backend/)
> Agora Test Domain: https://service.agora.io/uikit-karaoke


- Run the project with Android Studio to begin your experience.

## Quick integration
### 1. Add Source Code
**Copy the following source code into your own project:**

- [asceneskit](../asceneskit)

**Configure libraries in settings.gradle**
```groovy
include ':asceneskit'
```

**Configure resource path, viewBinding and dependencies in build.gradle**
```groovy
dependencies {
    implementation project(':asceneskit')
}
```

**Configure permissions and theme in AndroidManifest.xml**
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" /> 
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.CALL_PHONE" />

    <application
        android:theme="@style/Theme.AKaraoke"
        tools:replace="android:theme">
        
        ...

    </application>

</manifest>
```

**Configure server host in local.properties**

![PIC](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_serverhost_android.png)

``` 
SERVER_HOST= （Domain name of the business server）
```

> Agora Test Domain: https://service.agora.io/uikit-karaoke

### 2. Initialize KaraokeUiKit
```kotlin
// Create Common Config
val config = AUiCommonConfig()
config.context = application
config.appId = "Agora APP ID" // Get the Agora APP ID from agora console
config.userId = "User ID"
config.userName = "User Name"
config.userAvatar = "User Avatar"
// Initialize KaraokeUiKit. If you have your own rtmClient, rtcEngine or ktvApi, you can pass them to KaraokeUiKit.
KaraokeUiKit.setup(
    config = config, // must
    rtmClient = null, // option
    rtcEngineEx = null, // option
    ktvApi = null // option
)
```

### 3. Get Karaoke Room List
```kotlin
KaraokeUiKit.getRoomList(lastCreateTime, 10,
    success = { roomList ->
        runOnUiThread {
            // update ui
        }
    },
    failure = { error ->
        runOnUiThread {
            // update ui
        }
    }
)
```

### 4. Create Karaoke Room
```kotlin
val createRoomInfo = AUICreateRoomInfo()
createRoomInfo.roomName = roomName
KaraokeUiKit.createRoom(
    createRoomInfo,
    success = { roomInfo ->
        // create room success
    },
    failure = { error ->
        // create room failure
    }
)
```

### 5. Launch Karaoke Room
```kotlin
// RoomInfo from room list or room creator.
val roomInfo : AUIRoomInfo
// KaraokeRoomView in layout
val karaokeRoomView: KaraokeRoomView

val config = AUiRoomConfig(roomInfo.roomId)
// Generate token with config.channelName and AUIRoomContext.shared().currentUserInfo.userId
config.rtmToken = ""
config.rtcToken = ""
// Generate token with config.rtcChannelName and AUIRoomContext.shared().currentUserInfo.userId
config.rtcRtmToken = ""
config.rtcRtcToken = ""
// Generate token with config.rtcChorusChannelName and AUIRoomContext.shared().currentUserInfo.userId
config.rtcChorusRtcToken = ""

KaraokeUiKit.launchRoom(
    roomInfo, // must
    config, // must
    karaokeRoomView
)

// Subscribe room error events
val errorDelegate = object: AUIRtmErrorProxyDelegate{
  override fun onTokenPrivilegeWillExpire(channelName: String?) {
    // Callback when rtm token expired.
  }
}
KaraokeUiKit.subscribeError(roomInfo.roomId, errorDelegate)

// Subscribe room response events
val respDelegate = object: AUIRoomManagerRespDelegate{
  override fun onRoomDestroy(roomId: String){
    // Room destroyed by host
  }
}
KaraokeUiKit.bindRespDelegate(respDelegate)
```

### 6. Renew Token
```kotlin
val config = AUiRoomConfig(roomInfo.roomId)
// Generate token with config.channelName and AUIRoomContext.shared().currentUserInfo.userId
config.rtmToken = ""
config.rtcToken = ""
// Generate token with config.rtcChannelName and AUIRoomContext.shared().currentUserInfo.userId
config.rtcRtmToken = ""
config.rtcRtcToken = ""
// Generate token with config.rtcChorusChannelName and AUIRoomContext.shared().currentUserInfo.userId
config.rtcChorusRtcToken = ""

KaraokeUiKit.renewToken(config)
```

### 7. Destory Karaoke Room
```kotlin
KaraokeUiKit.destroyRoom(roomId)
KaraokeUiKit.unsubscribeError(roomId, errorDelegate)
KaraokeUiKit.unbindRespDelegate(respDelegate)
```

## API reference

### KaraokeUiKit

Karaoke Launch Class.

#### setup

```kotlin
fun setup(
    config: AUICommonConfig,
    ktvApi: KTVApi? = null,
    rtcEngineEx: RtcEngineEx? = null,
    rtmClient: RtmClient? = null
)
```

The parameters are shown in the table below:

| parameter   | type            | meaning                                                      |
| ----------- | --------------- | ------------------------------------------------------------ |
| config      | AUICommonConfig | General configuration, including user information and appId, etc. |
| ktvApi      | KTVApi          | (Optional) Agora KTV scene API instance. When the KTVApi has been integrated in the project, it can be imported, otherwise the empty transmission will be created internally. |
| rtcEngineEx | RtcEngineEx     | (Optional) Agora RTC engine. When Agora RTC has been integrated in the project, it can be passed in, otherwise it will be automatically created internally. |
| rtmClient   | RtmClient       | (Optional) Agora RTM engine. When Agora RTM has been integrated in the project, it can be passed in, otherwise it will be automatically created internally. |



#### createRoom

```kotlin
fun createRoom(
    createRoomInfo: AUICreateRoomInfo,
    success: (AUIRoomInfo) -> Unit,
    failure: (AUIException) -> Unit
)
```

The parameters are shown in the table below:

| parameter      | type              | meaning                                                  |
| -------------- | ----------------- | -------------------------------------------------------- |
| createRoomInfo | AUICreateRoomInfo | Information needed to create a room                      |
| success        | Function          | Success callback, success will return a room information |
| failure        | Function          | Failure callback                                         |



#### getRoomList

```kotlin
fun getRoomList(
  startTime: Long?,
  pageSize: Int,
  success: (List<AUIRoomInfo>) -> Unit,
  failure: (AUIException) -> Unit
)
```

The parameters are shown in the table below:

| parameter | type     | meaning                                                      |
| --------- | -------- | ------------------------------------------------------------ |
| startTime | Long     | The page start time                                          |
| pageSize  | Int      | The page size                                                |
| success   | Function | Success callback, success will return a list of room information |
| failure   | Function | Failure callback                                             |

#### launchRoom

```kotlin
fun launchRoom(
    roomInfo: AUIRoomInfo,
    config: AUIRoomConfig,
    karaokeView: KaraokeRoomView,
)
```

The parameters are shown in the table below:

| parameter   | type            | meaning                                                      |
| ----------- | --------------- | ------------------------------------------------------------ |
| roomInfo    | AUIRoomInfo     | Room information                                             |
| config      | AUIRoomConfig   | Related configuration in the room, including sub-channel name and token |
| karaokeView | KaraokeRoomView | Room UI View                                                 |

#### renewToken

```kotlin
fun renewToken(
    config: AUIRoomConfig
)
```

The parameters are shown in the table below:

| parameter   | type            | meaning                                                                 |
| ----------- | --------------- |-------------------------------------------------------------------------|
| config      | AUIRoomConfig   | Related configuration in the room, including sub-channel name and token |

#### destroyRoom

```kotlin
fun destroyRoom(roomId: String?)
```

The parameters are shown in the table below:

| parameter | type   | meaning                       |
| --------- | ------ | ----------------------------- |
| roomId    | String | The ID of the room to destroy |

#### subscribeError

Subscribe room error events.

```kotlin
fun subscribeError(roomId: String, delegate: AUIRtmErrorProxyDelegate)
```

The parameters are shown in the table below:

| parameter | type                     | meaning                      |
| --------- | ------------------------ | ---------------------------- |
| roomId    | String                   | The room id                  |
| delegate  | AUIRtmErrorProxyDelegate | The error delegate implement |

#### unsubscribeError

Unsubscribe room error events.

```kotlin
fun unsubscribeError(roomId: String, delegate: AUIRtmErrorProxyDelegate)
```

The parameters are shown in the table below:

| parameter | type                     | meaning                      |
| --------- | ------------------------ | ---------------------------- |
| roomId    | String                   | The room id                  |
| delegate  | AUIRtmErrorProxyDelegate | The error delegate implement |

#### bindRespDelegate

Bind to get the room response events.

```kotlin
fun bindRespDelegate(delegate: AUIRoomManagerRespDelegate)
```
The parameters are shown in the table below:

| parameter | type                       | meaning                              |
| --------- | -------------------------- | ------------------------------------ |
| delegate  | AUIRoomManagerRespDelegate | The room response delegate implement |

#### unbindRespDelegate

```kotlin
fun unbindRespDelegate(delegate: AUIRoomManagerRespDelegate)
```

The parameters are shown in the table below:

| parameter | type                       | meaning                              |
| --------- | -------------------------- | ------------------------------------ |
| delegate  | AUIRoomManagerRespDelegate | The room response delegate implement |

#### release

```kotlin
fun release()
```

### AUIRtmErrorProxyDelegate

The error delegate interface.

#### onConnectionStateChanged

Callback when network status changes.

The parameters are shown in the table below:

| parameter   | type   | meaning                                                      |
| ----------- | ------ | ------------------------------------------------------------ |
| channelName | String | Same with the room Id.                                       |
| state       | Int    | Network status，See **RtmConstants.RtmConnectionState**      |
| reason      | Int    | Change reason，See **RtmConstants.RtmConnectionChangeReason** |


#### onMsgRecvEmpty

Callback when receiving empty message.

The parameters are shown in the table below:

| parameter   | type   | meaning                |
| ----------- | ------ | ---------------------- |
| channelName | String | Same with the room Id. |


#### onTokenPrivilegeWillExpire

Callback when the rtm token expired.

The parameters are shown in the table below:

| parameter   | type   | meaning                |
| ----------- | ------ | ---------------------- |
| channelName | String | Same with the room Id. |

### AUIRoomManagerRespDelegate

Response delegate interface.

#### onRoomDestroy

Callback when the room destroyed.

The parameters are shown in the table below:

| parameter | type   | meaning     |
| --------- | ------ | ----------- |
| roomId    | String | The room id |


#### onRoomInfoChange

Callback when the room info changed.

The parameters are shown in the table below:

| parameter | type        | meaning               |
| --------- | ----------- | --------------------- |
| roomId    | String      | The room id           |
| roomInfo  | AUIRoomInfo | The room info changed |


#### onAnnouncementDidChange

Callback when room announcement is updated.

The parameters are shown in the table below:

| parameter | type   | meaning              |
| --------- | ------ | -------------------- |
| roomId    | String | The room id          |
| content   | String | announcement content |


#### onRoomUserBeKicked

Calledback when the user is kicked.

The parameters are shown in the table below:

| parameter | type   | meaning        |
| --------- | ------ | -------------- |
| roomId    | String | The room id    |
| userId    | String | Kicked user ID |


## Data Model

### AUICommonConfig

| parameter  | type    | meaning         |
| ---------- | ------- | --------------- |
| context    | Context | Android Contex  |
| appId      | String  | Agora AppID     |
| userId     | String  | User ID         |
| userName   | String  | User name       |
| userAvatar | String  | User avatar url |

### AUIRoomInfo

| parameter   | type                 | meaning           |
| ----------- | -------------------- | ----------------- |
| roomId      | String               | Room id           |
| roomOwner   | AUIUserThumbnailInfo | Room information  |
| onlineUsers | int                  | Online user count |
| createTime  | long                 | Room create time  |

### AUIUserThumbnailInfo

| parameter  | type   | meaning         |
| ---------- | ------ | --------------- |
| userId     | String | Room id         |
| userName   | String | User name       |
| userAvatar | String | User avatar url |

### AUIException

| parameter | type   | meaning       |
| --------- | ------ | ------------- |
| code      | int    | Error code    |
| message   | String | Error message |

### AUIRoomConfig

| parameter            | type   | meaning                                                      |
| -------------------- | ------ | ------------------------------------------------------------ |
| channelName          | String | Main channel name, usually roomId                            |
| rtmToken             | String | The rtm token of the main channel whose uid is setup in AUICommonConfig |
| rtcToken             | String | The rtc token of the main channel whose uid is setup in AUICommonConfig |
| rtcChannelName       | String | Video channel name, usually {roomId}_rtc                     |
| rtcRtcToken          | String | The rtc token of the video channel whose uid is setup in AUICommonConfig |
| rtcRtmToken          | String | The rtm token of the video channel whose uid is setup in AUICommonConfig |
| rtcChorusChannelName | String | Chorus channel name, usually {roomId}_rtc_ex                 |
| rtcChorusRtcToken    | String | The rtc token of the chorus channel whose uid is setup in AUICommonConfig |



## Directory Structure

```
.
├── app                                       				            // Demo
└── asceneskit																				
    └── src
        └── main
            ├── java
            │   └── io.agora.asceneskit.karaoke				
            │       ├── AUIKaraokeRoomService.kt	    // Room service, manage AUIKit services 
            │       ├── KaraokeRoomView.kt            // Room UI，manage AUIKit UI components
            │       ├── KaraokeUiKit.kt           	// Karaoke launch class
            │       └── binder
            │           ├── AUIChatBottomBarBinder.kt	// The binder of bottom bar UI and services
            │           ├── AUIChatListBinder.kt		// The binder of chat list UI and services
            │           ├── AUIGiftBarrageBinder.kt	// The binder of gift UI and services
            │           ├── AUIJukeboxBinder.java 	// The binder of jukebox UI and services
            │           ├── AUIMicSeatsBinder.java	// The binder of micseats UI and services
            │           ├── AUIMusicPlayerBinder.java	// The binder of music player UI and services
            │           └── IAUIBindable.java
            ├── res
            └── res-ktv
```



## License

Copyright © Agora Corporation. All rights reserved.
Licensed under the [MIT license](LICENSE).