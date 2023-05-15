# AUiKit组件介绍

*[English](README.md) | 中文*

<!-- TOC START -->

- [AUiKit基础组件](#auikit基础组件)
- [Service](#service)
  * [AUiMicSeatServiceDelegate](#auimicseatservicedelegate)
  * [AUiMicSeatRespDelegate](#auimicseatrespdelegate)
  * [AUiUserServiceDelegate](#auiuserservicedelegate)
  * [AUiUserRespDelegate](#auiuserrespdelegate)
  * [AUiChorusServiceDelegate](#auichorusservicedelegate)
  * [AUiChorusRespDelegate](#auichorusrespdelegate)
  * [AUiMusicServiceDelegate](#auimusicservicedelegate)
  * [AUiMusicRespDelegate](#auimusicrespdelegate)
  * [AUiPlayerServiceDelegate](#auiplayerservicedelegate)
  * [AUiPlayerRespDelegate](#auiplayerrespdelegate)
  * [AUiRoomManagerDelegate](#auiroommanagerdelegate)
  * [AUiRoomManagerRespDelegate](#auiroommanagerrespdelegate)
- [Widget](#widget)
  * [AUiActionSheet](#auiactionsheet)
    * [主容器配置](#主容器配置) 
    * [列表Item配置](#列表Item配置)
  

  <!-- TOC END -->

# AUiKit基础组件
```
AUiKit  
├─ Service                              // 基础服务组件
│  ├─ AUiMicSeatServiceDelegate         // 麦位管理请求协议
│  ├─ AUiMicSeatRespDelegate            // 麦位管理响应协议
│  ├─ AUiMicSeatServiceImpl             // 麦位管理实现类         
│  ├─ AUiUserServiceDelegate            // 用户管理请求协议       
│  ├─ AUiUserRespDelegate               // 用户管理响应协议
│  ├─ AUiUserServiceImpl                // 用户管理实现类
│  ├─ AUiChorusServiceDelegate          // 合唱管理请求协议
│  ├─ AUiChorusRespDelegate             // 合唱管理响应协议
│  ├─ AUiChorusServiceImpl              // 合唱管理实现类
│  ├─ AUiMusicServiceDelegate           // 音乐管理请求协议
│  ├─ AUiMusicRespDelegate              // 音乐管理响应协议
│  ├─ AUiMusicServiceImpl               // 音乐管理实现类
│  ├─ AUiPlayerServiceDelegate          // k歌播放管理请求协议
│  ├─ AUiPlayerRespDelegate             // k歌播放管理响应协议
│  ├─ AUiPlayerServiceImpl              // K歌播放管理实现类
│  ├─ AUiRoomManagerDelegate            // 房间管理请求协议
│  ├─ AUiRoomManagerRespDelegate        // 房间管理响应协议
│  └─ AUiRoomManagerImpl                // 房间管理实现类
│
├─ Widget                               // 无业务的基础UI组件
│  ├─ AUiActionSheet                    // 动作面板
│  ├─ Alert                             // 按钮组件
│  ├─ Button                            // 按钮
│  ├─ Segmented                         //
│  ├─ Slider                            // 滑动条
│  ├─ TableView                         // 
│  ├─ Tabs                              // 选项卡
│  ├─ TextField                         // 编辑框
│  └─ Toast                             // 提示框
│
└─ Component                           // 包含具体业务的UI模块
   ├─ JukeBox                           // 点歌器组件 
   │  ├─ AUiJukeBoxView                 // 点歌器View，不包含逻辑
   │  └─ AUiJukeBoxViewBinder           // 点歌器View与Service绑定类
   ├─ MicSeat                           // 麦位组件
   │  ├─ AUiMicSeatView                 // 麦位View，不包含逻辑
   │  └─ AUiMicSeatViewBinder           // 麦位View与Service绑定类
   └─ Player                            // KTV歌曲播放器组件
      ├─ AUiPlayerView                  // KTV歌曲播放器View，不包含逻辑
      └─ AUiPlayerViewBinder            // KTV歌曲播放器View与Service绑定类
   
```
# API参考
## Service

### AUiMicSeatServiceDelegate 

| API                | 描述                              |
| ------------------ | --------------------------------- |
| bindRespDelegate   | 绑定响应回调                      |
| unbindRespDelegate | 解除绑定响应回调                  |
| enterSeat          | 主动上麦（听众端和房主均可调用）  |
| leaveSeat          | 主动下麦（主播调用）              |
| pickSeat           | 抱人上麦（房主调用）              |
| kickSeat           | 踢人下麦（房主调用）              |
| muteAudioSeat      | 静音/解除静音某个麦位（房主调用） |
| muteVideoSeat      | 关闭/打开麦位摄像头（房主调用）   |
| closeSeat          | 封禁/解禁某个麦位（房主调用）     |

### AUiMicSeatRespDelegate

| API               | 描述                                |
| ----------------- | ----------------------------------- |
| onAnchorEnterSeat | 有成员上麦（主动上麦/房主抱人上麦） |
| onAnchorLeaveSeat | 有成员下麦（主动下麦/房主踢人下麦） |
| onSeatAudioMute   | 房主对麦位进行了静音/解禁           |
| onSeatVideoMute   | 房主对麦位摄像头进行禁用/启用       |
| onSeatClose       | 房主对麦位进行了封麦/解封           |

### AUiUserServiceDelegate

| API                | 描述                       |
| ------------------ | -------------------------- |
| bindRespDelegate   | 绑定响应回调               |
| unbindRespDelegate | 解除绑定响应回调           |
| getUserInfoList    | 获取用户列表信息           |
| muteUserAudio      | 对自己静音/解除静音        |
| muteUserVideo      | 对自己禁用/解禁摄像头  |

### AUiUserRespDelegate

| API                | 描述                               |
| ------------------ | ---------------------------------- |
| onRoomUserSnapshot | 用户进入房间后获取到的所有用户信息 |
| onRoomUserEnter    | 用户进入房间时的回调                   |
| onRoomUserLeave    | 用户离开房间时的回调                   |
| onRoomUserUpdate   | 用户的信息被修改时的回调               |
| onUserAudioMute    | 用户关闭/开启了麦克风时的回调            |
| onUserVideoMute    | 用户关闭/开启了摄像头时的回调            |

### AUiChorusServiceDelegate

| API                | 描述             |
| ------------------ | ---------------- |
| bindRespDelegate   | 绑定响应回调     |
| unbindRespDelegate | 解除绑定响应回调 |
| getChoristersList  | 获取合唱者列表   |
| joinChorus         | 加入合唱         |
| leaveChorus        | 退出合唱         |

### AUiChorusRespDelegate

| API                 | 描述       |
| ------------------- | ---------- |
| onChoristerDidEnter | 合唱者加入 |
| onChoristerDidLeave | 合唱者离开 |

### AUiMusicServiceDelegate

| API                  | 描述               |
| -------------------- | ------------------ |
| bindRespDelegate     | 绑定响应回调       |
| unbindRespDelegate   | 解除绑定响应回调   |
| getMusicList         | 获取歌曲列表       |
| searchMusic          | 搜索歌曲           |
| getAllChooseSongList | 获取当前点歌列表   |
| chooseSong           | 点一首歌           |
| removeSong           | 移除一首自己点的歌 |
| pinSong              | 置顶歌曲           |
| updatePlayStatus     | 更新歌曲播放状态   |

### AUiMusicRespDelegate

| API                    | 描述                                    |
| ---------------------- | --------------------------------------- |
| onAddChooseSong        | 新增一首歌曲时的回调                        |
| onRemoveChooseSong     | 删除一首歌曲时的回调                      |
| onUpdateChooseSong     | 更新一首歌曲时的回调（例如修改play status） |
| onUpdateAllChooseSongs | 更新所有歌曲时的回调（例如pin）             |

### AUiPlayerServiceDelegate

| API                            | 描述                               |
| ------------------------------ | ---------------------------------- |
| bindRespDelegate               | 绑定响应回调                       |
| unbindRespDelegate             | 解除绑定响应回调                   |
| loadMusic                      | 加载歌曲                           |
| switchSingerRole               | 切换角色                           |
| startSing                      | 播放歌曲                           |
| stopSing                       | 停止播放歌曲                       |
| resumeSing                     | 恢复播放                           |
| pauseSing                      | 暂停播放                           |
| seekSing                       | 调整进度                           |
| adjustMusicPlayerPlayoutVolume | 调整音乐本地播放的声音             |
| adjustRecordingSignalVolume    | 调整采集音量                       |
| adjustMusicPlayerPublishVolume | 调整音乐推送到远端的声音大小       |
| adjustPlaybackVolume           | 调整本地播放远端伴唱人声音量的大小 |
| selectMusicPlayerTrackMode     | 选择音轨，原唱、伴唱               |
| getPlayerDuration              | 获取播放时长                       |
| getMusicPlayer                 | 获取播放器实例                     |
| setAudioPitch                  | 升降调                             |
| setAudioEffectPreset           | 音效                               |
| setVoiceConversionPreset       | 变声                               |
| enableEarMonitoring            | 耳返                               |

### AUiPlayerRespDelegate

| API                       | 描述             |
| ------------------------- | ---------------- |
| onPreludeDidAppear        | 前奏开始加载     |
| onPreludeDidDisappear     | 前奏结束加载     |
| onPostludeDidAppear       | 尾奏开始加载     |
| onPostludeDidDisappear    | 尾奏结束加载     |
| onPlayerPositionDidChange | 获取时间进度回调 |
| onPlayerStateChanged      | 播放状态变化回调 |

### AUiRoomManagerDelegate

| API                | 描述                 |
| ------------------ | -------------------- |
| bindRespDelegate   | 绑定响应回调         |
| unbindRespDelegate | 解除绑定响应回调     |
| createRoom         | 创建房间（房主调用） |
| destroyRoom        | 销毁房间（房主调用） |
| enterRoom          | 进入房间（听众调用） |
| exitRoom           | 退出房间（听众调用） |
| getRoomInfoList    | 获取房间列表         |

### AUiRoomManagerRespDelegate

| API              | 描述             |
| ---------------- | ---------------- |
| onRoomDestroy    | 房间被销毁的回调 |
| onRoomInfoChange | 房间信息变更回调 |

## Widget
### AUiActionSheet
#### 主容器配置
| 属性                 | 描述       |
| ------------------- | ---------- |
| collectionViewTopEdge | 展示的列表内容距离上面空间的间距 |
| itemType | 列表样式，上下滑动还是左右滑动 |
| itemHeight | 列表Item高度 |
| titleLabelFont | 主标题字体 |
| titleLabelTextColor | 主标题字体颜色 |
| nameLabelFont | 用户主标题字体 |
| nameLabelTextColor | 用户主标题字体颜色 |
| seatLabelFont | 用户副标题字体 |
| seatLabelTextColor | 用户副标题字体颜色 |
| avatarWidth | 头像宽度 |
| avatarHeight | 头像高度 |

#### 列表Item配置
| 属性                 | 描述       |
| ------------------- | ---------- |
| icon | item图标 |
| backgroundIcon | item背景图标 |
| titleColor | 标题颜色 |
| imageWidth | 图片宽度 |
| imageHeight | 图片高度 |
| backgroundImageWidth | 背景图片宽度 |
| backgroundImageHeight | 背景图片高度 |
| padding | 文字图片内容的间隔 |
| selectedBorderColor | 选中框颜色 |
| selectedBorderWidth | 选中框宽度 |
| selectedBorderRadius | 选中框圆角 |


### Alert

### Button


### Segmented

### Slider

### TableView

### Tabs

### TextField

