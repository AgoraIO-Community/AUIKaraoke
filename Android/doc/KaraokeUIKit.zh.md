# KaraokeUIKit

*[English](KaraokeUIKit.md) | 中文*

KaraokeUIKit是一个Karaoke场景组件，提供房间管理以及拉起房间页面的功能，开发者可以凭借该组件快速搭起一个Karaoke应用。

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


### 2. 初始化KaraokeUiKit

```kotlin
// Create Common Config
val config = AUiCommonConfig()
config.context = application
config.host = "业务服务器域名"
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

> 声网业务服务器测试域名： https://service.agora.io/uikit-karaoke


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

KaraokeUiKit.launchRoom(
    roomInfo, // must
    karaokeRoomView
)

// 注册房间事件观察者
val respObserver = object: AUIRoomManagerRespObserver{
  override fun onRoomDestroy(roomId: String){
    // 房间被销毁
  }
}
KaraokeUiKit.registerRoomRespObserver(respObserver)
```

### 6. 销毁房间

```kotlin
KaraokeUiKit.destroyRoom(roomId)
KaraokeUiKit.unRegisterRoomRespObserver(respObserver)
```

## API参考

### KaraokeUiKit

Karaoke入口类。

#### setup

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
| config      | AUICommonConfig | 通用配置，包含用户信息和域名等                               |
| ktvApi      | KTVApi          | （可选）声网KTV场景化API实例。当项目里已经集成了KTV场景化可以传入，否则传空由内部自行创建。 |
| rtcEngineEx | RtcEngineEx     | （可选）声网RTC引擎。当项目里已集成Agora RTC可以传入，否则传空由内部自动创建。 |
| rtmClient   | RtmClient       | （可选）声网RTM引擎。当项目里已集成Agora RTM可以传入，否则传空由内部自动创建。 |



#### createRoom

创建房间。

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



#### getRoomList

获取房间列表。

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

#### launchRoom

```kotlin
fun launchRoom(
    roomInfo: AUIRoomInfo,
    karaokeView: KaraokeRoomView,
)
```

参数如下表所示：

| 参数        | 类型            | 含义        |
| ----------- | --------------- | ----------- |
| roomInfo    | AUIRoomInfo     | 房间信息    |
| karaokeView | KaraokeRoomView | 房间UI View |


#### destroyRoom

销毁房间。

```kotlin
fun destroyRoom(roomId: String?)
```

参数如下表所示：

| 参数   | 类型   | 含义           |
| ------ | ------ | -------------- |
| roomId | String | 要销毁的房间ID |

#### registerRoomRespObserver

绑定对应房间的响应，比如房间被销毁、用户被踢出、房间的信息更新等。

```kotlin
fun registerRoomRespObserver(observer: AUIRoomManagerRespDelegate)
```

参数如下表所示：

| 参数     | 类型                       | 含义         |
| -------- | -------------------------- | ------------ |
| observer | AUIRoomManagerRespObserver | 响应回调对象 |

#### unRegisterRoomRespObserver

解除绑定对应房间的响应。

```kotlin
fun unRegisterRoomRespObserver(observer: AUIRoomManagerRespObserver)
```

参数如下表所示：

| 参数     | 类型                       | 含义         |
| -------- | -------------------------- | ------------ |
| observer | AUIRoomManagerRespObserver | 响应回调对象 |


#### release

释放资源。

```kotlin
fun release()
```

### AUIRoomManagerRespObserver

响应回调接口。

#### onRoomDestroy

房间被销毁时回调。

参数如下表所示：

| 参数   | 类型   | 含义   |
| ------ | ------ | ------ |
| roomId | String | 房间ID |


#### onRoomInfoChange

房间信息变更时回调。

参数如下表所示：

| 参数     | 类型        | 含义           |
| -------- | ----------- | -------------- |
| roomId   | String      | 房间ID         |
| roomInfo | AUIRoomInfo | 变更后房间信息 |


#### onAnnouncementDidChange

房间公告被更新时回调。

参数如下表所示：

| 参数    | 类型   | 含义     |
| ------- | ------ | -------- |
| roomId  | String | 房间ID   |
| content | String | 公告信息 |


#### onRoomUserBeKicked

用户被踢时回调。

参数如下表所示：

| 参数   | 类型   | 含义       |
| ------ | ------ | ---------- |
| roomId | String | 房间ID     |
| userId | String | 被踢用户ID |


## 数据模型

### AUICommonConfig

| 参数       | 类型    | 含义                 |
| ---------- | ------- | -------------------- |
| context    | Context | Android Contex上下文 |
| host       | String  | 业务服务器地址       |
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

## 许可证

版权所有 Agora, Inc. 保留所有权利。
使用 [MIT 许可证](../LICENSE)