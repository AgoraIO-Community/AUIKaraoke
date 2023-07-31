//
//  AUIThemeSettingViewController.swift
//  AUIKitKaraoke
//
//  Created by FanPengpeng on 2023/7/25.
//

import UIKit
import AUIKitCore

private let kbackButtonWidth: CGFloat = 311
private let kbackButtonHeight: CGFloat = 50

class AUIThemeSettingViewController: UIViewController {
    private lazy var backgroundImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.theme_image = auiThemeImage("AUIThemeSettingViewController.backgroundImage")
        return imageView
    }()
    
    private lazy var themes: UILabel = {
        let label = UILabel().font(.systemFont(ofSize: 17, weight: .medium)).textColor(.white).text("Themes")
        label.aui_left = 44
        label.aui_size = CGSize(width: 70, height: 20)
        label.aui_centerY = modeSegment.aui_centerY
        return label
    }()
    private lazy var modeSegment: UISegmentedControl = {
        let segment = UISegmentedControl(items: ["Light","Dark"])
        let width: CGFloat = 96
        let height: CGFloat = 46
        let y: CGFloat = AScreenHeight - 180
        let x = AScreenWidth - (AScreenWidth - kbackButtonWidth) * 0.5 - width
        segment.frame = CGRect(x: x, y: y, width: width, height: height)
        segment.setImage(UIImage(named: "sun"), forSegmentAt: 0)
        segment.setImage(UIImage(named: "moon"), forSegmentAt: 1)
        segment.tintColor = UIColor(0x009EFF)
        segment.tag = 12
        segment.backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.2)
        if let themeName = AUIThemeManager.shared.currentThemeName {
            if themeName == "Light" {
                segment.selectedSegmentIndex = 0
            }
            if themeName == "Dark" {
                segment.selectedSegmentIndex = 1
            }
        }
        
        segment.selectedSegmentTintColor = UIColor(0x009EFF)
        segment.setTitleTextAttributes([.foregroundColor : UIColor.white,.font:UIFont.systemFont(ofSize: 18, weight: .medium)], for: .selected)
        segment.setTitleTextAttributes([.foregroundColor : UIColor.white,.font:UIFont.systemFont(ofSize: 16, weight: .regular)], for: .normal)
        segment.addTarget(self, action: #selector(onChanged(sender:)), for: .valueChanged)
        return segment
    }()
    
    
    private lazy var backButton: AUIButton = {
        let button = AUIButton()
        let style = AUIButtonDynamicTheme()
        style.backgroundColor = "CommonColor.primary"
        style.buttonWidth = ThemeCGFloatPicker(floats: kbackButtonWidth)
        style.buttonHeight = ThemeCGFloatPicker(floats: kbackButtonHeight)
        style.titleColor = AUIColor("#ffffff")
        button.style = style
        button.layoutIfNeeded()
        button.setTitle("Go Live", for: .normal)
        button.setGradient([UIColor(red: 0, green: 158/255.0, blue: 1, alpha: 1),UIColor(red: 124/255.0, green: 91/255.0, blue: 1, alpha: 1)],[CGPoint(x: 0, y: 0),CGPoint(x: 0, y: 1)])
        button.addTarget(self, action: #selector(self.onBackAction), for: .touchUpInside)
        return button
    }()

    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
    }
    
    private func setUpUI(){
        backgroundImageView.frame = view.bounds
        view.addSubview(backgroundImageView)
        
        view.addSubview(themes)
        
        view.addSubview(modeSegment)
        
        backButton.frame = CGRect(origin: CGPoint(x: (view.frame.width - backButton.frame.width) / 2,
                                                   y: view.frame.height - 74 - UIDevice.current.aui_SafeDistanceBottom),
                                   size: backButton.frame.size)
        view.addSubview(backButton)
    }
    
    @objc private func onBackAction(){
        navigationController?.popViewController(animated: true)
    }
    
    @objc private func onChanged(sender: UISegmentedControl) {
        AUIThemeManager.shared.switchThemeToNext()
    }
}
