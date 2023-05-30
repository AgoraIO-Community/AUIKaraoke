# KaraokeUiKit

*[English](KaraokeUiKit.md) | 中文*

KaraokeUiKit是一个Karaoke场景组件，提供房间管理以及拉起房间页面的功能，开发者可以使用他快速搭起一个Karaoke应用。


## 快速集成
> 在集成前请确保已经按照这个[教程](../README.zh.md)将项目成功运行起来。

### 1. 添加源码

**将以下源码复制到自己项目里：**

- [auikit](../../AUIKit/Android/auikit)
- [asceneskit](../asceneskit)
- [KaraokeUiKit](../app/src/main/java/io/agora/app/karaoke/kit)

**在settings.gradle里添加库**
```groovy
include ':auikit'
include ':asceneskit'
```

**在build.gradle里配置资源路径，viewBinding和依赖**
```groovy
android {
    buildFeatures {
        viewBinding true
    }
    sourceSets {
        main {
            res.srcDirs += "src/main/java/io/agora/app/karaoke/kit/res"
        }
    }
}
dependencies {
    implementation project(':asceneskit')
}
```

**在AndroidManifest.xml里配置权限，主题和KaraokeRoomActivity**
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

### 2. 初始化KaraokeUiKit
```kotlin
// Create Common Config
val config = AUiCommonConfig()
config.context = application
config.appId = "Agora APP ID" // Get the Agora APP ID from agora console
config.userId = "User ID"
config.userName = "User Name"
config.userAvatar = "User Avatar"
// Initialize KaraokeUiKit. If you have your own rtmClient, rtcEngine or ktvApi, you can pass them to KaraokeUiKit.
KaraokeUiKit.init(
    config = config, // must
    rtmClient = null, // option
    rtcEngineEx = null, // option
    ktvApi = null // option
)
```

### 3. 获取房间列表
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

### 4. 创建房间
```kotlin
val createRoomInfo = AUiCreateRoomInfo()
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

### 5. 拉起房间
```kotlin
val config = AUiRoomConfig(roomInfo.roomId)
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

## 许可证
版权所有 Agora, Inc. 保留所有权利。
使用 [MIT 许可证](../LICENSE)
