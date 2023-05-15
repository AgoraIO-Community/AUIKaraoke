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
  * [AUiAlertView](#auialertview)
  * [AUiButton](#auibutton)
  * [AUiSlider](#auislider)
  * [AUiTableViewCell](#auitableviewcell)
  * [AUiTabs](#auitabs)
  * [AUiTextField](#auitextfield)
  * [AUiToast](#auitoast)
  <!-- TOC END -->

# AUiKit基础组件
```
AUiKit  
├─ Service                              // 基础服务组件
│  ├─ AUiMicSeatServiceDelegate         // 麦位管理协议
│  ├─ AUiMicSeatRespDelegate            // 麦位管理回调
│  ├─ AUiMicSeatServiceImpl             // 麦位管理实现类         
│  ├─ AUiUserServiceDelegate            // 用户管理协议       
│  ├─ AUiUserRespDelegate               // 用户管理回调
│  ├─ AUiUserServiceImpl                // 用户管理实现类
│  ├─ AUiChorusServiceDelegate          // 合唱管理协议
│  ├─ AUiChorusRespDelegate             // 合唱管理回调
│  ├─ AUiChorusServiceImpl              // 合唱管理实现类
│  ├─ AUiMusicServiceDelegate           // 音乐管理协议
│  ├─ AUiMusicRespDelegate              // 音乐管理回调
│  ├─ AUiMusicServiceImpl               // 音乐管理实现类
│  ├─ AUiPlayerServiceDelegate          // k歌播放管理协议
│  ├─ AUiPlayerRespDelegate             // k歌播放管理回调
│  ├─ AUiPlayerServiceImpl              // K歌播放管理实现类
│  ├─ AUiRoomManagerDelegate            // 房间管理协议
│  ├─ AUiRoomManagerRespDelegate        // 房间管理回调
│  └─ AUiRoomManagerImpl                // 房间管理实现类
│
├─ Widget                               // 无业务的基础UI组件
│  ├─ AUiActionSheet                    // 动作面板
│  ├─ AUiAlertView                      // 对话框
│  ├─ AUiButton                         // 按钮
│  ├─ AUiSlider                         // 滑动条
│  ├─ AUiTableViewCell                  // 表格视图单元格 
│  ├─ AUiTabs                           // 选项卡
│  ├─ AUiTextField                      // 编辑框
│  └─ AUiToast                          // 提示框
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
Service是基于声网的实时音视频(RTC)和即时通信服务(RTM)组合而成的组件

### AUiMicSeatServiceDelegate 
#### 麦位管理协议

| API                | 描述                              |
| ------------------ | --------------------------------- |
| bindRespDelegate   | 绑定回调                      |
| unbindRespDelegate | 解除绑定回调                  |
| enterSeat          | 主动上麦（听众端和房主均可调用）  |
| leaveSeat          | 主动下麦（主播调用）              |
| pickSeat           | 抱人上麦（房主调用）              |
| kickSeat           | 踢人下麦（房主调用）              |
| muteAudioSeat      | 静音/解除静音某个麦位（房主调用） |
| muteVideoSeat      | 关闭/打开麦位摄像头（房主调用）   |
| closeSeat          | 封禁/解禁某个麦位（房主调用）     |

### AUiMicSeatRespDelegate
#### 麦位管理回调

| API               | 描述                                |
| ----------------- | ----------------------------------- |
| onAnchorEnterSeat | 有成员上麦（主动上麦/房主抱人上麦） |
| onAnchorLeaveSeat | 有成员下麦（主动下麦/房主踢人下麦） |
| onSeatAudioMute   | 房主对麦位进行了静音/解禁           |
| onSeatVideoMute   | 房主对麦位摄像头进行禁用/启用       |
| onSeatClose       | 房主对麦位进行了封麦/解封           |

### AUiUserServiceDelegate
#### 用户管理协议
| API                | 描述                       |
| ------------------ | -------------------------- |
| bindRespDelegate   | 绑定回调               |
| unbindRespDelegate | 解除绑定回调           |
| getUserInfoList    | 获取用户列表信息           |
| muteUserAudio      | 对自己静音/解除静音        |
| muteUserVideo      | 对自己禁用/解禁摄像头  |

### AUiUserRespDelegate
#### 用户管理回调
| API                | 描述                               |
| ------------------ | ---------------------------------- |
| onRoomUserSnapshot | 用户进入房间后获取到的所有用户信息 |
| onRoomUserEnter    | 用户进入房间时的回调                   |
| onRoomUserLeave    | 用户离开房间时的回调                   |
| onRoomUserUpdate   | 用户的信息被修改时的回调               |
| onUserAudioMute    | 用户关闭/开启了麦克风时的回调            |
| onUserVideoMute    | 用户关闭/开启了摄像头时的回调            |

### AUiChorusServiceDelegate
#### 合唱管理协议
| API                | 描述             |
| ------------------ | ---------------- |
| bindRespDelegate   | 绑定回调     |
| unbindRespDelegate | 解除绑定回调 |
| getChoristersList  | 获取合唱者列表   |
| joinChorus         | 加入合唱         |
| leaveChorus        | 退出合唱         |

### AUiChorusRespDelegate
#### 合唱管理回调
| API                 | 描述       |
| ------------------- | ---------- |
| onChoristerDidEnter | 合唱者加入 |
| onChoristerDidLeave | 合唱者离开 |

### AUiMusicServiceDelegate
#### 音乐管理协议
| API                  | 描述               |
| -------------------- | ------------------ |
| bindRespDelegate     | 绑定回调       |
| unbindRespDelegate   | 解除绑定回调   |
| getMusicList         | 获取歌曲列表       |
| searchMusic          | 搜索歌曲           |
| getAllChooseSongList | 获取当前点歌列表   |
| chooseSong           | 点一首歌           |
| removeSong           | 移除一首自己点的歌 |
| pinSong              | 置顶歌曲           |
| updatePlayStatus     | 更新歌曲播放状态   |

### AUiMusicRespDelegate
#### 音乐管理回调
| API                    | 描述                                    |
| ---------------------- | --------------------------------------- |
| onAddChooseSong        | 新增一首歌曲时的回调                        |
| onRemoveChooseSong     | 删除一首歌曲时的回调                      |
| onUpdateChooseSong     | 更新一首歌曲时的回调（例如修改play status） |
| onUpdateAllChooseSongs | 更新所有歌曲时的回调（例如pin）             |

### AUiPlayerServiceDelegate
#### k歌播放管理协议
| API                            | 描述                               |
| ------------------------------ | ---------------------------------- |
| bindRespDelegate               | 绑定回调                       |
| unbindRespDelegate             | 解除绑定回调                   |
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
#### k歌播放管理回调
| API                       | 描述             |
| ------------------------- | ---------------- |
| onPreludeDidAppear        | 前奏开始加载     |
| onPreludeDidDisappear     | 前奏结束加载     |
| onPostludeDidAppear       | 尾奏开始加载     |
| onPostludeDidDisappear    | 尾奏结束加载     |
| onPlayerPositionDidChange | 获取时间进度回调 |
| onPlayerStateChanged      | 播放状态变化回调 |

### AUiRoomManagerDelegate
#### 房间管理协议
| API                | 描述                 |
| ------------------ | -------------------- |
| bindRespDelegate   | 绑定回调         |
| unbindRespDelegate | 解除绑定回调     |
| createRoom         | 创建房间（房主调用） |
| destroyRoom        | 销毁房间（房主调用） |
| enterRoom          | 进入房间（听众调用） |
| exitRoom           | 退出房间（听众调用） |
| getRoomInfoList    | 获取房间列表         |

### AUiRoomManagerRespDelegate
#### 房间管理回调
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


### AUiAlertView
| 属性                 | 描述       |
| ------------------- | ---------- |
| background(color: UIColor?) | 背景色 |
| isShowCloseButton(isShow: Bool) | 是否显示关闭按钮 |
| title(title: String?) | 主标题 |
| titleColor(color: UIColor?) | 主标题文字颜色 |
| titleFont(font: UIFont?) | 主标题字体 |
| content(content: String?) | 内容 |
| contentTextAligment(textAlignment: NSTextAlignment) | 内容文字对齐方式 |
| contentColor(color: UIColor?) | 内容文字颜色 |
| contentFont(font: UIFont?) | 内容字体 |
| textField(text: String?) | 输入框内容 |
| textField(color: UIColor?) | 输入框文字颜色 |
| textField(font: UIFont?) | 内容字体 |
| textField(cornerRadius: CGFloat) | 输入框圆角大小 |
| textField(showBottomDivider: Bool) | 输入框底部分割是否显示 |
| textField(bottomDividerColor: UIColor?)  | 输入框底部分割颜色 |
| textFieldBackground(color: UIColor?)  | 输入框背景色 |
| textFieldPlaceholder(placeholder: String?)  | 输入框文字占位符 |
| textFieldPlaceholder(color: UIColor?)   | 输入框文字占位符文字颜色 |
| textFieldPlaceholder(font: UIFont?)  | 输入框文字占位符字体 |
| leftButton(title: String?)  | 左边按钮文字内容 |
| leftButton(color: UIColor?)  | 左边按钮文字颜色 |
| leftButton(font: UIFont?)  | 左边按钮文字字体大小 |
| leftButton(cornerRadius: CGFloat)  | 左边按钮圆角大小 |
| leftButtonBackground(color: UIColor?)  | 左边按钮背景色 |
| leftButtonBorder(color: UIColor?)   | 左边按钮边框颜色 |
| leftButtonBorder(width: CGFloat)   | 左边按钮边框宽度 |
| leftButtonTapClosure(onTap: @escaping () -> Void)   | 左边按钮点击回调 |
| rightButton(title: String?)   | 右边按钮文字内容 |
| rightButton(color: UIColor?)   | 右边按钮文字颜色 |
| rightButton(font: UIFont?)   | 右边按钮文字字体大小 |
| rightButton(cornerRadius: CGFloat)   | 右边按钮圆角大小 |
| rightButtonBackground(color: UIColor?)   | 右边按钮背景色 |
| rightButtonBorder(color: UIColor?)   | 右边按钮边框颜色 |
| rightButtonBorder(width: CGFloat)   | 右边按钮边框宽度 |
| rightButtonTapClosure(onTap: @escaping (String?) -> Void)   | 右边按钮点击回调(带输入框内容) |
| rightButtonTapClosure(onTap: @escaping () -> Void)   | 右边按钮点击回调(不带输入框内容) |

### AUiButton
| API              | 描述             |
| ---------------- | ---------------- |
| backgroundColor | 背景色 |
| icon    | 按钮图标 |
| selectedIcon | 选中图标 |
| iconWidth | 按钮图标宽度 |
| iconHeight | 按钮图标高度 |
| buttonWitdth | 按钮宽度 |
| buttonHeight | 宽度高度 |
| titleFont | 按钮文字字体  |
| titleColor | 按钮文字颜色 |
| selectedTitleColor | 选中文字颜色 |
| cornerRadius | 按钮圆角 |
| textAlpha | 按钮文字透明度 |


### AUiSlider
| API              | 描述             |
| ---------------- | ---------------- |
| backgroundColor    | 背景色 |
| minimumTrackColor | 滑块左边部分颜色 |
| maximumTrackColor | 滑块右边部分颜色 |
| thumbColor | 滑块颜色 |
| thumbBorderColor | 滑块边框颜色 |
| trackBigLabelFont | 数值描述的字体(文字描述居于左右时) |
| trackSmallLabelFont | 数值描述的字体(文字描述居于底部时) |
| trackLabelColor | 数值描述颜色 |
| titleLabelFont | 标题字体 |
| titleLabelColor | 标题颜色 |

### AUiTableViewCell

| API              | 描述             |
| ---------------- | ---------------- |
| titleFont    | 主标题字体 |
| titleColor    | 主标题字体颜色 |
| subTitleFont    | 副标题字体 |
| subTitleColor | 副标题字体颜色 |
| detailFont    | 详情字体 |
| detailColor    | 详情字体颜色 |
| highlightColor    | 必填项星号 |
| badgeFont    | 角标字体 |
| badgeColor    | 角标文字颜色 |
| badgeBackgroundColor    | 角标背景色 |
| switchTintColor    | 开关背景色 |
| switchThumbColor    | 开关滑块颜色 |
| arrow    | 箭头图标 |

### AUiTabs

| API              | 描述             |
| ---------------- | ---------------- |
| titleFont    | 标签文字字体 |
| indicatorColor    | 选中标签指示条颜色 |
| titleMargin    | 标签间距 |
| titlePendingHorizontal    | 水平方向左右间距 |
| titlePendingVertical    | 垂直方向上下间距 |
| minimumWidth    | 标签最小宽度 |
| normalTitleColor    | 标签未选中时的文字颜色 |
| normalBorderColor    | 标签未选中时边框颜色 |
| selectedTitleColor    | 标签选中时的文字颜色 |
| selectedBorderColor    | 标签选中时边框颜色 |
| indicatorWidth    | 选中标签指示条宽度 |

### AUiTextField
| API              | 描述             |
| ---------------- | ---------------- |
| backgroundColor    | 背景色 |
| leftIconImage    | 输入框左部图片 |
| rightIconImage    | 输入框右部未选中时图片 |
| rightSelectedIconImage    | 输入框右部选中时图片 |
| placeHolder    | 输入占位符 |
| placeHolderColor    | 输入占位符颜色 |
| placeHolderFont    | 输入占位符字体 |
| text    | 输入内容 |
| textColor    | 输入文字颜色 |
| textFont    | 输入文字字体 |
| keyBoardType    | 键盘类型 |
| isSecureTextEntry    | 是否是密码输入框 |
| clearButtonMode    | 清除按钮（输入框内右侧小叉） |
| textAlignment    | 文字对齐方式 |
| returnKeyType    | 键盘返回类型 |
| cornerRadius    | 圆角大小 |
| topText    | 顶部文字内容 |
| topTextFont    | 顶部文字字体 |
| topTextColor    | 顶部文字字体颜色 |
| bottomText    | 底部文字内容 |
| bottomTextFont    | 底部文字字体 |
| bottomTextColor    | 底部文字颜色 |
| dividerColor    | 输入内容底部分割线颜色 |


### AUiToast

| API              | 描述             |
| ---------------- | ---------------- |
| text    | 文字内容 |
| textColor    | 文字颜色 |
| font    | 文字字体 |
| tagImage    | 图标 |
| postion | 显示位置 |
