package io.agora.app.karaoke.kit

import io.agora.app.karaoke.BuildConfig
import io.agora.asceneskit.karaoke.AUiKaraokeRoomService
import io.agora.asceneskit.karaoke.KaraokeRoomView
import io.agora.auikit.model.AUiCommonConfig
import io.agora.auikit.model.AUiCreateRoomInfo
import io.agora.auikit.model.AUiRoomConfig
import io.agora.auikit.model.AUiRoomContext
import io.agora.auikit.model.AUiRoomInfo
import io.agora.auikit.service.IAUiRoomManager.AUiRoomManagerRespDelegate
import io.agora.auikit.service.callback.AUiException
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.imp.AUiRoomManagerImpl
import io.agora.auikit.service.ktv.KTVApi
import io.agora.auikit.service.rtm.AUiRtmErrorProxyDelegate
import io.agora.auikit.utils.AUiLogger
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineEx
import io.agora.rtm.RtmClient

object KaraokeUiKit {
    private val notInitException =
        RuntimeException("The KaraokeServiceManager has not been initialized!")
    private val initedException =
        RuntimeException("The KaraokeServiceManager has been initialized!")

    private var mRoomManager: AUiRoomManagerImpl? = null

    private var shouldReleaseRtc = true
    private var mRtcEngineEx: RtcEngineEx? = null
    private var mKTVApi: KTVApi? = null

    private var mService: AUiKaraokeRoomService? = null

    /**
     * 初始化。
     * 对于rtmClient、rtcEngineEx、ktvApi：
     *      当外部没传时内部会自行创建，并在release方法调用时销毁；
     *      当外部传入时在release时不会销毁
     */
    fun setup(
        config: AUiCommonConfig,
        ktvApi: KTVApi? = null,
        rtcEngineEx: RtcEngineEx? = null,
        rtmClient: RtmClient? = null
    ) {
        if (mRoomManager != null) {
            throw initedException
        }
        HttpManager.setBaseURL(BuildConfig.SERVER_HOST)
        AUiRoomContext.shared().commonConfig = config
        mKTVApi = ktvApi
        if (rtcEngineEx != null) { // 用户塞进来的engine由用户自己管理生命周期
            mRtcEngineEx = rtcEngineEx
            shouldReleaseRtc = false
        }
        mRoomManager = AUiRoomManagerImpl(config, rtmClient)
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

    fun launchRoom(
        roomInfo: AUiRoomInfo,
        config: AUiRoomConfig,
        karaokeView: KaraokeRoomView,
        eventHandler: RoomEventHandler? = null,
    ) {
        AUiRoomContext.shared().roomConfig = config
        val roomManager = mRoomManager ?: throw notInitException
        val roomService = AUiKaraokeRoomService(
            mRtcEngineEx,
            mKTVApi,
            roomManager,
            config,
            roomInfo
        )
        karaokeView.bindService(roomService)
        mService = roomService
        eventHandler?.onRoomLaunchSuccess
    }

    fun destroyRoom(roomId: String?) {
        mService?.destroyRoom()
        mService = null
    }

    fun subscribeError(roomId: String, delegate: AUiRtmErrorProxyDelegate) {
        mRoomManager?.rtmManager?.proxy?.subscribeError(roomId, delegate)
    }

    fun unsubscribeError(roomId: String, delegate: AUiRtmErrorProxyDelegate) {
        mRoomManager?.rtmManager?.proxy?.unsubscribeError(roomId, delegate)
    }

    fun bindRespDelegate(delegate: AUiRoomManagerRespDelegate) {
        mRoomManager?.bindRespDelegate(delegate)
    }

    fun unbindRespDelegate(delegate: AUiRoomManagerRespDelegate) {
        mRoomManager?.unbindRespDelegate(delegate)
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