//
//  AUiJukeBoxCellDataProtocol.swift
//  AUiKit
//
//  Created by wushengtao on 2023/4/10.
//

import Foundation

public protocol AUiJukeBoxItemDataProtocol: NSObjectProtocol {
    var songCode: String {get}
    var avatarUrl: String {get}   //头像
    var title: String {get}    //主标题
    var subTitle: String? {get}  //副标题
}

public protocol AUiJukeBoxItemSelectedDataProtocol: AUiJukeBoxItemDataProtocol {
    var isPlaying: Bool {get} //是否在播放
    var userId: String? {get}  //歌曲拥有者
}


extension AUiMusicModel: AUiJukeBoxItemDataProtocol {
    public var avatarUrl: String {
        return self.poster
    }
    
    public var title: String {
        return self.name
    }
    
    public var subTitle: String? {
        return self.singer
    }
}

extension AUiChooseMusicModel: AUiJukeBoxItemSelectedDataProtocol {
    public var userId: String? {
        return self.owner?.userId
    }
    
    public var isPlaying: Bool {
        return self.playStatus == .playing
    }
}
