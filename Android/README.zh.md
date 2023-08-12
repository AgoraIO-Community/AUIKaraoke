# AUIKItKaraoke-Android

*[English](README.md) | 中文*

AUIKitKaraoke 是一个开源的音视频 UI 组件，通过在项目中集成 AUIKitKaraoke 组件，您只需要编写几行代码就可以为您的应用添加在线 K 歌场景，体验 K 歌、麦位管理、收发礼物、文字聊天等 Agora RTC 在 KTV 场景下的相关能力。




## 快速跑通

### 1. 环境准备

- <mark>最低兼容 Android 7.0</mark>（SDK API Level 24）
- Android Studio 3.5及以上版本。
- Android 7.0 及以上的手机设备。
- JDK 17以上

---

### 2. 运行示例
- (可选)在AUIKitKaraoke目录下执行

  > 如果执行会了会使用本地AUIKit，否则使用在线maven上的AUIKit
```
git submodule update --init --remote
```

- 在项目的[**local.properties**](/local.properties)里配置业务服务器域名
  
  ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_serverhost_android.png)

``` 
SERVER_HOST= （业务服务器域名）
```

> 业务服务器搭建可参考 [后台README](../backend/)。
> 声网测试域名： https://service.agora.io/uikit-karaoke。

- 用 Android Studio 运行项目即可开始您的体验

## 快速集成

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

> 声网测试域名： https://service.agora.io/uikit-karaoke


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

## API参考

### setup

初始化。

```kotlin
fun setup(
    config: AUICommonConfig,
    ktvApi: KTVApi? = null,
    rtcEngineEx: RtcEngineEx? = null,
    rtmClient: RtmClient? = null
)
```

参数如下表所示：

| 参数        | 类型            | 含义                                                         |
| ----------- | --------------- | ------------------------------------------------------------ |
| config      | AUICommonConfig | 通用配置，包含用户信息和appId等                              |
| ktvApi      | KTVApi          | （可选）声网KTV场景化API实例。当项目里已经集成了KTV场景化可以传入，否则传空由内部自行创建。 |
| rtcEngineEx | RtcEngineEx     | （可选）声网RTC引擎。当项目里已集成Agora RTC可以传入，否则传空由内部自动创建。 |
| rtmClient   | RtmClient       | （可选）声网RTM引擎。当项目里已集成Agora RTM可以传入，否则传空由内部自动创建。 |



### createRoom

创建房间

```kotlin
fun createRoom(
    createRoomInfo: AUICreateRoomInfo,
    success: (AUIRoomInfo) -> Unit,
    failure: (AUIException) -> Unit
)
```

参数如下表所示：

| 参数           | 类型              | 含义                             |
| -------------- | ----------------- | -------------------------------- |
| createRoomInfo | AUICreateRoomInfo | 创建房间所需的信息               |
| success        | Function          | 成功回调，成功会返回一个房间信息 |
| failure        | Function          | 失败回调                         |



### getRoomList

获取房间列表

```kotlin
fun getRoomList(
  startTime: Long?,
  pageSize: Int,
  success: (List<AUIRoomInfo>) -> Unit,
  failure: (AUIException) -> Unit
)
```

参数如下表所示：

| 参数      | 类型     | 含义                                 |
| --------- | -------- | ------------------------------------ |
| startTime | Long     | 开始时间                             |
| pageSize  | Int      | 页数                                 |
| success   | Function | 成功回调，成功会返回一个房间信息列表 |
| failure   | Function | 失败回调                             |

### launchRoom

```kotlin
fun launchRoom(
    roomInfo: AUIRoomInfo,
    config: AUIRoomConfig,
    karaokeView: KaraokeRoomView,
)
```

参数如下表所示：

| 参数        | 类型            | 含义                                  |
| ----------- | --------------- | ------------------------------------- |
| roomInfo    | AUIRoomInfo     | 房间信息                              |
| config      | AUIRoomConfig   | 房间里相关的配置，包含子频道名和token |
| karaokeView | KaraokeRoomView | 房间UI View                           |

### destroyRoom

销毁房间

```kotlin
fun destroyRoom(roomId: String?)
```

参数如下表所示：

| 参数   | 类型   | 含义           |
| ------ | ------ | -------------- |
| roomId | String | 要销毁的房间ID |

### release

释放资源

```kotlin
fun release()
```



## 数据模型

### AUICommonConfig

| 参数       | 类型    | 含义                 |
| ---------- | ------- | -------------------- |
| context    | Context | Android Contex上下文 |
| appId      | String  | 声网AppID            |
| userId     | String  | 用户ID               |
| userName   | String  | 用户名               |
| userAvatar | String  | 用户头像             |

### AUIRoomInfo

| 参数        | 类型                 | 含义         |
| ----------- | -------------------- | ------------ |
| roomId      | String               | 房间id       |
| roomOwner   | AUIUserThumbnailInfo | 房主信息     |
| onlineUsers | int                  | 房间人数     |
| createTime  | long                 | 房间创建时间 |

### AUIUserThumbnailInfo

| 参数       | 类型   | 含义     |
| ---------- | ------ | -------- |
| userId     | String | 用户Id   |
| userName   | String | 用户名   |
| userAvatar | String | 用户头像 |

### AUIException

| 参数    | 类型   | 含义     |
| ------- | ------ | -------- |
| code    | int    | 错误码   |
| message | String | 错误信息 |

### AUIRoomConfig

| 参数                 | 类型   | 含义                                                         |
| -------------------- | ------ | ------------------------------------------------------------ |
| channelName          | String | 主频道名，一般为roomId                                       |
| rtmToken             | String | 主频道的rtm token，uid为setup时AUICommonConfig里的userId     |
| rtcToken             | String | 主频道的rtc token，uid为setup时AUICommonConfig里的userId     |
| rtcChannelName       | String | 音视频频道名，一般为{roomId}_rtc                             |
| rtcRtcToken          | String | 音视频频道的rtc token，uid为setup时AUICommonConfig里的userId |
| rtcRtmToken          | String | 音视频频道的rtm token，uid为setup时AUICommonConfig里的userId |
| rtcChorusChannelName | String | 合唱频道名，一般为{roomId}_rtc_ex                            |
| rtcChorusRtcToken    | String | 合唱频道的rtc token，uid为setup时AUICommonConfig里的userId   |



## 目录结构

```
.
├── app                                       				            // Demo
└── asceneskit																				
    └── src
        └── main
            ├── java
            │   └── io.agora.asceneskit.karaoke				
            │       ├── AUIKaraokeRoomService.kt	    // 房间服务，管理AUIKit各个Service
            │       ├── KaraokeRoomView.kt            // 房间UI，管理AUIKit各个UI组件
            │       ├── KaraokeUiKit.kt           	// K歌入口类
            │       └── binder
            │           ├── AUIChatBottomBarBinder.kt	// 底部栏UI和service的绑定者
            │           ├── AUIChatListBinder.kt		// 聊天UI和service的绑定者
            │           ├── AUIGiftBarrageBinder.kt   // 礼物UI和service的绑定者
            │           ├── AUIJukeboxBinder.java 	// 点歌器UI和service的绑定者
            │           ├── AUIMicSeatsBinder.java	// 麦位UI和service的绑定者
            │           ├── AUIMusicPlayerBinder.java	// 播放器UI和service的绑定者
            │           └── IAUIBindable.java
            ├── res
            └── res-ktv
```



## 许可证
版权所有 Agora, Inc. 保留所有权利。
使用 [MIT 许可证](LICENSE)
