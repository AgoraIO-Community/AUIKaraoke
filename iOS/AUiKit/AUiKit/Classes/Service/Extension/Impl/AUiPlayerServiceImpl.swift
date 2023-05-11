//
//  AUiPlayerServiceImpl.swift
//  AUiKit
//
//  Created by wushengtao on 2023/3/10.
//

import Foundation
import AgoraRtcKit
import YYModel

open class AUiPlayerServiceImpl: NSObject {
    private var respDelegates: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    private var ktvApi: KTVApiDelegate!
    private var rtcKit: AgoraRtcEngineKit!
    private var channelName: String!
    private var streamId: Int = 0
    
    deinit {
        aui_info("deinit AUiPlayerServiceImpl", tag: "AUiPlayerServiceImpl")
    }
    
    public init(channelName: String, rtcKit: AgoraRtcEngineKit, ktvApi: KTVApiDelegate, rtmManager: AUiRtmManager) {
        aui_info("init AUiPlayerServiceImpl", tag: "AUiPlayerServiceImpl")
        super.init()
        self.channelName = channelName
        self.rtcKit = rtcKit
        self.ktvApi = ktvApi

        let config = AgoraDataStreamConfig()
        config.ordered = false
        config.syncWithAudio = false
        rtcKit.createDataStream(&streamId, config: config)
    }
}

//MARK: AUiPlayerServiceDelegate
extension AUiPlayerServiceImpl: AUiPlayerServiceDelegate {
    
    public func bindRespDelegate(delegate: AUiPlayerRespDelegate) {
        respDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUiPlayerRespDelegate) {
        respDelegates.remove(delegate)
    }
    
    
    public func loadMusic(songCode: Int, config: KTVSongConfiguration, musicLoadStateListener: IMusicLoadStateListener) {
        ktvApi.loadMusic(songCode: songCode, config: config, onMusicLoadStateListener: musicLoadStateListener)
    }
    
    public func switchSingerRole(newRole: KTVSingRole, onSwitchRoleState: @escaping ISwitchRoleStateListener) {
        ktvApi.switchSingerRole(newRole: newRole, onSwitchRoleState: onSwitchRoleState)
    }
    
    public func startSing(songCode: Int) {
        ktvApi.startSing(songCode: songCode, startPos: 0)
    }
    
    public func stopSing() {
        ktvApi.getMediaPlayer()?.stop()
        ktvApi.switchSingerRole(newRole: .audience) { state, reason in
        }
    }
    
    public func getMusicPlayer() -> AgoraMusicPlayerProtocol? {
        return ktvApi.getMediaPlayer()
    }
    
    public func resumeSing() {
//        ktvApi.resumePlay()
        ktvApi.resumeSing()
    }
    
    public func pauseSing() {
        ktvApi.pauseSing()
    }
    
    public func seekSing(time: Int) {
        ktvApi.seekSing(time: time)
    }
    
    //音乐播放音量
    public func adjustMusicPlayerPlayoutVolume(volume: Int) {
        ktvApi.getMediaPlayer()?.adjustPlayoutVolume(Int32(volume))
    }
    
    public func adjustMusicPlayerPublishVolume(volume: Int) {
        ktvApi.getMediaPlayer()?.adjustPublishSignalVolume(Int32(volume))
//        ktvApi.adjustRemoteVolume(volume: Int32(volume))
    }
    
    //人声播放音量
    public func adjustPlaybackVolume(volume: Int) {
        rtcKit.adjustPlaybackSignalVolume(volume)
    }
    
    public func adjustRecordingSignalVolume(volume: Int) {
        rtcKit.adjustRecordingSignalVolume(volume)
    }
    
    public func selectMusicPlayerTrackMode(mode: KTVPlayerTrackMode) {
        ktvApi.getMediaPlayer()?.selectAudioTrack(mode == .origin ? 0 : 1)
    }
    
    public func getPlayerPosition() -> Int {
//        return ktvApi.getMediaPlayer().du
        return 0
    }
    
    public func getPlayerDuration() -> Int {
        return 0
    }
    
    public func getChannelName() -> String {
        return channelName
    }
    
    //升降调
    public func setAudioPitch(pitch: Int) {
        ktvApi.getMediaPlayer()?.setAudioPitch(pitch)
    }
    
    public func setAudioEffectPreset(present: AgoraAudioEffectPreset) {
        rtcKit.setAudioEffectPreset(present)
    }
    
    public func setVoiceConversionPreset(preset: AgoraVoiceConversionPreset) {
        rtcKit.setVoiceConversionPreset(preset)
    }
    
    public func enableEarMonitoring(inEarMonitoring: Bool) {
        rtcKit.enable(inEarMonitoring: inEarMonitoring)
    }
    
    public func setLrcView(delegate: KTVLrcViewDelegate) {
        ktvApi.setLrcView(view: delegate)
    }
    
    public func addEventHandler(ktvApiEventHandler: KTVApiEventHandlerDelegate) {
        ktvApi.addEventHandler(ktvApiEventHandler: ktvApiEventHandler)
    }
    
    public func removeEventHandler(ktvApiEventHandler: KTVApiEventHandlerDelegate) {
        ktvApi.removeEventHandler(ktvApiEventHandler: ktvApiEventHandler)
    }
    
    public func didKTVAPIReceiveAudioVolumeIndication(with speakers: [AgoraRtcAudioVolumeInfo], totalVolume: NSInteger) {
        ktvApi.didKTVAPIReceiveAudioVolumeIndication(with: speakers, totalVolume: totalVolume)
    }
    
    public func didKTVAPIReceiveStreamMessageFrom(uid: NSInteger, streamId: NSInteger, data: Data) {
        ktvApi.didKTVAPIReceiveStreamMessageFrom(uid: uid, streamId: streamId, data: data)
        respDelegates.objectEnumerator().forEach { obj in
            (obj as? AUiPlayerRespDelegate)?.onDataStreamMsgReceived(with: uid, streamId: streamId, data: data)
        }
    }
    
    public func didKTVAPILocalAudioStats(stats: AgoraRtcLocalAudioStats) {
        ktvApi.didKTVAPILocalAudioStats(stats: stats)
    }
    
    //开启耳返
    public func enableInEarMonitoring(enabled: Bool) {
        rtcKit.enable(inEarMonitoring: enabled, includeAudioFilters: .none)
    }
    
    public func sendStreamMsg(with dict: [String: Any]) {
        guard let data = compactDictionaryToData(dict) else {return}
       let code = rtcKit.sendStreamMessage(streamId, data: data)
        
    }
    
    private func compactDictionaryToData(_ dict: [String: Any]) -> Data? {
        do {
            let jsonData = try JSONSerialization.data(withJSONObject: dict, options: [])
            return jsonData
        }
        catch {
            print("Error encoding data: (error.localizedDescription)")
            return nil
        }
    }
    
//    public func setClientRole(role: AgoraClientRole) {
//        rtcKit.setClientRole(role)
//    }
    
//    public func publishAudioTrack(enable: Bool) {
//        let option = AgoraRtcChannelMediaOptions()
//        option.publishMicrophoneTrack = enable
//        rtcKit.updateChannel(with: option)
//    }
    
//    public func muteAudio(with uid: Int, enable: Bool, isLocal: Bool) {
//        if isLocal {
//            self.rtcKit.muteLocalAudioStream(enable)
//        } else {
//            self.rtcKit.muteRemoteAudioStream(UInt(uid), mute: enable)
//        }
//    }
//
//    public func muteVideo(with uid: Int, enable: Bool, isLocal: Bool) {
//        if isLocal {
//            self.rtcKit.muteLocalVideoStream(enable)
//        } else {
//            self.rtcKit.muteRemoteVideoStream(UInt(uid), mute: enable)
//        }
//    }
    
}

extension AUiPlayerServiceImpl: KTVApiEventHandlerDelegate {
    public func onChorusChannelTokenPrivilegeWillExpire(token: String?) {
        
    }
    
    public func onMusicPlayerStateChanged(state: AgoraMediaPlayerState, error: AgoraMediaPlayerError, isLocal: Bool) {
        
    }
    
    public func onSingingScoreResult(score: Float) {
        
    }
    
    public func onSingerRoleChanged(oldRole: KTVSingRole, newRole: KTVSingRole) {
        
    }
}

//MARK: KTVMusicLoadStateListener
extension AUiPlayerServiceImpl: IMusicLoadStateListener {
    
    public func onMusicLoadProgress(songCode: Int, percent: Int, status: AgoraMusicContentCenterPreloadStatus, msg: String?, lyricUrl: String?) {
        
    }
    
    public func onMusicLoadSuccess(songCode: Int, lyricUrl: String) {
        
    }
    
    public func onMusicLoadFail(songCode: Int, reason: KTVLoadSongFailReason) {
        
    }
    
    
    
    
//    public func onPlayerStateChanged(state: AgoraMediaPlayerState, isLocal: Bool) {
//        respDelegates.objectEnumerator().forEach { obj in
//            (obj as? AUiPlayerRespDelegate)?.onPlayerStateChanged(state: state, isLocal: isLocal)
//        }
//    }
//
//    public func onSyncMusicPosition(position: Int, pitch: Float) {
//        respDelegates.objectEnumerator().forEach { obj in
//            (obj as? AUiPlayerRespDelegate)?.onPlayerPositionDidChange(position: position)
//        }
//    }
//
//    public func onMusicLoaded(songCode: NSInteger, lyricUrl: String, role: KTVSingRole, state: KTVLoadSongState) {
//
//    }
//
//    public func onJoinChorusState(reason: KTVJoinChorusState) {
//
//    }
//
//    public func onSingerRoleChanged(oldRole: KTVSingRole, newRole: KTVSingRole) {
//
//    }
//
//    public func didSkipViewShowPreludeEndPosition() {
//        respDelegates.objectEnumerator().forEach { obj in
//            (obj as? AUiPlayerRespDelegate)?.onPreludeDidAppear()
//        }
//    }
//
//    public func didSkipViewShowEndDuration() {
//        respDelegates.objectEnumerator().forEach { obj in
//            (obj as? AUiPlayerRespDelegate)?.onPostludeDidAppear()
//        }
//    }
//
//    public func didlrcViewDidScrolled(with cumulativeScore: Int, totalScore: Int) {
//
//    }
//
//    public func didlrcViewDidScrollFinished(with cumulativeScore: Int, totalScore: Int, lineScore: Int) {
//
//    }
//
//
}

