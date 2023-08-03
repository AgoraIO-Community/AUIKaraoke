# AScenesKit

*English | [中文](AScenesKit.zh.md)*

AScenesKit is a Karaoke scene component that provides room management and the function of pulling up room pages. Developers can use it to quickly build a Karaoke application.


## Quick integration
> Please make sure you have successfully run the project according to [this tutorial](../README.md) before integrating.

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

// Subscribe room event
KaraokeUiKit.bindRespDelegate(object: AUIRoomManagerRespDelegate{
    override fun onRoomDestroy(roomId: String){
        // Room destroyed by host
    }
})
```

### 6. Destory Karaoke Room
```kotlin
KaraokeUiKit.destroyRoom(roomId)
// Unsubscribe room event
KaraokeUiKit.unbindRespDelegate(this@RoomActivity)
```

## License
Copyright © Agora Corporation. All rights reserved.
Licensed under the [MIT license](../LICENSE).
