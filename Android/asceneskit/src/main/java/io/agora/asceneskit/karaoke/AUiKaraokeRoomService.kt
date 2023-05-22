package io.agora.asceneskit.karaoke

import android.util.Log
import io.agora.auikit.model.AUiRoomConfig
import io.agora.auikit.model.AUiRoomContext
import io.agora.auikit.model.AUiRoomInfo
import io.agora.auikit.model.AUiUserInfo
import io.agora.auikit.service.IAUiChorusService
import io.agora.auikit.service.IAUiJukeboxService
import io.agora.auikit.service.IAUiMicSeatService
import io.agora.auikit.service.IAUiMusicPlayerService
import io.agora.auikit.service.IAUiUserService
import io.agora.auikit.service.IAUiUserService.AUiUserRespDelegate
import io.agora.auikit.service.callback.AUiException
import io.agora.auikit.service.imp.*
import io.agora.auikit.service.ktv.KTVApi
import io.agora.auikit.service.ktv.KTVApiConfig
import io.agora.auikit.service.ktv.KTVApiImpl
import io.agora.auikit.service.rtm.AUiRtmManager
import io.agora.auikit.utils.AUiLogger.Companion.logger
import io.agora.auikit.utils.AgoraEngineCreator
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine


class AUiKaraokeRoomService(
    private val rtcEngine: RtcEngine?,
    private val ktvApi: KTVApi?,
    private val roomManager: AUiRoomManagerImpl,
    private val roomConfig: AUiRoomConfig,
    private val roomInfo: AUiRoomInfo
): AUiUserRespDelegate {

    private val TAG = "AUiKaraokeRoomService"

    private val channelName: String
        get() { return  roomInfo.roomId }

    private val rtmManager: AUiRtmManager = roomManager.rtmManager

    private val userImpl: IAUiUserService by lazy {
        val user = AUiUserServiceImpl(roomInfo.roomId, rtmManager)
        user.bindRespDelegate(this)
        user
    }

    private val micSeatImpl: IAUiMicSeatService by lazy { AUiMicSeatServiceImpl(roomInfo.roomId, rtmManager) }

    private val playerImpl: IAUiMusicPlayerService by lazy { AUiMusicPlayerServiceImpl(mRtcEngine, roomInfo.roomId, mKtvApi) }

    private val chorusImpl: IAUiChorusService by lazy { AUiChorusServiceImpl(roomInfo.roomId, mKtvApi, rtmManager) }

    private val jukeboxImpl: IAUiJukeboxService by lazy { AUiJukeboxServiceImpl(roomInfo.roomId, rtmManager, mKtvApi) }

    private val mRtcEngine: RtcEngine = rtcEngine ?: AgoraEngineCreator.createRtcEngine(
        AUiRoomContext.shared().commonConfig.context,
        AUiRoomContext.shared().commonConfig.appId
    )
    private val mKtvApi: KTVApi = ktvApi ?: run {
        val config = KTVApiConfig(
            AUiRoomContext.shared().commonConfig.appId,
            roomConfig.rtcRtmToken006,
            mRtcEngine,
            roomConfig.rtcChannelName,
            AUiRoomContext.shared().commonConfig.userId.toInt(),
            roomConfig.rtcChorusChannelName,
            roomConfig.rtcChorusRtcToken007
        )
        KTVApiImpl(config)
    }
    fun getRoomManager() = roomManager
    fun getUserService() = userImpl
    fun getMicSeatsService() = micSeatImpl
    fun getJukeboxService() = jukeboxImpl
    fun getChorusService() = chorusImpl
    fun getMusicPlayerService() = playerImpl
    fun enterRoom(success: (AUiRoomInfo) -> Unit, failure: (AUiException) -> Unit) {
        roomManager.enterRoom(channelName, roomConfig.rtcToken007) { error ->
            logger().d(TAG, "enterRoom result : $error")
            if (error != null) {
                // failure
                failure.invoke(error)
            } else {
                // success
                success.invoke(roomInfo)

                // TODO Workaround: 在rtm加入成功之后才能加入rtc频道，且频道名不同，rtc频道为roomName+_rtc
                joinRtcRoom()
            }
            logger().d(TAG, "enterRoom end ...")
        }
    }
    fun destroyRoom() {
        roomManager.destroyRoom(channelName) {}
        mKtvApi.release()
        mRtcEngine.leaveChannel()
    }
    fun setupLocalStreamOn(isOn: Boolean) {
        Log.d("rtc_publish_state", "isOn: $isOn")
        if (isOn) {
            val mainChannelMediaOption = ChannelMediaOptions()
            mainChannelMediaOption.publishMicrophoneTrack = true
            mainChannelMediaOption.enableAudioRecordingOrPlayout = true
            mainChannelMediaOption.autoSubscribeVideo = true
            mainChannelMediaOption.autoSubscribeAudio = true
            mainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            mRtcEngine.updateChannelMediaOptions(mainChannelMediaOption)
        } else {
            val mainChannelMediaOption = ChannelMediaOptions()
            mainChannelMediaOption.publishMicrophoneTrack = false
            mainChannelMediaOption.enableAudioRecordingOrPlayout = true
            mainChannelMediaOption.autoSubscribeVideo = true
            mainChannelMediaOption.autoSubscribeAudio = true
            mainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
            mRtcEngine.updateChannelMediaOptions(mainChannelMediaOption)
        }
    }
    fun setupLocalAudioMute(isMute: Boolean) {
        if (isMute) {
            mKtvApi.setMicStatus(false)
            mRtcEngine.adjustRecordingSignalVolume(0)
        } else {
            mKtvApi.setMicStatus(true)
            mRtcEngine.adjustRecordingSignalVolume(100)
        }
    }

    fun setupRemoteAudioMute(userId: String, isMute: Boolean) {
        mRtcEngine.muteRemoteAudioStream(userId.toInt(), isMute)
    }

    private fun joinRtcRoom() {
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
        mRtcEngine.enableVideo()
        mRtcEngine.enableLocalVideo(false)
        mRtcEngine.enableAudio()
        mRtcEngine.setAudioProfile(
            Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY,
            Constants.AUDIO_SCENARIO_GAME_STREAMING
        )
        mRtcEngine.enableAudioVolumeIndication(50, 10, true)
        mRtcEngine.setClientRole(if (AUiRoomContext.shared().isRoomOwner(channelName)) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE)
        val ret: Int = mRtcEngine.joinChannel(
            roomConfig.rtcRtcToken006,
            roomConfig.rtcChannelName,
            null,
            AUiRoomContext.shared().commonConfig.userId.toInt()
        )

        if (ret == Constants.ERR_OK) {
            logger().d(TAG, "join rtc room success")
        }else{
            logger().d(TAG, "join rtc room failed")
        }
    }

    /** AUiUserRespDelegate */
    override fun onRoomUserSnapshot(roomId: String, userList: MutableList<AUiUserInfo>?) {
        userList?.firstOrNull { it.userId == AUiRoomContext.shared().currentUserInfo.userId }?.let { user ->
            onUserAudioMute(user.userId, (user.muteAudio == 1))
            onUserVideoMute(user.userId, (user.muteVideo == 1))
        }
    }

    override fun onUserAudioMute(userId: String, mute: Boolean) {
        if (userId != AUiRoomContext.shared().currentUserInfo.userId) {
            return
        }
        rtcEngine?.adjustRecordingSignalVolume(if (mute) 0 else 100)
    }

    override fun onUserVideoMute(userId: String, mute: Boolean) {
        if (userId != AUiRoomContext.shared().currentUserInfo.userId) {
            return
        }
        rtcEngine?.enableLocalVideo(!mute)
        val option = ChannelMediaOptions()
        option.publishCameraTrack = !mute
        rtcEngine?.updateChannelMediaOptions(option)
    }
}