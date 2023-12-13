package io.agora.asceneskit.karaoke

import android.util.Log
import io.agora.auikit.model.AUICommonConfig
import io.agora.auikit.model.AUICreateRoomInfo
import io.agora.auikit.model.AUIRoomConfig
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.service.IAUIRoomManager.AUIRoomManagerRespObserver
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.application.ApplicationInterface
import io.agora.auikit.service.http.application.TokenGenerateReq
import io.agora.auikit.service.http.application.TokenGenerateResp
import io.agora.auikit.service.imp.AUIRoomManagerImplRespResp
import io.agora.auikit.service.ktv.KTVApi
import io.agora.auikit.service.rtm.AUIRtmErrorRespObserver
import io.agora.auikit.utils.AUILogger
import io.agora.rtc2.RtcEngineEx
import io.agora.rtm.RtmClient
import retrofit2.Response

object KaraokeUiKit {
    private val notInitException =
        RuntimeException("The KaraokeServiceManager has not been initialized!")
    private val initedException =
        RuntimeException("The KaraokeServiceManager has been initialized!")

    private var mRoomManager: AUIRoomManagerImplRespResp? = null
    private var mRtcEngineEx: RtcEngineEx? = null
    private var mKTVApi: KTVApi? = null
    private var mService: AUIKaraokeRoomService? = null
    private val mErrorObservers = mutableListOf<AUIRtmErrorRespObserverImp>()

    /**
     * 初始化。
     * 对于rtmClient、rtcEngineEx、ktvApi：
     *      当外部没传时内部会自行创建，并在release方法调用时销毁；
     *      当外部传入时在release时不会销毁
     */
    fun setup(
        config: AUICommonConfig,
        ktvApi: KTVApi? = null,
        rtcEngineEx: RtcEngineEx? = null,
        rtmClient: RtmClient? = null
    ) {
        if (mRoomManager != null) {
            throw initedException
        }
        HttpManager.setBaseURL(config.host)
        AUIRoomContext.shared().commonConfig = config
        mKTVApi = ktvApi
        mRtcEngineEx = rtcEngineEx // 用户塞进来的engine由用户自己管理生命周期
        mRoomManager = AUIRoomManagerImplRespResp(config, rtmClient)
        AUILogger.initLogger(
            AUILogger.Config(
                AUIRoomContext.shared().commonConfig.context,
                "Karaoke"
            )
        )
    }

    /**
     * 释放资源
     */
    fun release() {
        mRtcEngineEx = null
        mRoomManager = null
        mKTVApi = null
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
        createRoomInfo: AUICreateRoomInfo,
        success: (AUIRoomInfo) -> Unit,
        failure: (AUIException) -> Unit
    ) {
        val roomManager = mRoomManager ?: throw notInitException
        roomManager.createRoom(
            createRoomInfo
        ) { error, roomInfo ->
            if (error == null && roomInfo != null) {
                success.invoke(roomInfo)
            } else {
                failure.invoke(error ?: AUIException(-999, "RoomInfo return null"))
            }
        }
    }

    /**
     * 启动房间
     *
     * @param roomInfo
     * @param karaokeView
     * @param success
     * @param failure
     */
    fun launchRoom(
        roomInfo: AUIRoomInfo,
        karaokeView: KaraokeRoomView,
        success: (() -> Unit)? = null,
        failure: ((AUIException) -> Unit)? = null
    ) {
        val roomManager = mRoomManager ?: throw notInitException
        generateToken(roomInfo.roomId,
            { config ->
                AUIRoomContext.shared().roomConfigMap[roomInfo.roomId] = config
                val roomService = AUIKaraokeRoomService(
                    mRtcEngineEx,
                    mKTVApi,
                    roomManager,
                    config,
                    roomInfo
                )
                mService = roomService
                karaokeView.bindService(roomService)

                val observer = AUIRtmErrorRespObserverImp(roomInfo.roomId)
                mErrorObservers.add(observer)
                mRoomManager?.rtmManager?.proxy?.registerErrorRespObserver(observer)
                success?.invoke()
            },
            { ex ->
                failure?.invoke(ex)
            })
    }

    /**
     * 销毁房间
     *
     * @param roomId
     */
    fun destroyRoom(roomId: String) {
        if (AUIRoomContext.shared().isRoomOwner(roomId)) {
            mService?.getRoomManager()?.destroyRoom(roomId) {}
        } else {
            mService?.getRoomManager()?.exitRoom(roomId) {}
        }
        mErrorObservers.filter { it.roomId == roomId }.forEach {
            mRoomManager?.rtmManager?.proxy?.unRegisterErrorRespObserver(it)
        }
        mService?.destroy()
        AUIRoomContext.shared().cleanRoom(roomId)
        mService = null
    }

    /**
     * 注册房间响应观察者
     *
     * @param observer
     */
    fun registerRoomRespObserver(observer: AUIRoomManagerRespObserver) {
        val roomManager = mRoomManager ?: throw notInitException
        roomManager.registerRespObserver(observer)
    }

    /**
     * 取消注册房间响应观察者
     *
     * @param observer
     */
    fun unRegisterRoomRespObserver(observer: AUIRoomManagerRespObserver) {
        val roomManager = mRoomManager ?: throw notInitException
        roomManager.unRegisterRespObserver(observer)
    }

    private fun generateToken(
        roomId: String,
        onSuccess: (AUIRoomConfig) -> Unit,
        onFailure: (AUIException) -> Unit
    ) {
        val config = AUIRoomConfig(roomId)
        var response = 3
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
            .getService(ApplicationInterface::class.java)
            .tokenGenerate(TokenGenerateReq(AUIRoomContext.shared().commonConfig.appId, AUIRoomContext.shared().commonConfig.appCert, config.channelName, userId))
            .enqueue(object : retrofit2.Callback<CommonResp<TokenGenerateResp>> {
                override fun onResponse(
                    call: retrofit2.Call<CommonResp<TokenGenerateResp>>,
                    response: Response<CommonResp<TokenGenerateResp>>
                ) {
                    val rspObj = response.body()?.data
                    if (rspObj != null) {
                        config.rtcToken = rspObj.rtcToken
                        config.rtmToken = rspObj.rtmToken
                        AUIRoomContext.shared().commonConfig.appId = rspObj.appId
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
            .getService(ApplicationInterface::class.java)
            .tokenGenerate(TokenGenerateReq(AUIRoomContext.shared().commonConfig.appId, AUIRoomContext.shared().commonConfig.appCert, config.rtcChannelName, userId))
            .enqueue(object : retrofit2.Callback<CommonResp<TokenGenerateResp>> {
                override fun onResponse(
                    call: retrofit2.Call<CommonResp<TokenGenerateResp>>,
                    response: Response<CommonResp<TokenGenerateResp>>
                ) {
                    val rspObj = response.body()?.data
                    if (rspObj != null) {
                        config.rtcRtcToken = rspObj.rtcToken
                        config.rtcRtmToken = rspObj.rtmToken
                    }
                    trySuccess.invoke()
                }

                override fun onFailure(
                    call: retrofit2.Call<CommonResp<TokenGenerateResp>>,
                    t: Throwable
                ) {
                    failure.invoke(AUIException(-2, t.message))
                }
            })
        HttpManager
            .getService(ApplicationInterface::class.java)
            .tokenGenerate(TokenGenerateReq(AUIRoomContext.shared().commonConfig.appId, AUIRoomContext.shared().commonConfig.appCert, config.rtcChorusChannelName, userId))
            .enqueue(object : retrofit2.Callback<CommonResp<TokenGenerateResp>> {
                override fun onResponse(
                    call: retrofit2.Call<CommonResp<TokenGenerateResp>>,
                    response: Response<CommonResp<TokenGenerateResp>>
                ) {
                    val rspObj = response.body()?.data
                    if (rspObj != null) {
                        // rtcChorusRtcToken007
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

    private class AUIRtmErrorRespObserverImp(val roomId: String) : AUIRtmErrorRespObserver {
        override fun onTokenPrivilegeWillExpire(channelName: String?) {
            if (roomId != channelName) {
                return
            }
            generateToken(channelName,
                { mService?.renew(it) },
                {
                    Log.e("KaraokeUiKit", "onTokenPrivilegeWillExpire >> renew token failed -- $it")
                })
        }
    }

}