//
//  AUiMicSeatServiceDelegate.swift
//  AUiKit
//
//  Created by wushengtao on 2023/2/20.
//

import Foundation


/// 麦位Service抽象协议
public protocol AUiMicSeatServiceDelegate: AUiCommonServiceDelegate {
    
    func bindRespDelegate(delegate: AUiMicSeatRespDelegate)
    
    func unbindRespDelegate(delegate: AUiMicSeatRespDelegate)
    
    /// 主动上麦（听众端和房主均可调用）
    /// - Parameters:
    ///   - seatIndex: <#seatIndex description#>
    ///   - callback: <#callback description#>
    func enterSeat(seatIndex: Int, callback: @escaping AUiCallback)
    
    
    /// 主动下麦（主播调用）
    /// - Parameter callback: <#callback description#>
    func leaveSeat(callback: @escaping AUiCallback)
    
    
    /// 抱人上麦（房主调用）
    /// - Parameters:
    ///   - seatIndex: <#seatIndex description#>
    ///   - userId: <#userId description#>
    ///   - callback: <#callback description#>
    func pickSeat(seatIndex: Int, userId: String, callback: @escaping AUiCallback)
    
    
    /// 踢人下麦（房主调用）
    /// - Parameters:
    ///   - seatIndex: <#seatIndex description#>
    ///   - callback: <#callback description#>
    func kickSeat(seatIndex: Int , callback: @escaping AUiCallback)
    
    
    /// 静音/解除静音某个麦位（房主调用）
    /// - Parameters:
    ///   - seatIndex: <#seatIndex description#>
    ///   - isMute: <#isMute description#>
    ///   - callback: <#callback description#>
    func muteAudioSeat(seatIndex: Int, isMute: Bool, callback: @escaping AUiCallback)
    
    
    /// 关闭/打开麦位摄像头
    /// - Parameters:
    ///   - seatIndex: <#seatIndex description#>
    ///   - isMute: <#isMute description#>
    ///   - callback: <#callback description#>
    func muteVideoSeat(seatIndex: Int, isMute: Bool, callback: @escaping AUiCallback)
    
    
    /// 封禁/解禁某个麦位（房主调用）
    /// - Parameters:
    ///   - seatIndex: <#seatIndex description#>
    ///   - isClose: <#isClose description#>
    ///   - callback: <#callback description#>
    func closeSeat(seatIndex: Int, isClose: Bool, callback: @escaping AUiCallback)
}

/// 麦位相关操作的响应
public protocol AUiMicSeatRespDelegate: NSObject {
    
    /// 有成员上麦（主动上麦/房主抱人上麦）
    /// - Parameters:
    ///   - seatIndex: <#seatIndex description#>
    ///   - user: <#user description#>
    func onAnchorEnterSeat(seatIndex: Int, user: AUiUserThumbnailInfo)
    
    
    /// 有成员下麦（主动下麦/房主踢人下麦）
    /// - Parameters:
    ///   - seatIndex: <#index description#>
    ///   - user: <#user description#>
    func onAnchorLeaveSeat(seatIndex: Int, user: AUiUserThumbnailInfo)
    
    
    /// 房主禁麦
    /// - Parameters:
    ///   - seatIndex: <#seatIndex description#>
    ///   - isMute: <#isMute description#>
    func onSeatAudioMute(seatIndex: Int, isMute: Bool)

    
    /// 房主禁摄像头
    /// - Parameters:
    ///   - seatIndex: <#seatIndex description#>
    ///   - isMute: <#isMute description#>
    func onSeatVideoMute(seatIndex: Int, isMute: Bool)
    
    
    /// 房主封麦
    /// - Parameters:
    ///   - seatIndex: <#seatIndex description#>
    ///   - isClose: <#isClose description#>
    func onSeatClose(seatIndex: Int, isClose: Bool)
}
