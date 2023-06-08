# KaraokeUiKit

*English | [中文](KaraokeUiKit.zh.md)*

KaraokeUiKit is a Karaoke scene component that provides room management and the function of pulling up room pages. Developers can use it to quickly build a Karaoke application.


## Quick integration
> Please make sure you have successfully run the project according to [this tutorial](../README.md) before integrating.

### 1. Add Source Code
**Copy the following source code into your own project:**

- [auikit](https://github.com/AgoraIO-Community/AUIKit/tree/main/Android/auikit)
- [asceneskit](../asceneskit)
- [KaraokeUiKit](../app/src/main/java/io/agora/app/karaoke/kit)

**Configure libraries in settings.gradle**
```groovy
include ':auikit'
include ':asceneskit'
```

**Configure resource path, viewBinding and dependencies in build.gradle**
```groovy
android {
    buildFeatures {
        viewBinding true
    }
    sourceSets {
        main {
            manifest.srcFile += "src/main/java/io/agora/app/karaoke/kit/AndroidManifest.xml"
            res.srcDirs += "src/main/java/io/agora/app/karaoke/kit/res"
        }
    }
}
dependencies {
    implementation project(':asceneskit')
}
```

**Configure permissions, theme and KaraokeRoomActivity in AndroidManifest.xml**
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
        
        <activity
            android:name=".kit.KaraokeRoomActivity"
            android:launchMode="singleTop"
            android:screenOrientation="behind" />

    </application>

</manifest>
```

### 2. Initialize KaraokeUiKit
```kotlin
// Create Common Config
val config = AUICommonConfig()
config.context = application
config.appId = "Agora APP ID"
config.userId = "User ID"
config.userName = "User Name"
config.userAvatar = "User Avatar"
// init AUIKit. If you have your own rtmClient, rtcEngine or ktvApi, you can pass them to KaraokeUiKit.
KaraokeUiKit.init(
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
val config = AUIRoomConfig(roomInfo.roomId)
config.themeId = io.agora.asceneskit.R.style.Theme_AKaraoke

KaraokeUiKit.launchRoom(
    roomInfo, // must
    config, // must
    KaraokeUiKit.RoomEventHandler // option
    ( 
        onRoomLaunchSuccess = {},
        onRoomLaunchFailure = {}
    )
)
```

## License
Copyright © Agora Corporation. All rights reserved.
Licensed under the [MIT license](../LICENSE).
