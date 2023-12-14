package io.agora.asceneskit.karaoke

import android.util.Log
import io.agora.auikit.model.AUIRoomConfig
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.model.AUIUserInfo
import io.agora.auikit.service.IAUIChorusService
import io.agora.auikit.service.IAUIGiftsService
import io.agora.auikit.service.IAUIIMManagerService
import io.agora.auikit.service.IAUIJukeboxService
import io.agora.auikit.service.IAUIMicSeatService
import io.agora.auikit.service.IAUIMusicPlayerService
import io.agora.auikit.service.IAUIUserService
import io.agora.auikit.service.IAUIUserService.AUIUserRespObserver
import io.agora.auikit.service.arbiter.AUIArbiter
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.im.AUIChatManager
import io.agora.auikit.service.imp.AUIChorusServiceImpl
import io.agora.auikit.service.imp.AUIGiftServiceImpl
import io.agora.auikit.service.imp.AUIIMManagerServiceImpl
import io.agora.auikit.service.imp.AUIJukeboxServiceImpl
import io.agora.auikit.service.imp.AUIMicSeatServiceImpl
import io.agora.auikit.service.imp.AUIMusicPlayerServiceImpl
import io.agora.auikit.service.imp.AUIUserServiceImpl
import io.agora.auikit.service.ktv.KTVApi
import io.agora.auikit.service.ktv.KTVApiConfig
import io.agora.auikit.service.ktv.KTVApiImpl
import io.agora.auikit.service.rtm.AUIRtmErrorRespObserver
import io.agora.auikit.service.rtm.AUIRtmLockRespObserver
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.utils.AUILogger.Companion.logger
import io.agora.auikit.utils.AgoraEngineCreator
import io.agora.auikit.utils.GsonTools
import io.agora.auikit.utils.ObservableHelper
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoEncoderConfiguration
import io.agora.rtm.LockEvent
import io.agora.rtm.RtmClient
import io.agora.rtm.RtmConfig
import io.agora.rtm.RtmConstants
import io.agora.rtm.RtmEventListener


val kRoomInfoAttrKey = "basic"

class AUIKaraokeRoomService(
    apiConfig: AUIAPIConfig,
    private var roomConfig: AUIRoomConfig,
) {

    private val TAG = "AUIKaraokeRoomService"

    private val rtcEngineCreateByService = apiConfig.rtcEngineEx == null

    val rtcEngine: RtcEngine = apiConfig.rtcEngineEx ?: AgoraEngineCreator.createRtcEngine(
        AUIRoomContext.shared().requireCommonConfig().context,
        AUIRoomContext.shared().requireCommonConfig().appId
    )

    val rtmClient = apiConfig.rtmClient ?: RtmClient.create(
        RtmConfig.Builder(
            AUIRoomContext.shared().requireCommonConfig().appId,
            AUIRoomContext.shared().currentUserInfo.userId
        ).presenceTimeout(20).eventListener(object: RtmEventListener{
            override fun onLockEvent(event: LockEvent?) {
                super.onLockEvent(event)
                Log.d(TAG, "onLockEvent event: $event")
            }
        }).build()
    )

    val ktvApi: KTVApi = apiConfig.ktvApi ?: run {
        val config = KTVApiConfig(
            AUIRoomContext.shared().requireCommonConfig().appId,
            roomConfig.rtcRtmToken,
            rtcEngine,
            roomConfig.rtcChannelName,
            AUIRoomContext.shared().requireCommonConfig().userId.toInt(),
            roomConfig.rtcChorusChannelName,
            roomConfig.rtcChorusRtcToken
        )
        KTVApiImpl().apply {
            initialize(config)
        }
    }

    val rtmManager: AUIRtmManager = AUIRtmManager(
        AUIRoomContext.shared().requireCommonConfig().context,
        rtmClient,
        apiConfig.rtmClient != null
    )

    val chatManager by lazy { AUIChatManager(channelName, AUIRoomContext.shared()) }

    val observableHelper = ObservableHelper<AUIKaraokeRoomServiceRespObserver>()

    val userService: IAUIUserService by lazy {
        val user = AUIUserServiceImpl(channelName, rtmManager)
        user.registerRespObserver(userRespObserver)
        user
    }

    val imManagerService: IAUIIMManagerService by lazy {
        AUIIMManagerServiceImpl(
            channelName,
            rtmManager,
            chatManager
        )
    }

    val micSeatService: IAUIMicSeatService by lazy {
        AUIMicSeatServiceImpl(
            channelName,
            rtmManager
        )
    }

    val musicPlayerService: IAUIMusicPlayerService by lazy {
        AUIMusicPlayerServiceImpl(
            rtcEngine,
            channelName,
            ktvApi
        )
    }

    val chorusService: IAUIChorusService by lazy {
        AUIChorusServiceImpl(
            channelName,
            ktvApi,
            rtmManager
        )
    }

    val jukeboxService: IAUIJukeboxService by lazy {
        AUIJukeboxServiceImpl(
            channelName,
            rtmManager,
            ktvApi
        )
    }

    val giftService: IAUIGiftsService by lazy {
        AUIGiftServiceImpl(
            channelName,
            rtmManager,
            chatManager
        )
    }

    private val rtmErrorRespObserver = object : AUIRtmErrorRespObserver {
        override fun onTokenPrivilegeWillExpire(channelName: String?) {
            channelName ?: return
            observableHelper.notifyEventHandlers {
                it.onTokenPrivilegeWillExpire(channelName)
            }
        }

        override fun onMsgReceiveEmpty(channelName: String) {
            super.onMsgReceiveEmpty(channelName)
            observableHelper.notifyEventHandlers {
                it.onRoomDestroy(channelName)
            }
        }

        override fun onConnectionStateChanged(channelName: String?, state: Int, reason: Int) {
            super.onConnectionStateChanged(channelName, state, reason)
            if(reason == RtmConstants.RtmConnectionChangeReason.getValue(RtmConstants.RtmConnectionChangeReason.REJOIN_SUCCESS)){
                AUIRoomContext.shared().getArbiter(this@AUIKaraokeRoomService.channelName)?.acquire()
            }
            if(state == RtmConstants.RtmConnectionState.getValue(RtmConstants.RtmConnectionState.FAILED)
                && reason == RtmConstants.RtmConnectionChangeReason.getValue(RtmConstants.RtmConnectionChangeReason.BANNED_BY_SERVER)){
                observableHelper.notifyEventHandlers {
                    it.onRoomUserBeKicked(this@AUIKaraokeRoomService.channelName, AUIRoomContext.shared().currentUserInfo.userId)
                }
            }
        }
    }

    private val rtmLockRespObserver = object : AUIRtmLockRespObserver {
        override fun onReceiveLock(channelName: String, lockName: String, lockOwner: String) {
            lockRetrived = true
        }

        override fun onReleaseLock(channelName: String, lockName: String, lockOwner: String) {

        }
    }

    private val userRespObserver = object : AUIUserRespObserver {
        override fun onRoomUserSnapshot(roomId: String, userList: MutableList<AUIUserInfo>?) {
            userSnapshotList = userList
            userList?.firstOrNull { it.userId == AUIRoomContext.shared().currentUserInfo.userId }
                ?.let { user ->
                    onUserAudioMute(user.userId, (user.muteAudio == 1))
                    onUserVideoMute(user.userId, (user.muteVideo == 1))
                }
        }

        override fun onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
            super.onRoomUserLeave(roomId, userInfo)
            if (AUIRoomContext.shared().getRoomOwner(channelName) == userInfo.userId) {
                cleanRoom()
            } else {
                cleanUserInfo()
            }
        }

        override fun onUserAudioMute(userId: String, mute: Boolean) {
            if (userId != AUIRoomContext.shared().currentUserInfo.userId) {
                return
            }
            rtcEngine.adjustRecordingSignalVolume(if (mute) 0 else 100)
        }

        override fun onUserVideoMute(userId: String, mute: Boolean) {
            if (userId != AUIRoomContext.shared().currentUserInfo.userId) {
                return
            }
            rtcEngine.enableLocalVideo(!mute)
            val option = ChannelMediaOptions()
            option.publishCameraTrack = !mute
            rtcEngine.updateChannelMediaOptions(option)
        }
    }

    private var enterRoomCompletion: ((AUIRoomInfo?) -> Unit)? = null

    private var subscribeSuccess = false
        set(value) {
            field = value
            checkRoomValid()
        }

    private var lockRetrived = false
        set(value) {
            field = value
            checkRoomValid()
        }

    private var userSnapshotList: List<AUIUserInfo>? = null
        set(value) {
            field = value
            checkRoomValid()
        }

    var roomInfo: AUIRoomInfo? = null
        set(value) {
            field = value
            AUIRoomContext.shared().insertRoomInfo(value)
            checkRoomValid()
        }

    val channelName = roomConfig.channelName // roomId

    init {
        AUIRoomContext.shared().roomConfigMap[channelName] = roomConfig
        AUIRoomContext.shared().roomArbiterMap[channelName] = AUIArbiter(
            channelName,
            rtmManager,
            AUIRoomContext.shared().currentUserInfo.userId
        )
    }

    fun create(roomInfo: AUIRoomInfo, completion: (AUIException?) -> Unit) {
        this.roomInfo = roomInfo

        rtmManager.login(
            roomConfig.rtmToken
        ) { error ->
            if (error != null) {
                completion.invoke(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
                return@login
            }
            AUIRoomContext.shared().getArbiter(channelName)?.create()
            initRoom { initError ->
                if (initError != null) {
                    completion.invoke(initError)
                } else {
                    enter(completion)
                }
            }
        }
    }

    fun enter(completion: (AUIException?) -> Unit) {
        val roomId = channelName
        rtmManager.login(
            roomConfig.rtmToken
        ) { error ->
            if (error != null) {
                completion.invoke(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
                return@login
            }
            enterRoomCompletion = {
                ktvApi.renewInnerDataStreamId()
                completion.invoke(null)
            }

            if (roomInfo == null) {
                rtmManager.getMetadata(roomId) { _, metadata ->
                    roomInfo =
                        GsonTools.toBean(metadata?.get(kRoomInfoAttrKey), AUIRoomInfo::class.java)
                }
            }

            AUIRoomContext.shared().getArbiter(channelName)?.acquire()
            rtmManager.subscribeError(rtmErrorRespObserver)
            rtmManager.subscribeLock(channelName, observer = rtmLockRespObserver)
            rtmManager.subscribe(channelName) { subscribeError ->
                if (subscribeError != null) {
                    completion.invoke(AUIException(AUIException.ERROR_CODE_RTM, "error : $subscribeError"))
                    return@subscribe
                }
                subscribeSuccess = true
            }

            joinRtcChannel()
        }
    }

    fun destroy() {
        cleanRoomInfo()
        cleanRoom()
        AUIRoomContext.shared().getArbiter(channelName)?.destroy()
    }

    fun exit() {
        cleanUserInfo()
        cleanRoom()
    }

    private fun checkRoomValid() {
        if (subscribeSuccess && roomInfo != null && lockRetrived) {
            if (enterRoomCompletion != null) {
                enterRoomCompletion?.invoke(roomInfo)
                enterRoomCompletion = null
                observableHelper.notifyEventHandlers {
                    it.onRoomInfoChange(channelName, roomInfo!!)
                }
            }

            // room owner not found, clean room
            if (userSnapshotList?.find {
                    it.userId == AUIRoomContext.shared().getRoomOwner(channelName)
                } == null) {
                cleanRoomInfo()
            }
        }
    }

    private fun initRoom(completion: (AUIException?) -> Unit) {
        val roomInfoStr = GsonTools.beanToString(roomInfo)
        if (roomInfoStr == null) {
            completion.invoke(
                AUIException(
                    AUIException.ERROR_CODE_NETWORK_PARSE,
                    "initRoom >> bean to string failed!"
                )
            )
            return
        }
        rtmManager.setBatchMetadata(
            channelName,
            lockName = "",
            metadata = mapOf(Pair(kRoomInfoAttrKey, roomInfoStr)),
            fetchImmediately = true
        ) { err ->
            if (err == null) {
                completion.invoke(null)
            } else {
                completion.invoke(
                    AUIException(
                        AUIException.ERROR_CODE_RTM,
                        "initRoom >> setBatchMetadata failed : $err"
                    )
                )
            }
        }
    }

    private fun cleanRoom() {
        rtmManager.unSubscribe(channelName)
        rtmManager.unSubscribeError(rtmErrorRespObserver)
        rtmManager.unsubscribeLock(rtmLockRespObserver)
        rtmManager.logout()
        ktvApi.release()
        leaveRtcChannel()
        AUIRoomContext.shared().cleanRoom(channelName)
    }

    private fun leaveRtcChannel() {
        rtcEngine.leaveChannel()
        if (rtcEngineCreateByService) {
            RtcEngine.destroy()
        }
    }

    private fun cleanUserInfo() {
        if (AUIRoomContext.shared().getArbiter(channelName)?.isArbiter() == true) {
            return
        }
        val index = micSeatService.getMicSeatIndex(AUIRoomContext.shared().currentUserInfo.userId)
        if (index >= 0) {
            micSeatService.kickSeat(index) {}
        }
    }

    private fun cleanRoomInfo() {
        if (AUIRoomContext.shared().getArbiter(channelName)?.isArbiter() == true) {
            return
        }
        micSeatService.deInitService {}
        musicPlayerService.deInitService { }
        chorusService.deInitService { }
        imManagerService.deInitService { }
        rtmManager.cleanBatchMetadata(
            channelName,
            remoteKeys = listOf(kRoomInfoAttrKey),
            fetchImmediately = true
        ) {}
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
            rtcEngine.updateChannelMediaOptions(mainChannelMediaOption)
        } else {
            val mainChannelMediaOption = ChannelMediaOptions()
            mainChannelMediaOption.publishMicrophoneTrack = false
            mainChannelMediaOption.enableAudioRecordingOrPlayout = true
            mainChannelMediaOption.autoSubscribeVideo = true
            mainChannelMediaOption.autoSubscribeAudio = true
            mainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
            rtcEngine.updateChannelMediaOptions(mainChannelMediaOption)
        }
    }

    fun setupLocalAudioMute(isMute: Boolean) {
        if (isMute) {
            ktvApi.setMicStatus(false)
            rtcEngine.adjustRecordingSignalVolume(0)
        } else {
            ktvApi.setMicStatus(true)
            rtcEngine.adjustRecordingSignalVolume(100)
        }
    }

    fun setupRemoteAudioMute(userId: String, isMute: Boolean) {
        rtcEngine.muteRemoteAudioStream(userId.toInt(), isMute)
    }

    fun renew(config: AUIRoomConfig) {
        roomConfig = config
        AUIRoomContext.shared().roomConfigMap[channelName] = config

        // KTVApi renew
        ktvApi.renewToken(config.rtcRtmToken, config.rtcChorusRtcToken)

        // rtm renew
        rtmManager.renew(config.rtmToken)
        rtmManager.renewStreamChannelToken(config.channelName, config.rtcToken)

        // rtc renew
        rtcEngine.renewToken(config.rtcRtcToken)
    }

    private fun joinRtcChannel() {
        setEngineConfig()
        val ret: Int = rtcEngine.joinChannel(
            roomConfig.rtcRtcToken,
            roomConfig.rtcChannelName,
            AUIRoomContext.shared().requireCommonConfig().userId.toInt(),
            channelMediaOptions()
        )

        if (ret == Constants.ERR_OK) {
            logger().d(TAG, "join rtc room success")
        } else {
            logger().d(TAG, "join rtc room failed")
        }
    }

    private fun channelMediaOptions() = ChannelMediaOptions().apply {
        val isRoomOwner = AUIRoomContext.shared().isRoomOwner(channelName)
        clientRoleType = if(isRoomOwner) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE
        publishCameraTrack = false
        publishMicrophoneTrack = isRoomOwner
        publishCustomAudioTrack = false
        autoSubscribeAudio = true
        autoSubscribeVideo = true
        publishMediaPlayerId = ktvApi.getMediaPlayer().mediaPlayerId
        enableAudioRecordingOrPlayout = true
    }

    private fun setEngineConfig(){
        rtcEngine.setDefaultAudioRoutetoSpeakerphone(true)
        rtcEngine.enableLocalAudio(true)
//        rtcEngine.setAudioScenario(.gameStreaming)
        rtcEngine.setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY)
//        rtcEngine.setChannelProfile(.liveBroadcasting)
        rtcEngine.setParameters("{\"rtc.enable_nasa2\": false}")
        rtcEngine.setParameters("{\"rtc.ntp_delay_drop_threshold\": 1000}")
        rtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp\": true}")
        rtcEngine.setParameters("{\"rtc.net.maxS2LDelay\": 800}")
        rtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\": true}")
        rtcEngine.setParameters("{\"rtc.net.maxS2LDelayBroadcast\": 400}")
        rtcEngine.setParameters("{\"che.audio.neteq.prebuffer\": true}")
        rtcEngine.setParameters("{\"che.audio.neteq.prebuffer_max_delay\": 600}")
        rtcEngine.setParameters("{\"che.audio.max_mixed_participants\": 8}")
        rtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast_dynamic\": true}")
        rtcEngine.setParameters("{\"che.audio.custom_bitrate\": 48000}")

        //开启唱歌评分功能
        rtcEngine.enableAudioVolumeIndication(50, 3, true)
        rtcEngine.enableAudio()
        rtcEngine.enableVideo()

        rtcEngine.setVideoEncoderConfiguration(VideoEncoderConfiguration(
            VideoEncoderConfiguration.VideoDimensions(100, 100),
            VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_7,
            20,
            VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_LANDSCAPE,
            VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_AUTO
        ))
    }

}