
//
//  AUiRoomManagerDelegate.swift
//  AUiKit
//
//  Created by wushengtao on 2023/2/20.
//

import Foundation


/// 房间管理抽象协议
public protocol AUiRoomManagerDelegate: NSObject {
    
    /// 绑定响应
    /// - Parameter delegate: <#delegate description#>
    func bindRespDelegate(delegate: AUiRoomManagerRespDelegate)
    
    /// 解绑响应
    /// - Parameter delegate: <#delegate description#>
    func unbindRespDelegate(delegate: AUiRoomManagerRespDelegate)
    
    /// 创建房间（房主调用），若房间不存在，系统将自动创建一个新房间
    /// - Parameters:
    ///   - roomId: <#roomId description#>
    ///   - room: <#room description#>
    ///   - callback: <#callback description#>
    func createRoom(room: AUiCreateRoomInfo, callback: @escaping AUiCreateRoomCallback)
    
    /// 销毁房间（房主调用）
    /// - Parameters:
    ///   - roomId: <#roomId description#>
    ///   - callback: <#callback description#>
    func destroyRoom(roomId: String, callback: @escaping AUiCallback)
    
    /// 进入房间（听众调用）。
    /// - Parameters:
    ///   - roomId: <#roomId description#>
    ///   - callback: <#callback description#>
    func enterRoom(roomId: String, callback: @escaping AUiCallback)
    
    
    /// 退出房间（听众调用）
    /// - Parameters:
    ///   - roomId: <#roomId description#>
    ///   - callback: <#callback description#>
    func exitRoom(roomId: String, callback: @escaping AUiCallback)
    
    /// 获取房间列表的详细信息
    /// - Parameters:
    ///   - lastCreateTime: 最后1条数据的创建时间, 返回数据list的createTime字段值，如果为空, 默认会设置为服务器当前时间戳
    ///   - pageSize: 分页大小
    ///   - callback: <#callback description#>
    func getRoomInfoList(lastCreateTime: Int64?, pageSize: Int, callback: @escaping AUiRoomListCallback)
}

/// 房间操作对应的响应
public protocol AUiRoomManagerRespDelegate: NSObject {

    /// 房间被销毁的回调
    /// - Parameter roomId: <#roomId description#>
    func onRoomDestroy(roomId: String)
    
    /// 房间信息变更回调
    /// - Parameters:
    ///   - roomId: <#roomId description#>
    ///   - roomInfo: <#roomInfo description#>
    func onRoomInfoChange(roomId: String, roomInfo: AUiRoomInfo)
}
