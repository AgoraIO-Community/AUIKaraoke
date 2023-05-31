//
//  AUIKaraokeRoomView.swift
//  AUIScenesKit
//
//  Created by wushengtao on 2023/2/23.
//

import AUIKit
import SwiftTheme
import UIKit
import AgoraRtcKit

private let kSeatRoomPadding: CGFloat = 16

/// 卡拉OK 房间容器，负责初始化卡拉OK Service和各个子组件，并负责子组件和子Service的绑定工作
open class AUIKaraokeRoomView: UIView {
    private var service: AUIKaraokeRoomService?
    
    /// 房间信息UI
    private lazy var roomInfoView: AUIRoomInfoView = AUIRoomInfoView(frame: CGRect(x: 16, y: 35, width: 175, height: 56))
    
    /// 歌词播放UI
    private lazy var playerView: AUIPlayerView = AUIPlayerView(frame: CGRect(x: kSeatRoomPadding, y: 107, width: self.bounds.size.width - kSeatRoomPadding * 2, height: 252))
    private lazy var playerBinder: AUIPlayerViewBinder = AUIPlayerViewBinder()
    
    //麦位UI
    private lazy var micSeatView: AUIMicSeatView = AUIMicSeatView(frame: CGRect(x: kSeatRoomPadding, y: 375, width: self.bounds.size.width - kSeatRoomPadding * 2, height: 220))
    private lazy var micSeatBinder: AUIMicSeatViewBinder = AUIMicSeatViewBinder(rtcEngine: service!.rtcEngine)
    
//    private lazy var invitationView: AUIInvitationView = AUIInvitationView()
    
    /// 背景图片
    private lazy var bgImageView: UIImageView = UIImageView(frame: bounds)
    
    /// 背景遮罩
    private lazy var bgMaskView: UIView = UIView(frame: bounds)
    
    /// 点歌器UI
    private lazy var jukeBoxView: AUIJukeBoxView = AUIJukeBoxView()
    private lazy var jukeBoxBinder: AUIJukeBoxViewBinder = AUIJukeBoxViewBinder()
    
    
    /// 用户列表UI
    private lazy var membersView: AUIRoomMembersView = AUIRoomMembersView()
    private lazy var userBinder: AUIUserViewBinder = AUIUserViewBinder()
    
    // 关闭按钮
    private lazy var closeButton: AUIButton = {
        let theme = AUIButtonDynamicTheme()
        theme.icon = auiThemeImage("Room.offBtnIcon")
        theme.iconWidth = "Room.offBtnIconWidth"
        theme.iconHeight = "Room.offBtnIconHeight"
        theme.buttonWidth = "Room.offBtnWidth"
        theme.buttonHeight = "Room.offBtnHeight"
        theme.backgroundColor = "Room.offBtnBgColor"
        theme.cornerRadius = "Room.offBtnCornerRadius"
        
        let button = AUIButton()
        button.style = theme
        button.addTarget(self, action: #selector(didClickOffButton), for: .touchUpInside)
        
        return button
    }()
    
    // 聊天按钮
    private lazy var chatButton: AUIButton = {
        let theme = AUIButtonDynamicTheme()
        theme.icon = auiThemeImage("Room.chatBtnIcon")
        theme.iconWidth = "Room.chatBtnIconWidth"
        theme.iconHeight = "Room.chatBtnIconHeight"
        theme.buttonWidth = "Room.chatBtnWidth"
        theme.buttonHeight = "Room.chatBtnHeight"
        theme.backgroundColor = "Room.chatBtnBgColor"
        theme.cornerRadius = "Room.chatBtnCornerRadius"
        
        let button = AUIButton()
        button.style = theme
        button.addTarget(self, action: #selector(didClickChatButton), for: .touchUpInside)
        
        return button
    }()
    
    // 麦克风开关按钮
    private lazy var microphoneButton: AUIButton = {
        let theme = AUIButtonDynamicTheme()
        theme.selectedIcon = auiThemeImage("Room.microphoneMuteBtnIcon")
        theme.icon = auiThemeImage("Room.microphoneUnmuteBtnIcon")
        theme.iconWidth = "Room.microphoneBtnIconWidth"
        theme.iconHeight = "Room.microphoneBtnIconHeight"
        theme.buttonWidth = "Room.microphoneBtnWidth"
        theme.buttonHeight = "Room.microphoneBtnHeight"
        theme.backgroundColor = "Room.microphoneBtnBgColor"
        theme.cornerRadius = "Room.microphoneBtnCornerRadius"
        
        let button = AUIButton()
        button.style = theme
        button.addTarget(self, action: #selector(didClickVoiceChatButton(_:)), for: .touchUpInside)
        
        return button
    }()
    
    
    /// 摄像头开关
    private lazy var cameraButton: AUIButton = {
        let theme = AUIButtonDynamicTheme()
        theme.selectedIcon = auiThemeImage("Room.cameraMuteBtnIcon")
        theme.icon = auiThemeImage("Room.cameraUnmuteBtnIcon")
        theme.iconWidth = "Room.cameraBtnIconWidth"
        theme.iconHeight = "Room.cameraBtnIconHeight"
        theme.buttonWidth = "Room.cameraBtnWidth"
        theme.buttonHeight = "Room.cameraBtnHeight"
        theme.backgroundColor = "Room.cameraBtnBgColor"
        theme.cornerRadius = "Room.cameraBtnCornerRadius"
        
        let button = AUIButton()
        button.isSelected = true
        button.style = theme
        button.addTarget(self, action: #selector(onMuteCameraAction(_:)), for: .touchUpInside)
        
        return button
    }()
    
    /// 更多按钮
    private lazy var moreButton: AUIButton = {
        let theme = AUIButtonDynamicTheme()
        theme.icon = auiThemeImage("Room.moreBtnIcon")
        theme.iconWidth = "Room.moreBtnIconWidth"
        theme.iconHeight = "Room.moreBtnIconHeight"
        theme.buttonWidth = "Room.moreBtnWidth"
        theme.buttonHeight = "Room.moreBtnHeight"
        theme.backgroundColor = "Room.moreBtnBgColor"
        theme.cornerRadius = "Room.moreBtnCornerRadius"
        
        let button = AUIButton()
        button.style = theme
        button.addTarget(self, action: #selector(onMoreAction(_:)), for: .touchUpInside)
        
        return button
    }()
    
    // 礼物按钮
    private lazy var giftButton: AUIButton = {
        let theme = AUIButtonDynamicTheme()
        theme.icon = auiThemeImage("Room.giftBtnIcon")
        theme.iconWidth = "Room.giftBtnIconWidth"
        theme.iconHeight = "Room.giftBtnIconHeight"
        theme.buttonWidth = "Room.giftBtnWidth"
        theme.buttonHeight = "Room.giftBtnHeight"
        theme.backgroundColor = "Room.giftBtnBgColor"
        theme.cornerRadius = "Room.giftBtnCornerRadius"
        
        let button = AUIButton()
        button.style = theme
        button.addTarget(self, action: #selector(didClickGiftChatButton), for: .touchUpInside)
        
        return button
    }()
    
    
    public var onClickOffButton: (()->())?
    
    deinit {
        aui_info("deinit AUIKaraokeRoomView", tag: "AUIKaraokeRoomView")
    }
    
    public override init(frame: CGRect) {
        aui_info("init AUIKaraokeRoomView", tag: "AUIKaraokeRoomView")
        super.init(frame: frame)
        
        //设置皮肤路径
        if let folderPath = Bundle.main.path(forResource: "auiKaraokeTheme", ofType: "bundle") {
            AUIRoomContext.shared.addThemeFolderPath(path: URL(fileURLWithPath: folderPath) )
        }
        
        loadBg()
    }
    
    required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func loadBg() {
        aui_info("loadBg", tag: "AUIKaraokeRoomView")
        
        //背景图片
        addSubview(bgImageView)
        bgImageView.theme_image = auiThemeImage("Global.backgroundImage")
        //蒙版，渐变或模糊背景图片
        addSubview(bgMaskView)
        bgMaskView.theme_backgroundColor = "Global.maskBackgroundColor"
        
        //关闭
        addSubview(closeButton)
        
        closeButton.aui_centerY = roomInfoView.aui_centerY
        closeButton.aui_right = aui_width - 15
    }
    
    public func bindService(service: AUIKaraokeRoomService) {
        self.service = service
        loadSubviews()
        viewBinderConnected()
        
        let channelName:String = service.channelName
        aui_info("enter room: \(channelName)", tag: "AUIKaraokeRoomView")
        service.roomManagerImpl.enterRoom(roomId: channelName) { error in
            aui_info("enter room success", tag: "AUIKaraokeRoomView")
        }
        service.joinRtcChannel { error in
            aui_info("joinRtcChannel finished: \(error?.localizedDescription ?? "success")", tag: "AUIKaraokeRoomView")
        }
    }
    
    private func loadSubviews() {
        aui_info("load karaoke room subview", tag: "AUIKaraokeRoomView")
        
//        let folderPath = Bundle.main.path(forResource: "auiTheme", ofType: "bundle")
//        AUIThemeManager.shared.loadTheme(themeFolderPath: folderPath!)
        
        //歌词组件
        playerView.selectSongButton.addTarget(self, action: #selector(onSelectedMusic), for: .touchUpInside)
        addSubview(playerView)
        
        //麦位组件
        addSubview(micSeatView)
        
        jukeBoxView.aui_size = CGSize(width: aui_width, height: 562)
        
        addSubview(roomInfoView)
        
        addSubview(chatButton)
        addSubview(microphoneButton)
//        addSubview(cameraButton)
//        addSubview(moreButton)
        addSubview(giftButton)
        
        addSubview(membersView)
        membersView.aui_centerY = roomInfoView.aui_centerY
        membersView.aui_right = closeButton.aui_left - 8
        
        
        chatButton.aui_bottom = bounds.height - 15 - UIDevice.current.aui_SafeDistanceBottom
        chatButton.aui_left = 15
        
        giftButton.aui_bottom = chatButton.aui_bottom
        giftButton.aui_right = aui_width - 15
        
//        microphoneButton.aui_bottom = chatButton.aui_bottom
//        microphoneButton.aui_left = chatButton.aui_right + 8
//
//        cameraButton.aui_bottom = chatButton.aui_bottom
//        cameraButton.aui_left = microphoneButton.aui_right + 8
//
//        moreButton.aui_bottom = chatButton.aui_bottom
//        moreButton.aui_left = cameraButton.aui_right + 8
//
        
        
        microphoneButton.aui_bottom = chatButton.aui_bottom
        microphoneButton.aui_right = giftButton.aui_left - 8
        
    }
    
    private func viewBinderConnected() {
        aui_info("viewBinderConnected", tag: "AUIKaraokeRoomView")
        
        guard let service = service else {
            assert(false, "service is empty")
            aui_error("service is empty", tag: "AUIKaraokeRoomView")
            return
        }
        
        //绑定Service
        micSeatBinder.bind(micSeatView: micSeatView,
                           micSeatService: service.micSeatImpl,
                           userService: service.userImpl,
                           musicSeatService: service.musicImpl,
                           chorusService: service.chorusImpl)
//        invitationView.invitationdelegate = service.invitationImpl
//        invitationView.roomDelegate = service.roomManagerImpl
        jukeBoxBinder.bind(jukeBoxView: jukeBoxView, service: service.musicImpl)

        playerBinder.bind(playerView: playerView,
                          playerService: service.playerImpl,
                          micSeatService: service.micSeatImpl,
                          musicService: service.musicImpl,
                          chorusService: service.chorusImpl)
        playerView.addActionHandler(playerViewActionHandler: self)
        
        service.micSeatImpl.bindRespDelegate(delegate: self)
        microphoneButton.isHidden = !AUIRoomContext.shared.isRoomOwner(channelName: service.channelName)
        
        userBinder.bind(userView: membersView,
                        userService: service.userImpl,
                        micSeatService: service.micSeatImpl)
        
        if let roomInfo = AUIRoomContext.shared.roomInfoMap[service.channelName] {
            self.roomInfoView.updateRoomInfo(withRoomId: roomInfo.roomId, roomName: roomInfo.roomName, ownerHeadImg: roomInfo.owner?.userAvatar)
        }
    }
}

extension AUIKaraokeRoomView {
    @objc public func onBackAction() {
        guard let service = service else {return}
        if AUIRoomContext.shared.isRoomOwner(channelName: service.channelName) {
            service.roomManagerImpl.destroyRoom(roomId: service.channelName) { err in
            }
        } else {
            service.roomManagerImpl.exitRoom(roomId: service.channelName) { err in
            }
        }
        service.destory()
        AUIRoomContext.shared.clean(channelName: service.channelName)
    }
    
    @objc func onSelectedMusic() {
        aui_info("onSelectedMusic", tag: "AUIKaraokeRoomView")
    }
}

//MARK: AUIPlayerViewDelegate
extension AUIKaraokeRoomView: AUIPlayerViewDelegate {
    public func onButtonTapAction(playerView: AUIPlayerView, actionType: AUIPlayerViewButtonType) {
        if actionType == .selectSong {
            AUICommonDialog.show(contentView: jukeBoxView, theme: AUICommonDialogTheme())
        }
    }
    
    public func onVoiceConversionDidChanged(playerView: AUIPlayerView, index: Int) {
    }
}


extension AUIKaraokeRoomView {
    @objc private func didClickOffButton(){
        onClickOffButton?()
    }
    
    @objc private func didClickChatButton(){
        //Mock
//        if playerView.musicInfo == nil {
//            let musicInfo = AUIChooseMusicModel()
//            musicInfo.name = "I Get the bag (feat. Migos)"
//            playerView.musicInfo = musicInfo
//        } else {
//            playerView.musicInfo = nil
//        }
    }
    
    @objc private func didClickVoiceChatButton(_ button: UIButton){
        button.isSelected = !button.isSelected
        service?.userImpl.muteUserAudio(isMute: button.isSelected) { err in
        }
    }
    
    @objc private func onMuteCameraAction(_ button: UIButton){
        button.isSelected = !button.isSelected
        service?.userImpl.muteUserVideo(isMute: button.isSelected) { err in
        }
    }
    
    @objc private func onMoreAction(_ button: UIButton){

        let item = AUIActionSheetThemeItem.vertical()
        item.backgroundIcon = "Player.voiceConversionDialogItemBackgroundIcon"
        item.icon = "Room.moreDialogLrcBackgroundIcon"
        item.title = auikaraoke_localized("lrcBackground")
        item.callback = { [weak self] in
            aui_info("onMoreAction click", tag: "AUIKaraokeRoomView")
            guard let self = self else {return}
            
        }
        
        let theme = AUIActionSheetTheme()
        theme.itemType = "Player.voiceConversionDialogItemType"
        theme.itemHeight = "Player.voiceConversionDialogItemHeight"
        theme.collectionViewTopEdge = "Player.collectionViewTopEdge"
        let dialogView = AUIActionSheet(title: auikaraoke_localized("more"),
                                        items: [item],
                                        headerInfo: nil)
        dialogView.setTheme(theme: theme)
        AUICommonDialog.show(contentView: dialogView, theme: AUICommonDialogTheme())
    }
    
    @objc private func didClickGiftChatButton(){
    }
}

extension AUIKaraokeRoomView: AUIMicSeatRespDelegate {
    public func onAnchorEnterSeat(seatIndex: Int, user: AUIKit.AUIUserThumbnailInfo) {
        if user.userId == service?.userImpl.getRoomContext().currentUserInfo.userId {
            microphoneButton.isHidden = false
        }
    }
    
    public func onAnchorLeaveSeat(seatIndex: Int, user: AUIKit.AUIUserThumbnailInfo) {
        if user.userId == service?.userImpl.getRoomContext().currentUserInfo.userId {
            microphoneButton.isHidden = true
        }
    }
    
    public func onSeatAudioMute(seatIndex: Int, isMute: Bool) {
        
    }
    
    public func onSeatVideoMute(seatIndex: Int, isMute: Bool) {
        
    }
    
    public func onSeatClose(seatIndex: Int, isClose: Bool) {
        
    }
    
    
}

