# AScenesKit

*[English](AScenesKit.md) | 中文*

AScenesKit是一个Karaoke场景组件，提供房间管理以及拉起房间页面的功能，开发者可以使用他快速搭起一个Karaoke应用。


## 快速集成
> 在集成前请确保已经按照这个[教程](../README.zh.md)将项目成功运行起来。

### 1. 添加源码

**将以下源码复制到自己项目里：**

- [asceneskit](../asceneskit)

**在settings.gradle里添加库**
```groovy
include ':asceneskit'
```

**在build.gradle里配置资源路径，viewBinding和依赖**
```groovy
dependencies {
    implementation project(':asceneskit')
}
```

**在AndroidManifest.xml里配置权限和主题**
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

**在项目的[**local.properties**](../local.properties)里配置业务服务器域名**

  ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_serverhost_android.png)

``` 
SERVER_HOST= （业务服务器域名）
```

> 声网测试域名： https://service.agora.io/uikit-karaoke，


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
KaraokeUiKit.setup(
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
// 房间信息，由房间列表而来或者是创建房间得到
val roomInfo : AUIRoomInfo
// layout里的KaraokeRoomView
val karaokeRoomView: KaraokeRoomView

val config = AUiRoomConfig(roomInfo.roomId)
// 生成config.channelName及AUIRoomContext.shared().currentUserInfo.userId的token
config.rtmToken = ""
config.rtcToken = ""
// 生成config.rtcChannelName及AUIRoomContext.shared().currentUserInfo.userId的token
config.rtcRtmToken = ""
config.rtcRtcToken = ""
// 生成config.rtcChorusChannelName及AUIRoomContext.shared().currentUserInfo.userId的token
config.rtcChorusRtcToken = ""

KaraokeUiKit.launchRoom(
    roomInfo, // must
    config, // must
    karaokeRoomView
)

// 订阅房间事件
KaraokeUiKit.bindRespDelegate(object: AUIRoomManagerRespDelegate{
    override fun onRoomDestroy(roomId: String){
        // 房间被销毁
    }
})
```

### 5. 销毁房间
```kotlin
KaraokeUiKit.destroyRoom(roomId)
// 取消订阅房间事件
KaraokeUiKit.unbindRespDelegate(this@RoomActivity)
```

## 许可证
版权所有 Agora, Inc. 保留所有权利。
使用 [MIT 许可证](../LICENSE)
