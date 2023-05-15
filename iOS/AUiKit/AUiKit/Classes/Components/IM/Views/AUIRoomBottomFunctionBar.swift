//
//  AUIRoomBottomFunctionBar.swift
//  AgoraLyricsScore
//
//  Created by 朱继超 on 2023/5/15.
//

import Foundation


public class AUIRoomBottomFunctionBar: UIView, UICollectionViewDelegate, UICollectionViewDataSource {

    public var raiseKeyboard: (() -> Void)?
    
    public var actionClosure: ((AUIChatFunctionBottomEntity) -> Void)?

    public var datas = [AUIChatFunctionBottomEntity]()

    lazy var chatRaiser: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: 15, y: 5, width: (110 / 375.0) * AScreenWidth, height: self.frame.height - 10)).backgroundColor(UIColor(red: 0, green: 0, blue: 0, alpha: 0.2)).cornerRadius((self.frame.height - 10) / 2.0).font(.systemFont(ofSize: 12, weight: .regular)).textColor(UIColor(white: 1, alpha: 0.8), .normal).addTargetFor(self, action: #selector(raiseAction), for: .touchUpInside)
    }()

    lazy var flowLayout: UICollectionViewFlowLayout = {
        let layout = UICollectionViewFlowLayout()
        layout.itemSize = CGSize(width: self.frame.height - 10, height: self.frame.height - 10)
        layout.minimumInteritemSpacing = 8
        layout.scrollDirection = .horizontal
        return layout
    }()

    public lazy var toolBar: UICollectionView = {
        UICollectionView(frame: CGRect(x: self.frame.width - (40 * CGFloat(self.datas.count)) - (CGFloat(self.datas.count) - 1) * 10 - 15, y: 0, width: 40 * CGFloat(self.datas.count) + (CGFloat(self.datas.count) - 1) * 10, height: self.frame.height), collectionViewLayout: self.flowLayout).delegate(self).dataSource(self).backgroundColor(.clear).registerCell(AUIChatBarFunctionCell.self, forCellReuseIdentifier: "AUIChatBarFunctionCell").showsVerticalScrollIndicator(false).showsHorizontalScrollIndicator(false)
    }()

    override public init(frame: CGRect) {
        super.init(frame: frame)
    }

    public convenience init(frame: CGRect, datas: [AUIChatFunctionBottomEntity], hiddenChat: Bool) {
        self.init(frame: frame)
        self.datas = datas
        self.chatRaiser.isHidden = hiddenChat
        self.addSubViews([chatRaiser, toolBar])
        self.chatRaiser.setImage(UIImage("chatraise",.voiceRoom), for: .normal)
        self.chatRaiser.setTitle(" " + "Let's Chat!".a.localize(type: .voiceRoom), for: .normal)
        self.chatRaiser.titleEdgeInsets = UIEdgeInsets(top: self.chatRaiser.titleEdgeInsets.top, left: 10, bottom: self.chatRaiser.titleEdgeInsets.bottom, right: 10)
        self.chatRaiser.imageEdgeInsets = UIEdgeInsets(top: 5, left: 10, bottom: 5, right: 80)
        self.chatRaiser.contentHorizontalAlignment = .left
        
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

public extension AUIRoomBottomFunctionBar {
    
    @objc func raiseAction() {
        if self.raiseKeyboard != nil {
            self.raiseKeyboard!()
        }
    }
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        self.datas.count
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "AUIChatBarFunctionCell", for: indexPath) as? AUIChatBarFunctionCell
        let entity = self.datas[safe:indexPath.row] ?? AUIChatFunctionBottomEntity()
        let selected = entity.selected ?? false
        cell?.icon.image = selected ? entity.selectedImage:entity.normalImage
        cell?.redDot.isHidden = !selected
        return cell ?? AUIChatBarFunctionCell()
    }

    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        collectionView.deselectItem(at: indexPath, animated: true)
        let entity = self.datas[safe:indexPath.row] ?? AUIChatFunctionBottomEntity()
        if self.actionClosure != nil {
            self.actionClosure!(entity)
        }
    }
}
