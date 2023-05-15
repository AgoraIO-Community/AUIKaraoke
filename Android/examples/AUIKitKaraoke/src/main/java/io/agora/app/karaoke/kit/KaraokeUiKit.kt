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
        AUiRoomContext.shared().commonConfig = config

        mRoomManager = AUiRoomManagerImpl(config, rtmClient)

        if (rtcEngineEx != null) { // 用户塞进来的engine由用户自己管理生命周期
            mRtcEngineEx = rtcEngineEx
            shouldReleaseRtc = false
        }
        if (ktvApi != null) {
            mKTVApi = ktvApi
        }
        AUiLogger.initLogger(AUiLogger.Config(AUiRoomContext.shared().commonConfig.context, "Karaoke"))
    }

    /**
     * 释放资源
     */
    fun release() {
        if (shouldReleaseRtc) {
            RtcEngine.destroy()
        }
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
            mRtcEngineEx = mRtcEngineEx ?: AgoraEngineCreator.createRtcEngine(
                AUiRoomContext.shared().commonConfig.context,
                AUiRoomContext.shared().commonConfig.appId
            )
            mKTVApi = mKTVApi ?: AgoraEngineCreator.createKTVApi()

            val roomManager = mRoomManager ?: throw notInitException
            val rtcEngine = mRtcEngineEx ?: throw notInitException
            val ktvApi = mKTVApi ?: throw notInitException
            val channelName = roomInfo.roomId
            val rtmManager = roomManager.rtmManager

            // login rtm
            rtmManager.login(
                config.rtmToken007
            ) { error ->
                if (error == null) {
                    // init ktv api
                    ktvApi.initialize(
                        KTVApiConfig(
                            AUiRoomContext.shared().commonConfig.appId,
                            config.rtcRtmToken006,
                            rtcEngine,
                            config.rtcChannelName,
                            AUiRoomContext.shared().commonConfig.userId.toInt(),
                            config.rtcChorusChannelName,
                            config.rtcChorusRtcToken007
                        )
                    )
                    ktvApi.renewInnerDataStreamId()

                    // create room service
                    val roomService = KaraokeRoomService(
                        AUiRoomContext.shared(),
                        rtcEngine,
                        config,
                        roomInfo,
                        roomManager,
                        AUiUserServiceImpl(AUiRoomContext.shared(), channelName, rtmManager),
                        AUiMicSeatServiceImpl(AUiRoomContext.shared(), channelName, rtmManager),
                        AUiJukeboxServiceImpl(AUiRoomContext.shared(), channelName, rtmManager, ktvApi),
                        AUiMusicPlayerServiceImpl(AUiRoomContext.shared(), rtcEngine, channelName, ktvApi),
                        AUiChorusServiceImpl(AUiRoomContext.shared(), channelName, rtmManager, ktvApi),
                        ktvApi
                    )

                    // launch room activity
                    KaraokeRoomActivity.launch(
                        AUiRoomContext.shared().commonConfig.context,
                        config.themeId,
                        roomService,
                        onRoomCreated = {
                            eventHandler?.onRoomLaunchSuccess?.invoke()
                        },
                        onRoomDestroy = { e ->
                            ktvApi.release()
                            mRoomManager?.rtmManager?.logout()
                            if (e != null) {
                                eventHandler?.onRoomLaunchFailure?.invoke(e)
                            }
                        }
                    )
                } else {
                    eventHandler?.onRoomLaunchFailure?.invoke(
                        ErrorCode.RTM_LOGIN_FAILURE
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

        val userId = AUiRoomContext.shared().currentUserInfo.userId
        HttpManager
            .getService(ApplicationInterface::class.java)
            .tokenGenerate(TokenGenerateReq(config.channelName, userId))
            .enqueue(object : retrofit2.Callback<CommonResp<TokenGenerateResp>> {
                override fun onResponse(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, response: Response<CommonResp<TokenGenerateResp>>) {
                    val rspObj = response.body()?.data
                    if (rspObj != null) {
                        config.rtcToken007 = rspObj.rtcToken
                        config.rtmToken007 = rspObj.rtmToken
                        AUiRoomContext.shared()?.commonConfig?.appId = rspObj.appId
                    }
                    trySuccess.invoke()
                }
                override fun onFailure(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, t: Throwable) {
                    trySuccess.invoke()
                }
            })
        HttpManager
            .getService(ApplicationInterface::class.java)
            .tokenGenerate006(TokenGenerateReq(config.rtcChannelName, userId))
            .enqueue(object : retrofit2.Callback<CommonResp<TokenGenerateResp>> {
                override fun onResponse(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, response: Response<CommonResp<TokenGenerateResp>>) {
                    val rspObj = response.body()?.data
                    if (rspObj != null) {
                        //rtcRtcToken006
                        config.rtcRtcToken006 = rspObj.rtcToken
                        config.rtcRtmToken006 = rspObj.rtmToken
                    }
                    trySuccess.invoke()
                }
                override fun onFailure(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, t: Throwable) {
                    trySuccess.invoke()
                }
            })
        HttpManager
            .getService(ApplicationInterface::class.java)
            .tokenGenerate(TokenGenerateReq(config.rtcChorusChannelName, userId))
            .enqueue(object : retrofit2.Callback<CommonResp<TokenGenerateResp>> {
                override fun onResponse(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, response: Response<CommonResp<TokenGenerateResp>>) {
                    val rspObj = response.body()?.data
                    if (rspObj != null) {
                        // rtcChorusRtcToken007
                        config.rtcChorusRtcToken007 = rspObj.rtcToken
                    }
                    trySuccess.invoke()
                }
                override fun onFailure(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, t: Throwable) {
                    trySuccess.invoke()
                }
            })
    }

    enum class ErrorCode(val value: Int, val message: String) {
        RTM_LOGIN_FAILURE(100, "Rtm login failed!"),
        ROOM_PERMISSIONS_LEAK(101, "The room leak required permissions!"),
        ROOM_DESTROYED(102, "The room has been destroyed!"),
    }

    data class RoomEventHandler(
        val onRoomLaunchSuccess: (() -> Unit)? = null,
        val onRoomLaunchFailure: ((ErrorCode) -> Unit)? = null,
    )
}