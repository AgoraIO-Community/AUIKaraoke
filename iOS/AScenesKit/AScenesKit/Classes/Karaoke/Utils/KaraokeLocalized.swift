//
//  AUiLocalized.swift
//  AgoraLyricsScore
//
//  Created by wushengtao on 2023/3/30.
//

import AUiKit

public func auikaraoke_localized(_ string: String) -> String {
    return aui_localized(string, bundleName: "auiKaraokeLocalizable")
}
