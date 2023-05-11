//
//  KaraokeUIKit.swift
//  AScenesKit_Example
//
//  Created by wushengtao on 2023/4/28.
//  Copyright © 2023 CocoaPods. All rights reserved.
//

import Foundation
import AScenesKit
import AUiKit
import AgoraRtcKit

class KaraokeUIKit: NSObject {
    static let shared: KaraokeUIKit = KaraokeUIKit()
    public var roomConfig: AUiCommonConfig?
    private var ktvApi: KTVApiDelegate?
    private var rtcEngine: AgoraRtcEngineKit?
    private var rtmClient: AgoraRtmClientKit?
    private var service: AUiKaraokeRoomService?
    
    private var roomManager: AUiRoomManagerImpl?
    
    func setup(roomConfig: AUiCommonConfig,
               ktvApi: KTVApiDelegate? = nil,
               rtcEngine: AgoraRtcEngineKit? = nil,
               rtmClient: AgoraRtmClientKit? = nil) {
        self.roomConfig = roomConfig
        self.ktvApi = ktvApi
        self.rtcEngine = rtcEngine
        self.rtmClient = rtmClient
        self.roomManager = AUiRoomManagerImpl(commonConfig: roomConfig, rtmClient: rtmClient)
    }
    
    func getRoomInfoList(lastCreateTime: Int64?, pageSize: Int, callback: @escaping AUiRoomListCallback) {
        guard let roomManager = self.roomManager else {
            assert(false, "please invoke setup first")
            return
        }
        roomManager.getRoomInfoList(lastCreateTime: lastCreateTime, pageSize: pageSize, callback: callback)
    }
    
    func renew(config: AUiRoomConfig) {
        service?.renew(config: config)
    }
    
    func createRoom(roomInfo: AUiCreateRoomInfo,
                    success: ((AUiRoomInfo?)->())?,
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
    
    func launchRoom(roomInfo: AUiRoomInfo,
                    appId: String,
                    config: AUiRoomConfig,
                    karaokeView: AUiKaraokeRoomView,
                    completion: @escaping ((Int)->())) {
        guard /*let rtmClient = self.rtmClient,*/ let roomManager = self.roomManager else {
            assert(false, "please invoke setup first")
            return
        }
        AUiRoomContext.shared.commonConfig?.appId = appId
        let service = AUiKaraokeRoomService(rtcEngine: rtcEngine,
                                            ktvApi: ktvApi,
                                            roomManager: roomManager,
                                            roomConfig: config,
                                            roomInfo: roomInfo)
        karaokeView.bindService(service: service)
        self.service = service
        completion(0)
    }
    
    func destoryRoom(roomId: String) {
//        rtmClient?.logout()
        service = nil
    }
    
    func subscribeError(roomId: String, delegate: AUiRtmErrorProxyDelegate) {
        roomManager?.rtmManager.subscribeError(channelName: roomId, delegate: delegate)
    }
    
    func unsubscribeError(roomId: String, delegate: AUiRtmErrorProxyDelegate) {
        roomManager?.rtmManager.unsubscribeError(channelName: roomId, delegate: delegate)
    }
    
    func bindRespDelegate(delegate: AUiRoomManagerRespDelegate) {
        roomManager?.bindRespDelegate(delegate: delegate)
    }
    
    func unbindRespDelegate(delegate: AUiRoomManagerRespDelegate) {
        roomManager?.unbindRespDelegate(delegate: delegate)
    }
}

extension KaraokeUIKit: AgoraRtcEngineDelegate {
    
}

extension KaraokeUIKit: AgoraRtmClientDelegate {
    
}
