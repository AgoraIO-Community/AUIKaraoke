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
                                                                                 rtmManager: rtmManager)
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
                                                                        rtmManager: rtmManager)
    lazy var chorusImpl: AUIChorusServiceDelegate = AUIChorusLocalServiceImpl(channelName: channelName,
                                                                              rtcKit: rtcEngine,
                                                                              ktvApi: ktvApi,
                                                                              rtmManager: rtmManager)
    
    lazy var chatImplement: AUIMManagerServiceDelegate = AUIIMManagerServiceImplement(channelName: channelName,
                                                                                      rtmManager: rtmManager)
    
    lazy var giftImplement: AUIGiftServiceImplement = AUIGiftServiceImplement(channelName: channelName,
                                                                              rtmManager: rtmManager)
    
    
    private(set) var channelName: String!
    private var roomConfig: AUIRoomConfig!
    private(set) var rtcEngine: AgoraRtcEngineKit!
    private var ktvApi: KTVApiDelegate!
    private var rtcEngineCreateBySercice = false
    private var ktvApiCreateBySercice = false
    
    private var rtmClient: AgoraRtmClientKit!
    public private(set) lazy var rtmManager: AUIRtmManager = {
        return AUIRtmManager(rtmClient: self.rtmClient, rtmChannelType: .stream, isExternalLogin: !rtmClientCreateBySercice)
    }()
    private var rtmClientCreateBySercice = false
    
    private var respDelegates: NSHashTable<AUIRoomManagerRespDelegate> = NSHashTable<AUIRoomManagerRespDelegate>.weakObjects()
    
    private var rtcJoinClousure: ((Error?)->())?
    
    deinit {
        aui_info("deinit AUIKaraokeRoomService", tag: "AUIKaraokeRoomService")
    }
    
    public init(rtcEngine: AgoraRtcEngineKit?,
                ktvApi: KTVApiDelegate?,
                rtmClient: AgoraRtmClientKit?,
                commonConfig: AUICommonConfig,
                roomConfig: AUIRoomConfig,
                roomId: String) {
        aui_info("init AUIKaraokeRoomService", tag: "AUIKaraokeRoomService")
        super.init()
        self.channelName = roomId
        self.roomConfig = roomConfig
        if let rtcEngine = rtcEngine {
            self.rtcEngine = rtcEngine
        } else {
            self.rtcEngine = self._createRtcEngine(commonConfig: commonConfig)
            rtcEngineCreateBySercice = true
        }
        
        if let rtmClient = rtmClient {
            self.rtmClient = rtmClient
        } else {
            rtmClientCreateBySercice = true
            self.rtmClient = createRtmClient()
        }
        
        self.userImpl.bindRespDelegate(delegate: self)
        if let ktvApi = ktvApi {
            self.ktvApi = ktvApi
        } else {
            let appId = AUIRoomContext.shared.commonConfig?.appId ?? ""
            let userId = Int(AUIRoomContext.shared.commonConfig?.userId ?? "") ?? 0
            let config = KTVApiConfig(appId: appId,
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
        AUIRoomContext.shared.roomArbiterMap[channelName] = AUIArbiter(channelName: channelName, rtmManager: rtmManager, userInfo: AUIRoomContext.shared.currentUserInfo)
    }
    
    //token过期之后调用该方法更新所有token
    public func renew(config: AUIRoomConfig) {
        roomConfig = config
        AUIRoomContext.shared.roomConfigMap[channelName] = roomConfig
        
        //ktvapi renew
        ktvApi.renewToken(rtmToken: roomConfig.rtcRtmToken, chorusChannelRtcToken: roomConfig.rtcChorusRtcToken)
        
        //rtm renew
        rtmManager.renew(token: config.rtmToken007)
        rtmManager.renewChannel(channelName: channelName, token: roomConfig.rtcToken007)
        
        //rtc renew
        rtcEngine.renewToken(roomConfig.rtcRtcToken)
    }
    
    private func joinRtcChannel(completion: ((Error?)->())?) {
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
    
    private func leaveRtcChannel() {
        self.rtcEngine.leaveChannel()
        if rtcEngineCreateBySercice {
            AgoraRtcEngineKit.destroy()
        }
        aui_error("leaveRtcChannel", tag: "AUIKaraokeRoomService")
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
        config.appId = commonConfig.appId
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
    
    private func createRtmClient() -> AgoraRtmClientKit {
        let commonConfig = AUIRoomContext.shared.commonConfig!
        let rtmConfig = AgoraRtmClientConfig(appId: commonConfig.appId, userId: commonConfig.userId)
//        let log = AgoraRtmLogConfig()
//        log.filePath = NSHomeDirectory() + "/Documents/RTMLog/"
//        rtmConfig.logConfig = log
        if rtmConfig.userId.count == 0 {
            aui_error("userId is empty")
            assert(false, "userId is empty")
        }
        if rtmConfig.appId.count == 0 {
            aui_error("appId is empty, please check 'AUIRoomContext.shared.commonConfig.appId' ")
            assert(false, "appId is empty, please check 'AUIRoomContext.shared.commonConfig.appId' ")
        }
        rtmConfig.presenceTimeout = 200
        let rtmClient = try? AgoraRtmClientKit(rtmConfig, delegate: nil)
        return rtmClient!
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
        guard AUIRoomContext.shared.isRoomOwner(channelName: roomId, userId: userInfo.userId) else {
            cleanUserInfo(channelName: roomId, userId: userInfo.userId)
            return
        }
        cleanRoomInfo(channelName: roomId)
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

// room manager handler
extension AUIKaraokeRoomService {
    private func cleanUserInfo(channelName: String, userId: String) {
        guard AUIRoomContext.shared.getArbiter(channelName: channelName)?.isArbiter() ?? false else {return}
        guard let idx = micSeatImpl.getMicSeatIndex?(userId: userId), idx >= 0 else {return}
        micSeatImpl.kickSeat(seatIndex: idx) { err in
        }
    }
    
    private func cleanRoomInfo(channelName: String) {
        guard AUIRoomContext.shared.getArbiter(channelName: channelName)?.isArbiter() ?? false else {return}
        let removeKeys = NSMutableArray()
        removeKeys.add(kRoomInfoAttrKry)
        //TODO: remove im key
        removeKeys.add("chatRoom")
        _ = micSeatImpl.onRoomWillDestroy?(removeKeys: removeKeys)
        _ = musicImpl.onRoomWillDestroy?(removeKeys: removeKeys)
        _ = chorusImpl.onRoomWillDestroy?(removeKeys: removeKeys)
        rtmManager.cleanMetadata(channelName: channelName,
                                 removeKeys: removeKeys as! [String],
                                 lockName: kRTM_Referee_LockName) { err in
        }
    }
    
    public func createRoom(roomInfo: AUIRoomInfo, completion:@escaping (Error?)->()) {
        guard let rtmToken = AUIRoomContext.shared.roomConfigMap[roomInfo.roomId]?.rtmToken007 else {
            assert(false)
            return
        }
        guard rtmManager.isLogin else {
            rtmManager.login(token: rtmToken) {[weak self] err in
                if let err = err {
                    completion(err as NSError)
                    return
                }
                self?.createRoom(roomInfo: roomInfo, completion: completion)
            }
            return
        }
        
        guard let roomInfoStr = roomInfo.yy_modelToJSONString() else {
            assert(false)
            completion(nil)
            return
        }
        
        let metaData = NSMutableDictionary()
        metaData[kRoomInfoAttrKry] = roomInfoStr
        _ = micSeatImpl.onRoomWillInit?(metaData: metaData)
        _ = musicImpl.onRoomWillInit?(metaData: metaData)
        _ = chorusImpl.onRoomWillInit?(metaData: metaData)
        
        let channelName = roomInfo.roomId
        rtmManager.setMetadata(channelName: channelName,
                               lockName: "",
                               metadata: metaData as! [String : String]) { err in
            if let err = err {
                completion(err)
                return
            }
            AUIRoomContext.shared.roomInfoMap[channelName] = roomInfo
            completion(nil)
        }
        
        AUIRoomContext.shared.getArbiter(channelName: channelName)?.create()
    }
    
    public func enterRoom(completion:@escaping (Error?)->()) {
        guard let rtmToken = AUIRoomContext.shared.roomConfigMap[channelName]?.rtmToken007 else {
            assert(false)
            return
        }
        guard rtmManager.isLogin else {
            rtmManager.login(token: rtmToken) {[weak self] err in
                if let err = err {
                    completion(err as NSError)
                    return
                }
                self?.enterRoom(completion: completion)
            }
            return
        }
        
        let roomId = channelName!
        guard let roomConfig = AUIRoomContext.shared.roomConfigMap[roomId] else {
            assert(false)
            aui_info("enterRoom: \(roomId) fail", tag: "AUIKaraokeRoomService")
            completion(AUICommonError.missmatchRoomConfig.toNSError())
            return
        }
        assert(!rtmToken.isEmpty, "rtm token invalid")
        guard rtmManager.isLogin else {
            rtmManager.login(token: rtmToken) {[weak self] err in
                if let err = err {
                    completion(err as NSError)
                    return
                }
                self?.enterRoom(completion: completion)
            }

            return
        }
        
        aui_info("enterRoom subscribe: \(roomId)", tag: "AUIKaraokeRoomService")
        rtmManager.subscribe(channelName: roomId, rtcToken: roomConfig.rtcToken007) { error in
            aui_info("enterRoom subscribe finished \(roomId) \(error?.localizedDescription ?? "")", tag: "AUIRoomManagerImpl")
            completion(error as? NSError)
        }
        AUIRoomContext.shared.getArbiter(channelName: channelName)?.acquire()
        rtmManager.subscribeAttributes(channelName: channelName, itemKey: kRoomInfoAttrKry, delegate: self)
        rtmManager.subscribeError(channelName: roomId, delegate: self)
        
        //join rtc
        joinRtcChannel { error in
            aui_info("joinRtcChannel finished: \(error?.localizedDescription ?? "success")", tag: "AUIKaraokeRoomService")
        }
    }
    
    public func exitRoom(callback: @escaping (NSError?) -> ()) {
        let roomId = channelName!
        aui_info("exitRoom: \(roomId)", tag: "AUIKaraokeRoomService")
        cleanUserInfo(channelName: roomId, userId: AUIRoomContext.shared.currentUserInfo.userId)
        cleanRoom()
        callback(nil)
    }
    
    public func destroyRoom(callback: @escaping (NSError?) -> ()) {
        let roomId = channelName!
        aui_info("destroyRoom: \(roomId)", tag: "AUIKaraokeRoomService")
        cleanRoomInfo(channelName: roomId)

        cleanRoom()
        
        AUIRoomContext.shared.getArbiter(channelName: channelName)?.destroy()
    }
    
    private func cleanRoom() {
        let roomId = channelName!
        self.rtmManager.unSubscribe(channelName: roomId)
        
        rtmManager.unsubscribeAttributes(channelName: channelName, itemKey: kRoomInfoAttrKry, delegate: self)
        
        rtmManager.unsubscribeError(channelName: roomId, delegate: self)
        
        rtmManager.logout()
        
        ktvApi.cleanCache()
        leaveRtcChannel()
    }
}

extension AUIKaraokeRoomService: AUIRtmAttributesProxyDelegate {
    public func onAttributesDidChanged(channelName: String, key: String, value: Any) {
        guard key == kRoomInfoAttrKry, let roomInfo = AUIRoomInfo.yy_model(withJSON: value) else {return}
        
        AUIRoomContext.shared.roomInfoMap[channelName] = roomInfo
    }
}


extension AUIKaraokeRoomService: AUIRtmErrorProxyDelegate {
    public func bindRespDelegate(delegate: AUIRoomManagerRespDelegate) {
        respDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUIRoomManagerRespDelegate) {
        respDelegates.remove(delegate)
    }
    
    @objc public func onMsgRecvEmpty(channelName: String) {
        self.respDelegates.allObjects.forEach { obj in
            obj.onRoomDestroy?(roomId: channelName)
        }
    }
    
    @objc public func onConnectionStateChanged(channelName: String,
                                               connectionStateChanged state: AgoraRtmClientConnectionState,
                                               result reason: AgoraRtmClientConnectionChangeReason) {
        if reason == .changedRejoinSuccess {
            AUIRoomContext.shared.getArbiter(channelName: channelName)?.acquire()
        }
        guard state == .failed, reason == .changedBannedByServer else {
            return
        }
        
        for obj in self.respDelegates.allObjects {
            obj.onRoomUserBeKicked?(roomId: channelName, userId: AUIRoomContext.shared.currentUserInfo.userId)
        }
    }
}
