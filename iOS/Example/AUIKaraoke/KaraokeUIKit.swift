//
//  KaraokeUIKit.swift
//  AScenesKit_Example
//
//  Created by wushengtao on 2023/4/28.
//  Copyright © 2023 CocoaPods. All rights reserved.
//

import Foundation
import AScenesKit
import AUIKitCore
import AgoraRtcKit
import AgoraRtmKit

class KaraokeUIKit: NSObject {
    static let shared: KaraokeUIKit = KaraokeUIKit()
    public var roomConfig: AUICommonConfig?
    private var ktvApi: KTVApiDelegate?
    private var rtcEngine: AgoraRtcEngineKit?
    private var rtmClient: AgoraRtmClientKit?
    private var service: AUIKaraokeRoomService?
    
    private var roomManager: AUIRoomManagerImpl?
    
    func setup(roomConfig: AUICommonConfig,
               ktvApi: KTVApiDelegate? = nil,
               rtcEngine: AgoraRtcEngineKit? = nil,
               rtmClient: AgoraRtmClientKit? = nil) {
        self.roomConfig = roomConfig
        self.ktvApi = ktvApi
        self.rtcEngine = rtcEngine
        self.rtmClient = rtmClient
        self.roomManager = AUIRoomManagerImpl(commonConfig: roomConfig, rtmClient: rtmClient)
    }
    
    func getRoomInfoList(lastCreateTime: Int64?, pageSize: Int, callback: @escaping AUIRoomListCallback) {
        guard let roomManager = self.roomManager else {
            assert(false, "please invoke setup first")
            return
        }
        roomManager.getRoomInfoList(lastCreateTime: lastCreateTime, pageSize: pageSize, callback: callback)
    }
    
    func renew(config: AUIRoomConfig) {
        service?.renew(config: config)
    }
    
    func createRoom(roomInfo: AUICreateRoomInfo,
                    success: ((AUIRoomInfo?)->())?,
                    failure: ((Error)->())?) {
        guard let roomManager = self.roomManager else {
            assert(false, "please invoke setup first")
            return
        }
        roomManager.createRoom(room: roomInfo) { error, info in
            if let error = error {
                failure?(error)
                return
            }
            
            success?(info)
        }
    }
    
    func launchRoom(roomInfo: AUIRoomInfo,
                    appId: String?,
                    config: AUIRoomConfig,
                    karaokeView: AUIKaraokeRoomView) {
        guard /*let rtmClient = self.rtmClient,*/ let roomManager = self.roomManager else {
            assert(false, "please invoke setup first")
            return
        }
        if let appId = appId {
            AUIRoomContext.shared.commonConfig?.appId = appId
        }
        let service = AUIKaraokeRoomService(rtcEngine: rtcEngine,
                                            ktvApi: ktvApi,
                                            roomManager: roomManager,
                                            roomConfig: config,
                                            roomInfo: roomInfo)
        karaokeView.bindService(service: service)
        self.service = service
    }
    
    func destoryRoom(roomId: String) {
//        rtmClient?.logout()
        service = nil
    }
    
    func subscribeError(delegate: AUIRtmErrorProxyDelegate) {
        roomManager?.rtmManager.subscribeError(channelName: "", delegate: delegate)
    }
    
    func unsubscribeError(delegate: AUIRtmErrorProxyDelegate) {
        roomManager?.rtmManager.unsubscribeError(channelName: "", delegate: delegate)
    }
    
    func bindRespDelegate(delegate: AUIRoomManagerRespDelegate) {
        roomManager?.bindRespDelegate(delegate: delegate)
    }
    
    func unbindRespDelegate(delegate: AUIRoomManagerRespDelegate) {
        roomManager?.unbindRespDelegate(delegate: delegate)
    }
}

extension KaraokeUIKit: AgoraRtcEngineDelegate {
    
}

extension KaraokeUIKit: AgoraRtmClientDelegate {
    
}
