//
//  RoomViewController.swift
//  AScenesKit_Example
//
//  Created by wushengtao on 2023/3/9.
//  Copyright © 2023 CocoaPods. All rights reserved.
//

import Foundation
import UIKit
import AScenesKit
import AUIKitCore

class RoomViewController: UIViewController {
    var roomInfo: AUIRoomInfo?
    var themeIdx = 0
    private var karaokeView: AUIKaraokeRoomView?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = .white
        
        self.navigationItem.title = roomInfo?.roomName
        
        let uid = KaraokeUIKit.shared.commonConfig?.userId ?? ""
        //创建房间容器
        let karaokeView = AUIKaraokeRoomView(frame: self.view.bounds)
        let isOwner = roomInfo?.owner?.userId == uid ? true : false
        karaokeView.onClickOffButton = { [weak self] in
            aui_info("onClickOffButton", tag: "RoomViewController")
            AUIAlertView.theme_defaultAlert()
                .contentTextAligment(textAlignment: .center)
                .title(title: isOwner ? "解散房间" : "离开房间")
                .content(content: isOwner ? "确定解散该房间吗?" : "确定离开该房间吗？")
                .leftButton(title: "取消")
                .rightButton(title: "确定")
                .rightButtonTapClosure {
                    guard let self = self else {return}
                    self.karaokeView?.onBackAction()
                    self.navigationController?.popViewController(animated: true)
                    aui_info("rightButtonTapClosure", tag: "RoomViewController")
                }.leftButtonTapClosure {
                    aui_info("leftButtonTapClosure", tag: "RoomViewController")
                }
                .show()
        }
        self.view.addSubview(karaokeView)
        self.karaokeView = karaokeView
        
        KaraokeUIKit.shared.launchRoom(roomId: roomInfo?.roomId ?? "",
                                       karaokeView: karaokeView) {[weak self] error in
            guard let self = self else {return}
            if let _ = error { return }
            //订阅房间被销毁回调
            KaraokeUIKit.shared.bindRespDelegate(delegate: self)
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        navigationController?.isNavigationBarHidden = true
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        KaraokeUIKit.shared.destroyRoom(roomId: roomInfo?.roomId ?? "")
        KaraokeUIKit.shared.unbindRespDelegate(delegate: self)
    }
    
    override func willMove(toParent parent: UIViewController?) {
        super.willMove(toParent: parent)
        if parent == nil {
            navigationController?.isNavigationBarHidden = false
        }
    }
}

extension RoomViewController: AUIKaraokeRoomServiceRespDelegate {
    func onRoomAnnouncementChange(roomId: String, announcement: String) {
    }
    
    func onRoomUserBeKicked(roomId: String, userId: String) {
    }
    
    func onRoomDestroy(roomId: String) {
        self.karaokeView?.onBackAction()
        AUIAlertView()
            .background(color: UIColor(red: 0.1055, green: 0.0062, blue: 0.4032, alpha: 1))
            .isShowCloseButton(isShow: false)
            .title(title: "房间已销毁")
            .titleColor(color: .white)
            .rightButton(title: "确认")
            .rightButtonTapClosure(onTap: {[weak self] text in
                guard let self = self else { return }
                self.navigationController?.popViewController(animated: true)
            })
            .show(fromVC: self)
    }
    
    func onRoomInfoChange(roomId: String, roomInfo: AUIRoomInfo) {
        
    }
    
    func onTokenPrivilegeWillExpire(channelName: String?) {
        KaraokeUIKit.shared.onTokenPrivilegeWillExpire(channelName: channelName)
    }
}
