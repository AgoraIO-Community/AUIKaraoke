//
//  AUIChatCell.swift
//  AgoraLyricsScore
//
//  Created by 朱继超 on 2023/5/15.
//

import UIKit

public class AUIChatCell: UITableViewCell {

    lazy var container: UIImageView = {
        UIImageView(frame: CGRect(x: 15, y: 6, width: self.contentView.frame.width - 30, height: self.frame.height - 6)).image(UIImage("chatBg",.voiceRoom)).backgroundColor(.clear)
    }()

    lazy var content: UILabel = {
        UILabel(frame: CGRect(x: 10, y: 7, width: self.container.frame.width - 20, height: self.container.frame.height - 18)).backgroundColor(.clear).numberOfLines(0).lineBreakMode(.byWordWrapping)
    }()

    override public init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        backgroundColor = .clear
        contentView.backgroundColor = .clear
        contentView.addSubview(container)
        container.addSubview(content)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func refresh(chat: AUIChatEntity) {
        self.container.frame = CGRect(x: 15, y: 6, width: chat.width! + 30, height: chat.height! - 6)
        self.content.attributedText = chat.attributeContent
        self.content.preferredMaxLayoutWidth =  self.container.frame.width - 24
        self.content.frame = CGRect(x: 12, y: 7, width:  self.container.frame.width - 24, height:  self.container.frame.height - 16)
        self.container.image = (chat.joined == true ? UIImage("joined_msg_bg",.voiceRoom)?.resizableImage(withCapInsets: UIEdgeInsets(top: 8, left: 12, bottom: 8, right: 12), resizingMode: .stretch) : UIImage("chatBg",.voiceRoom))
        
    }
}

