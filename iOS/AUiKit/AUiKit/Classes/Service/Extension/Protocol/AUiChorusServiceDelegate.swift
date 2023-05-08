//
//  AUiChorusServiceDelegate.swift
//  AUiKit
//
//  Created by wushengtao on 2023/4/7.
//

import Foundation


/// 合唱者模型
open class AUiChoristerModel: NSObject {
    @objc public var userId: String = ""
    public var chorusSongNo: String?          //合唱者演唱歌曲
    public var owner: AUiUserThumbnailInfo?   //合唱者信息
}

/// 合唱Service
public protocol AUiChorusServiceDelegate: AUiCommonServiceDelegate {
    
    /// 绑定响应协议
    func bindRespDelegate(delegate: AUiChorusRespDelegate)
    
    /// 解绑响应协议
    /// - Parameter delegate: <#delegate description#>
    func unbindRespDelegate(delegate: AUiChorusRespDelegate)
    
    /// 获取合唱者列表
    /// - Parameter completion: <#completion description#>
    func getChoristersList(completion: (Error?, [AUiChoristerModel]?)->())
    
    /// 加入合唱
    /// - Parameters:
    ///   - completion: <#completion description#>
    func joinChorus(songCode: String, userId: String?, completion: @escaping AUiCallback)
    
    /// 退出合唱
    /// - Parameter completion: <#completion description#>
    func leaveChorus(songCode: String, userId: String?, completion: @escaping AUiCallback)
    
    /// 切换角色
    /// - Parameters:
    ///   - newRole: <#newRole description#>
    ///   - stateCallBack: <#stateCallBack description#>
    func switchSingerRole(newRole: KTVSingRole,
                          stateCallBack: @escaping (KTVSwitchRoleState, KTVSwitchRoleFailReason)->())
}


/// 合唱响应协议
public protocol AUiChorusRespDelegate: NSObject {
    
    /// 合唱者加入
    /// - Parameter chorus: <#chorus description#>
    func onChoristerDidEnter(chorister: AUiChoristerModel)
    
    /// 合唱者离开
    /// - Parameter chorister: <#chorister description#>
    func onChoristerDidLeave(chorister: AUiChoristerModel)
    
    /// 角色切换回调
    /// - Parameters:
    ///   - oldRole: <#oldRole description#>
    ///   - newRole: <#newRole description#>
    func onSingerRoleChanged(oldRole: KTVSingRole, newRole: KTVSingRole)
}
