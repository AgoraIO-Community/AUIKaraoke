//
//  AUIUserViewBinder.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/11.
//

import AUIKitCore

open class AUIUserViewBinder: NSObject {
    private weak var userView: AUIRoomMembersView?
    private weak var userDelegate: AUIUserServiceDelegate? {
        didSet {
            userDelegate?.unbindRespDelegate(delegate: self)
            userDelegate?.bindRespDelegate(delegate: self)
        }
    }
    private weak var micSeatDelegate: AUIMicSeatServiceDelegate? {
        didSet {
            micSeatDelegate?.unbindRespDelegate(delegate: self)
            micSeatDelegate?.bindRespDelegate(delegate: self)
        }
    }
    
    public func bind(userView: AUIRoomMembersView, userService: AUIUserServiceDelegate, micSeatService: AUIMicSeatServiceDelegate) {
        self.userView = userView
        self.userDelegate = userService
        self.micSeatDelegate = micSeatService
    }
}

extension AUIUserViewBinder: AUIUserRespDelegate {
    public func onUserBeKicked(roomId: String, userId: String) {
        
    }
    
    public func onUserAudioMute(userId: String, mute: Bool) {
        
    }
    
    public func onUserVideoMute(userId: String, mute: Bool) {
        
    }
    
    public func onRoomUserSnapshot(roomId: String, userList: [AUIUserInfo]) {
        aui_info("onRoomUserSnapshot", tag: "AUIUserViewBinder")
        userView?.members = userList
    }
    
    public func onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        aui_info("onRoomUserEnter \(userInfo.userId) \(userInfo.userName)", tag: "AUIUserViewBinder")
        userView?.members.append(userInfo)
    }
    
    public func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
        aui_info("onRoomUserLeave \(userInfo.userId) \(userInfo.userName)", tag: "AUIUserViewBinder")
        userView?.members.removeAll(where: {$0.userId == userInfo.userId})
    }
    
    public func onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        aui_info("onRoomUserUpdate \(userInfo.userId) \(userInfo.userName)", tag: "AUIUserViewBinder")
        if let index = userView?.members.firstIndex(where: {$0.userId == userInfo.userId}) {
            userView?.members[index] = userInfo
        } else {
            userView?.members.append(userInfo)
        }
    }
}

extension AUIUserViewBinder: AUIMicSeatRespDelegate {
    public func onAnchorEnterSeat(seatIndex: Int, user: AUIUserThumbnailInfo) {
        userView?.seatMap[user.userId] = seatIndex
    }
    
    public func onAnchorLeaveSeat(seatIndex: Int, user: AUIUserThumbnailInfo) {
        userView?.seatMap[user.userId] = nil
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
