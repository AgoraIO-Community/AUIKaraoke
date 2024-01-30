//
//  AUIUserViewBinder.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/11.
//

import AUIKitCore

open class AUIUserViewBinder: NSObject {
    private weak var rtmManager: AUIRtmManager?
    private weak var userView: AUIRoomMembersView?
    private weak var userDelegate: AUIUserServiceDelegate? {
        didSet {
            userDelegate?.unbindRespDelegate(delegate: self)
            userDelegate?.bindRespDelegate(delegate: self)
        }
    }
    private weak var chorusDelegate: AUIChorusServiceDelegate?
    private weak var musicDelegate: AUIMusicServiceDelegate?
    private weak var micSeatDelegate: AUIMicSeatServiceDelegate? {
        didSet {
            micSeatDelegate?.unbindRespDelegate(delegate: self)
            micSeatDelegate?.bindRespDelegate(delegate: self)
        }
    }
    
    private var seatIndexMap: [String: Int] = [:]
    
    public func bind(userView: AUIRoomMembersView,
                     rtmManager: AUIRtmManager,
                     userService: AUIUserServiceDelegate,
                     micSeatService: AUIMicSeatServiceDelegate,
                     musicService: AUIMusicServiceDelegate,
                     chorusService: AUIChorusServiceDelegate) {
        self.userView = userView
        self.rtmManager = rtmManager
        self.userDelegate = userService
        self.micSeatDelegate = micSeatService
        self.musicDelegate = musicService
        self.chorusDelegate = chorusService
    }
}

extension AUIUserViewBinder: AUIUserRespDelegate {
    public func onUserBeKicked(roomId: String, userId: String) {
        guard AUIRoomContext.shared.getArbiter(channelName: roomId)?.isArbiter() ?? false else { return }
        _ = micSeatDelegate?.cleanUserInfo?(userId: userId, completion: { err in
        })
        _ = musicDelegate?.cleanUserInfo?(userId: userId, completion: { err in
        })
        _ = chorusDelegate?.cleanUserInfo?(userId: userId, completion: { err in
        })
    }
    
    public func onUserAudioMute(userId: String, mute: Bool) {
        
    }
    
    public func onUserVideoMute(userId: String, mute: Bool) {
        
    }
    
    public func onRoomUserSnapshot(roomId: String, userList: [AUIUserInfo]) {
        aui_info("onRoomUserSnapshot", tag: "AUIUserViewBinder")
        userView?.updateMembers(members: userList.map({$0.createData(seatIndexMap[$0.userId] ?? -1)}), channelName: roomId)
    }
    
    public func onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        aui_info("onRoomUserEnter \(userInfo.userId) \(userInfo.userName)", tag: "AUIUserViewBinder")
        userView?.appendMember(member: userInfo.createData(seatIndexMap[userInfo.userId] ?? -1))
    }
    
    public func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
        aui_info("onRoomUserLeave \(userInfo.userId) \(userInfo.userName)", tag: "AUIUserViewBinder")
        userView?.removeMember(userId: userInfo.userId)
    }
    
    public func onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        aui_info("onRoomUserUpdate \(userInfo.userId) \(userInfo.userName)", tag: "AUIUserViewBinder")
        userView?.updateMember(member: userInfo.createData(seatIndexMap[userInfo.userId] ?? -1))
    }
}

extension AUIUserViewBinder: AUIMicSeatRespDelegate {
    public func onAnchorEnterSeat(seatIndex: Int, user: AUIUserThumbnailInfo) {
        seatIndexMap[user.userId] = seatIndex
        userView?.updateSeatInfo(userId: user.userId, seatIndex: seatIndex)
    }
    
    public func onAnchorLeaveSeat(seatIndex: Int, user: AUIUserThumbnailInfo) {
        seatIndexMap[user.userId] = -1
        userView?.updateSeatInfo(userId: user.userId, seatIndex: -1)
    }
    
    public func onSeatAudioMute(seatIndex: Int, isMute: Bool) {
        
    }
    
    public func onSeatVideoMute(seatIndex: Int, isMute: Bool) {
        
    }
    
    public func onUserMicrophoneMute(userId: String, mute: Bool) {
        
    }
    
    public func onSeatClose(seatIndex: Int, isClose: Bool) {
        
    }
}
