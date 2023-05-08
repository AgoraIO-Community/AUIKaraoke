//
//  AUiMicSeatItemCell.swift
//  AUiKit
//
//  Created by wushengtao on 2023/3/23.
//

import Foundation
import SwiftTheme
import Kingfisher

public enum MicRole: Int {
    case mainSinger
    case coSinger
    case onlineAudience
    case offlineAudience
}

/// 麦位管理对话框cell
open class AUiMicSeatItemCell: UICollectionViewCell {
    weak var item: AUiMicSeatCellDataProtocol? {
        didSet {
            reloadData()
        }
    }
    
    public lazy var canvasView: UIView = {
        let view = UIView()
        view.backgroundColor = .black
        return view
    }()
    
    private lazy var avatarView: UIImageView = {
        let view = UIImageView()
        view.theme_backgroundColor = "SeatItem.backgroundColor"
        view.theme_image = "SeatItem.backgroundImage"
        return view
    }()
    
    private lazy var defaultImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    
    //头像
    private lazy var avatarImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    
    private lazy var seatLabel: UILabel = {
        let label = UILabel()
        label.theme_font = "SeatItem.labelFont"
        label.theme_textColor = "SeatItem.labelTextColor"
        label.textAlignment = .center
//        label.theme_attributedText = "SeatItem.seatLabelText"
        return label
    }()
    
    //静音/锁麦图标
    private lazy var statusImageView: UIImageView = {
        let imageView = UIImageView()
        return imageView
    }()
    
    //角色标记
    lazy var micRoleBtn: UIButton = {
        let theme = AUiButtonDynamicTheme()
        theme.buttonWitdth = "SeatItem.micRoleButtonWidth"
        theme.buttonHeight = "SeatItem.micRoleButtonHeight"
        theme.icon = "SeatItem.micSeatItemIconMainSinger"
        theme.selectedIcon = "SeatItem.micSeatItemIconCoSinger"
        theme.titleFont = "CommonFont.small"
        theme.padding = "SeatItem.padding"
        theme.iconWidth = "SeatItem.micRoleButtonIconWidth"
        theme.iconHeight = "SeatItem.micRoleButtonIconHeight"
        theme.cornerRadius = nil
        let button = AUiButton()
        button.textImageAlignment = .imageLeftTextRight
        button.style = theme
        button.setTitle("主唱", for: .normal)
        button.setTitle("副唱", for: .selected)
        return button
    }()
    
    //房主标记
    lazy var hostIcon: AUiButton = {
        let theme = AUiButtonDynamicTheme()
        theme.titleFont = "SeatItem.micSeatHostSmall"
        theme.icon = "SeatItem.micSeatHostIcon"
        theme.buttonWitdth = "SeatItem.micHostButtonWidth"
        theme.buttonHeight = "SeatItem.micHostButtonHeight"
        let button = AUiButton()
        button.textImageAlignment = .imageCenterTextCenter
        button.style = theme
        button.setTitle("房主", for: .normal)
        return button
    }()
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        _loadView()
    }
    
    required public init?(coder: NSCoder) {
        super.init(coder: coder)
        _loadView()
    }
    
    private func _loadView() {
        addSubview(avatarView)
        addSubview(defaultImageView)
        addSubview(avatarImageView)
        addSubview(seatLabel)
        addSubview(micRoleBtn)
        addSubview(hostIcon)
        addSubview(statusImageView)
        avatarImageView.addSubview(canvasView)
        micRoleBtn.isHidden = true
        hostIcon.isHidden = true
    }
    
    private func updateRoleUI(with role:  MicRole) {
        switch role {
        case .mainSinger:
            micRoleBtn.isHidden = false
            micRoleBtn.isSelected = false
        case .coSinger:
            micRoleBtn.isHidden = false
            micRoleBtn.isSelected = true
        case .onlineAudience:
            micRoleBtn.isHidden = true
        case .offlineAudience:
            micRoleBtn.isHidden = true
        }
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        
        avatarView.theme_width = "SeatItem.avatarWidth"
        avatarView.theme_height = "SeatItem.avatarHeight"
//        avatarView.frame = CGRect(x: 0, y: 0, width: self.bounds.width, height: self.frame.width)
        
        defaultImageView.theme_width = "SeatItem.defaultImageWidth"
        defaultImageView.theme_height = "SeatItem.defaultImageHeight"
        let size = defaultImageView.aui_size
        defaultImageView.frame = CGRect(x: (avatarView.bounds.width - size.width) / 2,
                                        y: (avatarView.bounds.height - size.height) / 2,
                                        width: size.width,
                                        height: size.height)
        avatarImageView.frame = avatarView.bounds
        
        statusImageView.aui_size = CGSize(width: size.width, height: size.height)
        statusImageView.theme_centerX = "SeatItem.muteCenterX"
        statusImageView.theme_centerY = "SeatItem.muteCenterY"
        statusImageView.theme_width = "SeatItem.muteWidth"
        statusImageView.theme_height = "SeatItem.muteHeight"
        
        seatLabel.frame = CGRect(x: 0, y: avatarView.frame.height + 4, width: frame.width, height: 20)
        
        avatarView.layer.theme_cornerRadius = "SeatItem.avatarCornerRadius"
        avatarImageView.layer.theme_cornerRadius = "SeatItem.avatarCornerRadius"
        avatarView.clipsToBounds = true
        avatarImageView.clipsToBounds = true
        
        micRoleBtn.aui_centerX = aui_width / 2.0
        micRoleBtn.aui_top = seatLabel.aui_bottom
        
        hostIcon.aui_bottom = avatarView.aui_bottom
        hostIcon.aui_centerX = aui_width / 2.0
        
        canvasView.frame = avatarImageView.bounds
    }
    
    private func reloadData() {
        aui_info("reload seat name \(item?.seatName ?? "") url: \(item?.avatarUrl ?? "") mute video: \(item?.isMuteVideo ?? true)", tag: "AUiMicSeatItemCell")
        avatarImageView.kf.setImage(with: URL(string: item?.avatarUrl ?? ""))
        seatLabel.text = item?.seatName
        if let _ = item?.avatarUrl {
            avatarImageView.layer.theme_borderColor = "SeatItem.avatarBorderColor"
            avatarImageView.layer.theme_borderWidth = "SeatItem.avatarBorderWidth"
        } else {
            avatarImageView.layer.theme_borderColor = nil
            avatarImageView.layer.theme_borderWidth = nil
            avatarImageView.layer.borderWidth = 0
        }
        
        if item?.isLock ?? false {
            defaultImageView.theme_image = "SeatItem.lockImage"
            statusImageView.theme_image = nil
        } else if item?.isMuteAudio ?? false {
            defaultImageView.theme_image = "SeatItem.defaultImage"
            statusImageView.theme_image = "SeatItem.muteAudioImage"
        } else if item?.isMuteVideo ?? false {
            defaultImageView.theme_image = "SeatItem.defaultImage"
            statusImageView.theme_image = nil//SeatItem.muteVideoImage"
            statusImageView.image = nil
        } else {
            statusImageView.theme_image = nil
            statusImageView.image = nil
            defaultImageView.theme_image = "SeatItem.defaultImage"
        }
        
        if item?.avatarUrl?.count ?? 0 > 0 {
            canvasView.alpha = item?.isMuteVideo ?? true ? 0 : 1
        } else {
            canvasView.alpha = 0
        }
        
        hostIcon.isHidden = item?.micSeat != 0
        
        updateRoleUI(with: item?.role ?? .offlineAudience)
        setNeedsLayout()
    }
}
