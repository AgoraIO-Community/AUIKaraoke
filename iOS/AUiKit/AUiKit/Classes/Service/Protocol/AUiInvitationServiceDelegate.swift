//
//  AUiInvitationServiceDelegate.swift
//  AUiKit
//
//  Created by wushengtao on 2023/2/21.
//

import Foundation


/// 邀请Service抽象协议
public protocol AUiInvitationServiceDelegate: AUiCommonServiceDelegate {
    
    func bindRespDelegate(delegate: AUiInvitationRespDelegate)
    
    func unbindRespDelegate(delegate: AUiInvitationRespDelegate)
    
    /// 向用户发送邀请
    /// - Parameters:
    ///   - cmd: <#cmd description#>
    ///   - userId: <#userId description#>
    ///   - content: <#content description#>
    ///   - callback: <#callback description#>
    /// - Returns: <#description#>
    func sendInvitation(cmd: String, userId: String, content: String, callback: AUiCallback) -> String
    
    
    /// 接受邀请
    /// - Parameters:
    ///   - id: <#id description#>
    ///   - callback: <#callback description#>
    func acceptInvitation(id: String, callback: AUiCallback)
    
    
    /// 拒绝邀请
    /// - Parameters:
    ///   - id: <#id description#>
    ///   - callback: <#callback description#>
    func rejectInvitation(id: String, callback: AUiCallback)
    
    /// 取消邀请
    /// - Parameters:
    ///   - id: <#id description#>
    ///   - callback: <#callback description#>
    func cancelInvitation(id: String, callback: AUiCallback)

}


/// 邀请相关操作的响应
public protocol AUiInvitationRespDelegate: NSObject {
    
    /// 收到新的邀请请求
    /// - Parameters:
    ///   - id: <#id description#>
    ///   - inviter: <#inviter description#>
    ///   - cmd: <#cmd description#>
    ///   - content: <#content description#>
    func onReceiveNewInvitation(id: String, inviter: String, cmd: String, content: String)
    
    
    /// 被邀请者接受邀请
    /// - Parameters:
    ///   - id: <#id description#>
    ///   - inviteeId: <#inviteeId description#>
    func onInviteeAccepted(id: String, inviteeId: String)
    
    
    /// 被邀请者拒绝邀请
    /// - Parameters:
    ///   - id: <#id description#>
    ///   - invitee: <#invitee description#>
    func onInviteeRejected(id: String, invitee: String)
    
    
    /// 邀请人取消邀请
    /// - Parameters:
    ///   - id: <#id description#>
    ///   - inviter: <#inviter description#>
    func onInvitationCancelled(id: String, inviter: String)

}
