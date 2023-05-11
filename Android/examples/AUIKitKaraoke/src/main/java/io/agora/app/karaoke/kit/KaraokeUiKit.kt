package io.agora.app.karaoke.kit

import io.agora.app.karaoke.BuildConfig
import io.agora.asceneskit.karaoke.KaraokeRoomService
import io.agora.auikit.model.AUiCommonConfig
import io.agora.auikit.model.AUiCreateRoomInfo
import io.agora.auikit.model.AUiRoomConfig
import io.agora.auikit.model.AUiRoomContext
import io.agora.auikit.model.AUiRoomInfo
import io.agora.auikit.service.callback.AUiException
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.application.ApplicationInterface
import io.agora.auikit.service.http.application.TokenGenerateReq
import io.agora.auikit.service.http.application.TokenGenerateResp
import io.agora.auikit.service.imp.AUiChorusServiceImpl
import io.agora.auikit.service.imp.AUiJukeboxServiceImpl
import io.agora.auikit.service.imp.AUiMicSeatServiceImpl
import io.agora.auikit.service.imp.AUiMusicPlayerServiceImpl
import io.agora.auikit.service.imp.AUiRoomManagerImpl
import io.agora.auikit.service.imp.AUiUserServiceImpl
import io.agora.auikit.service.ktv.KTVApi
import io.agora.auikit.service.ktv.KTVApiConfig
import io.agora.auikit.utils.AUiLogger
import io.agora.auikit.utils.AgoraEngineCreator
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineEx
import io.agora.rtm.RtmClient
import retrofit2.Response

object KaraokeUiKit {
    private val notInitException =
        RuntimeException("The KaraokeServiceManager has not been initialized!")
    private val initedException =
        RuntimeException("The KaraokeServiceManager has been initialized!")

    private var mRoomManager: AUiRoomManagerImpl? = null
    private var mRoomContext: AUiRoomContext? = null

    private var shouldReleaseRtc = true
    private var mRtmClient: RtmClient? = null
    private var mRtcEngineEx: RtcEngineEx? = null
    private var mKTVApi: KTVApi? = null

    /**
     * 初始化。
     * 对于rtmClient、rtcEngineEx、ktvApi：
     *      当外部没传时内部会自行创建，并在release方法调用时销毁；
     *      当外部传入时在release时不会销毁
     */
    fun init(
        config: AUiCommonConfig,
        rtmClient: RtmClient? = null,
        rtcEngineEx: RtcEngineEx? = null,
        ktvApi: KTVApi? = null
    ) {
        if (mRoomManager != null) {
            throw initedException
        }
        HttpManager.setBaseURL(BuildConfig.SERVER_HOST)
        val roomContext = AUiRoomContext()
        roomContext.roomConfig = config
        roomContext.currentUserInfo.userId = config.userId
        roomContext.currentUserInfo.userName = config.userName
        roomContext.currentUserInfo.userAvatar = config.userAvatar
        mRoomContext = roomContext

        mRoomManager = AUiRoomManagerImpl(roomContext, rtmClient)

        if (rtcEngineEx != null) { // 用户塞进来的engine由用户自己管理生命周期
            mRtcEngineEx = rtcEngineEx
            shouldReleaseRtc = false
        }
        if (ktvApi != null) {
            mKTVApi = ktvApi
        }
        AUiLogger.initLogger(AUiLogger.Config(roomContext.roomConfig.context, "Karaoke"))
    }

    /**
     * 释放资源
     */
    fun release() {
        if (shouldReleaseRtc) {
            RtcEngine.destroy()
        }
        mRoomContext = null
        mRtcEngineEx = null
        mRtmClient = null
        mRoomManager = null
        mKTVApi = null
    }

    /**
     * 获取房间列表
     */
    fun getRoomList(
        startTime: Long?,
        pageSize: Int,
        success: (List<AUiRoomInfo>) -> Unit,
        failure: (AUiException) -> Unit
    ) {
        val roomManager = mRoomManager ?: throw notInitException
        roomManager.getRoomInfoList(
            startTime, pageSize
        ) { error, roomList ->
            if (error == null) {
                success.invoke(roomList ?: emptyList())
            } else {
                failure.invoke(error)
            }
        }
    }

    /**
     * 创建房间
     */
    fun createRoom(
        createRoomInfo: AUiCreateRoomInfo,
        success: (AUiRoomInfo) -> Unit,
        failure: (AUiException) -> Unit
    ) {
        val roomManager = mRoomManager ?: throw notInitException
        roomManager.createRoom(
            createRoomInfo
        ) { error, roomInfo ->
            if (error == null && roomInfo != null) {
                success.invoke(roomInfo)
            } else {
                failure.invoke(error ?: AUiException(-999, "RoomInfo return null"))
            }
        }
    }

    /**
     * 拉起并跳转的房间页面
     */
    fun launchRoom(
        roomInfo: AUiRoomInfo,
        config: AUiRoomConfig,
        eventHandler: RoomEventHandler? = null,
    ) {
        generateTokenWithConfig(config) {
            val roomContext = mRoomContext ?: throw notInitException

            mRtcEngineEx = mRtcEngineEx ?: AgoraEngineCreator.createRtcEngine(
                roomContext.roomConfig.context,
                roomContext.roomConfig.appId
            )
            mKTVApi = mKTVApi ?: AgoraEngineCreator.createKTVApi()

            val roomManager = mRoomManager ?: throw notInitException
            val rtcEngine = mRtcEngineEx ?: throw notInitException
            val ktvApi = mKTVApi ?: throw notInitException
            val channelName = roomInfo.roomId
            val rtmManager = roomManager.rtmManager

            // login rtm
            rtmManager.login(
                config.tokenMap[AUiRoomConfig.TOKEN_RTM_LOGIN]
            ) { error ->
                if (error == null) {
                    // init ktv api
                    ktvApi.initialize(
                        KTVApiConfig(
                            roomContext.roomConfig.appId,
                            config.tokenMap[AUiRoomConfig.TOKEN_RTM_KTV],
                            rtcEngine,
                            config.ktvChannelName,
                            roomContext.roomConfig.userId.toInt(),
                            config.ktvChorusChannelName,
                            config.tokenMap[AUiRoomConfig.TOKEN_RTC_KTV_CHORUS]
                        )
                    )
                    ktvApi.renewInnerDataStreamId()

                    // create room service
                    val roomService = KaraokeRoomService(
                        roomContext,
                        rtcEngine,
                        roomInfo,
                        config.tokenMap[AUiRoomConfig.TOKEN_RTC_007],
                        config.tokenMap[AUiRoomConfig.TOKEN_RTC_SERVICE],
                        roomManager,
                        AUiUserServiceImpl(roomContext, channelName, rtmManager),
                        AUiMicSeatServiceImpl(roomContext, channelName, rtmManager),
                        AUiJukeboxServiceImpl(roomContext, channelName, rtmManager, ktvApi),
                        AUiMusicPlayerServiceImpl(roomContext, rtcEngine, channelName, ktvApi),
                        AUiChorusServiceImpl(roomContext, channelName, rtmManager, ktvApi),
                        ktvApi
                    )

                    // launch room activity
                    KaraokeRoomActivity.launch(
                        roomContext.roomConfig.context,
                        config.themeId,
                        roomService,
                        onRoomCreated = {
                            eventHandler?.onRoomLaunchSuccess?.invoke()
                        },
                        onRoomDestroy = { isPermsLeak ->
                            ktvApi.release()
                            mRoomManager?.rtmManager?.logout()
                            if (isPermsLeak) {
                                eventHandler?.onRoomLaunchFailure?.invoke(
                                    AUiException(
                                        ErrorCode.PERMISSIONS_LEAK.value,
                                        ""
                                    )
                                )
                            }
                        }
                    )
                } else {
                    eventHandler?.onRoomLaunchFailure?.invoke(
                        AUiException(
                            ErrorCode.RTM_LOGIN_FAILURE.value,
                            "$error"
                        )
                    )
                }
            }
        }
    }

    private fun generateTokenWithConfig(config: AUiRoomConfig, onSuccess: () -> Unit) {
        var response = 3
        val trySuccess = {
            response -= 1;
            if (response == 0) {
                onSuccess.invoke()
            }
        }

        val userId = mRoomContext?.currentUserInfo?.userId ?: ""
        HttpManager
            .getService(ApplicationInterface::class.java)
            .tokenGenerate(TokenGenerateReq(config.channelName, userId))
            .enqueue(object : retrofit2.Callback<CommonResp<TokenGenerateResp>> {
                override fun onResponse(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, response: Response<CommonResp<TokenGenerateResp>>) {
                    val rspObj = response.body()?.data
                    if (rspObj != null) {
                        //rtcToken007
                        config.tokenMap[AUiRoomConfig.TOKEN_RTC_007] = rspObj.rtcToken
                        //rtmToken007
                        config.tokenMap[AUiRoomConfig.TOKEN_RTM_LOGIN] = rspObj.rtmToken
                        mRoomContext?.roomConfig?.appId = rspObj.appId
                    }
                    trySuccess.invoke()
                }
                override fun onFailure(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, t: Throwable) {
                    trySuccess.invoke()
                }
            })
        HttpManager
            .getService(ApplicationInterface::class.java)
            .tokenGenerate006(TokenGenerateReq(config.ktvChannelName, userId))
            .enqueue(object : retrofit2.Callback<CommonResp<TokenGenerateResp>> {
                override fun onResponse(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, response: Response<CommonResp<TokenGenerateResp>>) {
                    val rspObj = response.body()?.data
                    if (rspObj != null) {
                        //rtcRtcToken006
                        config.tokenMap[AUiRoomConfig.TOKEN_RTC_SERVICE] = rspObj.rtcToken
                        //rtcRtmToken006
                        config.tokenMap[AUiRoomConfig.TOKEN_RTM_KTV] = rspObj.rtmToken
                    }
                    trySuccess.invoke()
                }
                override fun onFailure(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, t: Throwable) {
                    trySuccess.invoke()
                }
            })
        HttpManager
            .getService(ApplicationInterface::class.java)
            .tokenGenerate(TokenGenerateReq(config.ktvChorusChannelName, userId))
            .enqueue(object : retrofit2.Callback<CommonResp<TokenGenerateResp>> {
                override fun onResponse(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, response: Response<CommonResp<TokenGenerateResp>>) {
                    val rspObj = response.body()?.data
                    if (rspObj != null) {
                        // rtcChorusRtcToken007
                        config.tokenMap[AUiRoomConfig.TOKEN_RTC_KTV_CHORUS] = rspObj.rtcToken
                    }
                    trySuccess.invoke()
                }
                override fun onFailure(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, t: Throwable) {
                    trySuccess.invoke()
                }
            })
    }

    enum class ErrorCode(val value: Int) {
        RTM_LOGIN_FAILURE(100),
        PERMISSIONS_LEAK(101)
    }

    data class RoomEventHandler(
        val onRoomLaunchSuccess: (() -> Unit)? = null,
        val onRoomLaunchFailure: ((AUiException) -> Unit)? = null,
    )
}