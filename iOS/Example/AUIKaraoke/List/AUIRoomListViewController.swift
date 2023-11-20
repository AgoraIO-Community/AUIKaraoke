//
//  AUIRoomListViewController.swift
//  AUICell
//
//  Created by zhaoyongqiang on 2023/4/11.
//

import UIKit
import AScenesKit
import AUIKitCore
import MJRefresh
import SwiftTheme


private let kButtonWidth: CGFloat = 327
private let kListCountPerPage: Int = 10
class AUIRoomListViewController: UIViewController {
    private var roomList: [AUIRoomInfo] = []
    private var userInfo: UserInfo = UserInfo()
    
    private lazy var collectionView: UICollectionView = {
       let flowLayout = UICollectionViewFlowLayout()
        flowLayout.scrollDirection = .vertical
        flowLayout.minimumLineSpacing = 8
        flowLayout.minimumInteritemSpacing = 8
        let w = (UIScreen.main.bounds.width - 8 * 3) * 0.5
        flowLayout.itemSize = CGSize(width: w, height: 180)
        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: flowLayout)
        collectionView.dataSource = self
        collectionView.delegate = self
        collectionView.register(AUIRoomListCell.self, forCellWithReuseIdentifier: "cell")
        collectionView.backgroundColor = .clear
        return collectionView
    }()
    
    private lazy var createButton: AUIButton = {
        let button = AUIButton()
        let style = AUIButtonDynamicTheme()
        style.backgroundColor = "CommonColor.primary"
        style.buttonWidth = ThemeCGFloatPicker(floats: 155)
        style.buttonHeight = ThemeCGFloatPicker(floats: 50)
        style.iconWidth =  ThemeCGFloatPicker(floats: 24)
        style.iconHeight =  ThemeCGFloatPicker(floats: 24)
        style.icon = ThemeAnyPicker(keyPath: "ListViewController.create_room_icon")
        button.textImageAlignment = .imageLeftTextRight
        button.style = style
        button.layoutIfNeeded()
        button.setTitle("创建房间", for: .normal)
        button.setGradient([UIColor(red: 0, green: 158/255.0, blue: 1, alpha: 1),UIColor(red: 124/255.0, green: 91/255.0, blue: 1, alpha: 1)],[CGPoint(x: 0, y: 0),CGPoint(x: 0, y: 1)])
        button.addTarget(self, action: #selector(self.onCreateAction), for: .touchUpInside)
        return button
    }()
    
    private lazy var themeButton: AUIButton = {
        let button = AUIButton()
        let style = AUIButtonDynamicTheme()
        style.buttonWidth = ThemeCGFloatPicker(floats: 40)
        style.buttonHeight = ThemeCGFloatPicker(floats: 40)
        style.iconWidth = ThemeCGFloatPicker(floats: 40)
        style.iconHeight = ThemeCGFloatPicker(floats: 40)

        style.icon = ThemeAnyPicker(keyPath: "ListViewController.setting_button_icon")
        button.style = style
        button.layoutIfNeeded()
        button.addTarget(self, action: #selector(self.onThemeChangeAction), for: .touchUpInside)
        return button
    }()
    
    private lazy var emptyView: AUIListEmptyView = {
        let emptyView = AUIListEmptyView(frame: CGRect(x: 0, y: 0, width: 200, height: 150))
        emptyView.center = view.center
        emptyView.setImage(auiThemeImage("ListViewController.list_empty"), title: "暂无房间列表")
        return emptyView
    }()
    
    private lazy var gradientLayer: CAGradientLayer = {
        let gradientLayer = CAGradientLayer()
        gradientLayer.locations = [0, 1]
        gradientLayer.startPoint = CGPoint.zero
        gradientLayer.endPoint = CGPoint(x: 0, y: 1)
        return gradientLayer
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
//        AUIThemeManager.shared.themeNames = ["UIKit", "KTV"]
        AUIThemeManager.shared.resetTheme()
        UIScrollView.appearance().contentInsetAdjustmentBehavior = .never
        view.theme_backgroundColor = AUIColor("ListViewController.backgroudColor")

        collectionView.mj_header = MJRefreshNormalHeader(refreshingBlock: { [weak self] in
            self?.onRefreshAction()
        })
        view.addSubview(collectionView)
        view.addSubview(createButton)
        view.addSubview(themeButton)
        collectionView.frame = view.bounds
        _layoutButton()
        initEngine()
        onRefreshAction()
        
        collectionView.addSubview(emptyView)
        
        //设置皮肤路径
        if let folderPath = Bundle.main.path(forResource: "exampleTheme", ofType: "bundle") {
            AUIThemeManager.shared.addThemeFolderPath(path: URL(fileURLWithPath: folderPath) )
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController?.setNavigationBarHidden(true, animated: false)
    }
    
    private func initEngine() {
        //设置基础信息到KaraokeUIKit里
        let commonConfig = AUICommonConfig()
        commonConfig.appId = KeyCenter.AppId
        commonConfig.host = KeyCenter.HostUrl
        let ownerInfo = AUIUserThumbnailInfo()
        ownerInfo.userId = userInfo.userId
        ownerInfo.userName = userInfo.userName
        ownerInfo.userAvatar = userInfo.userAvatar
        commonConfig.owner = ownerInfo
        KaraokeUIKit.shared.setup(commonConfig: commonConfig,
                                  apiConfig: nil)
    }
    
    private func _layoutButton() {
        let window = UIApplication.shared.windows.first
        let bottomSafeAreaInset = window?.safeAreaInsets.bottom ?? 0
        createButton.frame = CGRect(origin: CGPoint(x: (view.frame.width - createButton.frame.width) / 2,
                                                    y: view.frame.height - 74 - bottomSafeAreaInset),
                                    size: createButton.frame.size)
        
        themeButton.frame = CGRect(origin: CGPoint(x: view.aui_width - themeButton.aui_width - 20, y: 40),
                                   size: themeButton.frame.size)
    }
    
    func onRefreshAction() {
        self.roomList = []
        self.collectionView.reloadData()
        self.collectionView.mj_footer = nil
        KaraokeUIKit.shared.getRoomInfoList(lastCreateTime: 0, pageSize: kListCountPerPage, callback: {[weak self] error, list in
            guard let self = self else {return}
            defer {
                self.collectionView.mj_header?.endRefreshing()
            }
            if let error = error {
                AUIToast.show(text: error.localizedDescription)
                return
            }
            self.roomList = list ?? []
            self.emptyView.isHidden = self.roomList.count > 0
            self.collectionView.reloadData()

            if self.roomList.count == kListCountPerPage {
                self.collectionView.mj_footer = MJRefreshAutoNormalFooter(refreshingBlock: { [weak self] in
                    self?.onLoadMoreAction()
                })
            }
            self._layoutButton()
        })
    }
    
    func onLoadMoreAction() {
        let lastCreateTime = roomList.last?.createTime
        KaraokeUIKit.shared.getRoomInfoList(lastCreateTime: lastCreateTime ?? 0, pageSize: kListCountPerPage, callback: {[weak self] error, list in
            guard let self = self else {return}
            self.roomList += list ?? []
            self.collectionView.reloadData()
            self.collectionView.mj_footer?.endRefreshing()

            if list?.count ?? 0 < kListCountPerPage {
                self.collectionView.mj_footer?.endRefreshingWithNoMoreData()
            }
            self._layoutButton()
        })
    }
    
    
    @objc func onCreateAction() {
        AUIAlertView()
            .theme_background(color: "ListViewController.alertBgColor")
            .isShowCloseButton(isShow: true)
            .title(title: "房间主题")
            .theme_titleColor(color: "ListViewController.alertTitleColor")
            .rightButton(title: "一起嗨歌")
            .theme_rightButtonBackground(color: "CommonColor.primary")
            .rightButtonTapClosure(onTap: {[weak self] text in
                guard let self = self else { return }
                guard let text = text, text.count > 0 else {
                    AUIToast.show(text: "请输入房间名")
                    return
                }
                print("create room with name(\(text))")
                
                let room = AUIRoomInfo()
                room.roomName = text
                room.thumbnail = self.userInfo.userAvatar
                room.micSeatCount = 8
                room.owner = AUIRoomContext.shared.currentUserInfo
                let vc = RoomViewController()
                vc.roomInfo = room
                self.navigationController?.pushViewController(vc, animated: true)
            })
            .textFieldPlaceholder(placeholder: "房间主题")
            .textFieldPlaceholder(color: UIColor(hex: "#5a5a5a"))
            .textFieldPlaceholder(placeholder: "请输入房间主题")
            .theme_textFieldTextColor(color: "ListViewController.alertInputTextColor")
            .textField(text: "room\(arc4random_uniform(99999))")
            .theme_textFieldBackground(color: "ListViewController.alertInputBgColor")
            .textField(cornerRadius: 25)
            .show()
    }
    
    @objc func onThemeChangeAction() {
        let vc = AUIThemeSettingViewController()
        self.navigationController?.pushViewController(vc, animated: true)
    }
}

//extension AUIRoomListViewController: AgoraRtmClientDelegate {
//    func rtmKit(_ rtmKit: AgoraRtmClientKit, onTokenPrivilegeWillExpire channel: String?) {
//        print("rtm token WillExpire channel: \(channel ?? "")")
//    }
//}


extension AUIRoomListViewController: UICollectionViewDataSource, UICollectionViewDelegate, UICollectionViewDelegateFlowLayout {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        self.roomList.count
    }
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "cell", for: indexPath) as! AUIRoomListCell
        cell.roomInfo = self.roomList[indexPath.row]
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAt section: Int) -> UIEdgeInsets {
        UIEdgeInsets(top: 84, left: 8, bottom: 80, right: 8)
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let roomInfo = self.roomList[indexPath.row]
        
//        roomManager?.rtmManager.getUserCount(channelName: roomInfo.roomId) { err, count in
//
//        }
//        return
        
        let vc = RoomViewController()
        vc.roomInfo = roomInfo
        self.navigationController?.pushViewController(vc, animated: true)
    }
}

extension AUIRoomListViewController {
    override func motionEnded(_ motion: UIEvent.EventSubtype, with event: UIEvent?) {
        super.motionEnded(motion, with: event)
        let vc = UIActivityViewController(activityItems: [URL(fileURLWithPath: AUILog.cacheDir())], applicationActivities: nil)
        present(vc, animated: true)
    }
}
