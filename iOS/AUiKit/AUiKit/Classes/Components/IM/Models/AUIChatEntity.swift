//
//  AUIChatEntity.swift
//  AgoraLyricsScore
//
//  Created by 朱继超 on 2023/5/15.
//

import UIKit


@objcMembers open class AUIChatEntity: NSObject {
    
    var chatId: String? = ""
    
    var userName: String? = ""
    
    var content: String? = ""
    
    var joined: Bool? = false
    
    var attachmentImage: UIImage?
    
    var fullText: String? {
        (self.userName ?? "") + (self.content ?? "")
    }

    lazy var height: CGFloat? = UILabel(frame: CGRect(x: 0, y: 0, width: chatViewWidth - 54, height: 15)).backgroundColor(.clear).numberOfLines(0).lineBreakMode(.byWordWrapping).attributedText(self.attributeContent).sizeThatFits(CGSize(width: chatViewWidth - 54, height: 9999)).height + 26

    lazy var width: CGFloat? = UILabel(frame: CGRect(x: 0, y: 0, width: chatViewWidth - 54, height: 15)).backgroundColor(.clear).numberOfLines(0).lineBreakMode(.byWordWrapping).attributedText(self.attributeContent).sizeThatFits(CGSize(width: chatViewWidth - 54, height: 9999)).width

    lazy var attributeContent: NSAttributedString? = self.renderAttributeText()
}

public extension AUIChatEntity {
    
    func renderAttributeText() -> NSAttributedString {
        if joined! == false {
            let attachment = NSTextAttachment()
            attachment.image = UIImage("fangzhu",.voiceRoom)
            attachment.bounds = CGRect(x: 0, y: -1.5, width: 14, height: 14)
            let host = NSMutableAttributedString(attachment: attachment)
            host.append(NSAttributedString(string: " "))
            var text = NSMutableAttributedString {
                AttributedText(self.userName! + " : ").foregroundColor(Color(0x8BB3FF)).font(.systemFont(ofSize: 14, weight: .semibold)).lineSpacing(5)
                AttributedText(self.content!).foregroundColor(self.joined! == false ? Color.white : Color(0xFCF0B3)).font(.systemFont(ofSize: 14, weight: .regular)).lineSpacing(5)
            }
            var string = text.string as NSString
            if self.attachmentImage != nil {
                host.append(text)
                text = host
                string = host.string as NSString
            }
            for symbol in AUIChatEmojiManager.shared.emojis {
                if string.range(of: symbol).location != NSNotFound {
                    let ranges = text.string.a.rangesOfString(symbol)
                    text = AUIChatEmojiManager.shared.convertEmoji(input: text, ranges: ranges, symbol: symbol)
                }
            }
            return text
        } else {
            let attachment = NSTextAttachment()
            attachment.image = UIImage("shaking_hand",.voiceRoom)
            attachment.bounds = CGRect(x: 0, y: -4.5, width: 18, height: 18)
            let attributeText = NSMutableAttributedString {
                AttributedText(self.userName!).foregroundColor(Color(0x8BB3FF)).font(.systemFont(ofSize: 13, weight: .semibold)).lineSpacing(5)
                Space()
                AttributedText("Joined".a.localize(type: .voiceRoom)).foregroundColor(self.joined! == false ? Color.white : Color(0xFCF0B3)).font(.systemFont(ofSize: 13, weight: .semibold)).lineSpacing(5)
                Space()
            }
            attributeText.append(NSMutableAttributedString(attachment: attachment))
            return attributeText
        }
    }
}

