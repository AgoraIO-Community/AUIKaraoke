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
        guard let room = self.roomInfo else {
            assert(false)
            return
        }
        self.navigationItem.title = room.roomName

        let uid = KaraokeUIKit.shared.commonConfig?.owner?.userId ?? ""
        // 创建房间容器
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
        let roomConfig = AUIRoomConfig()
        if isOwner {
#if DEBUG
            roomConfig.generateToken = { [weak self] roomId in
                self?.generateToken(channelName: roomId,
                                    roomConfig: roomConfig,
                                    completion: { error in
                    roomConfig.generateTokenCompletion?(error as NSError?)
                })
            }
#else
            #error("remove it")
#endif
            KaraokeUIKit.shared.createRoom(roomInfo: room,
                                           roomConfig: roomConfig,
                                           karaokeView: karaokeView) {[weak self] roomInfo, error in
                guard let self = self else {return}
                if let error = error {
                    AUIToast.show(text: error.localizedDescription)
                    return
                }
                self.roomInfo = roomInfo
                // 订阅房间被销毁回调
                KaraokeUIKit.shared.bindRespDelegate(delegate: self)
            }
        } else {
            let roomId = roomInfo?.roomId ?? ""
            generateToken(channelName: roomId,
                          roomConfig: roomConfig) {[weak self] err  in
                KaraokeUIKit.shared.enterRoom(roomId: roomId,
                                              roomConfig: roomConfig,
                                              karaokeView: karaokeView) { error in
                    guard let self = self else {return}
                    if let error = error {
                        AUIToast.show(text: error.localizedDescription)
                        return
                    }
                    // 订阅房间被销毁回调
                    KaraokeUIKit.shared.bindRespDelegate(delegate: self)
                }
            }
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

    private func generateToken(channelName: String,
                               roomConfig: AUIRoomConfig,
                               completion: @escaping ((Error?) -> Void)) {
        let uid = KaraokeUIKit.shared.commonConfig?.owner?.userId ?? ""
        let rtcChannelName = "\(channelName)_rtc"
        let rtcChorusChannelName = "\(channelName)_rtc_ex"
        roomConfig.channelName = channelName
        roomConfig.rtcChannelName = rtcChannelName
        roomConfig.rtcChorusChannelName = rtcChorusChannelName
        print("generateTokens: \(uid)")

        let group = DispatchGroup()

        var err: Error?
        group.enter()
        let tokenModel1 = AUITokenGenerateNetworkModel()
        tokenModel1.channelName = channelName
        tokenModel1.userId = uid
        tokenModel1.request { error, result in
            defer {
                if err == nil {
                    err = error
                }
                group.leave()
            }
            guard let tokenMap = result as? [String: String], tokenMap.count >= 2 else {return}
            roomConfig.rtcToken007 = tokenMap["rtcToken"] ?? ""
            roomConfig.rtmToken007 = tokenMap["rtmToken"] ?? ""
        }

        group.enter()
        let tokenModel2 = AUITokenGenerateNetworkModel()
        tokenModel2.channelName = rtcChannelName
        tokenModel2.userId = uid
        tokenModel2.request { error, result in
            defer {
                if err == nil {
                    err = error
                }
                group.leave()
            }
            guard let tokenMap = result as? [String: String], tokenMap.count >= 2 else {return}
            roomConfig.rtcRtcToken = tokenMap["rtcToken"] ?? ""
            roomConfig.rtcRtmToken = tokenMap["rtmToken"] ?? ""
        }

        group.enter()
        let tokenModel3 = AUITokenGenerateNetworkModel()
        tokenModel3.channelName = rtcChorusChannelName
        tokenModel3.userId = uid
        tokenModel3.request { error, result in
            defer {
                if err == nil {
                    err = error
                }
                group.leave()
            }

            guard let tokenMap = result as? [String: String], tokenMap.count >= 2 else {return}

            roomConfig.rtcChorusRtcToken = tokenMap["rtcToken"] ?? ""
        }

        group.notify(queue: DispatchQueue.main) {
            completion(err)
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
            .rightButtonTapClosure(onTap: {[weak self] _ in
                guard let self = self else { return }
                self.navigationController?.popViewController(animated: true)
            })
            .show(fromVC: self)
    }

    func onRoomInfoChange(roomId: String, roomInfo: AUIRoomInfo) {
    }

    func onTokenPrivilegeWillExpire(channelName: String?) {
        let config = AUIRoomConfig()
        generateToken(channelName: channelName!, roomConfig: config) { _ in
            KaraokeUIKit.shared.renew(config: config)
        }
    }
}
