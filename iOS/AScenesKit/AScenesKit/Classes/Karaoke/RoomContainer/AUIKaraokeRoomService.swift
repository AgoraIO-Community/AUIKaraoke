//
//  AUIKaraokeRoomService.swift
//  AScenesKit
//
//  Created by wushengtao on 2023/2/23.
//

import Foundation
import AgoraRtcKit
import AUIKitCore
import AVFAudio
import AgoraRtmKit

/// 卡拉OK房间Service，内部负责初始化房间内需要的Service组件，包括房间Service，邀请Service，麦位Service，...
open class AUIKaraokeRoomService: NSObject {
    lazy var micSeatImpl: AUIMicSeatServiceDelegate = AUIMicSeatLocalServiceImpl(channelName: channelName,
                                                                            rtmManager: rtmManager,
                                                                            roomManager: roomManagerImpl)
//    lazy var invitationImpl: AUIInvitationServiceDelegate = AUIInvitationServiceImpl(channelName: self.channelName,
//                                                                                     rtmManager: self.rtmManager)
    lazy var musicImpl: AUIMusicServiceDelegate = AUIMusicLocalServiceImpl(channelName: channelName,
                                                                           rtmManager: rtmManager,
                                                                           ktvApi: ktvApi)
    lazy var playerImpl: AUIPlayerServiceDelegate = AUIPlayerServiceImpl(channelName: channelName,
                                                                         rtcKit: rtcEngine,
                                                                         ktvApi: ktvApi,
                                                                         rtmManager: rtmManager)
    lazy var userImpl: AUIUserServiceDelegate = AUIUserLocalServiceImpl(channelName: channelName,
                                                                        rtmManager: rtmManager,
                                                                        roomManager: roomManagerImpl)
    lazy var chorusImpl: AUIChorusServiceDelegate = AUIChorusServiceImpl(channelName: channelName,
                                                                         rtcKit: rtcEngine,
                                                                         ktvApi: ktvApi,
                                                                         rtmManager: rtmManager)
    
    lazy var chatImplement: AUIMManagerServiceDelegate = AUIIMManagerServiceImplement(channelName: channelName,
                                                                                      rtmManager: rtmManager)
    
    lazy var giftImplement: AUIGiftServiceImplement = AUIGiftServiceImplement(channelName: channelName,
                                                                              rtmManager: rtmManager)
    
    
    var roomManagerImpl: AUIRoomLocalManagerImpl!
    private(set) var channelName: String!
    private var roomConfig: AUIRoomConfig!
    private(set) var rtcEngine: AgoraRtcEngineKit!
    private var rtmManager: AUIRtmManager!
    private var ktvApi: KTVApiDelegate!
    private var rtcEngineCreateBySercice = false
    private var ktvApiCreateBySercice = false
    
    private var rtcJoinClousure: ((Error?)->())?
    
    deinit {
        aui_info("deinit AUIKaraokeRoomService", tag: "AUIKaraokeRoomService")
    }
    
    public init(rtcEngine: AgoraRtcEngineKit?,
                ktvApi: KTVApiDelegate?,
                roomManager: AUIRoomLocalManagerImpl,
                roomConfig: AUIRoomConfig,
                roomInfo: AUIRoomInfo) {
        aui_info("init AUIKaraokeRoomService", tag: "AUIKaraokeRoomService")
        super.init()
        self.channelName = roomInfo.roomId
        self.roomConfig = roomConfig
        if let rtcEngine = rtcEngine {
            self.rtcEngine = rtcEngine
        } else {
            self.rtcEngine = self._createRtcEngine(commonConfig: roomManager.commonConfig)
            rtcEngineCreateBySercice = true
        }
        self.roomManagerImpl = roomManager
        self.rtmManager = roomManager.rtmManager
        self.userImpl.bindRespDelegate(delegate: self)
        if let ktvApi = ktvApi {
            self.ktvApi = ktvApi
        } else {
            let userId = Int(roomManager.commonConfig.userId) ?? 0
            let config = KTVApiConfig(appId:  AUIRoomContext.shared.appId,
                                      rtmToken: roomConfig.rtcRtmToken,
                                      engine: self.rtcEngine,
                                      channelName: roomConfig.rtcChannelName,
                                      localUid: userId,
                                      chorusChannelName: roomConfig.rtcChorusChannelName,
                                      chorusChannelToken: roomConfig.rtcChorusRtcToken,
                                      type: .normal,
                                      maxCacheSize: 10)
            self.ktvApi = KTVApiImpl.init(config: config)
            self.ktvApi.renewInnerDataStreamId()
            
            ktvApiCreateBySercice = true
        }
        
        AUIRoomContext.shared.roomConfigMap[channelName] = roomConfig
        AUIRoomContext.shared.roomInfoMap[channelName] = roomInfo
        AUIRoomContext.shared.roomInteractionHandlerMap[channelName] = AUIServiceInteractionHandler(channelName: channelName, rtmManager: rtmManager, userInfo: AUIRoomContext.shared.currentUserInfo)
    }
    
    //token过期之后调用该方法更新所有token
    public func renew(config: AUIRoomConfig) {
        roomConfig = config
        AUIRoomContext.shared.roomConfigMap[channelName] = roomConfig
        
        //ktvapi renew
        ktvApi.renewToken(rtmToken: roomConfig.rtcRtmToken, chorusChannelRtcToken: roomConfig.rtcChorusRtcToken)
        
        //rtm renew
        rtmManager.renew(token: roomConfig.rtmToken007)
        rtmManager.renewChannel(channelName: channelName, token: roomConfig.rtcToken007)
        
        //rtc renew
        rtcEngine.renewToken(roomConfig.rtcRtcToken)
    }
    
    func joinRtcChannel(completion: ((Error?)->())?) {
        guard let commonConfig = AUIRoomContext.shared.commonConfig,
              let uid = UInt(commonConfig.userId) else {
            aui_error("joinRtcChannel fail, commonConfig is empty", tag: "AUIKaraokeRoomService")
            completion?(nil)
            return
        }
        
        setEngineConfig(with: uid)
        
        let ret =
        self.rtcEngine.joinChannel(byToken: roomConfig.rtcRtcToken,
                                   channelId: roomConfig.rtcChannelName,
                                   uid: uid,
                                   mediaOptions: channelMediaOptions())
#if DEBUG
        aui_info("joinChannel channelName ret: \(ret) channelName:\(roomConfig.rtcChannelName), uid: \(uid) token: \(roomConfig.rtcRtcToken)", tag: "AUIKaraokeRoomService")
#endif
        
        if ret != 0 {
            completion?(AUICommonError.rtcError(ret).toNSError())
            return
        }
        rtcJoinClousure = completion
    }
    
    func leaveRtcChannel() {
        self.rtcEngine.leaveChannel()
        if rtcEngineCreateBySercice {
            AgoraRtcEngineKit.destroy()
        }
        aui_error("leaveRtcChannel", tag: "AUIKaraokeRoomService")
    }
    
    func destroy() {
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
        let isRoomOwner = AUIRoomContext.shared.isRoomOwner(channelName: channelName)
        let option = AgoraRtcChannelMediaOptions()
        option.clientRoleType = isRoomOwner ? .broadcaster : .audience
        option.publishCameraTrack = false
        option.publishMicrophoneTrack = option.clientRoleType == .broadcaster ? true : false
        option.publishCustomAudioTrack = false
        option.autoSubscribeAudio = true
        option.autoSubscribeVideo = true
        option.publishMediaPlayerId = Int(self.ktvApi.getMediaPlayer()?.getMediaPlayerId() ?? 0)
        option.enableAudioRecordingOrPlayout = true
        
        aui_info("update clientRoleType: \(option.clientRoleType.rawValue)", tag: "AUIKaraokeRoomService")
        return option
    }
}

//private method
extension AUIKaraokeRoomService {
    private func _rtcEngineConfig(commonConfig: AUICommonConfig) -> AgoraRtcEngineConfig {
       let config = AgoraRtcEngineConfig()
        config.appId = AUIRoomContext.shared.appId
        config.channelProfile = .liveBroadcasting
        config.audioScenario = .gameStreaming
        config.areaCode = .global
        
        if config.appId?.count ?? 0 == 0 {
            aui_error("config.appId is empty, please check 'AUIRoomContext.shared.commonConfig.appId'", tag: "AUIKaraokeRoomService")
            assert(false, "config.appId is empty, please check 'AUIRoomContext.shared.commonConfig.appId'")
        }
        return config
    }
    
    private func _createRtcEngine(commonConfig: AUICommonConfig) ->AgoraRtcEngineKit {
        let engine = AgoraRtcEngineKit.sharedEngine(with: _rtcEngineConfig(commonConfig: commonConfig),
                                                    delegate: self)
        engine.delegate = self
        return engine
    }
}

extension AUIKaraokeRoomService: AgoraRtcEngineDelegate {
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        aui_info("didJoinChannel channel:\(channel) uid: \(uid)", tag: "AUIKaraokeRoomService")
        
        guard uid == UInt(AUIRoomContext.shared.currentUserInfo.userId) else {
            return
        }
        
        aui_info("joinChannel  channelName success channelName:\(channel), uid: \(uid)", tag: "AUIKaraokeRoomService")
//        self.rtcEngine.setAudioSessionOperationRestriction(.deactivateSession)
        rtcJoinClousure?(nil)
        rtcJoinClousure = nil
    }
   
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        aui_error("didOccurError: \(errorCode.rawValue)", tag: "AUIKaraokeRoomService")
        rtcJoinClousure?(AUICommonError.rtcError(Int32(errorCode.rawValue)).toNSError())
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

extension AUIKaraokeRoomService: AgoraRtmClientDelegate {
    
}

extension AUIKaraokeRoomService: AUIUserRespDelegate {
    public func onUserBeKicked(roomId: String, userId: String) {
        
    }
    
    public func onRoomUserSnapshot(roomId: String, userList: [AUIUserInfo]) {
        guard let user = userList.filter({$0.userId == AUIRoomContext.shared.currentUserInfo.userId }).first else {return}
        aui_info("onRoomUserSnapshot", tag: "AUIKaraokeRoomService")
        onUserAudioMute(userId: user.userId, mute: user.muteAudio)
        onUserVideoMute(userId: user.userId, mute: user.muteVideo)
    }
    
    public func onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        
    }
    
    public func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
        
    }
    
    public func onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        
    }
    
    public func onUserAudioMute(userId: String, mute: Bool) {
        guard userId == AUIRoomContext.shared.currentUserInfo.userId else {return}
        aui_info("onUserAudioMute mute current user: \(mute)", tag: "AUIKaraokeRoomService")
        rtcEngine.adjustRecordingSignalVolume(mute ? 0 : 100)
    }
    
    public func onUserVideoMute(userId: String, mute: Bool) {
        guard userId == AUIRoomContext.shared.currentUserInfo.userId else {return}
        aui_info("onMuteVideo onUserVideoMute [\(userId)]: \(mute)", tag: "AUIKaraokeRoomService")
        rtcEngine.enableLocalVideo(!mute)
        let option = AgoraRtcChannelMediaOptions()
        option.publishCameraTrack = !mute
        rtcEngine.updateChannel(with: option)
    }
}
