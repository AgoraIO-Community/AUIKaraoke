//
//  KaraokeUIKit.swift
//  AScenesKit_Example
//
//  Created by wushengtao on 2023/4/28.
//  Copyright © 2023 CocoaPods. All rights reserved.
//

import Foundation
import AUIKitCore
import AgoraRtcKit
import AgoraRtmKit

@objcMembers
public class KaraokeUIKit: NSObject {
    public static let shared: KaraokeUIKit = KaraokeUIKit()
    public var commonConfig: AUICommonConfig?
    private var ktvApi: KTVApiDelegate?
    private var rtcEngine: AgoraRtcEngineKit?
    private var rtmClient: AgoraRtmClientKit?
    private var service: AUIKaraokeRoomService?
    private var roomId: String?
    private var isRoomOwner: Bool = false
    
    private var roomManager: AUIRoomLocalManagerImpl?
    
    public func setup(roomConfig: AUICommonConfig,
                      ktvApi: KTVApiDelegate? = nil,
                      rtcEngine: AgoraRtcEngineKit? = nil,
                      rtmClient: AgoraRtmClientKit? = nil) {
        self.commonConfig = roomConfig
        self.ktvApi = ktvApi
        self.rtcEngine = rtcEngine
        self.rtmClient = rtmClient
        self.roomManager = AUIRoomLocalManagerImpl(commonConfig: roomConfig, rtmClient: rtmClient)
    }
    
    public func getRoomInfoList(lastCreateTime: Int64, pageSize: Int, callback: @escaping AUIRoomListCallback) {
        guard let roomManager = self.roomManager else {
            assert(false, "please invoke setup first")
            return
        }
        roomManager.getRoomInfoList(lastCreateTime: lastCreateTime, pageSize: pageSize, callback: callback)
    }
    
    public func createRoom(roomInfo: AUIRoomInfo,
                           success: ((AUIRoomInfo?)->())?,
                           failure: ((Error)->())?) {
        guard let roomManager = self.roomManager else {
            assert(false, "please invoke setup first")
            return
        }
        roomManager.createRoom(room: roomInfo) {[weak self] error, info in
            if let error = error {
                failure?(error)
                return
            }
            
            self?.generateToken(channelName: info?.roomId ?? "") { roomConfig in
                guard let self = self else { return }
                let service = AUIKaraokeRoomService(rtcEngine: self.rtcEngine,
                                                    ktvApi: self.ktvApi,
                                                    rtmClient: self.rtmClient,
                                                    commonConfig: roomManager.commonConfig,
                                                    roomConfig: roomConfig,
                                                    roomId: info!.roomId)
                self.service = service
                self.roomId = info?.roomId ?? ""
                //TODO: create & enter
                service.createRoom(roomInfo: info!) { err in
                    self.isRoomOwner = true
                    success?(info)
                }
            }
        }
    }
    
    public func launchRoom(roomId: String,
                           karaokeView: AUIKaraokeRoomView,
                           completion: @escaping (NSError?)->()) {
        guard /*let rtmClient = self.rtmClient,*/ let roomManager = self.roomManager else {
            assert(false, "please invoke setup first")
            return
        }
        //TODO: remove it
        if roomId == self.roomId, let service = self.service {
            self.subscribeError(delegate: self)
            karaokeView.bindService(service: service)
            service.enterRoom { err in
                completion(nil)
            }
            return
        }
        
        generateToken(channelName: roomId) {[weak self] roomConfig in
            guard let self = self else { return }
            let service = AUIKaraokeRoomService(rtcEngine: self.rtcEngine,
                                                ktvApi: self.ktvApi,
                                                rtmClient: self.rtmClient,
                                                commonConfig: roomManager.commonConfig,
                                                roomConfig: roomConfig,
                                                roomId: roomId)
            //订阅Token过期回调
            self.subscribeError(delegate: self)
            karaokeView.bindService(service: service)
            self.service = service
            self.roomId = roomId
            service.enterRoom { err in
                completion(nil)
            }
        }
    }
    
    public func destroyRoom(roomId: String) {
        if isRoomOwner {
            roomManager?.destroyRoom(roomId: roomId, callback: { err in
            })
        }
        isRoomOwner = false
//        rtmClient?.logout()
        self.unsubscribeError(delegate: self)
        self.service = nil
        self.roomId = nil
    }
    
    public func bindRespDelegate(delegate: AUIRoomManagerRespDelegate) {
        service?.bindRespDelegate(delegate: delegate)
    }
    
    public func unbindRespDelegate(delegate: AUIRoomManagerRespDelegate) {
        service?.unbindRespDelegate(delegate: delegate)
    }
}

//MARK: private method
extension KaraokeUIKit {
    private func subscribeError(delegate: AUIRtmErrorProxyDelegate) {
        service?.rtmManager.subscribeError(channelName: "", delegate: delegate)
    }
    
    private func unsubscribeError(delegate: AUIRtmErrorProxyDelegate) {
        service?.rtmManager.unsubscribeError(channelName: "", delegate: delegate)
    }
    
    private func renew(config: AUIRoomConfig) {
        service?.renew(config: config)
    }
    
    private func generateToken(channelName: String,
                               completion:@escaping ((AUIRoomConfig)->())) {
        let uid = commonConfig?.userId ?? ""
        let rtcChannelName = "\(channelName)_rtc"
        let rtcChorusChannelName = "\(channelName)_rtc_ex"
        let roomConfig = AUIRoomConfig()
        roomConfig.channelName = channelName
        roomConfig.rtcChannelName = rtcChannelName
        roomConfig.rtcChorusChannelName = rtcChorusChannelName
        print("generateTokens: \(uid)")
                
        let group = DispatchGroup()
        
        group.enter()
        let tokenModel1 = AUITokenGenerateNetworkModel()
        tokenModel1.channelName = channelName
        tokenModel1.userId = uid
        tokenModel1.request { error, result in
            defer {
                group.leave()
            }
            
            guard let tokenMap = result as? [String: String], tokenMap.count >= 2 else {return}
            
            roomConfig.rtcToken007 = tokenMap["rtcToken"] ?? ""
            roomConfig.rtmToken007 = tokenMap["rtmToken"] ?? ""
        }
        
        group.enter()
        let tokenModel2 = AUITokenGenerateNetworkModel()
        tokenModel2.channelName = rtcChannelName
        tokenModel2.userId = uid
        tokenModel2.request { error, result in
            defer {
                group.leave()
            }
            
            guard let tokenMap = result as? [String: String], tokenMap.count >= 2 else {return}
            
            roomConfig.rtcRtcToken = tokenMap["rtcToken"] ?? ""
            roomConfig.rtcRtmToken = tokenMap["rtmToken"] ?? ""
        }
        
        group.enter()
        let tokenModel3 = AUITokenGenerateNetworkModel()
        tokenModel3.channelName = rtcChorusChannelName
        tokenModel3.userId = uid
        tokenModel3.request { error, result in
            defer {
                group.leave()
            }
            
            guard let tokenMap = result as? [String: String], tokenMap.count >= 2 else {return}
            
            roomConfig.rtcChorusRtcToken = tokenMap["rtcToken"] ?? ""
        }
        
        group.notify(queue: DispatchQueue.main) {
            completion(roomConfig)
        }
    }
}

extension KaraokeUIKit: AUIRtmErrorProxyDelegate {
    @objc public func onTokenPrivilegeWillExpire(channelName: String?) {
        guard let channelName = roomId else { return }
        generateToken(channelName: channelName) {[weak self] config in
            self?.renew(config: config)
        }
    }
}
