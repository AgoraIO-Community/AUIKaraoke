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

private let kSertviceTag = "AUIKaraokeRoomService"
private let kRoomInfoAttrKry = "basic"

/// 卡拉OK房间Service，内部负责初始化房间内需要的Service组件，包括房间Service，邀请Service，麦位Service，...
open class AUIKaraokeRoomService: NSObject {
    lazy var micSeatImpl: AUIMicSeatServiceDelegate = AUIMicSeatServiceImpl(channelName: channelName,
                                                                            rtmManager: rtmManager)
    lazy var musicImpl: AUIMusicServiceDelegate = AUIMusicServiceImpl(channelName: channelName,
                                                                      rtmManager: rtmManager,
                                                                      ktvApi: ktvApi)
    lazy var playerImpl: AUIPlayerServiceDelegate = AUIPlayerServiceImpl(channelName: channelName,
                                                                         rtcKit: rtcEngine,
                                                                         ktvApi: ktvApi,
                                                                         rtmManager: rtmManager)
    lazy var userImpl: AUIUserServiceDelegate = AUIUserServiceImpl(channelName: channelName,
                                                                   rtmManager: rtmManager)
    lazy var chorusImpl: AUIChorusServiceDelegate = AUIChorusServiceImpl(channelName: channelName,
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
    
    private var subscribeDate: Date?
    private var lockRetrived: Bool = false {
        didSet {
            checkRoomValid()
        }
    }
    
    private var subscribeSuccess: Bool = false {
        didSet {
            checkRoomValid()
        }
    }
    private var userSnapshotList: [AUIUserInfo]? {
        didSet {
            checkRoomValid()
        }
    }
    private var roomInfo: AUIRoomInfo? {
        didSet {
            if let info = roomInfo {
                AUIRoomContext.shared.roomInfoMap[info.roomId] = info
            }
            checkRoomValid()
        }
    }
    private var enterRoomCompletion: ((AUIRoomInfo?)-> ())?
    
    private var rtmClient: AgoraRtmClientKit!
    public private(set) lazy var rtmManager: AUIRtmManager = {
        return AUIRtmManager(rtmClient: self.rtmClient, rtmChannelType: .message, isExternalLogin: !rtmClientCreateBySercice)
    }()
    private var rtmClientCreateBySercice = false
    
    private var respDelegates: NSHashTable<AUIKaraokeRoomServiceRespDelegate> = NSHashTable<AUIKaraokeRoomServiceRespDelegate>.weakObjects()
    
    private var rtcJoinClousure: ((Error?)->())?
    
    deinit {
        aui_info("deinit AUIKaraokeRoomService", tag: kSertviceTag)
    }
    
    public init(apiConfig: AUIAPIConfig?,
                roomConfig: AUIRoomConfig) {
        aui_info("init AUIKaraokeRoomService", tag: kSertviceTag)
        super.init()
        self.channelName = roomConfig.channelName
        self.roomConfig = roomConfig
        if let rtcEngine = apiConfig?.rtcEngine {
            self.rtcEngine = rtcEngine
        } else {
            self.rtcEngine = self._createRtcEngine(commonConfig: AUIRoomContext.shared.commonConfig!)
            rtcEngineCreateBySercice = true
        }
        
        if let rtmClient = apiConfig?.rtmClient {
            self.rtmClient = rtmClient
        } else {
            rtmClientCreateBySercice = true
            self.rtmClient = createRtmClient()
        }
        
        self.userImpl.bindRespDelegate(delegate: self)
        if let ktvApi = apiConfig?.ktvApi {
            self.ktvApi = ktvApi
        } else {
            let appId = AUIRoomContext.shared.commonConfig?.appId ?? ""
            let userId = Int(AUIRoomContext.shared.currentUserInfo.userId) ?? 0
            let config = KTVApiConfig(appId: appId,
                                      rtmToken: roomConfig.rtcToken,
                                      engine: self.rtcEngine,
                                      channelName: roomConfig.channelName,
                                      localUid: userId,
                                      chorusChannelName: roomConfig.rtcChorusChannelName,
                                      chorusChannelToken: roomConfig.rtcChorusRtcToken,
                                      type: .normal,
                                      maxCacheSize: 10,
                                      musicType: .mcc,
                                      isDebugMode: false)
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
        ktvApi.renewToken(rtmToken: roomConfig.rtmToken, chorusChannelRtcToken: roomConfig.rtcChorusRtcToken)
        
        //rtm renew
        rtmManager.renew(token: config.rtmToken)
        
        //rtc renew
        rtcEngine.renewToken(roomConfig.rtcToken)
    }
}

// MARK: private method
extension AUIKaraokeRoomService {
    private func joinRtcChannel(completion: ((Error?)->())?) {
        let currentUserInfo = AUIRoomContext.shared.currentUserInfo
        guard let uid = UInt(currentUserInfo.userId) else {
            aui_error("joinRtcChannel fail, commonConfig is empty", tag: kSertviceTag)
            completion?(nil)
            return
        }
        
        setEngineConfig(with: uid)
        let ret = self.rtcEngine.joinChannel(byToken: roomConfig.rtcToken,
                                             channelId: roomConfig.channelName,
                                             uid: uid,
                                             mediaOptions: channelMediaOptions())
        aui_info("joinChannel channelName ret: \(ret) channelName:\(roomConfig.channelName), uid: \(uid)", tag: kSertviceTag)
        
        guard ret == 0 else {
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
        aui_error("leaveRtcChannel", tag: kSertviceTag)
    }
    
    private func setEngineConfig(with uid:UInt) {
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
        option.publishMediaPlayerId = Int(self.ktvApi.getMusicPlayer()?.getMediaPlayerId() ?? 0)
        option.enableAudioRecordingOrPlayout = true
        
        aui_info("update clientRoleType: \(option.clientRoleType.rawValue)", tag: kSertviceTag)
        return option
    }
    private func _rtcEngineConfig(commonConfig: AUICommonConfig) -> AgoraRtcEngineConfig {
       let config = AgoraRtcEngineConfig()
        config.appId = commonConfig.appId
        config.channelProfile = .liveBroadcasting
        config.audioScenario = .gameStreaming
        config.areaCode = .global
        
        if config.appId?.count ?? 0 == 0 {
            aui_error("config.appId is empty, please check 'AUIRoomContext.shared.commonConfig.appId'", tag: kSertviceTag)
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
        let userInfo = AUIRoomContext.shared.currentUserInfo
        let rtmConfig = AgoraRtmClientConfig(appId: commonConfig.appId, userId: userInfo.userId)
        rtmConfig.presenceTimeout = 60
        if rtmConfig.userId.count == 0 {
            aui_error("userId is empty")
            assert(false, "userId is empty")
        }
        if rtmConfig.appId.count == 0 {
            aui_error("appId is empty, please check 'AUIRoomContext.shared.commonConfig.appId' ")
            assert(false, "appId is empty, please check 'AUIRoomContext.shared.commonConfig.appId' ")
        }
        let rtmClient = try? AgoraRtmClientKit(rtmConfig, delegate: nil)
        return rtmClient!
    }
    
    private func checkRoomValid() {
        guard subscribeSuccess,  let roomInfo = roomInfo, lockRetrived else { return }
        if let completion = self.enterRoomCompletion {
            completion(roomInfo)
            self.enterRoomCompletion = nil
            for obj in self.respDelegates.allObjects {
                obj.onRoomInfoChange?(roomId: roomInfo.roomId, roomInfo: roomInfo)
            }
            
            //TODO: add more service.sereviceDidLoad
            chatImplement.sereviceDidLoad?()
        }
        
        guard let userList = userSnapshotList else { return }
        guard roomInfo.roomId.count > 0,
              let _ = userList.filter({ AUIRoomContext.shared.isRoomOwner(channelName: channelName, userId: $0.userId)}).first else {
            //room owner not found, clean room
            cleanRoomInfo(channelName: channelName)
            return
        }
    }
}

// MARK: AgoraRtcEngineDelegate
extension AUIKaraokeRoomService: AgoraRtcEngineDelegate {
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        aui_info("didJoinChannel channel:\(channel) uid: \(uid)", tag: kSertviceTag)
        guard uid == UInt(AUIRoomContext.shared.currentUserInfo.userId) else { return }
        aui_info("joinChannel  channelName success channelName:\(channel), uid: \(uid)", tag: kSertviceTag)
        rtcJoinClousure?(nil)
        rtcJoinClousure = nil
    }
   
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        aui_error("didOccurError: \(errorCode.rawValue)", tag: kSertviceTag)
        rtcJoinClousure?(AUICommonError.rtcError(Int32(errorCode.rawValue)).toNSError())
        rtcJoinClousure = nil
    }
}

// room manager handler
extension AUIKaraokeRoomService {
    private func cleanUserInfo(channelName: String, userId: String) {
        //TODO: 仲裁者暂无
        guard AUIRoomContext.shared.getArbiter(channelName: channelName)?.isArbiter() ?? false else {return}
        guard let idx = micSeatImpl.getMicSeatIndex?(userId: userId), idx >= 0 else {return}
        micSeatImpl.kickSeat(seatIndex: idx) { err in }
    }
    
    
    /// 清理房间，仲裁者才有权限操作
    /// - Parameter channelName: <#channelName description#>
    private func cleanRoomInfo(channelName: String) {
        guard let arbiter = AUIRoomContext.shared.getArbiter(channelName: channelName),
              arbiter.isArbiter() else {return}

        micSeatImpl.deinitService?(completion: { err in })
        musicImpl.deinitService?(completion: { err in })
        chorusImpl.deinitService?(completion: { err in })
        chatImplement.deinitService?(completion: { err in })
        rtmManager.cleanBatchMetadata(channelName: channelName,
                                      lockName: kRTM_Referee_LockName, 
                                      removeKeys: [kRoomInfoAttrKry],
                                      fetchImmediately: true,
                                      completion: { err in
        })
        
        arbiter.destroy()
    }
    
    public func create(roomInfo: AUIRoomInfo, completion:@escaping (NSError?)->()) {
        guard let rtmToken = AUIRoomContext.shared.roomConfigMap[roomInfo.roomId]?.rtmToken else {
            assert(false)
            return
        }
        guard rtmManager.isLogin else {
            let date = Date()
            rtmManager.login(token: rtmToken) {[weak self] err in
                aui_info("[Benchmark]rtm login: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: kSertviceTag)
                if let err = err {
                    completion(err as NSError)
                    return
                }
                self?.create(roomInfo: roomInfo, completion: completion)
            }
            return
        }
        
        self.roomInfo = roomInfo
        AUIRoomContext.shared.getArbiter(channelName: roomInfo.roomId)?.create()
        initRoom(roomInfo: roomInfo) {[weak self] err in
            if let err = err {
                completion(err)
                return
            }
            self?.enter(completion: { _, err in
                completion(err)
            })
        }
    }
    
    public func enter(completion:@escaping (AUIRoomInfo?, NSError?)->()) {
        let roomId = channelName!
        guard let config = AUIRoomContext.shared.roomConfigMap[roomId], config.rtmToken.count > 0 else {
            assert(false)
            aui_info("enterRoom: \(roomId) fail", tag: kSertviceTag)
            completion(nil, AUICommonError.missmatchRoomConfig.toNSError())
            return
        }
        
        guard rtmManager.isLogin else {
            let date = Date()
            rtmManager.login(token: config.rtmToken) {[weak self] err in
                aui_info("[Benchmark]rtm login: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: kSertviceTag)
                if let err = err {
                    completion(nil, err)
                    return
                }
                self?.enter(completion: completion)
            }
            return
        }
        
        aui_info("enterRoom subscribe: \(roomId)", tag: kSertviceTag)
        let date = Date()
        self.enterRoomCompletion = { roomInfo in
            aui_info("[Benchmark]enterRoomCompletion: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: kSertviceTag)
            completion(roomInfo, nil)
        }
        
        if self.roomInfo == nil {
            rtmManager.getMetadata(channelName: roomId) { err, metadata in
                guard let value = metadata?[kRoomInfoAttrKry], let roomInfo = AUIRoomInfo.yy_model(withJSON: value) else {
                    self.roomInfo = AUIRoomInfo()
                    return
                }
                
                self.roomInfo = roomInfo
            }
        }
        subscribeDate = Date()
        AUIRoomContext.shared.getArbiter(channelName: roomId)?.acquire()
        rtmManager.subscribeError(channelName: roomId, delegate: self)
        rtmManager.subscribeLock(channelName: roomId, lockName: kRTM_Referee_LockName, delegate: self)
        rtmManager.subscribe(channelName: roomId) {[weak self] error in
            guard let self = self else { return }
            if let error = error {
                completion(nil, error)
                return
            }
            self.subscribeSuccess = true
            aui_info("[Benchmark]rtm manager subscribe: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: kSertviceTag)
            aui_info("enterRoom subscribe finished \(roomId) \(error?.localizedDescription ?? "")", tag: kSertviceTag)
        }
        
        //join rtc
        joinRtcChannel { error in
            aui_info("joinRtcChannel finished: \(error?.localizedDescription ?? "success")", tag: kSertviceTag)
        }
    }
    
    public func exit(callback: @escaping (NSError?) -> ()) {
        let roomId = channelName!
        aui_info("exitRoom: \(roomId)", tag: kSertviceTag)
        cleanUserInfo(channelName: roomId, userId: AUIRoomContext.shared.currentUserInfo.userId)
        cleanSDK()
        callback(nil)
    }
    
    public func destroy(callback: @escaping (NSError?) -> ()) {
        let roomId = channelName!
        aui_info("destroyRoom: \(roomId)", tag: kSertviceTag)
        cleanRoomInfo(channelName: roomId)
        cleanSDK()
    }
    
    private func initRoom(roomInfo: AUIRoomInfo, completion:@escaping (NSError?)->()) {
        guard let roomInfoStr = roomInfo.yy_modelToJSONString() else {
            assert(false)
            completion(nil)
            return
        }
        
        _ = micSeatImpl.initService?(completion: { err in
        })
        
        let date = Date()
        rtmManager.setBatchMetadata(channelName: roomInfo.roomId,
                                    lockName: "",
                                    metadata: [kRoomInfoAttrKry: roomInfoStr],
                                    fetchImmediately: true) { err in
            aui_info("[Benchmark]rtm setMetaData: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: kSertviceTag)
            if let err = err {
                completion(err)
                return
            }
            completion(nil)
        }
    }
    
    private func cleanSDK() {
        let roomId = channelName!
        self.rtmManager.unSubscribe(channelName: roomId)
        rtmManager.unsubscribeError(channelName: roomId, delegate: self)
        rtmManager.unsubscribeLock(channelName: roomId, lockName: kRTM_Referee_LockName, delegate: self)
        rtmManager.logout()
        ktvApi.cleanCache()
        leaveRtcChannel()
    }
}

extension AUIKaraokeRoomService: AUIUserRespDelegate {
    public func onUserBeKicked(roomId: String, userId: String) {
    }
    
    public func onRoomUserSnapshot(roomId: String, userList: [AUIUserInfo]) {
        self.userSnapshotList = userList
        
        guard let user = userList.filter({$0.userId == AUIRoomContext.shared.currentUserInfo.userId }).first else {return}
        aui_info("onRoomUserSnapshot", tag: kSertviceTag)
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
        aui_info("onUserAudioMute mute current user: \(mute)", tag: kSertviceTag)
        rtcEngine.adjustRecordingSignalVolume(mute ? 0 : 100)
    }
    
    public func onUserVideoMute(userId: String, mute: Bool) {
        guard userId == AUIRoomContext.shared.currentUserInfo.userId else {return}
        aui_info("onMuteVideo onUserVideoMute [\(userId)]: \(mute)", tag: kSertviceTag)
        rtcEngine.enableLocalVideo(!mute)
        let option = AgoraRtcChannelMediaOptions()
        option.publishCameraTrack = !mute
        rtcEngine.updateChannel(with: option)
    }
}
extension AUIKaraokeRoomService: AUIRtmLockProxyDelegate {
    public func onReceiveLockDetail(channelName: String, lockDetail: AgoraRtmLockDetail) {
        aui_benchmark("onReceiveLockDetail", cost: -(subscribeDate?.timeIntervalSinceNow ?? 0), tag: kSertviceTag)
        self.lockRetrived = true
    }
    
    public func onReleaseLockDetail(channelName: String, lockDetail: AgoraRtmLockDetail) {
    }
}

extension AUIKaraokeRoomService: AUIRtmErrorProxyDelegate {
    public func onTokenPrivilegeWillExpire(channelName: String?) {
        aui_info("onTokenPrivilegeWillExpire: \(channelName ?? "")", tag: kSertviceTag)
        for obj in self.respDelegates.allObjects {
            obj.onTokenPrivilegeWillExpire?(channelName: channelName)
        }
    }
    
    public func bindRespDelegate(delegate: AUIKaraokeRoomServiceRespDelegate) {
        respDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUIKaraokeRoomServiceRespDelegate) {
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
