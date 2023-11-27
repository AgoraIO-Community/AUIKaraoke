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
    private var isRoomDestroy: Bool = false

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
                           roomConfig: AUIRoomConfig,
                           karaokeView: AUIKaraokeRoomView,
                           completion: @escaping (AUIRoomInfo?, NSError?) -> Void) {
        checkSetupAndCommonConfig()
        var date = Date()
        isRoomDestroy = false
        roomManager.createRoom(room: roomInfo) {[weak self] error, info in
            guard let self = self else {return}
            if let error = error {
                completion(nil, error)
                return
            }
            aui_info("restful createRoom: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: "Benchmark")
            date = Date()
            let service = AUIKaraokeRoomService(apiConfig: self.apiConfig,
                                                roomConfig: roomConfig)
            self.bindRespDelegate(delegate: self)
            aui_info("generateToken1: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: "Benchmark")
            self.service = service
            self.roomId = info?.roomId ?? ""
            date = Date()
            karaokeView.bindService(service: service)
            self.isRoomOwner = true
            service.create(roomInfo: info!) { err in
                aui_info("service createRoom: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: "Benchmark")
                completion(info!, nil)
            }
        }
    }

    public func enterRoom(roomId: String,
                          roomConfig: AUIRoomConfig,
                          karaokeView: AUIKaraokeRoomView,
                          completion: @escaping (AUIRoomInfo?, NSError?) -> Void) {
        checkSetupAndCommonConfig()
        isRoomDestroy = false
        let date = Date()
        let service = AUIKaraokeRoomService(apiConfig: self.apiConfig,
                                            roomConfig: roomConfig)
        self.service = service
        self.roomId = roomId
        self.bindRespDelegate(delegate: self)
        karaokeView.bindService(service: service)
        service.enter { roomInfo, err in
            aui_info("service enterRoom2: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: "Benchmark")
            if let _ = roomInfo?.owner?.userId {
                self.isRoomOwner = roomInfo?.owner?.userId == AUIRoomContext.shared.currentUserInfo.userId
            } else {
                self.isRoomOwner = true
            }
            completion(roomInfo, err as NSError?)
        }
    }

    public func leaveRoom(roomId: String) {
        checkSetupAndCommonConfig()
        self.unbindRespDelegate(delegate: self)
        if isRoomOwner || isRoomDestroy {
            roomManager.destroyRoom(roomId: roomId, callback: { _ in
            })
        }
        isRoomOwner = false
//        rtmClient?.logout()
        self.service = nil
        self.roomId = nil
    }

    public func renew(config: AUIRoomConfig) {
        service?.renew(config: config)
    }

    public func bindRespDelegate(delegate: AUIKaraokeRoomServiceRespDelegate) {
        service?.bindRespDelegate(delegate: delegate)
    }

    public func unbindRespDelegate(delegate: AUIKaraokeRoomServiceRespDelegate) {
        service?.unbindRespDelegate(delegate: delegate)
    }
}

extension KaraokeUIKit: AUIKaraokeRoomServiceRespDelegate {
    public func onRoomDestroy(roomId: String) {
        isRoomDestroy = true
    }
}

// MARK: private method
extension KaraokeUIKit {
    private func checkSetupAndCommonConfig() {
        assert(AUIRoomContext.shared.commonConfig?.isValidate() ?? false, "make sure invoke setup first")
    }
}
