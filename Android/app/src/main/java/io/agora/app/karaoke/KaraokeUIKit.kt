package io.agora.app.karaoke

import io.agora.asceneskit.karaoke.AUIAPIConfig
import io.agora.asceneskit.karaoke.AUIKaraokeRoomService
import io.agora.asceneskit.karaoke.AUIKaraokeRoomServiceRespObserver
import io.agora.asceneskit.karaoke.KaraokeRoomView
import io.agora.auikit.model.AUICommonConfig
import io.agora.auikit.model.AUIRoomConfig
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.token.TokenGenerateReq
import io.agora.auikit.service.http.token.TokenGenerateResp
import io.agora.auikit.service.http.token.TokenInterface
import io.agora.auikit.service.room.AUIRoomManager
import io.agora.auikit.utils.AUILogger
import retrofit2.Response

object KaraokeUIKit {
    private var mAPIConfig: AUIAPIConfig? = null
    private val mRoomManager by lazy {
        AUIRoomManager(
            AUIRoomContext.shared().mCommonConfig?.appId ?: "",
            mScenesId
        )
    }
    private val mScenesId = "Karaoke"

    private val mServices = mutableMapOf<String, AUIKaraokeRoomService>()

    /**
     * 初始化。
     * 对于rtmClient、rtcEngineEx、ktvApi：
     *      当外部没传时内部会自行创建，并在release方法调用时销毁；
     *      当外部传入时在release时不会销毁
     */
    fun setup(
        commonConfig: AUICommonConfig,
        apiConfig: AUIAPIConfig
    ) {
        mAPIConfig = apiConfig
        AUIRoomContext.shared().setCommonConfig(commonConfig)

        HttpManager.setBaseURL(commonConfig.host)
        AUILogger.initLogger(
            AUILogger.Config(
                AUIRoomContext.shared().requireCommonConfig().context,
                "Karaoke"
            )
        )
    }

    /**
     * 释放资源
     */
    fun release() {
        mAPIConfig = null
    }

    /**
     * 获取房间列表
     */
    fun getRoomList(
        startTime: Long?,
        pageSize: Int,
        success: (List<AUIRoomInfo>) -> Unit,
        failure: (AUIException) -> Unit
    ) {
        checkSetupAndCommonConfig()
        mRoomManager.getRoomInfoList(
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
        roomInfo: AUIRoomInfo,
        roomConfig: AUIRoomConfig,
        karaokeView: KaraokeRoomView,
        completion: (AUIException?, AUIRoomInfo?) -> Unit
    ) {
        checkSetupAndCommonConfig()
        if (mServices[roomInfo.roomId] != null) {
            completion.invoke(AUIException(AUIException.ERROR_CODE_ROOM_EXITED, ""), null)
            return
        }
        mRoomManager.createRoom(
            roomInfo
        ) { error, _ ->
            AUILogger.logger().d(
                tag = "KaraokeUiKit",
                message = "Create room >> error=$error, roomInfo=$roomInfo"
            )
        }
        val roomService = AUIKaraokeRoomService(
            mAPIConfig!!,
            roomConfig
        )
        mServices[roomInfo.roomId] = roomService
        // 加入房间
        roomService.create(roomInfo) {error ->
            if(error == null){
                // success
                AUILogger.logger().d(tag = "KaraokeUiKit", message = "Enter room successfully")
                completion.invoke(null, roomInfo)
            } else {
                AUILogger.logger().d(tag = "KaraokeUiKit", message = "Enter room failed : ${error.code}")
                completion.invoke(error, null)
            }
        }
        karaokeView.bindService(roomService)
    }

    /**
     * 启动房间
     *
     * @param roomInfo
     * @param karaokeView
     * @param success
     * @param failure
     */
    fun enterRoom(
        roomInfo: AUIRoomInfo,
        roomConfig: AUIRoomConfig,
        karaokeView: KaraokeRoomView,
        completion: (AUIException?, AUIRoomInfo?) -> Unit
    ) {
        checkSetupAndCommonConfig()
        if (mServices[roomInfo.roomId] != null) {
            completion.invoke(AUIException(AUIException.ERROR_CODE_ROOM_EXITED, ""), null)
            return
        }
        val roomService = AUIKaraokeRoomService(
            mAPIConfig!!,
            roomConfig
        )
        mServices[roomInfo.roomId] = roomService
        // 加入房间
        roomService.enter { error ->
            if (error == null) {
                // success
                karaokeView.bindService(roomService)
                AUILogger.logger().d(tag = "KaraokeUiKit", message = "Enter room successfully")
                completion.invoke(null, roomInfo)
            } else {
                AUILogger.logger().d(tag = "KaraokeUiKit", message = "Enter room failed : ${error.code}")
                completion.invoke(error, null)
            }
        }

    }

    /**
     * 离开房间
     *
     * @param roomId
     */
    fun leaveRoom(roomId: String) {
        if (AUIRoomContext.shared().isRoomOwner(roomId)) {
            mRoomManager.destroyRoom(
                roomId
            ) {}
            mServices[roomId]?.destroy()
        } else if (mServices[roomId]?.exit() == true) {
            mRoomManager.destroyRoom(
                roomId
            ) {}
        }
        mServices.remove(roomId)
    }

    /**
     * 注册房间响应观察者
     *
     * @param observer
     */
    fun registerRoomRespObserver(roomId: String, observer: AUIKaraokeRoomServiceRespObserver) {
        mServices[roomId]?.observableHelper?.subscribeEvent(observer)
    }

    /**
     * 取消注册房间响应观察者
     *
     * @param observer
     */
    fun unRegisterRoomRespObserver(roomId: String, observer: AUIKaraokeRoomServiceRespObserver) {
        mServices[roomId]?.observableHelper?.unSubscribeEvent(observer)
    }

    fun renewToken(roomId: String, roomConfig: AUIRoomConfig) {
        mServices[roomId]?.renew(roomConfig)
    }

    fun generateToken(
        roomId: String,
        onSuccess: (AUIRoomConfig) -> Unit,
        onFailure: (AUIException) -> Unit
    ) {
        val config = AUIRoomConfig(roomId)
        var response = 2
        var isFailure = false
        val trySuccess = {
            response -= 1
            if (response == 0 && !isFailure) {
                onSuccess.invoke(config)
            }
        }
        val failure = { ex: AUIException ->
            if (!isFailure) {
                isFailure = true
                onFailure.invoke(ex)
            }
        }

        val userId = AUIRoomContext.shared().currentUserInfo.userId
        HttpManager
            .getService(TokenInterface::class.java)
            .tokenGenerate(
                TokenGenerateReq(
                    AUIRoomContext.shared().requireCommonConfig().appId,
                    AUIRoomContext.shared().requireCommonConfig().appCert,
                    config.channelName,
                    userId
                )
            )
            .enqueue(object : retrofit2.Callback<CommonResp<TokenGenerateResp>> {
                override fun onResponse(
                    call: retrofit2.Call<CommonResp<TokenGenerateResp>>,
                    response: Response<CommonResp<TokenGenerateResp>>
                ) {
                    val rspObj = response.body()?.data
                    if (rspObj != null) {
                        config.rtcToken = rspObj.rtcToken
                        config.rtmToken = rspObj.rtmToken
                        AUIRoomContext.shared().requireCommonConfig().appId = rspObj.appId
                    }
                    trySuccess.invoke()
                }

                override fun onFailure(
                    call: retrofit2.Call<CommonResp<TokenGenerateResp>>,
                    t: Throwable
                ) {
                    failure.invoke(AUIException(-1, t.message))
                }
            })
        HttpManager
            .getService(TokenInterface::class.java)
            .tokenGenerate(
                TokenGenerateReq(
                    AUIRoomContext.shared().requireCommonConfig().appId,
                    AUIRoomContext.shared().requireCommonConfig().appCert,
                    config.rtcChorusChannelName,
                    userId
                )
            )
            .enqueue(object : retrofit2.Callback<CommonResp<TokenGenerateResp>> {
                override fun onResponse(
                    call: retrofit2.Call<CommonResp<TokenGenerateResp>>,
                    response: Response<CommonResp<TokenGenerateResp>>
                ) {
                    val rspObj = response.body()?.data
                    if (rspObj != null) {
                        // rtcChorusRtcToken
                        config.rtcChorusRtcToken = rspObj.rtcToken
                    }
                    trySuccess.invoke()
                }

                override fun onFailure(
                    call: retrofit2.Call<CommonResp<TokenGenerateResp>>,
                    t: Throwable
                ) {
                    failure.invoke(AUIException(-3, t.message))
                }
            })
    }

    private fun checkSetupAndCommonConfig() {
        if (AUIRoomContext.shared().mCommonConfig == null) {
            throw RuntimeException("make sure invoke setup first")
        }
    }

}