//
//  KaraokeUIKit.swift
//  AScenesKit_Example
//
//  Created by wushengtao on 2023/4/28.
//  Copyright © 2023 CocoaPods. All rights reserved.
//

import Foundation
import AUIKitCore
import AScenesKit
import AgoraRtcKit
import AgoraRtmKit

@objcMembers
public class KaraokeUIKit: NSObject {
    public static let shared: KaraokeUIKit = KaraokeUIKit()
    public var commonConfig: AUICommonConfig?
    private var apiConfig: AUIAPIConfig?
    private var service: AUIKaraokeRoomService?
    private var roomId: String?
    private var isRoomOwner: Bool = false
    
    private lazy var roomManager: AUIRoomManagerImpl = AUIRoomManagerImpl()
    
    public func setup(commonConfig: AUICommonConfig,
                      apiConfig: AUIAPIConfig? = nil) {
        AUIRoomContext.shared.commonConfig = commonConfig
        self.commonConfig = commonConfig
        self.apiConfig = apiConfig
    }
    
    public func getRoomInfoList(lastCreateTime: Int64, pageSize: Int, callback: @escaping AUIRoomListCallback) {
        checkSetupAndCommonConfig()
        roomManager.getRoomInfoList(lastCreateTime: lastCreateTime, pageSize: pageSize, callback: callback)
    }
    
    public func createRoom(roomInfo: AUIRoomInfo,
                           success: ((AUIRoomInfo?)->())?,
                           failure: ((Error)->())?) {
        checkSetupAndCommonConfig()
        var date = Date()
        roomManager.createRoom(room: roomInfo) {[weak self] error, info in
            if let error = error {
                failure?(error)
                return
            }
            aui_info("restful createRoom: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: "Benchmark")
            date = Date()
            self?.generateToken(channelName: info?.roomId ?? "") { roomConfig in
                guard let self = self else { return }
                let service = AUIKaraokeRoomService(apiConfig: self.apiConfig,
                                                    roomConfig: roomConfig)
                aui_info("generateToken1: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: "Benchmark")
                self.service = service
                self.roomId = info?.roomId ?? ""
                //TODO: create & enter
                date = Date()
                service.createRoom(roomInfo: info!) { err in
                    aui_info("service createRoom: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: "Benchmark")
                    self.isRoomOwner = true
                    success?(info)
                }
            }
        }
    }
    
    public func launchRoom(roomId: String,
                           karaokeView: AUIKaraokeRoomView,
                           completion: @escaping (NSError?)->()) {
        checkSetupAndCommonConfig()
        let date = Date()
        //TODO: remove it
        if roomId == self.roomId, let service = self.service {
            karaokeView.bindService(service: service)
            service.enterRoom { err in
                aui_info("service enterRoom1: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: "Benchmark")
                completion(nil)
            }
            return
        }
        
        generateToken(channelName: roomId) {[weak self] roomConfig in
            guard let self = self else { return }
            aui_info("generateToken2: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: "Benchmark")
            let service = AUIKaraokeRoomService(apiConfig: self.apiConfig,
                                                roomConfig: roomConfig)
            //订阅Token过期回调
            karaokeView.bindService(service: service)
            self.service = service
            self.roomId = roomId
            service.enterRoom { err in
                aui_info("service enterRoom2: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: "Benchmark")
                completion(nil)
            }
        }
    }
    
    public func destroyRoom(roomId: String) {
        checkSetupAndCommonConfig()
        if isRoomOwner {
            roomManager.destroyRoom(roomId: roomId, callback: { err in
            })
        }
        isRoomOwner = false
//        rtmClient?.logout()
        self.service = nil
        self.roomId = nil
    }
    
    public func bindRespDelegate(delegate: AUIKaraokeRoomServiceRespDelegate) {
        service?.bindRespDelegate(delegate: delegate)
    }
    
    public func unbindRespDelegate(delegate: AUIKaraokeRoomServiceRespDelegate) {
        service?.unbindRespDelegate(delegate: delegate)
    }
    
    public func onTokenPrivilegeWillExpire(channelName: String?) {
        guard let channelName = roomId else { return }
        generateToken(channelName: channelName) {[weak self] config in
            self?.renew(config: config)
        }
    }
}

//MARK: private method
extension KaraokeUIKit {
    private func checkSetupAndCommonConfig() {
        assert(AUIRoomContext.shared.commonConfig?.isValidate() ?? false, "make sure invoke setup first")
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
