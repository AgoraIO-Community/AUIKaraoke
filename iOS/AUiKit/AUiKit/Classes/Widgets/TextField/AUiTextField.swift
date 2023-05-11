//
//  AUiTextField.swift
//  AUiTextField
//
//  Created by zhaoyongqiang on 2023/4/4.
//

import UIKit
import SwiftTheme

public class AUiTextField: UIView {
    private lazy var stackView: UIStackView = {
        let stackView = UIStackView()
        stackView.spacing = 0
        stackView.axis = .horizontal
        stackView.alignment = .fill
        stackView.distribution = .fill
        return stackView
    }()
    private lazy var leftIconContainerView: UIView = {
        let view = UIView()
        view.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        view.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        view.isHidden = true
        return view
    }()
    private lazy var leftIconImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage(systemName: "magnifyingglass"))
        return imageView
    }()
    private lazy var textFieldContainerView: UIView = {
        let view = UIView()
        return view
    }()
    private lazy var textField = UITextField()
    private lazy var rightIconContainerView: UIButton = {
        let view = UIButton()
        view.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        view.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        view.isHidden = true
        view.addTarget(self, action: #selector(clickRightButton(sender:)), for: .touchUpInside)
        return view
    }()
    private lazy var rightIconImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    
    private var textFieldLeftCons: NSLayoutConstraint?
    
    // MARK: Public
    public var textEditingChangedClosure: ((String?) -> Void)?
    public var textEditingEndedClosure: ((String?) -> Void)?
    public var clickRightButtonClosure: ((Bool) -> Void)?
    
    @objc public var leftIconImage: UIImage? {
        didSet {
            leftIconImageView.image = leftIconImage
            leftIconContainerView.isHidden = leftIconImage == nil
            textFieldLeftCons?.constant = 0
            textFieldLeftCons?.isActive = true
        }
    }
    @objc public var rightIconImage: UIImage? {
        didSet {
            rightIconImageView.image = rightIconImage
            rightIconContainerView.isHidden = rightIconImage == nil
        }
    }
    @objc public var rightSelectedIconImage: UIImage?
    
    public var placeHolder: String? {
        didSet {
            textField.placeholder = placeHolder
        }
    }
    
    public var text: String? {
        didSet {
            textField.text = text
        }
    }
    
    @objc public var placeHolderColor: UIColor? {
        didSet {
            guard let color = placeHolderColor else { return }
            var attr = NSAttributedString(string: placeHolder ?? "",
                                          attributes: [.foregroundColor: color])
            if let font = placeHolderFont {
                attr = NSAttributedString(string: placeHolder ?? "",
                                          attributes: [.foregroundColor: color,
                                                       .font: font])
            }
            textField.attributedPlaceholder = attr
        }
    }
    @objc public var placeHolderFont: UIFont? {
        didSet {
            guard let font = placeHolderFont else { return }
            var attr = NSAttributedString(string: placeHolder ?? "",
                                          attributes: [.font: font])
            if let color = placeHolderColor  {
                attr = NSAttributedString(string: placeHolder ?? "",
                                          attributes: [.font: font,
                                                       .foregroundColor: color])
            }
            textField.attributedPlaceholder = attr
        }
    }
    @objc public var textColor: UIColor? {
        didSet {
            textField.textColor = textColor
        }
    }
    @objc public var textFont: UIFont? {
        didSet {
            textField.font = textFont
        }
    }
    /// 键盘类型
    public var keyBoardType: UIKeyboardType? {
        didSet {
            textField.keyboardType = keyBoardType ?? .URL
        }
    }
    /// 密码输入框
    public var isSecureTextEntry: Bool = false {
        didSet {
            textField.isSecureTextEntry = isSecureTextEntry
        }
    }
    /// 清除按钮（输入框内右侧小叉）
    public var clearButtonMode: UITextField.ViewMode = .never {
        didSet {
            textField.clearButtonMode = clearButtonMode
        }
    }
    public var textAlignment: NSTextAlignment = .left {
        didSet {
            textField.textAlignment = textAlignment
        }
    }
    public var returnKeyType: UIReturnKeyType = .done {
        didSet {
            textField.returnKeyType = returnKeyType
        }
    }
    public func becomeFirstResponder() {
        textField.becomeFirstResponder()
    }
    public func resignFirstResponder() {
        textField.resignFirstResponder()
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        addSubview(stackView)
        stackView.translatesAutoresizingMaskIntoConstraints = false
        stackView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        stackView.topAnchor.constraint(equalTo: topAnchor).isActive = true
        stackView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        stackView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        
        stackView.addArrangedSubview(leftIconContainerView)
        stackView.addArrangedSubview(textFieldContainerView)
        stackView.addArrangedSubview(rightIconContainerView)
        
        leftIconContainerView.widthAnchor.constraint(equalToConstant: 44).isActive = true
        leftIconContainerView.addSubview(leftIconImageView)
        leftIconImageView.translatesAutoresizingMaskIntoConstraints = false
        leftIconImageView.leadingAnchor.constraint(equalTo: leftIconContainerView.leadingAnchor,
                                               constant: 20).isActive = true
        leftIconImageView.trailingAnchor.constraint(equalTo: leftIconContainerView.trailingAnchor,
                                                constant: -8).isActive = true
        leftIconImageView.widthAnchor.constraint(equalToConstant: 16).isActive = true
        leftIconImageView.heightAnchor.constraint(equalToConstant: 16).isActive = true
        leftIconImageView.centerYAnchor.constraint(equalTo: leftIconContainerView.centerYAnchor).isActive = true
        
        textFieldContainerView.addSubview(textField)
        textField.translatesAutoresizingMaskIntoConstraints = false
        textFieldLeftCons = textField.leadingAnchor.constraint(equalTo: textFieldContainerView.leadingAnchor, constant: 16)
        textFieldLeftCons?.isActive = true
        textField.trailingAnchor.constraint(equalTo: textFieldContainerView.trailingAnchor).isActive = true
        textField.topAnchor.constraint(equalTo: textFieldContainerView.topAnchor).isActive = true
        textField.bottomAnchor.constraint(equalTo: textFieldContainerView.bottomAnchor).isActive = true
        
        rightIconContainerView.widthAnchor.constraint(equalToConstant: 35).isActive = true
        rightIconContainerView.addSubview(rightIconImageView)
        rightIconImageView.translatesAutoresizingMaskIntoConstraints = false
        rightIconImageView.leadingAnchor.constraint(equalTo: rightIconContainerView.leadingAnchor).isActive = true
        rightIconImageView.centerYAnchor.constraint(equalTo: rightIconContainerView.centerYAnchor).isActive = true
        rightIconImageView.trailingAnchor.constraint(equalTo: rightIconContainerView.trailingAnchor, constant: -21).isActive = true
        
        defaultConfig()
    }
    
    private func defaultConfig() {
        placeHolderFont = .systemFont(ofSize: 14)
        placeHolderColor = .darkGray
        textField.addTarget(self, action: #selector(textEditingChanged(sender:)),
                            for: .editingChanged)
        
        textField.addTarget(self, action: #selector(textEditingDidEnd(sender:)),
                            for: .editingDidEndOnExit)
    }
    
    @objc
    private func textEditingChanged(sender: UITextField) {
        textEditingChangedClosure?(sender.text)
    }
    
    @objc
    private func textEditingDidEnd(sender: UITextField) {
        textEditingEndedClosure?(sender.text)
    }
    
    @objc func clickRightButton(sender: UIButton) {
        sender.isSelected = !sender.isSelected
        rightIconImageView.image = sender.isSelected ? rightSelectedIconImage : rightIconImage
        clickRightButtonClosure?(sender.isSelected)
    }
}


extension AUiTextField {
    var theme_placeHolderColor: ThemeColorPicker? {
        get { return aui_getThemePicker(self, "setPlaceHolderColor:") as? ThemeColorPicker }
        set { aui_setThemePicker(self, "setPlaceHolderColor:", newValue) }
    }
    
    var theme_placeHolderFont: ThemeFontPicker? {
        get { return aui_getThemePicker(self, "setPlaceHolderFont:") as? ThemeFontPicker }
        set { aui_setThemePicker(self, "setPlaceHolderFont:", newValue) }
    }
    
    var theme_textColor: ThemeColorPicker? {
        get { return aui_getThemePicker(self, "setTextColor:") as? ThemeColorPicker }
        set { aui_setThemePicker(self, "setTextColor:", newValue) }
    }
    
    var theme_textFont: ThemeFontPicker? {
        get { return aui_getThemePicker(self, "setTextFont:") as? ThemeFontPicker }
        set { aui_setThemePicker(self, "setTextFont:", newValue) }
    }
    
    var theme_leftIconImage: ThemeImagePicker? {
        get { return aui_getThemePicker(self, "setLeftIconImage:") as? ThemeImagePicker }
        set { aui_setThemePicker(self, "setLeftIconImage:", newValue) }
    }
    
    var theme_rightIconImage: ThemeImagePicker? {
        get { return aui_getThemePicker(self, "setRightIconImage:") as? ThemeImagePicker }
        set { aui_setThemePicker(self, "setRightIconImage:", newValue) }
    }
    
    var theme_rightSelectedIconImage: ThemeImagePicker? {
        get { return aui_getThemePicker(self, "setRightSelectedIconImage:") as? ThemeImagePicker }
        set { aui_setThemePicker(self, "setRightSelectedIconImage:", newValue) }
    }
}
