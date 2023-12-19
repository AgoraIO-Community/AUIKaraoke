# KaraokeUIKit

*English | [中文](KaraokeUIKit.zh.md)*

KaraokeUIKit is a Karaoke scenario component that provides room management and the ability to pull up room pages. Developers can use this component to quickly build a Karaoke application。

## Quick Started

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


### 2. Initialize KaraokeUiKit

```kotlin
// Create Common Config
val config = AUiCommonConfig()
config.context = application
config.host = "Domain name of the business server"
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

> Agora Test Domain: https://service.agora.io/uikit-karaoke

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

KaraokeUiKit.launchRoom(
    roomInfo, // must
    karaokeRoomView
)

// Register room response observer
val respObserver = object: AUIRoomManagerRespObserver{
  override fun onRoomDestroy(roomId: String){
    // Room destroyed by host
  }
}
KaraokeUiKit.registerRoomRespObserver(respObserver)

```

### 6. Destory Karaoke Room

```kotlin
KaraokeUiKit.destroyRoom(roomId)
KaraokeUiKit.unbindRespDelegate(respObserver)
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
| config      | AUICommonConfig | General configuration, including user information and domain, etc. |
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
    karaokeView: KaraokeRoomView,
)
```

The parameters are shown in the table below:

| parameter   | type            | meaning          |
| ----------- | --------------- | ---------------- |
| roomInfo    | AUIRoomInfo     | Room information |
| karaokeView | KaraokeRoomView | Room UI View     |

#### destroyRoom

```kotlin
fun destroyRoom(roomId: String?)
```

The parameters are shown in the table below:

| parameter | type   | meaning                       |
| --------- | ------ | ----------------------------- |
| roomId    | String | The ID of the room to destroy |

#### registerRoomRespObserver

Bind to get the room response events.

```kotlin
fun registerRoomRespObserver(observer: AUIRoomManagerRespObserver)
```

The parameters are shown in the table below:

| parameter | type                       | meaning                              |
| --------- | -------------------------- | ------------------------------------ |
| observer  | AUIRoomManagerRespObserver | The room response observer implement |

#### unRegisterRoomRespObserver

```kotlin
fun unRegisterRoomRespObserver(observer: AUIRoomManagerRespObserver)
```

The parameters are shown in the table below:

| parameter | type                       | meaning                              |
| --------- | -------------------------- | ------------------------------------ |
| observer  | AUIRoomManagerRespObserver | The room response observer implement |

#### release

```kotlin
fun release()
```

### AUIRoomManagerRespObserver

Response observer interface.

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

| parameter  | type    | meaning                            |
| ---------- | ------- | ---------------------------------- |
| context    | Context | Android Contex                     |
| host       | String  | Domain name of the business server |
| userId     | String  | User ID                            |
| userName   | String  | User name                          |
| userAvatar | String  | User avatar url                    |

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

## License

Copyright © Agora Corporation. All rights reserved.
Licensed under the [MIT license](LICENSE).