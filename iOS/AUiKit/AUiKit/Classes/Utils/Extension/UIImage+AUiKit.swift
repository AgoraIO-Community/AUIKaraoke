//
//  UIImage+AUiKit.swift
//  AUiKit
//
//  Created by wushengtao on 2023/4/3.
//

import UIKit
import SwiftTheme

extension UIImage {
    public class func aui_Image(named: String) -> UIImage? {
        for path in AUiRoomContext.shared.themeResourcePaths {
            let filePath = path.appendingPathComponent(named).path
            if let image = UIImage(contentsOfFile: filePath) {
                return image
            }
        }
        if let filePath = ThemeManager.currentThemePath?.URL?.appendingPathComponent(named).path {
            return UIImage(contentsOfFile: filePath)
        }
        
        return nil
    }
}
