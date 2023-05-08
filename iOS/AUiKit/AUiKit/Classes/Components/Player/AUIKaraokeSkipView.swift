//
//  AUIKaraokeSkipView.swift
//  AUiKit
//
//  Created by CP on 2023/4/23.
//

import UIKit

public enum SkipActionType: Int {
    case down = 0
    case cancel = 1
}

public enum SkipType: Int {
    case prelude = 0
    case epilogue = 1
}

open class AUIKaraokeSkipView: UIView {
    
    typealias OnSkipCallback = (_ type: SkipActionType) -> Void
    
    lazy var bgView: UIView = {
        let bgView = UIView()
        bgView.backgroundColor = UIColor(red: 63/255.0, green: 64/255.0, blue: 93/255.0, alpha: 1)
        bgView.layer.borderColor = UIColor(red: 1, green: 1, blue: 1, alpha: 0.6).cgColor
        return bgView
    }()
    
    lazy var skipBtn: AUiButton = {
        let theme = AUiButtonDynamicTheme()
        theme.buttonWitdth = "Player.skipButtonWidth"
        theme.buttonHeight = "Player.skipButtonHeight"
        theme.titleFont = "CommonFont.middleBold"
        theme.cornerRadius = nil
        let button = AUiButton()
        button.textImageAlignment = .imageCenterTextCenter
        button.style = theme
        button.setTitle("跳过前奏", for: .normal)
        button.addTarget(self, action: #selector(skip(_:)), for: .touchUpInside)
        return button
    }()
    
    lazy var cancleBtn: AUiButton = {
        let theme = AUiButtonDynamicTheme()
        theme.buttonWitdth = "Player.skipCancleButtonWidth"
        theme.buttonHeight = "Player.skipCancleButtonHeight"
        theme.icon = "Player.playerLrcItemSkipItem"
        theme.cornerRadius = nil
        let button = AUiButton()
        button.textImageAlignment = .imageCenterTextCenter
        button.style = theme
        button.addTarget(self, action: #selector(skip(_:)), for: .touchUpInside)
        return button
    }()

    public var completion: ((SkipActionType) -> Void)?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }

    required public init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func setSkipType(type: SkipType) {
        skipBtn.setTitle("跳过\(type == .prelude ? "前" : "尾")奏", for: .normal)
    }

    private func setupUI() {
        addSubview(bgView)
        bgView.addSubview(skipBtn)
        bgView.addSubview(cancleBtn)
    }

    open override func layoutSubviews() {
        super.layoutSubviews()
        bgView.frame = bounds
        bgView.layer.cornerRadius = bounds.size.height / 2.0
        bgView.layer.masksToBounds = true
        bgView.layer.borderWidth = 1
        
        skipBtn.aui_left = 10
        skipBtn.aui_top = 0
        
        cancleBtn.aui_left = skipBtn.aui_right
        cancleBtn.aui_top = 4
    }

    @objc private func skip(_ btn: UIButton) {
        completion?(btn.tag == 200 ? .down : .cancel)
    }
    
}
