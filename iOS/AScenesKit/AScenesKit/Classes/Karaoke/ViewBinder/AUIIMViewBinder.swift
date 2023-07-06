//
//  AUIIMViewBinder.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/5/31.
//

import UIKit
import AUIKit

@objcMembers open class AUIIMViewBinder: NSObject {
    
    private weak var chatView: IAUIChatListView?
    
    private weak var chatDelegate: AUIMManagerServiceDelegate? {
        didSet {
            chatDelegate?.unbindRespDelegate(delegate: self)
            chatDelegate?.bindRespDelegate(delegate: self)
        }
    }
    
    public func bind(chat: IAUIChatListView, chatService: AUIMManagerServiceDelegate) {
        self.chatView = chat
        self.chatDelegate = chatService
    }

}

@objc extension AUIIMViewBinder {
    func sendTextMessage(text: String) {
        guard let channelName = self.chatDelegate?.getChannelName() else {
            AUIToast.show(text: "ChatroomId can't be empty,when you send message.")
            return
        }
        self.chatDelegate?.sendMessage(roomId: channelName, text: text, completion: { message, error in
            if error == nil,message != nil {
                self.chatView?.showNewMessage(entity: self.convertTextMessageToRenderEntity(message: message!))
            } else {
                AUIToast.show(text: "Send message failed!")
            }
        })
    }
}

@objc extension AUIIMViewBinder: AUIMManagerRespDelegate {
    
    public func messageDidReceive(roomId: String, message: AgoraChatTextMessage) {
        self.chatView?.showNewMessage(entity: self.convertTextMessageToRenderEntity(message: message))
    }
    
    public func onUserDidJoinRoom(roomId: String, message: AgoraChatTextMessage) {
        self.chatView?.showNewMessage(entity: self.convertJoinMessageToRenderEntity(message: message))
    }
    
    private func convertTextMessageToRenderEntity(message: AgoraChatTextMessage) -> AUIChatEntity {
        let entity = AUIChatEntity()
        entity.messageId = message.messageId
        entity.user = message.user ?? AUIUserThumbnailInfo()
        entity.content = message.content
        entity.messageId = message.messageId
        entity.joined = false
        entity.attributeContent = entity.attributeContent
        entity.width = entity.width
        entity.height = entity.height
        return entity
    }
    
    private func convertJoinMessageToRenderEntity(message: AgoraChatTextMessage) -> AUIChatEntity {
        let entity = AUIChatEntity()
        entity.messageId = message.messageId
        entity.user = message.user ?? AUIUserThumbnailInfo()
        entity.content = "Joined".a.localize(type: .chat)
        entity.joined = true
        entity.attributeContent = entity.attributeContent
        entity.width = entity.width
        entity.height = entity.height
        return entity
    }
}

