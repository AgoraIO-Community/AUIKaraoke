//
//  AUiButton.swift
//  AUiKit
//
//  Created by wushengtao on 2023/3/29.
//

import Foundation
import SwiftTheme

@objc public enum AUiButtonTextImageAlignment: Int {
    case imageCenterTextCenter = 0
    case imageLeftTextRight
    case textLeftImageRight
    case imageTopTextBottom
    case textTopImageBottom
}

public class AUiButtonStyle: NSObject {
    public func setupStyle(button: AUiButton) {
    }
    
    public func layoutStyle(button: AUiButton) {
    }
}

public class AUiButtonDynamicTheme: AUiButtonStyle {
    public var icon: ThemeImagePicker?
    public var selectedIcon: ThemeImagePicker?
    public var iconWidth: ThemeCGFloatPicker?
    public var iconHeight: ThemeCGFloatPicker?
    public var padding: ThemeCGFloatPicker?
    public var buttonWitdth: ThemeCGFloatPicker = "Button.buttonWidth"
    public var buttonHeight: ThemeCGFloatPicker = "Button.buttonHeight"
    public var titleFont: ThemeFontPicker = "Button.titleFont"
    public var titleColor: ThemeColorPicker = "Button.titleColor"
    public var selectedTitleColor: ThemeColorPicker = "Button.titleColor"
    public var backgroundColor: ThemeColorPicker = "Button.backgroundColor"
    public var cornerRadius: ThemeCGFloatPicker? = "Button.cornerRadius"
    public var textAlpha: ThemeCGFloatPicker = "Button.titleAlpha"
    
    public static func toolbarTheme() -> AUiButtonDynamicTheme {
        let theme = AUiButtonDynamicTheme()
        theme.titleFont = "CommonFont.small"
        theme.iconWidth = "Player.toolIconWidth"
        theme.iconHeight = "Player.toolIconHeight"
        theme.buttonWitdth = "Player.playButtonWidth"
        theme.buttonHeight = "Player.playButtonHeight"
        theme.cornerRadius = nil
        return theme
    }
    
    public override func setupStyle(button: AUiButton) {
        button.theme_setImage(self.icon, forState: .normal)
        button.theme_setImage(self.selectedIcon, forState: .selected)
        button.imageView?.theme_image = self.icon
        
        button.theme_setTitleColor(self.titleColor, forState: .normal)
        button.theme_setTitleColor(self.selectedTitleColor, forState: .selected)
        
        button.theme_backgroundColor = self.backgroundColor
        button.theme_padding = padding
        
        button.titleLabel?.theme_alpha = textAlpha
    }
    
    public override func layoutStyle(button: AUiButton) {
        button.imageView?.theme_width = self.iconWidth
        button.imageView?.theme_height = self.iconHeight
        button.theme_width = self.buttonWitdth
        button.theme_height = self.buttonHeight
        button.titleLabel?.theme_font = self.titleFont
        button.layer.theme_cornerRadius = self.cornerRadius
    }
}

public class AUiButtonNativeTheme: AUiButtonStyle {
    public var icon: UIImage?
    public var selectedIcon: UIImage?
    public var iconWidth: CGFloat = 0
    public var iconHeight: CGFloat = 0
    public var padding: CGFloat = 0
    public var buttonWitdth: CGFloat = 240
    public var buttonHeight: CGFloat = 50
    public var titleFont: UIFont = UIFont(name: "PingFangSC-Semibold", size: 17)!
    public var titleColor: UIColor = .white
    public var selectedTitleColor: UIColor = .white
    public var backgroundColor: UIColor = .clear
    public var cornerRadius: CGFloat = 25
    public var textAlpha: CGFloat = 1
    
    public override func setupStyle(button: AUiButton) {
        button.setImage(self.icon, for: .normal)
        button.setImage(self.selectedIcon, for: .selected)
        
        button.setTitleColor(self.titleColor, for: .normal)
        button.setTitleColor(self.selectedTitleColor, for: .selected)
        
        button.backgroundColor = self.backgroundColor
        button.padding = padding
        button.titleLabel?.alpha = textAlpha
    }
    
    public override func layoutStyle(button: AUiButton) {
        button.imageView?.aui_width = self.iconWidth
        button.imageView?.aui_height = self.iconHeight
        button.aui_width = self.buttonWitdth
        button.aui_height = self.buttonHeight
        button.layer.cornerRadius = self.cornerRadius
        button.titleLabel?.font = self.titleFont
    }
}

open class AUiButton: UIButton {
    @objc public var textImageAlignment: AUiButtonTextImageAlignment = .imageCenterTextCenter {
        didSet {
            setNeedsLayout()
        }
    }
    public var style: AUiButtonStyle? {
        didSet {
            style?.setupStyle(button: self)
            style?.layoutStyle(button: self)
            setNeedsLayout()
            layoutIfNeeded()
        }
    }
    
    @objc fileprivate var padding: CGFloat = 0 {
        didSet {
            setNeedsLayout()
        }
    }
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubViews()
    }
    
    required public init?(coder: NSCoder) {
        super.init(coder: coder)
        _loadSubViews()
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        guard let imageView = imageView,let titleLabel = titleLabel else {return}
        style?.layoutStyle(button: self)
        titleLabel.sizeToFit()
        
        switch textImageAlignment {
        case .imageLeftTextRight:
            let width = imageView.aui_width + titleLabel.aui_width + padding
            imageView.aui_center = CGPoint(x: (aui_width - width) / 2 + imageView.aui_width / 2, y: aui_height / 2)
            titleLabel.aui_center = CGPoint(x: imageView.aui_right + titleLabel.aui_width / 2 + padding, y: imageView.aui_centerY)
        case .textLeftImageRight:
            let width = imageView.aui_width + titleLabel.aui_width
            titleLabel.aui_center = CGPoint(x: (aui_width - width) / 2 + titleLabel.aui_width / 2, y: aui_height / 2)
            imageView.aui_center = CGPoint(x: titleLabel.aui_right + imageView.aui_width / 2 + padding, y: titleLabel.aui_centerY)
        case .imageTopTextBottom:
            let height = imageView.aui_height + titleLabel.aui_height + padding
            imageView.aui_center = CGPoint(x: aui_width / 2, y: (aui_height - height) / 2 + imageView.aui_height / 2)
            titleLabel.aui_center = CGPoint(x: imageView.aui_centerX, y: imageView.aui_bottom + titleLabel.aui_height / 2 + padding)
        case .textTopImageBottom:
            let height = imageView.aui_height + titleLabel.aui_height
            titleLabel.aui_center = CGPoint(x: aui_width / 2, y: (aui_height - height) / 2 + titleLabel.aui_height / 2)
            imageView.aui_center = CGPoint(x: titleLabel.aui_centerX, y: titleLabel.aui_bottom + imageView.aui_height / 2 + padding)
        default:
            titleLabel.center = CGPoint(x: aui_width / 2, y: aui_height / 2)
            imageView.center = CGPoint(x: aui_width / 2, y: aui_height / 2)
        }
    }
    
    private func _loadSubViews() {
        imageView?.contentMode = .scaleAspectFit
        self.clipsToBounds = true
    }
}


extension AUiButton {
    var theme_padding: ThemeCGFloatPicker? {
        get { return aui_getThemePicker(self, "setPadding:") as? ThemeCGFloatPicker }
        set { aui_setThemePicker(self, "setPadding:", newValue) }
    }
}
