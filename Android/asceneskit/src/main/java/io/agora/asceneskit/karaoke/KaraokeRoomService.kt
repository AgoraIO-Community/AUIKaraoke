package io.agora.asceneskit.karaoke

import android.util.Log
import io.agora.auikit.model.AUiRoomContext
import io.agora.auikit.model.AUiRoomInfo
import io.agora.auikit.model.AUiUserInfo
import io.agora.auikit.service.IAUiChorusService
import io.agora.auikit.service.IAUiJukeboxService
import io.agora.auikit.service.IAUiMicSeatService
import io.agora.auikit.service.IAUiMusicPlayerService
import io.agora.auikit.service.IAUiRoomManager
import io.agora.auikit.service.IAUiUserService
import io.agora.auikit.service.callback.AUiException
import io.agora.auikit.service.ktv.KTVApi
import io.agora.auikit.utils.AUiLogger.Companion.logger
import io.agora.auikit.utils.DelegateHelper
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine


class KaraokeRoomService(
    private val mRoomContext: AUiRoomContext,
    private val mRtcEngine: RtcEngine,
    private val roomInfo: AUiRoomInfo,
    private val roomToken: String,
    private val rtcToken: String,
    private val roomManager: IAUiRoomManager,
    private val userService: IAUiUserService,
    private val micSeatService: IAUiMicSeatService,
    private val jukeboxService: IAUiJukeboxService,
    private val musicPlayerService: IAUiMusicPlayerService,
    private val chorusService: IAUiChorusService,
    private val ktvApi: KTVApi
) : IKaraokeRoomService {

    private val mDelegateHelper = DelegateHelper<IKaraokeRoomService.KaraokeRoomRoomRespDelegate>()

    override fun bindRespDelegate(delegate: IKaraokeRoomService.KaraokeRoomRoomRespDelegate?) {
        mDelegateHelper.bindDelegate(delegate)
    }

    override fun unbindRespDelegate(delegate: IKaraokeRoomService.KaraokeRoomRoomRespDelegate?) {
        mDelegateHelper.unBindDelegate(delegate)
    }

    override fun getContext() = roomManager.context
    override fun getChannelName() = roomInfo.roomId
    override fun getRoomManager() = roomManager
    override fun getUserService() = userService
    override fun getMicSeatsService() = micSeatService
    override fun getJukeboxService() = jukeboxService
    override fun getChorusService() = chorusService

    override fun getMusicPlayerService() = musicPlayerService

    override fun enterRoom(success: (AUiRoomInfo) -> Unit, failure: (AUiException) -> Unit) {
        logger().d("EnterRoom", "enterRoom $channelName start ...")
        roomManager.enterRoom(channelName, roomToken) { error ->
            logger().d("EnterRoom", "enterRoom $channelName result : $error")
            if (error != null) {
                // failure
                failure.invoke(error)
            } else {
                // success
                success.invoke(roomInfo)

                // TODO Workaround: 在rtm加入成功之后才能加入rtc频道，且频道名不同，rtc频道为roomName+_rtc
                joinRtcRoom()
            }
            logger().d("EnterRoom", "enterRoom $channelName end ...")
        }
    }

    override fun exitRoom() {
        roomManager.exitRoom(channelName) {}
        ktvApi.release()
        mDelegateHelper.notifyDelegate { it.onRoomExitedOrDestroyed() }
        leaveRtcRoom()
    }

    override fun destroyRoom() {
        roomManager.destroyRoom(channelName) {}
        ktvApi.release()
        mDelegateHelper.notifyDelegate { it.onRoomExitedOrDestroyed() }
        leaveRtcRoom()
    }

    override fun getRoomInfo() = roomInfo

    override fun getUserList(callback: (List<AUiUserInfo>) -> Unit) {
        userService.getUserInfoList(channelName, null) { error, userList ->
            if (error != null || userList == null) {
                logger().e(
                    "KaraokeRoomService",
                    "getUserInfoList error : $error - $userList"
                )
            } else {
                logger().d(
                    "KaraokeRoomService",
                    "getUserInfoList userList : $userList"
                )
                callback.invoke(userList)
            }
        }
    }
    override fun setupLocalStreamOn(isOn: Boolean) {
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
    override fun setupLocalAudioMute(isMute: Boolean) {
        if (isMute) {
            ktvApi.setMicStatus(false)
            mRtcEngine.adjustRecordingSignalVolume(0)
        } else {
            ktvApi.setMicStatus(true)
            mRtcEngine.adjustRecordingSignalVolume(100)
        }
    }

    override fun setupRemoteAudioMute(userId: String, isMute: Boolean) {
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
        mRtcEngine.setClientRole(if (context.isRoomOwner(channelName)) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE)
        val ret: Int = mRtcEngine.joinChannel(
            rtcToken,
            channelName,
            null,
            mRoomContext.roomConfig.userId.toInt()
        )
        if (ret != Constants.ERR_OK) {
            // TODO LOG
        }
    }

    private fun leaveRtcRoom() {
        mRtcEngine.leaveChannel()
    }
}