//
//  AUiRoomListViewController.swift
//  AUiCell
//
//  Created by zhaoyongqiang on 2023/4/11.
//

import UIKit
import AScenesKit
import AUiKit
import MJRefresh
import SwiftTheme

let kAppId: String = "925dd81d763a42919862fee9f3f204a7"

private let kButtonWidth: CGFloat = 327
private let kListCountPerPage: Int = 10
class AUiRoomListViewController: UIViewController {
    private var roomList: [AUiRoomInfo] = []
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
        collectionView.register(AUiRoomListCell.self, forCellWithReuseIdentifier: "cell")
        collectionView.backgroundColor = .clear
        return collectionView
    }()
    
    private lazy var createButton: AUiButton = {
        let button = AUiButton()
        let style = AUiButtonDynamicTheme()
        style.backgroundColor = "CommonColor.primary"
        style.buttonWitdth = ThemeCGFloatPicker(floats: 327)
        button.style = style
        button.layoutIfNeeded()
        button.setTitle("创建房间", for: .normal)
        button.addTarget(self, action: #selector(self.onCreateAction), for: .touchUpInside)
        return button
    }()
    
    private lazy var themeButton: AUiButton = {
        let button = AUiButton()
        let style = AUiButtonDynamicTheme()
        style.backgroundColor = "CommonColor.primary"
        style.buttonWitdth = ThemeCGFloatPicker(floats: 327)
        button.style = style
        button.layoutIfNeeded()
        button.setTitle("换肤", for: .normal)
        button.addTarget(self, action: #selector(self.onThemeChangeAction), for: .touchUpInside)
        return button
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
        AUiRoomContext.shared.resetTheme()
        UIScrollView.appearance().contentInsetAdjustmentBehavior = .never
        view.layer.addSublayer(gradientLayer)
        gradientLayer.frame = view.bounds
        gradientLayer.theme_colors = AUiGradientColor("CommonColor.normalGradient")

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
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController?.setNavigationBarHidden(true, animated: false)
    }
    
    private func initEngine() {
        //设置用户信息到AUiKit里
        let commonConfig = AUiCommonConfig()
        commonConfig.appId = kAppId
        commonConfig.userId = userInfo.userId
        commonConfig.userName = userInfo.userName
        commonConfig.userAvatar = userInfo.userAvatar
        
        //创建room manager， 用于获取房间列表
        KaraokeUIKit.shared.setup(roomConfig: commonConfig)
    }
    
    private func _layoutButton() {
        if self.roomList.count > 0 {
            createButton.frame = CGRect(origin: CGPoint(x: (view.frame.width - createButton.frame.width) / 2,
                                                        y: view.frame.height - 74 - UIDevice.current.aui_SafeDistanceBottom),
                                        size: createButton.frame.size)
        } else {
            createButton.center = CGPoint(x: view.frame.size.width / 2, y: view.frame.size.height / 2)
        }
        
        themeButton.frame = CGRect(origin: CGPoint(x: createButton.frame.origin.x, y: createButton.frame.origin.y - themeButton.frame.size.height - 5),
                                   size: themeButton.frame.size)
    }
    
    func onRefreshAction() {
        self.roomList = []
        self.collectionView.reloadData()
        self.collectionView.mj_footer = nil
        KaraokeUIKit.shared.getRoomInfoList(lastCreateTime: nil, pageSize: kListCountPerPage, callback: {[weak self] error, list in
            guard let self = self else {return}
            self.roomList = list ?? []
            self.collectionView.reloadData()
            self.collectionView.mj_header?.endRefreshing()

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
        KaraokeUIKit.shared.getRoomInfoList(lastCreateTime: lastCreateTime, pageSize: kListCountPerPage, callback: {[weak self] error, list in
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
        AUiAlertView()
            .background(color: UIColor(red: 0.1055, green: 0.0062, blue: 0.4032, alpha: 1))
            .isShowCloseButton(isShow: true)
            .title(title: "房间主题")
            .titleColor(color: .white)
            .rightButton(title: "一起嗨歌")
            .rightButtonTapClosure(onTap: {[weak self] text in
                guard let self = self else { return }
                guard let text = text, text.count > 0 else {
                    AUiToast.show(text: "请输入房间名")
                    return
                }
                print("create room with name(\(text)")
                let room = AUiCreateRoomInfo()
                room.roomName = text
                room.thumbnail = self.userInfo.userAvatar
                room.seatCount = 8
                KaraokeUIKit.shared.createRoom(roomInfo: room) { roomInfo in
                    let vc = RoomViewController()
                    vc.roomInfo = roomInfo
                    self.navigationController?.pushViewController(vc, animated: true)
                } failure: { error in
                    AUiToast.show(text: error.localizedDescription)
                }
            })
            .textFieldPlaceholder(placeholder: "房间主题")
            .textField(text: "room_\(arc4random_uniform(99999))")
            .textField(color: UIColor(hex: "#5a5a5a"))
            .textFieldBackground(color: UIColor(red: 0.2578, green: 0.1875, blue: 0.7812, alpha: 0.35))
            .textField(cornerRadius: 25)
            .show()
    }
    
    @objc func onThemeChangeAction() {
        AUiRoomContext.shared.switchThemeToNext()
    }
}

//extension AUiRoomListViewController: AgoraRtmClientDelegate {
//    func rtmKit(_ rtmKit: AgoraRtmClientKit, onTokenPrivilegeWillExpire channel: String?) {
//        print("rtm token WillExpire channel: \(channel ?? "")")
//    }
//}


extension AUiRoomListViewController: UICollectionViewDataSource, UICollectionViewDelegate, UICollectionViewDelegateFlowLayout {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        self.roomList.count
    }
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "cell", for: indexPath) as! AUiRoomListCell
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
