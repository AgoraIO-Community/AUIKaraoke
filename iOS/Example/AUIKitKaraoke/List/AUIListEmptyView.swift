//
//  AUIListEmptyView.swift
//  AUIKitKaraoke
//
//  Created by FanPengpeng on 2023/7/25.
//

import UIKit
import SwiftTheme

class AUIListEmptyView: UIView {
    
    private lazy var imageView: UIImageView = {
        let imgView = UIImageView()
        imgView.frame = CGRect(x: 0, y: 0, width: 200, height: 116)
        return imgView
    }()
    private var titleLabel: UILabel = {
        let label = UILabel()
        label.frame = CGRect(x: 0, y: 136, width: 200, height: 20)
        label.textAlignment = .center
        label.theme_textColor = "AUIListEmptyView.emptyTitleColor"
        return label
    }()

    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI(){
        addSubview(imageView)
        addSubview(titleLabel)
    }
    
    func setImage(_ theme_image: ThemeImagePicker?, title: String) {
        imageView.theme_image = theme_image
        titleLabel.text = title
    }

}
