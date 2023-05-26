//
//  AUILocalized.swift
//  AgoraLyricsScore
//
//  Created by wushengtao on 2023/3/30.
//

import AUIKit

public func auikaraoke_localized(_ string: String) -> String {
    return aui_localized(string, bundleName: "auiKaraokeLocalizable")
}
