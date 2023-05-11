//
//  AUiRoomMemberListView.swift
//  AUiKit
//
//  Created by FanPengpeng on 2023/4/3.
//

import AUiKit

private let headImageWidth: CGFloat = 32

public typealias AUiRoomMembersViewMoreBtnAction = (_ members: [AUiUserInfo])->()

//用户头像展示
public class AUiRoomMembersView: UIView {
    private var onClickMoreButtonAction: AUiRoomMembersViewMoreBtnAction?
    private weak var memberListView: AUiRoomMemberListView?
    
    public var members: [AUiUserCellUserDataProtocol] = [] {
        didSet {
            let imgs = members.map({$0.userAvatar})
            updateWithMemberImgs(imgs)
            memberListView?.memberList = members
        }
    }
    
    public var seatMap: [String: Int] = [:]
    
    public var roomId: String?
    
    private lazy var moreButton: AUiButton = {
        let theme = AUiButtonDynamicTheme()
        theme.icon = auiThemeImage("Room.membersMoreIcon")
        theme.iconWidth = "Room.membersMoreIconWidth"
        theme.iconHeight = "Room.membersMoreIconHeight"
        theme.buttonWitdth = "Room.membersMoreWidth"
        theme.buttonHeight = "Room.membersMoreHeight"
        theme.backgroundColor = "Room.membersMoreBgColor"
        theme.cornerRadius = "Room.membersMoreCornerRadius"
        
        let button = AUiButton()
        button.style = theme
        button.addTarget(self, action: #selector(didClickMoreMemberButton), for: .touchUpInside)
        return button
    }()
    
    private lazy var leftImgView: UIImageView = {
        let imgview = UIImageView()
        imgview.layer.cornerRadius = headImageWidth * 0.5
        imgview.layer.masksToBounds = true
        imgview.contentMode = .scaleAspectFill
        imgview.isHidden = true
        return imgview
    }()
    
    private lazy var rightImgView: UIImageView = {
        let imgview = UIImageView()
        imgview.layer.cornerRadius = headImageWidth * 0.5
        imgview.layer.masksToBounds = true
        imgview.contentMode = .scaleAspectFill
        imgview.isHidden = true
        return imgview
    }()
    
    private lazy var countLabel: UILabel = {
        let label = UILabel()
        label.numberOfLines = 1
        label.textColor = .white
        label.font = UIFont.systemFont(ofSize: 14)
        label.textAlignment = .center
        label.text = ""
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        _createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func _createSubviews(){
        addSubview(moreButton)
        addSubview(rightImgView)
        addSubview(leftImgView)
 
        let views = [leftImgView,rightImgView,moreButton]
        for (i, view) in views.enumerated() {
            view.frame = CGRect(x: CGFloat(i) * (headImageWidth + 8) , y: 0, width: headImageWidth, height: headImageWidth)
        }
        self.bounds = CGRect(x: 0, y: 0, width: CGFloat(views.count) * headImageWidth + CGFloat(views.count - 1) * 8, height: headImageWidth)
        
        rightImgView.addSubview(countLabel)
        countLabel.frame = rightImgView.bounds
        
    }
    
    public func updateWithMemberImgs(_ imgs: [String]) {
        if imgs.count < 1 {
            aui_error("err = empty member", tag: "AUiRoomMembersView")
            return
        }
        
        for (i, imgView) in [rightImgView, leftImgView].enumerated() {
            imgView.isHidden = false
            if imgs.count > i {
                imgView.kf.setImage(with: URL(string: imgs[i]), placeholder: UIImage.aui_Image(named: "aui_micseat_dialog_avatar_idle"))
            }else{
                imgView.isHidden = true
            }
        }
        if imgs.count > 2 {
            countLabel.text = "\(imgs.count)"
        }
    }
    
//    public func requestData(roomId: String){
////        guard let roomId = roomId else { return }
//        userServiceDelegate?.getUserInfoList(roomId: roomId, userIdList: [], callback: { err, userList in
//            if let userList = userList {
//                self.members = userList
//            }
//        })
//        /*
//        for _ in 0...5 {
//            let user = AUiUserInfo()
//            user.userName = "user"
//            user.userAvatar = "https://img1.baidu.com/it/u=413643897,2296924942&fm=253&app=138&size=w931&n=0&f=JPEG&fmt=auto?sec=1680627600&t=e945c37a601f9ee7ca3be932c73e605a"
//            members.append(user)
//        }
//         */
//    }
}

extension AUiRoomMembersView {
    
    public func clickMoreButtonAction(_ action: AUiRoomMembersViewMoreBtnAction?) {
        onClickMoreButtonAction = action
    }
}

extension AUiRoomMembersView {
    @objc private func didClickMoreMemberButton(){
        let listView = AUiRoomMemberListView()
        listView.aui_size =  CGSize(width: UIScreen.main.bounds.width, height: 562)
        listView.memberList = members
        listView.seatMap = seatMap
        AUiCommonDialog.show(contentView: listView, theme: AUiCommonDialogTheme())
        self.memberListView = listView
    }
}
