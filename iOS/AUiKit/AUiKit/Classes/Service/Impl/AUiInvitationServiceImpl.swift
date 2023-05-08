//
//  AUiInvitationServiceImpl.swift
//  AUiKit
//
//  Created by wushengtao on 2023/2/23.
//

import Foundation
import AgoraRtcKit

//邀请Service实现
open class AUiInvitationServiceImpl: NSObject {
    private var respDelegates: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    private var channelName: String!
    private var rtmManager: AUiRtmManager!
    
    deinit {
        aui_info("deinit AUiInvitationServiceImpl", tag: "AUiInvitationServiceImpl")
    }
    
    init(channelName: String, rtmManager: AUiRtmManager) {
        self.channelName = channelName
        self.rtmManager = rtmManager
        super.init()
        
        aui_info("init AUiInvitationServiceImpl", tag: "AUiInvitationServiceImpl")
    }
}

extension AUiInvitationServiceImpl: AUiInvitationServiceDelegate {
    public func getChannelName() -> String {
        return channelName
    }
    
    public func bindRespDelegate(delegate: AUiInvitationRespDelegate) {
        respDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUiInvitationRespDelegate) {
        respDelegates.remove(delegate)
    }
    
    public func sendInvitation(cmd: String, userId: String, content: String, callback: (Error?) -> ()) -> String {
        return ""
    }
    
    public func acceptInvitation(id: String, callback: (Error?) -> ()) {
        
    }
    
    public func rejectInvitation(id: String, callback: (Error?) -> ()) {
        
    }
    
    public func cancelInvitation(id: String, callback: (Error?) -> ()) {
        
    }
}
