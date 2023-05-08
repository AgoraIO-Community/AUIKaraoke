//
//  AUiRoomConfig.swift
//  AUiKit
//
//  Created by wushengtao on 2023/2/24.
//

import Foundation

open class AUiCommonConfig: NSObject {
    public var appId: String = ""
    public var host: String = ""
    public var userId: String = ""
    public var userName: String = ""
    public var userAvatar: String = ""
    
    open override var description: String {
        return "AUiCommonConfig: appId: \(appId), userId: \(userId) userName: \(userName)"
    }
}

open class AUiRoomConfig: NSObject {
    public var channelName: String = ""     //正常rtm使用的频道
    public var rtmToken007: String = ""     //rtm login用，只能007
    public var rtcToken007: String = ""     //rtm join用
    
    public var rtcChannelName: String = ""  //rtc使用的频道
    public var rtcRtcToken006: String = ""  //rtc join使用
    public var rtcRtmToken006: String = ""  //rtc mcc使用，只能006
    
    public var rtcChorusChannelName: String = ""  //rtc 合唱使用的频道
    public var rtcChorusRtcToken007: String = ""  //rtc 合唱join使用
}

