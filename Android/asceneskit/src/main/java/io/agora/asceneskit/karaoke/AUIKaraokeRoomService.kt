package io.agora.asceneskit.karaoke

import android.util.Log
import com.google.gson.reflect.TypeToken
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
import io.agora.auikit.service.rtm.AUIRtmPayload
import io.agora.auikit.utils.AUILogger.Companion.logger
import io.agora.auikit.utils.AgoraEngineCreator
import io.agora.auikit.utils.GsonTools
import io.agora.auikit.utils.ObservableHelper
import io.agora.auikit.utils.ThreadManager
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

    val channelName = roomConfig.channelName // roomId

    private val rtcEngineCreateByService = apiConfig.rtcEngineEx == null
    private val rtmCreateCreateByService = apiConfig.rtmClient == null

    val rtcEngine: RtcEngine = apiConfig.rtcEngineEx ?: AgoraEngineCreator.createRtcEngine(
        AUIRoomContext.shared().requireCommonConfig().context,
        AUIRoomContext.shared().requireCommonConfig().appId
    )

    val rtmClient = apiConfig.rtmClient ?: RtmClient.create(
        RtmConfig.Builder(
            AUIRoomContext.shared().requireCommonConfig().appId,
            AUIRoomContext.shared().currentUserInfo.userId
        ).presenceTimeout(60).eventListener(object: RtmEventListener{
            override fun onLockEvent(event: LockEvent?) {
                super.onLockEvent(event)
                Log.d(TAG, "onLockEvent event: $event")
            }
        }).build()
    )

    val ktvApi: KTVApi = apiConfig.ktvApi ?: run {
        val config = KTVApiConfig(
            AUIRoomContext.shared().requireCommonConfig().appId,
            roomConfig.rtmToken,
            rtcEngine,
            roomConfig.channelName,
            AUIRoomContext.shared().currentUserInfo.userId.toInt(),
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

    val chatManager = AUIChatManager(channelName, AUIRoomContext.shared())

    val observableHelper = ObservableHelper<AUIKaraokeRoomServiceRespObserver>()

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
                cleanRoomInfo(roomId)
            } else {
                cleanUserInfo(roomId, userInfo.userId)
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

    val userService: IAUIUserService = AUIUserServiceImpl(channelName, rtmManager).apply {
        registerRespObserver(userRespObserver)
    }

    val imManagerService: IAUIIMManagerService = AUIIMManagerServiceImpl(
        channelName,
        rtmManager,
        chatManager
    )

    val micSeatService: IAUIMicSeatService = AUIMicSeatServiceImpl(
        channelName,
        rtmManager
    )

    val musicPlayerService: IAUIMusicPlayerService = AUIMusicPlayerServiceImpl(
        rtcEngine,
        channelName,
        ktvApi
    )

    val chorusService: IAUIChorusService = AUIChorusServiceImpl(
        channelName,
        ktvApi,
        rtmManager
    )

    val jukeboxService: IAUIJukeboxService = AUIJukeboxServiceImpl(
        channelName,
        rtmManager,
        ktvApi
    )

    val giftService: IAUIGiftsService = AUIGiftServiceImpl(
        channelName,
        rtmManager,
        chatManager
    )

    private var isRoomDestroyed = false

    private val rtmErrorRespObserver = object : AUIRtmErrorRespObserver {
        override fun onTokenPrivilegeWillExpire(channelName: String?) {
            channelName ?: return
            observableHelper.notifyEventHandlers {
                it.onTokenPrivilegeWillExpire(channelName)
            }
        }

        override fun onMsgReceiveEmpty(channelName: String) {
            super.onMsgReceiveEmpty(channelName)
            if (this@AUIKaraokeRoomService.channelName != channelName) {
                return
            }
            isRoomDestroyed = true
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



    private var enterRoomCompletion: ((AUIRoomInfo?) -> Unit)? = null

    private var subscribeSuccess = false
        set(value) {
            if(field != value){
                field = value
                checkRoomValid()
            }
        }

    private var lockRetrived = false
        set(value) {
            if(field != value){
                field = value
                checkRoomValid()
            }
        }

    private var userSnapshotList: List<AUIUserInfo>? = null
        set(value) {
            if(field != value){
                field = value
                checkRoomValid()
            }
        }

    var roomInfo: AUIRoomInfo? = null
        set(value) {
            if(field != value){
                field = value
                AUIRoomContext.shared().insertRoomInfo(value)
                checkRoomValid()
            }
        }

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
                    val payloadInfo = GsonTools.toBean<AUIRtmPayload<AUIRoomInfo>>(
                        metadata?.get(kRoomInfoAttrKey),
                        object : TypeToken<AUIRtmPayload<AUIRoomInfo>>() {}.type
                    )
                    payloadInfo?.payload?.roomId = payloadInfo?.roomId ?: ""
                    roomInfo = payloadInfo?.payload ?: AUIRoomInfo()
                }
            }

            AUIRoomContext.shared().getArbiter(channelName)?.acquire { lockError ->
                if(lockError != null && enterRoomCompletion != null){
                    enterRoomCompletion = null
                    isRoomDestroyed = true
                    ThreadManager.getInstance().runOnMainThread {
                        completion.invoke(AUIException(AUIException.ERROR_CODE_RTM, "error: $lockError"))
                    }
                    return@acquire
                }
            }
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
        cleanRoomInfo(channelName)
        cleanRoom()
    }

    fun exit(): Boolean {
        cleanUserInfo(channelName, AUIRoomContext.shared().currentUserInfo.userId)
        cleanRoom()
        AUIRoomContext.shared().getArbiter(channelName)?.release()
        return isRoomDestroyed
    }

    private fun checkRoomValid() {
        if (subscribeSuccess && roomInfo != null && lockRetrived && userSnapshotList != null) {

            micSeatService.initService{}

            if (enterRoomCompletion != null) {
                enterRoomCompletion?.invoke(roomInfo)
                enterRoomCompletion = null
                observableHelper.notifyEventHandlers {
                    it.onRoomInfoChange(channelName, roomInfo!!)
                }
            }

            // room owner not found, clean room
            val snapShotOwnerId = userSnapshotList?.find {
                it.userId == AUIRoomContext.shared().getRoomOwner(channelName)
            }
            if (snapShotOwnerId == null) {
                cleanRoomInfo(channelName)
            }

            imManagerService.serviceDidLoad()
        }
    }

    private fun initRoom(completion: (AUIException?) -> Unit) {
        val basicInfo = AUIRtmPayload<AUIRoomInfo>(
            channelName,
            payload = roomInfo
        )

        val basicInfoStr = GsonTools.beanToString(basicInfo)
        if (basicInfoStr == null) {
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
            metadata = mapOf(Pair(kRoomInfoAttrKey, basicInfoStr)),
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
        ktvApi.release()
        logoutRtm()
        leaveRtcChannel()
        AUIRoomContext.shared().cleanRoom(channelName)
    }

    private fun logoutRtm() {
        rtmManager.deInit()
        rtmManager.unSubscribe(channelName)
        rtmManager.unSubscribeError(rtmErrorRespObserver)
        rtmManager.unsubscribeLock(rtmLockRespObserver)
        rtmManager.logout()
        if (rtmCreateCreateByService) {
            RtmClient.release()
        }
    }

    private fun leaveRtcChannel() {
        rtcEngine.leaveChannel()
        if (rtcEngineCreateByService) {
            RtcEngine.destroy()
        }
    }

    private fun cleanUserInfo(roomId: String, userId: String) {
        if (AUIRoomContext.shared().getArbiter(roomId)?.isArbiter() != true) {
            return
        }
        val index = micSeatService.getMicSeatIndex(userId)
        if (index >= 0) {
            micSeatService.kickSeat(index) {}
        }
    }

    private fun cleanRoomInfo(roomId: String) {
        if (AUIRoomContext.shared().getArbiter(roomId)?.isArbiter() != true) {
            return
        }
        micSeatService.deInitService { }
        chorusService.deInitService { }
        jukeboxService.deInitService { }
        imManagerService.deInitService { }
        rtmManager.cleanBatchMetadata(
            channelName,
            remoteKeys = listOf(kRoomInfoAttrKey),
            fetchImmediately = true
        ) {}
        AUIRoomContext.shared().getArbiter(channelName)?.destroy()
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
        ktvApi.renewToken(config.rtmToken, config.rtcChorusRtcToken)

        // rtm renew
        rtmManager.renew(config.rtmToken)
        rtmManager.renewStreamChannelToken(config.channelName, config.rtcToken)

        // rtc renew
        rtcEngine.renewToken(config.rtcToken)
    }

    private fun joinRtcChannel() {
        setEngineConfig()
        val ret: Int = rtcEngine.joinChannel(
            roomConfig.rtcToken,
            roomConfig.channelName,
            AUIRoomContext.shared().currentUserInfo.userId.toInt(),
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