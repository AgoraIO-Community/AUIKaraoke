//
//  AUiUserViewBinder.swift
//  AUiKit
//
//  Created by wushengtao on 2023/4/11.
//

import AUiKit

open class AUiUserViewBinder: NSObject {
    private weak var userView: AUiRoomMembersView?
    private weak var userDelegate: AUiUserServiceDelegate? {
        didSet {
            userDelegate?.unbindRespDelegate(delegate: self)
            userDelegate?.bindRespDelegate(delegate: self)
        }
    }
    private weak var micSeatDelegate: AUiMicSeatServiceDelegate? {
        didSet {
            micSeatDelegate?.unbindRespDelegate(delegate: self)
            micSeatDelegate?.bindRespDelegate(delegate: self)
        }
    }
    
    public func bind(userView: AUiRoomMembersView, userService: AUiUserServiceDelegate, micSeatService: AUiMicSeatServiceDelegate) {
        self.userView = userView
        self.userDelegate = userService
        self.micSeatDelegate = micSeatService
    }
}

extension AUiUserViewBinder: AUiUserRespDelegate {
    public func onUserAudioMute(userId: String, mute: Bool) {
        
    }
    
    public func onUserVideoMute(userId: String, mute: Bool) {
        
    }
    
    public func onRoomUserSnapshot(roomId: String, userList: [AUiUserInfo]) {
        aui_info("onRoomUserSnapshot", tag: "AUiUserViewBinder")
        userView?.members = userList
    }
    
    public func onRoomUserEnter(roomId: String, userInfo: AUiUserInfo) {
        aui_info("onRoomUserEnter \(userInfo.userId) \(userInfo.userName)", tag: "AUiUserViewBinder")
        userView?.members.append(userInfo)
    }
    
    public func onRoomUserLeave(roomId: String, userInfo: AUiUserInfo) {
        aui_info("onRoomUserLeave \(userInfo.userId) \(userInfo.userName)", tag: "AUiUserViewBinder")
        userView?.members.removeAll(where: {$0.userId == userInfo.userId})
    }
    
    public func onRoomUserUpdate(roomId: String, userInfo: AUiUserInfo) {
        aui_info("onRoomUserUpdate \(userInfo.userId) \(userInfo.userName)", tag: "AUiUserViewBinder")
        if let index = userView?.members.firstIndex(where: {$0.userId == userInfo.userId}) {
            userView?.members[index] = userInfo
        } else {
            userView?.members.append(userInfo)
        }
    }
}

extension AUiUserViewBinder: AUiMicSeatRespDelegate {
    public func onAnchorEnterSeat(seatIndex: Int, user: AUiUserThumbnailInfo) {
        userView?.seatMap[user.userId] = seatIndex
    }
    
    public func onAnchorLeaveSeat(seatIndex: Int, user: AUiUserThumbnailInfo) {
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
