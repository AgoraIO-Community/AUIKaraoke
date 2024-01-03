//
//  AUIRoomListCell.swift
//  AUICell
//
//  Created by zhaoyongqiang on 2023/4/11.
//

import UIKit
import AScenesKit
import AUIKitCore
import SDWebImage

class AUIRoomListCell: UICollectionViewCell {
    var roomInfo: AUIRoomInfo? {
        didSet {
            titleLabel.text = roomInfo?.roomName
//            statusLabel.text = "\(roomInfo?.memberCount ?? 0)人正在嗨歌"
            avatarImageView.sd_setImage(with: URL(string: roomInfo?.owner?.userAvatar ?? ""), placeholderImage: UIImage(systemName: "person.circle"))
            ownerLabel.text = "\(roomInfo?.owner?.userName ?? "")的房间"
        }
    }
    private lazy var iconImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.theme_image = auiThemeImage("AUIRoomListCell.starIcon")
        imageView.translatesAutoresizingMaskIntoConstraints = false
        return imageView
    }()
    private lazy var statusLabel: UILabel = {
        let label = UILabel()
        label.textColor = .white
        label.font = .systemFont(ofSize: 11)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var topBgView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor.init(hex: "#000000", alpha: 0.2)
        view.layer.cornerRadius = 9
        view.layer.masksToBounds = true
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()
    private lazy var avatarImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.layer.cornerRadius = 16
        imageView.layer.masksToBounds = true
        imageView.contentMode = .scaleAspectFill
        imageView.translatesAutoresizingMaskIntoConstraints = false
        return imageView
    }()
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = .white
        label.font = .systemFont(ofSize: 14)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var ownerLabel: UILabel = {
        let label = UILabel()
        label.theme_textColor = AUIColor("AUIRoomListCell.ownerTextColor")
        label.font = .systemFont(ofSize: 11)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()

    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private func setupUI() {
        contentView.layer.cornerRadius = 24
        contentView.clipsToBounds = true

        contentView.addSubview(avatarImageView)
        contentView.addSubview(topBgView)
        contentView.addSubview(statusLabel)
        contentView.addSubview(iconImageView)
        contentView.addSubview(titleLabel)
        contentView.addSubview(ownerLabel)

        avatarImageView.frame = contentView.bounds

        topBgView.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 12).isActive = true
        topBgView.rightAnchor.constraint(equalTo: contentView.rightAnchor, constant: -12).isActive = true
        topBgView.widthAnchor.constraint(equalToConstant: 88).isActive = true
        topBgView.heightAnchor.constraint(equalToConstant: 18).isActive = true

        statusLabel.centerYAnchor.constraint(equalTo: topBgView.centerYAnchor).isActive = true
        statusLabel.rightAnchor.constraint(equalTo: topBgView.rightAnchor, constant: -5).isActive = true

        iconImageView.rightAnchor.constraint(equalTo: statusLabel.leftAnchor, constant: -5).isActive = true
        iconImageView.centerYAnchor.constraint(equalTo: statusLabel.centerYAnchor).isActive = true
        iconImageView.widthAnchor.constraint(equalToConstant: 14).isActive = true
        iconImageView.heightAnchor.constraint(equalToConstant: 14).isActive = true

        titleLabel.leftAnchor.constraint(equalTo: contentView.leftAnchor, constant: 20).isActive = true
        titleLabel.bottomAnchor.constraint(equalTo: contentView.bottomAnchor, constant: -20).isActive = true

        ownerLabel.leftAnchor.constraint(equalTo: contentView.leftAnchor, constant: 20).isActive = true
        ownerLabel.bottomAnchor.constraint(equalTo: contentView.bottomAnchor, constant: -45).isActive = true
    }
}
