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
import AUiKit

class RoomViewController: UIViewController {
    var roomInfo: AUiRoomInfo?
    var themeIdx = 0
    private var karaokeView: AUiKaraokeRoomView?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = .white
        
        self.navigationItem.title = roomInfo?.roomName
        
        let uid = KaraokeUIKit.shared.roomConfig?.userId ?? ""
        //创建房间容器
        let karaokeView = AUiKaraokeRoomView(frame: self.view.bounds)
        let isOwner = roomInfo?.owner?.userId == uid ? true : false
        karaokeView.onClickOffButton = { [weak self] in
            aui_info("onClickOffButton", tag: "RoomViewController")
            AUiAlertView.theme_defaultAlert()
                .contentTextAligment(textAlignment: .center)
                .title(title: isOwner ? "解散房间" : "离开房间")
                .content(content: isOwner ? "确定解散该房间吗?" : "确定离开该房间吗？")
                .leftButton(title: "取消")
                .rightButton(title: "确定")
                .rightButtonTapClosure {
                    guard let self = self else {return}
                    self.navigationController?.popViewController(animated: true)
                    aui_info("rightButtonTapClosure", tag: "RoomViewController")
                }.leftButtonTapClosure {
                    aui_info("leftButtonTapClosure", tag: "RoomViewController")
                }
                .show()
        }
        self.view.addSubview(karaokeView)
        self.karaokeView = karaokeView
        
        //通过generateToken方法获取到必须的token和appid
        generateToken {[weak self] roomConfig, appId in
            guard let self = self else {return}
            KaraokeUIKit.shared.launchRoom(roomInfo: self.roomInfo!,
                                           appId: appId,
                                           config: roomConfig,
                                           karaokeView: karaokeView) {_ in
            }
            //订阅Token过期回调
            KaraokeUIKit.shared.subscribeError(roomId: self.roomInfo?.roomId ?? "", delegate: self)
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
        self.karaokeView?.onBackAction()
        KaraokeUIKit.shared.destoryRoom(roomId: roomInfo?.roomId ?? "")
        KaraokeUIKit.shared.unsubscribeError(roomId: roomInfo?.roomId ?? "", delegate: self)
        KaraokeUIKit.shared.unbindRespDelegate(delegate: self)
    }
    
    override func willMove(toParent parent: UIViewController?) {
        super.willMove(toParent: parent)
        if parent == nil {
            navigationController?.isNavigationBarHidden = false
        }
    }
    
    private func generateToken(completion:@escaping ((AUiRoomConfig, String)->())) {
        let uid = KaraokeUIKit.shared.roomConfig?.userId ?? ""
        let channelName = roomInfo?.roomId ?? ""
        let rtcChannelName = "\(channelName)_rtc"
        let rtcChorusChannelName = "\(channelName)_rtc_ex"
        let roomConfig = AUiRoomConfig()
        roomConfig.channelName = channelName
        roomConfig.rtcChannelName = rtcChannelName
        roomConfig.rtcChorusChannelName = rtcChorusChannelName
        print("generateTokens: \(uid)")
        
        var appId = ""
        
        let group = DispatchGroup()
        
        group.enter()
        let tokenModel1 = AUiTokenGenerateNetworkModel()
        tokenModel1.channelName = channelName
        tokenModel1.userId = uid
        tokenModel1.request { error, result in
            defer {
                group.leave()
            }
            
            guard let tokenMap = result as? [String: String], tokenMap.count >= 2 else {return}
            
            roomConfig.rtcToken007 = tokenMap["rtcToken"] ?? ""
            roomConfig.rtmToken007 = tokenMap["rtmToken"] ?? ""
            appId = tokenMap["appId"] ?? ""
        }
        
        group.enter()
        let tokenModel2 = AUiTokenGenerate006NetworkModel()
        tokenModel2.channelName = rtcChannelName
        tokenModel2.userId = uid
        tokenModel2.request { error, result in
            defer {
                group.leave()
            }
            
            guard let tokenMap = result as? [String: String], tokenMap.count >= 2 else {return}
            
            roomConfig.rtcRtcToken006 = tokenMap["rtcToken"] ?? ""
            roomConfig.rtcRtmToken006 = tokenMap["rtmToken"] ?? ""
        }
        
        group.enter()
        let tokenModel3 = AUiTokenGenerateNetworkModel()
        tokenModel3.channelName = rtcChorusChannelName
        tokenModel3.userId = uid
        tokenModel3.request { error, result in
            defer {
                group.leave()
            }
            
            guard let tokenMap = result as? [String: String], tokenMap.count >= 2 else {return}
            
            roomConfig.rtcChorusRtcToken007 = tokenMap["rtcToken"] ?? ""
        }
        
        group.notify(queue: DispatchQueue.main) {
            completion(roomConfig, appId)
        }
    }
}

extension RoomViewController: AUiRoomManagerRespDelegate {
    func onRoomDestroy(roomId: String) {
        AUiAlertView()
            .background(color: UIColor(red: 0.1055, green: 0.0062, blue: 0.4032, alpha: 1))
            .isShowCloseButton(isShow: true)
            .title(title: "房间已销毁")
            .titleColor(color: .white)
            .rightButton(title: "确认")
            .rightButtonTapClosure(onTap: {[weak self] text in
                guard let self = self else { return }
                self.navigationController?.popViewController(animated: true)
            })
            .show(fromVC: self)
    }
    
    func onRoomInfoChange(roomId: String, roomInfo: AUiRoomInfo) {
        
    }
}


extension RoomViewController: AUiRtmErrorProxyDelegate {
    @objc func onTokenPrivilegeWillExpire(channelName: String?) {
        generateToken { config, _ in
            KaraokeUIKit.shared.renew(config: config)
        }
    }
}
