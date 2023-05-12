# AUiKit-iOS

*English | [中文](README_zh.md)*


<!-- TOC START -->

- [AUiKit Basic Components](#auikit-basic-components)
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
    * [Main container configuration](#main- container-configuration)
    * [List Item Configuration](#list-item-configuration)
  
  
  <!-- TOC END -->

# AUiKit Basic Components
```
AUiKit  
├─ Service                              // Basic service components
│  ├─ AUiMicSeatServiceDelegate         // MicSeat management protocol
│  ├─ AUiMicSeatRespDelegate            // MicSeat management response protocol
│  ├─ AUiMicSeatServiceImpl             // MicSeat management implementation
│  ├─ AUiUserServiceDelegate            // User management protocol      
│  ├─ AUiUserRespDelegate               // User management response protocol
│  ├─ AUiUserServiceImpl                // User management implementation
│  ├─ AUiChorusServiceDelegate          // Chorus management protocol 
│  ├─ AUiChorusRespDelegate             // Chorus management response protocol 
│  ├─ AUiChorusServiceImpl              // Chorus management implementation
│  ├─ AUiMusicServiceDelegate           // Music management protocol      
│  ├─ AUiMusicRespDelegate              // User management response protocol      
│  ├─ AUiMusicServiceImpl               // User management implementation
│  ├─ AUiPlayerServiceDelegate          // Karaoke music player protocol      
│  ├─ AUiPlayerRespDelegate             // Karaoke music player response protocol      
│  ├─ AUiPlayerServiceImpl              // Karaoke music player implementation
│  ├─ AUiRoomManagerDelegate            // Room management protocol 
│  ├─ AUiRoomManagerRespDelegate        // Room management response protocol 
│  └─ AUiRoomManagerImpl                // Room management implementation
│
├─ Widget                               // Basic UI components without business
│  ├─ AUiActionSheet                    // Action sheet
│  ├─ AUiAlert                          // Alert
│  ├─ AUiButton                         // Button
│  ├─ AUiSegmented                      // Segmented
│  ├─ AUiSlider                         // Slider
│  ├─ AUiTableView                      // TableView
│  ├─ AUiTabs                           // Tabs
│  ├─ AUiTextField                      // TextField
│  └─ AUiToast                          // Toast
│
└─ Component                            // UI module containing specific business
   ├─ JukeBox                           // Jukebox component
   │  ├─ AUiJukeBoxView                 // Jukebox view，without business
   │  └─ AUiJukeBoxViewBinder           // View and Service binding class for Jukebox
   ├─ MicSeat                           // MicSeat component
   │  ├─ AUiMicSeatView                 // MicSeat view，without business
   │  └─ AUiMicSeatViewBinder           // View and Service binding class for MicSeat
   └─ Player                            // Karaoke player component
      ├─ AUiPlayerView                  // Player view，without business
      └─ AUiPlayerViewBinder            // View and Service binding class for Player
   
```
# API reference

## Service

### AUiMicSeatServiceDelegate 
#### MicSeat management protocol
| API                | Describe                              |
| ------------------ | --------------------------------- |
| bindRespDelegate   | Bind response delegate           |
| unbindRespDelegate | Unbind response delegate        |
| enterSeat          | Enter seat（Invoked by audience and owner）  |
| leaveSeat          | Leave seat（Invoked by owner）     |
| pickSeat           | Select a member to enter seat（Invoked by owner）|
| kickSeat           | Kick a member to leave seat（Invoked by owner）|
| muteAudioSeat      | Turn off/on the microphone for a seat（Invoked by owner） |
| muteVideoSeat      | Turn off/on the camera for a seat（Invoked by owner）   |
| closeSeat          | Disable/enable a seat（Invoked by owner）     |

### AUiMicSeatRespDelegate
#### MicSeat management response protocol
| API               | Describe                                |
| ----------------- | ----------------------------------- |
| onAnchorEnterSeat | Member did enter seat |
| onAnchorLeaveSeat | Member did leave seat |
| onSeatAudioMute   | Microphone did off/on for a seat        |
| onSeatVideoMute   | Camera did turn off/on for a seat     |
| onSeatClose       | The micSeat has been disabled/enable        |

### AUiUserServiceDelegate
#### User management protocol 
| API                | Describe                       |
| ------------------ | -------------------------- |
| bindRespDelegate   | Bind response delegate      |
| unbindRespDelegate | Unbind response delegate    |
| getUserInfoList    | Obtain user list  |
| muteUserAudio      | Disable/enable the microphone for oneself  |
| muteUserVideo      | Disable/enable the camera for oneself  |

### AUiUserRespDelegate
#### User management response protocol
| API                | Describe                               |
| ------------------ | ---------------------------------- |
| onRoomUserSnapshot | All user information obtained after the user enters the room |
| onRoomUserEnter    | Callback when the user enters the room |
| onRoomUserLeave    | Callback when the user leaves the room |
| onRoomUserUpdate   | Callback when user information is modified |
| onUserAudioMute    | Callback when the user turns off/on the microphone |
| onUserVideoMute    | Callback when the user turns off/on the camera |

### AUiChorusServiceDelegate
#### Chorus management protocol
| API                | Describe             |
| ------------------ | ---------------- |
| bindRespDelegate   | Bind response delegate |
| unbindRespDelegate | Unbind response delegate |
| getChoristersList  | Obtain chorus singer list |
| joinChorus         | join chorus         |
| leaveChorus        | leave chorus         |

### AUiChorusRespDelegate
#### Chorus management response protocol 
| API                 | Describe       |
| ------------------- | ---------- |
| onChoristerDidEnter | chorus singer did enter |
| onChoristerDidLeave | chorus singer did leave |

### AUiMusicServiceDelegate
#### Music management protocol
| API                  | Describe               |
| -------------------- | ------------------ |
| bindRespDelegate     | Bind Response Delegate  |
| unbindRespDelegate   | Unbind Response Delegate |
| getMusicList         | Obtain music list by charts       |
| searchMusic          | Obtain music list by key           |
| getAllChooseSongList | Obtain the current song request list |
| chooseSong           | choose a song|
| removeSong           | Remove a song  |
| pinSong              | Put the song at the top            |
| updatePlayStatus     | Update song playback status    |

### AUiMusicRespDelegate
#### Music management response protocol
| API                    | Describe                                    |
| ---------------------- | --------------------------------------- |
| onAddChooseSong        | Callback when adding a song     |
| onRemoveChooseSong     | Callback when deleting a song    |
| onUpdateChooseSong     | Callback when updating a song（For example, modifying the play status） |
| onUpdateAllChooseSongs | Callback when updating all songs（For example, pin song）             |

### AUiPlayerServiceDelegate
#### Karaoke music player protocol 

| API                            | Describe                               |
| ------------------------------ | ---------------------------------- |
| bindRespDelegate               | Bind Response Delegate            |
| unbindRespDelegate             | Unbind Response Delegate          |
| loadMusic                      | Load music                      |
| switchSingerRole               | Switch singer role              |
| startSing                      | Play music playback              |
| stopSing                       | Stop music playback             |
| resumeSing                     | Resume music playback             |
| pauseSing                      | Pause music playback           |
| seekSing                       | Adjust playback progress      |
| adjustMusicPlayerPlayoutVolume | Adjusting the sound for local music playback  |
| adjustRecordingSignalVolume    | Adjusts the recording volume |
| adjustMusicPlayerPublishVolume | Adjust publish signal volume |
| adjustPlaybackVolume           | Adjusts the playback volume  |
| selectMusicPlayerTrackMode     | Switch the audio track to original and accompaniment |
| getPlayerDuration              | Obtain playback duration         | 
| getMusicPlayer                 | Get player instance             |
| setAudioPitch                  | Set audio pitch                  |
| setAudioEffectPreset           | Set audio effect                 |
| setVoiceConversionPreset       | Set voice conversion             |
| enableEarMonitoring            | Enables in-ear monitoring        |

### AUiPlayerRespDelegate
#### Karaoke music player response protocol
| API                       | Describe             |
| ------------------------- | ---------------- |
| onPreludeDidAppear        | Prelude start loading |
| onPreludeDidDisappear     | Prelude end load |
| onPostludeDidAppear       | Postlude start loading  |
| onPostludeDidDisappear    | Postlude end load |
| onPlayerPositionDidChange | Callback when playback progress changes |
| onPlayerStateChanged      | Callback when playback status changes   |

### AUiRoomManagerDelegate
#### Room management protocol
| API                | Describe                 |
| ------------------ | -------------------- |
| bindRespDelegate   | Bind Response Delegate    |
| unbindRespDelegate | Unbind Response Delegate  |
| createRoom         | Create a room（Invoked by owner） |
| destroyRoom        | Destory the room（Invoked by owner） |
| enterRoom          | Join room（Invoked by audience） |
| exitRoom           | Exit the room（Invoked by audience） |
| getRoomInfoList    | Get room list         |

### AUiRoomManagerRespDelegate
#### Room management response protocol 
| API              | Describe             |
| ---------------- | ---------------- |
| onRoomDestroy    | Callback when the room is destroyed  |
| onRoomInfoChange | Callback when room information changes  |

## Widget
### AUiActionSheet
#### Main container configuration
| Attribute                 | Describe       |
| ------------------- | ---------- |
| collectionViewTopEdge | The distance between the displayed list content and the space above |
| itemType | List style, sliding up and down or left and right |
| itemHeight | List item height |
| titleLabelFont | Main title font |
| titleLabelTextColor | Main title font color |
| nameLabelFont | User main title font |
| nameLabelTextColor | User main title font |
| seatLabelFont | User subtitle font |
| seatLabelTextColor | User subtitle font color |
| avatarWidth | Avatar image width |
| avatarHeight | Avatar image height |

#### List Item Configuration
| Attribute                 | Describe       |
| ------------------- | ---------- |
| icon | Item icon |
| backgroundIcon | Item background icon |
| titleColor | Title color |
| imageWidth | Image width |
| imageHeight | Image height |
| backgroundImageWidth | Background image width |
| backgroundImageHeight | Background image height |
| padding | padding between text and image |
| selectedBorderColor | Selected border color |
| selectedBorderWidth | Selected border color |
| selectedBorderRadius | Selected border radius |


### Alert


### Button


### Segmented

### Slider

### TableView

### Tabs

### TextField

### Toast