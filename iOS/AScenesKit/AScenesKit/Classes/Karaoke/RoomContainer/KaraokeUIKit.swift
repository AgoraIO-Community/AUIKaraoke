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
    public var roomConfig: AUICommonConfig?
    private var ktvApi: KTVApiDelegate?
    private var rtcEngine: AgoraRtcEngineKit?
    private var rtmClient: AgoraRtmClientKit?
    private var service: AUIKaraokeRoomService?
    private var roomId: String?
    
    private var roomManager: AUIRoomLocalManagerImpl?
    
    public func setup(roomConfig: AUICommonConfig,
                      ktvApi: KTVApiDelegate? = nil,
                      rtcEngine: AgoraRtcEngineKit? = nil,
                      rtmClient: AgoraRtmClientKit? = nil) {
        self.roomConfig = roomConfig
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
    
    public func createRoom(roomInfo: AUICreateRoomInfo,
                           success: ((AUIRoomInfo?)->())?,
                           failure: ((Error)->())?) {
        guard let roomManager = self.roomManager else {
            assert(false, "please invoke setup first")
            return
        }
        
        let tokenModel1 = AUITokenGenerateNetworkModel()
        tokenModel1.channelName = "a"
        tokenModel1.userId = roomConfig?.userId ?? ""
        tokenModel1.request { error, result in
            if let error = error {
                failure?(error)
                return
            }
            guard let tokenMap = result as? [String: String], tokenMap.count >= 2 else {return}
            
            AUIRoomContext.shared.roomRtmToken = tokenMap["rtmToken"] ?? ""
            roomManager.createRoom(room: roomInfo) { error, info in
                if let error = error {
                    failure?(error)
                    return
                }
                
                success?(info)
            }
        }
    }
    
    public func launchRoom(roomInfo: AUIRoomInfo,
                           karaokeView: AUIKaraokeRoomView,
                           completion: @escaping (NSError?)->()) {
        guard /*let rtmClient = self.rtmClient,*/ let roomManager = self.roomManager else {
            assert(false, "please invoke setup first")
            return
        }
        
        generateToken(channelName: roomInfo.roomId) {[weak self] roomConfig, roomRtmToken in
            guard let self = self else { return }
            AUIRoomContext.shared.roomRtmToken = roomRtmToken
            let service = AUIKaraokeRoomService(rtcEngine: rtcEngine,
                                                ktvApi: ktvApi,
                                                roomManager: roomManager,
                                                roomConfig: roomConfig,
                                                roomInfo: roomInfo)
            //订阅Token过期回调
            self.subscribeError(delegate: self)
            karaokeView.bindService(service: service)
            self.service = service
            self.roomId = roomInfo.roomId
            completion(nil)
        }
    }
    
    public func destroyRoom(roomId: String) {
//        rtmClient?.logout()
        self.unsubscribeError(delegate: self)
        service = nil
    }
    
    public func bindRespDelegate(delegate: AUIRoomManagerRespDelegate) {
        roomManager?.bindRespDelegate(delegate: delegate)
    }
    
    public func unbindRespDelegate(delegate: AUIRoomManagerRespDelegate) {
        roomManager?.unbindRespDelegate(delegate: delegate)
    }
}

//MARK: private method
extension KaraokeUIKit {
    private func subscribeError(delegate: AUIRtmErrorProxyDelegate) {
        roomManager?.rtmManager.subscribeError(channelName: "", delegate: delegate)
    }
    
    private func unsubscribeError(delegate: AUIRtmErrorProxyDelegate) {
        roomManager?.rtmManager.unsubscribeError(channelName: "", delegate: delegate)
    }
    
    private func renew(roomRtmToken: String, config: AUIRoomConfig) {
        service?.renew(roomRtmToken: roomRtmToken, config: config)
    }
    
    private func generateToken(channelName: String,
                               completion:@escaping ((AUIRoomConfig, String)->())) {
        let uid = roomConfig?.userId ?? ""
        let rtcChannelName = "\(channelName)_rtc"
        let rtcChorusChannelName = "\(channelName)_rtc_ex"
        let roomConfig = AUIRoomConfig()
        roomConfig.channelName = channelName
        roomConfig.rtcChannelName = rtcChannelName
        roomConfig.rtcChorusChannelName = rtcChorusChannelName
        print("generateTokens: \(uid)")
        
        var roomRtmToken = ""
        
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
            roomRtmToken = tokenMap["rtmToken"] ?? ""
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
            completion(roomConfig, roomRtmToken)
        }
    }
}

extension KaraokeUIKit: AUIRtmErrorProxyDelegate {
    @objc public func onTokenPrivilegeWillExpire(channelName: String?) {
        guard let channelName = roomId else { return }
        generateToken(channelName: channelName) {[weak self] config, roomRtmToken in
            self?.renew(roomRtmToken: roomRtmToken, config: config)
        }
    }
}
