//
//  AUiMicSeatServiceDelegate.swift
//  AUiKit
//
//  Created by wushengtao on 2023/2/20.
//

import Foundation


/// 麦位Service抽象协议
public protocol AUiMicSeatServiceDelegate: AUiCommonServiceDelegate {
    
    /// 绑定响应回调
    /// - Parameter delegate: 需要回调的对象
    func bindRespDelegate(delegate: AUiMicSeatRespDelegate)
    
    /// 解除绑定响应回调
    /// - Parameter delegate: 需要回调的对象
    func unbindRespDelegate(delegate: AUiMicSeatRespDelegate)
    
    /// 主动上麦（听众端和房主均可调用）
    /// - Parameters:
    ///   - seatIndex: 麦位索引
    ///   - callback: 操作完成回调
    func enterSeat(seatIndex: Int, callback: @escaping AUiCallback)
    
    
    /// 主动下麦（主播调用）
    /// - Parameter callback: 操作完成回调
    func leaveSeat(callback: @escaping AUiCallback)
    
    
    /// 抱人上麦（房主调用）
    /// - Parameters:
    ///   - seatIndex: 麦位索引
    ///   - userId: 用户id
    ///   - callback: 操作完成回调
    func pickSeat(seatIndex: Int, userId: String, callback: @escaping AUiCallback)
    
    
    /// 踢人下麦（房主调用）
    /// - Parameters:
    ///   - seatIndex: 麦位索引
    ///   - callback: 操作完成回调
    func kickSeat(seatIndex: Int , callback: @escaping AUiCallback)
    
    
    /// 静音/解除静音某个麦位（房主调用）
    /// - Parameters:
    ///   - seatIndex: 麦位索引
    ///   - isMute: 静音状态 true: 设置静音 false: 解除静音
    ///   - callback: 操作完成回调
    func muteAudioSeat(seatIndex: Int, isMute: Bool, callback: @escaping AUiCallback)
    
    
    /// 关闭/打开麦位摄像头（房主调用）
    /// - Parameters:
    ///   - seatIndex: 麦位索引
    ///   - isMute: 摄像头状态 true: 关闭摄像头 false: 打开摄像头
    ///   - callback: 操作完成回调
    func muteVideoSeat(seatIndex: Int, isMute: Bool, callback: @escaping AUiCallback)
    
    
    /// 封禁/解禁某个麦位（房主调用）
    /// - Parameters:
    ///   - seatIndex: 麦位索引
    ///   - isClose: 锁定麦位情况 true: 锁定麦位 false: 解锁麦位
    ///   - callback: 操作完成回调
    func closeSeat(seatIndex: Int, isClose: Bool, callback: @escaping AUiCallback)
}

/// 麦位相关操作的响应
public protocol AUiMicSeatRespDelegate: NSObject {
    
    /// 有成员上麦（主动上麦/房主抱人上麦）
    /// - Parameters:
    ///   - seatIndex: 麦位索引
    ///   - user: 用户信息
    func onAnchorEnterSeat(seatIndex: Int, user: AUiUserThumbnailInfo)
    
    
    /// 有成员下麦（主动下麦/房主踢人下麦）
    /// - Parameters:
    ///   - seatIndex: 麦位索引
    ///   - user: 用户信息
    func onAnchorLeaveSeat(seatIndex: Int, user: AUiUserThumbnailInfo)
    
    
    /// 房主对麦位进行了静音/解禁
    /// - Parameters:
    ///   - seatIndex: 麦位索引
    ///   - isMute:麦克风开关状态
    func onSeatAudioMute(seatIndex: Int, isMute: Bool)

    
    /// 房主对麦位摄像头进行禁用/启用
    /// - Parameters:
    ///   - seatIndex: 麦位索引
    ///   - isMute: 摄像头开关状态
    func onSeatVideoMute(seatIndex: Int, isMute: Bool)
    
    
    /// 房主对麦位进行了封麦/解封
    /// - Parameters:
    ///   - seatIndex: 麦位索引
    ///   - isClose: 麦位锁定状态
    func onSeatClose(seatIndex: Int, isClose: Bool)
}
