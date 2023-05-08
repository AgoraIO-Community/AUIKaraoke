//
//  AUiKaraokeRoomService.swift
//  AScenesKit
//
//  Created by wushengtao on 2023/2/23.
//

import Foundation
import AgoraRtcKit
import AUiKit
import AVFAudio

/// 卡拉OK房间Service，内部负责初始化房间内需要的Service组件，包括房间Service，邀请Service，麦位Service，...
open class AUiKaraokeRoomService: NSObject {
    lazy var micSeatImpl: AUiMicSeatServiceDelegate = AUiMicSeatServiceImpl(channelName: channelName,
                                                                            rtmManager: rtmManager,
                                                                            roomManager: roomManagerImpl)
//    lazy var invitationImpl: AUiInvitationServiceDelegate = AUiInvitationServiceImpl(channelName: self.channelName,
//                                                                                     rtmManager: self.rtmManager)
    lazy var musicImpl: AUiMusicServiceDelegate = AUiMusicServiceImpl(channelName: channelName,
                                                                      rtmManager: rtmManager,
                                                                      ktvApi: ktvApi)
    lazy var playerImpl: AUiPlayerServiceDelegate = AUiPlayerServiceImpl(channelName: channelName,
                                                                         rtcKit: rtcEngine,
                                                                         ktvApi: ktvApi,
                                                                         rtmManager: rtmManager)
    lazy var userImpl: AUiUserServiceDelegate = AUiUserServiceImpl(channelName: channelName,
                                                                   rtmManager: rtmManager,
                                                                   roomManager: roomManagerImpl)
    lazy var chorusImpl: AUiChorusServiceDelegate = AUiChorusServiceImpl(channelName: channelName,
                                                                         rtcKit: rtcEngine,
                                                                         ktvApi: ktvApi,
                                                                         rtmManager: rtmManager)
    var roomManagerImpl: AUiRoomManagerImpl!
    private(set) var channelName: String!
    private var roomConfig: AUiRoomConfig!
    private(set) var rtcEngine: AgoraRtcEngineKit!
    private var rtmManager: AUiRtmManager!
    private var ktvApi: KTVApiDelegate!
    
    private var rtcJoinClousure: ((Error?)->())?
    
    deinit {
        aui_info("deinit AUiKaraokeRoomService", tag: "AUiKaraokeRoomService")
    }
    
    public init(rtcEngine: AgoraRtcEngineKit?,
                roomManager: AUiRoomManagerImpl,
                roomConfig: AUiRoomConfig,
                roomInfo: AUiRoomInfo) {
        aui_info("init AUiKaraokeRoomService", tag: "AUiKaraokeRoomService")
        super.init()
        self.channelName = roomInfo.roomId
        self.roomConfig = roomConfig
        self.rtcEngine = rtcEngine ?? self._createRtcEngine(commonConfig: roomManager.commonConfig)
        self.roomManagerImpl = roomManager
        self.rtmManager = roomManager.rtmManager
        self.userImpl.bindRespDelegate(delegate: self)
        let userId = Int(roomManager.commonConfig.userId) ?? 0
        let config = KTVApiConfig(appId: roomManager.commonConfig.appId,
                                  rtmToken: roomConfig.rtcRtmToken006,
                                  engine: self.rtcEngine,
                                  channelName: roomConfig.rtcChannelName,
                                  localUid: userId,
                                  chorusChannelName: roomConfig.rtcChorusChannelName,
                                  chorusChannelToken: roomConfig.rtcChorusRtcToken007)
        ktvApi = KTVApiImpl.init(config: config)
        
        AUiRoomContext.shared.roomConfigMap[channelName] = roomConfig
        AUiRoomContext.shared.roomInfoMap[channelName] = roomInfo
    }
    
    //token过期之后调用该方法更新所有token
    public func renew(config: AUiRoomConfig) {
        roomConfig = config
        AUiRoomContext.shared.roomConfigMap[channelName] = roomConfig
        
        //ktvapi renew
        ktvApi.getMusicContentCenter()?.renewToken(roomConfig.rtcRtmToken006)
        //TODO: 2nd chorus token renew
        
        //rtm renew
        rtmManager.renew(token: roomConfig.rtmToken007)
        rtmManager.renewChannel(channelName: channelName, token: roomConfig.rtcToken007)
        
        //rtc renew
        rtcEngine.renewToken(roomConfig.rtcRtcToken006)
    }
    
    func joinRtcChannel(completion: ((Error?)->())?) {
        guard let commonConfig = AUiRoomContext.shared.commonConfig,
              let uid = UInt(commonConfig.userId) else {
            aui_error("joinRtcChannel fail, commonConfig is empty", tag: "AUiKaraokeRoomService")
            completion?(nil)
            return
        }
        
        setEngineConfig(with: uid)
        
        let ret =
        self.rtcEngine.joinChannel(byToken: roomConfig.rtcRtcToken006,
                                   channelId: roomConfig.rtcChannelName,
                                   uid: uid,
                                   mediaOptions: channelMediaOptions())
#if DEBUG
        aui_info("joinChannel channelName ret: \(ret) channelName:\(roomConfig.rtcChannelName), uid: \(uid) token: \(roomConfig.rtcRtcToken006)", tag: "AUiKaraokeRoomService")
#endif
        
        if ret != 0 {
            completion?(AUiCommonError.rtcError(ret).toNSError())
            return
        }
        rtcJoinClousure = completion
    }
    
    func leaveRtcChannel() {
        self.rtcEngine.leaveChannel()
        AgoraRtcEngineKit.destroy()
        aui_error("leaveRtcChannel", tag: "AUiKaraokeRoomService")
    }
    
    func destory() {
        ktvApi.cleanCache()
        ktvApi = nil
        leaveRtcChannel()
    }
    
    private func setEngineConfig(with uid:UInt) {
        //todo 因为sdk的问题需要在加入频道前修改audioSession权限 退出频道去掉这个
        rtcEngine.setAudioSessionOperationRestriction(.all)
        try? AVAudioSession.sharedInstance().setCategory(.playAndRecord, options: [.defaultToSpeaker,.mixWithOthers,.allowBluetoothA2DP])
        
        rtcEngine.setDefaultAudioRouteToSpeakerphone(true)
        rtcEngine.enableLocalAudio(true)
//        rtcEngine.setAudioScenario(.gameStreaming)
        rtcEngine.setAudioProfile(.musicHighQuality)
//        rtcEngine.setChannelProfile(.liveBroadcasting)
        rtcEngine.setParameters("{\"rtc.enable_nasa2\": false}")
        rtcEngine.setParameters("{\"rtc.ntp_delay_drop_threshold\": 1000}")
        rtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp\": true}")
        rtcEngine.setParameters("{\"rtc.net.maxS2LDelay\": 800}")
        rtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\": true}")
        rtcEngine.setParameters("{\"rtc.net.maxS2LDelayBroadcast\": 400}")
        rtcEngine.setParameters("{\"che.audio.neteq.prebuffer\": true}")
        rtcEngine.setParameters("{\"che.audio.neteq.prebuffer_max_delay\": 600}")
        rtcEngine.setParameters("{\"che.audio.max_mixed_participants\": 8}")
        rtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast_dynamic\": true}")
        rtcEngine.setParameters("{\"che.audio.custom_bitrate\": 48000}")
        
        //开启唱歌评分功能
        rtcEngine.enableAudioVolumeIndication(50, smooth: 3, reportVad: true)
         rtcEngine.enableAudio()
        rtcEngine.enableVideo()
        
        let encoderConfiguration = AgoraVideoEncoderConfiguration(size: CGSize(width: 100, height: 100), frameRate: .fps7, bitrate: 20, orientationMode: .fixedLandscape, mirrorMode: .auto)
        rtcEngine.setVideoEncoderConfiguration(encoderConfiguration)
    }
    
    private func channelMediaOptions() -> AgoraRtcChannelMediaOptions {
        let isRoomOwner = AUiRoomContext.shared.isRoomOwner(channelName: channelName)
        let option = AgoraRtcChannelMediaOptions()
        option.clientRoleType = isRoomOwner ? .broadcaster : .audience
        option.publishCameraTrack = false
        option.publishMicrophoneTrack = option.clientRoleType == .broadcaster ? true : false
        option.publishCustomAudioTrack = false
        option.autoSubscribeAudio = true
        option.autoSubscribeVideo = true
        option.publishMediaPlayerId = Int(self.ktvApi.getMediaPlayer()?.getMediaPlayerId() ?? 0)
        option.enableAudioRecordingOrPlayout = true
        
        aui_info("update clientRoleType: \(option.clientRoleType.rawValue)", tag: "AUiKaraokeRoomService")
        return option
    }
}

//private method
extension AUiKaraokeRoomService {
    private func _rtcEngineConfig(commonConfig: AUiCommonConfig) -> AgoraRtcEngineConfig {
       let config = AgoraRtcEngineConfig()
        config.appId = commonConfig.appId
        config.channelProfile = .liveBroadcasting
        config.audioScenario = .gameStreaming
        config.areaCode = .global
        return config
    }
    
    private func _createRtcEngine(commonConfig: AUiCommonConfig) ->AgoraRtcEngineKit {
        let engine = AgoraRtcEngineKit.sharedEngine(with: _rtcEngineConfig(commonConfig: commonConfig),
                                                    delegate: self)
        engine.delegate = self
        return engine
    }
    
    private func _createRtmClient(commonConfig: AUiCommonConfig) ->AgoraRtmClientKit {
        let rtmConfig = AgoraRtmClientConfig()
        rtmConfig.userId = commonConfig.userId
        rtmConfig.appId = commonConfig.appId;
        
        let rtmClient = AgoraRtmClientKit(config: rtmConfig, delegate: self)!
//        rtmClient.destroy()
        return rtmClient
    }
}

extension AUiKaraokeRoomService: AgoraRtcEngineDelegate {
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        aui_info("didJoinChannel channel:\(channel) uid: \(uid)", tag: "AUiKaraokeRoomService")
        
        guard uid == UInt(AUiRoomContext.shared.currentUserInfo.userId) else {
            return
        }
        
        aui_info("joinChannel  channelName success channelName:\(channel), uid: \(uid)", tag: "AUiKaraokeRoomService")
//        self.rtcEngine.setAudioSessionOperationRestriction(.deactivateSession)
        rtcJoinClousure?(nil)
        rtcJoinClousure = nil
    }
   
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        aui_error("didOccurError: \(errorCode.rawValue)", tag: "AUiKaraokeRoomService")
        rtcJoinClousure?(AUiCommonError.rtcError(Int32(errorCode.rawValue)).toNSError())
        rtcJoinClousure = nil
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, receiveStreamMessageFromUid uid: UInt, streamId: Int, data: Data) {
        playerImpl.didKTVAPIReceiveStreamMessageFrom(uid: NSInteger(uid), streamId: streamId, data: data)
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, localAudioStats stats: AgoraRtcLocalAudioStats) {
        playerImpl.didKTVAPILocalAudioStats(stats: stats)
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, reportAudioVolumeIndicationOfSpeakers speakers: [AgoraRtcAudioVolumeInfo], totalVolume: Int) {
        playerImpl.didKTVAPIReceiveAudioVolumeIndication(with: speakers, totalVolume: totalVolume)
    }
}

extension AUiKaraokeRoomService: AgoraRtmClientDelegate {
    
}

extension AUiKaraokeRoomService: AUiUserRespDelegate {
    
    public func onRoomUserSnapshot(roomId: String, userList: [AUiUserInfo]) {
        guard let user = userList.filter({$0.userId == AUiRoomContext.shared.currentUserInfo.userId }).first else {return}
        aui_info("onRoomUserSnapshot", tag: "AUiKaraokeRoomService")
        onUserAudioMute(userId: user.userId, mute: user.muteAudio)
        onUserVideoMute(userId: user.userId, mute: user.muteVideo)
    }
    
    public func onRoomUserEnter(roomId: String, userInfo: AUiUserInfo) {
        
    }
    
    public func onRoomUserLeave(roomId: String, userInfo: AUiUserInfo) {
        
    }
    
    public func onRoomUserUpdate(roomId: String, userInfo: AUiUserInfo) {
        
    }
    
    public func onUserAudioMute(userId: String, mute: Bool) {
        guard userId == AUiRoomContext.shared.currentUserInfo.userId else {return}
        aui_info("onUserAudioMute mute current user: \(mute)", tag: "AUiKaraokeRoomService")
        rtcEngine.adjustRecordingSignalVolume(mute ? 0 : 100)
    }
    
    public func onUserVideoMute(userId: String, mute: Bool) {
        guard userId == AUiRoomContext.shared.currentUserInfo.userId else {return}
        aui_info("onMuteVideo onUserVideoMute [\(userId)]: \(mute)", tag: "AUiKaraokeRoomService")
        rtcEngine.enableLocalVideo(!mute)
        let option = AgoraRtcChannelMediaOptions()
        option.publishCameraTrack = !mute
        rtcEngine.updateChannel(with: option)
    }
}
