//
//  AUIIMManagerDelegate.swift
//  AUiCore
//
//  Created by 朱继超 on 2023/5/12.
//

import Foundation

public struct AgoraChatMessage {
    
}


public enum AgoraChatroomBeKickedReason {
    case kicked
    case offline
    case destroyed
}

public protocol AUIMManagerServiceDelegate: AUiCommonServiceDelegate {
    
    
    /// Description 发送文本消息
    /// - Parameters:
    ///   - roomId: 聊天室id
    ///   - text: 文本
    ///   - userInfo: 用户信息
    ///   - completion: 回调包含发送的消息以及是否成功
    func sendMessage(roomId: String, text: String, userInfo: AUiUserCellUserDataProtocol, completion: @escaping (AgoraChatMessage?, Error?) -> Void)
    
    /// Description 加入聊天室
    /// - Parameters:
    ///   - roomId: 聊天室id
    ///   - completion: 回调包含聊天室id以及是否成功
    func joinedChatRoom(roomId: String, completion: @escaping ((String?, Error?) -> Void))
    
    /// Description 退出聊天室
    /// - Parameter completion: 是否退出成功
    func userQuitRoom(completion: ((Error?) -> Void)?)
    
    /// Description 销毁聊天室
    func userDestroyedChatroom()
    
}

public protocol AUIMManagerRespDelegate: AUiCommonServiceDelegate {
    
    /// Description 收到消息回调
    /// - Parameter messages: 消息数组
    func messagesDidReceive(messages: [AgoraChatMessage])

    
    /// Description 有用户加入聊天室 加入消息
    /// - Parameters:
    ///   - roomId: 聊天室id
    ///   - username: 环信id
    func onUserJoinedRoom(roomId: String, username: String)
}

fileprivate let once = AUIVoiceRoomIMManager()

@objcMembers public class AUIVoiceRoomIMManager: NSObject {
    
    public var currentRoomId = ""
    
    /// Description 单例
    public static var shared: AUIVoiceRoomIMManager? = once
    
    /// Description 回调协议
    public weak var responseDelegate: AUIMManagerRespDelegate?
    
    /// Description 请求协议
    public weak var requestDelegate: AUIMManagerServiceDelegate?
    
    var isLogin: Bool {
        false
    }
    
    /// Description 登录IMSDK
    /// - Parameters:
    ///   - chatId: 环信id
    ///   - token: 环信chatToken
    ///   - completion: 回调
    func login(chatId: String,token: String, completion: @escaping (Error?) -> Void) {
        
    }
    
    /// Description 退出登录IMSDK
    func logout() {
        
    }
    
    /// Description 初始化IMSDK
    /// - Parameters:
    ///   - appKey: appKey
    ///   - user: user 信息
    /// - Returns: 是否成功
    func configIM(appKey:String, user:AUiUserThumbnailInfo) -> Error? {
        nil
    }
}
