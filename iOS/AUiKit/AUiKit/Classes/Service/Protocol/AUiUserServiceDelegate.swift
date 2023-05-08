//
//  AUiUserServiceDelegate.swift
//  AUiKit
//
//  Created by wushengtao on 2023/4/7.
//

import Foundation

public protocol AUiUserServiceDelegate: AUiCommonServiceDelegate {
    
    /// 绑定响应
    /// - Parameter delegate: <#delegate description#>
    func bindRespDelegate(delegate: AUiUserRespDelegate)
    
    /// 解绑协议
    /// - Parameter delegate: <#delegate description#>
    func unbindRespDelegate(delegate: AUiUserRespDelegate)
    
    /// 获取指定 userId 的用户信息，如果为 null，则获取房间内所有人的信息
    /// - Parameters:
    ///   - roomId: <#roomId description#>
    ///   - userIdList: <#userIdList description#>
    ///   - callback: <#callback description#>
    func getUserInfoList(roomId: String, userIdList: [String] , callback: @escaping AUiUserListCallback)

    /// 获取用户信息
    /// - Parameter userId: <#userId description#>
    /// - Returns: <#description#>
    func getUserInfo(by userId: String) -> AUiUserThumbnailInfo?
    
    
    /// 对自己静音/解除静音
    /// - Parameters:
    ///   - seatIndex: <#seatIndex description#>
    ///   - isMute: <#isMute description#>
    ///   - callback: <#callback description#>
    func muteUserAudio(isMute: Bool, callback: @escaping AUiCallback)
    
    
    /// 对自己禁摄像头/解禁摄像头
    /// - Parameters:
    ///   - seatIndex: <#seatIndex description#>
    ///   - isMute: <#isMute description#>
    ///   - callback: <#callback description#>
    func muteUserVideo(isMute: Bool, callback: @escaping AUiCallback)
}

public protocol AUiUserRespDelegate: NSObject {
    
    /// 用户进入房间后获取到的所有用户信息
    /// - Parameters:
    ///   - roomId: <#roomId description#>
    ///   - userList: <#userList description#>
    func onRoomUserSnapshot(roomId: String, userList: [AUiUserInfo])
    
    /// 用户进入房间回调
    /// - Parameters:
    ///   - roomId: <#roomId description#>
    ///   - userInfo: <#userInfo description#>
    func onRoomUserEnter(roomId: String, userInfo: AUiUserInfo)
    
    
    ///  用户离开房间回调
    /// - Parameters:
    ///   - roomId: <#roomId description#>
    ///   - userInfo: <#userInfo description#>
    func onRoomUserLeave(roomId: String, userInfo: AUiUserInfo)
    
    
    /// 用户信息修改
    /// - Parameters:
    ///   - roomId: <#roomId description#>
    ///   - userInfo: <#userInfo description#>
    func onRoomUserUpdate(roomId: String, userInfo: AUiUserInfo)
    
    /// 用户是否静音
    /// - Parameters:
    ///   - userId: <#userId description#>
    ///   - mute: <#mute description#>
    func onUserAudioMute(userId: String, mute: Bool)
    
    
    /// 用户是否禁用摄像头
    /// - Parameters:
    ///   - userId: <#userId description#>
    ///   - mute: <#mute description#>
    func onUserVideoMute(userId: String, mute: Bool)
}
