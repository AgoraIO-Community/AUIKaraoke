package io.agora.asceneskit.karaoke

import io.agora.asceneskit.BuildConfig
import io.agora.auikit.model.AUICommonConfig
import io.agora.auikit.model.AUICreateRoomInfo
import io.agora.auikit.model.AUIRoomConfig
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.service.IAUIRoomManager.AUIRoomManagerRespDelegate
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.imp.AUIRoomManagerImpl
import io.agora.auikit.service.ktv.KTVApi
import io.agora.auikit.service.rtm.AUIRtmErrorProxyDelegate
import io.agora.auikit.utils.AUILogger
import io.agora.rtc2.RtcEngineEx
import io.agora.rtm2.RtmClient

object KaraokeUiKit {
    private val notInitException =
        RuntimeException("The KaraokeServiceManager has not been initialized!")
    private val initedException =
        RuntimeException("The KaraokeServiceManager has been initialized!")

    private var mRoomManager: AUIRoomManagerImpl? = null

    private var mRtcEngineEx: RtcEngineEx? = null
    private var mKTVApi: KTVApi? = null

    private var mService: AUIKaraokeRoomService? = null

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
        HttpManager.setBaseURL(BuildConfig.SERVER_HOST)
        AUIRoomContext.shared().commonConfig = config
        mKTVApi = ktvApi
        mRtcEngineEx = rtcEngineEx // 用户塞进来的engine由用户自己管理生命周期
        mRoomManager = AUIRoomManagerImpl(config, rtmClient)
        AUILogger.initLogger(AUILogger.Config(AUIRoomContext.shared().commonConfig.context, "Karaoke"))
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

    fun launchRoom(
        roomInfo: AUIRoomInfo,
        config: AUIRoomConfig,
        karaokeView: KaraokeRoomView,
    ) {
        AUIRoomContext.shared().roomConfig = config
        val roomManager = mRoomManager ?: throw notInitException
        val roomService = AUIKaraokeRoomService(
            mRtcEngineEx,
            mKTVApi,
            roomManager,
            config,
            roomInfo
        )
        mService = roomService
        karaokeView.bindService(roomService)
    }

    fun destroyRoom(roomId: String) {
        if (AUIRoomContext.shared().isRoomOwner(roomId)) {
            mService?.getRoomManager()?.destroyRoom(roomId){}
        }else{
            mService?.getRoomManager()?.exitRoom(roomId){}
        }
        mService?.destroy()
        AUIRoomContext.shared().cleanRoom(roomId)
        mService = null
    }

    fun subscribeError(roomId: String, delegate: AUIRtmErrorProxyDelegate) {
        mRoomManager?.rtmManager?.proxy?.subscribeError(roomId, delegate)
    }

    fun unsubscribeError(roomId: String, delegate: AUIRtmErrorProxyDelegate) {
        mRoomManager?.rtmManager?.proxy?.unsubscribeError(roomId, delegate)
    }

    fun bindRespDelegate(delegate: AUIRoomManagerRespDelegate) {
        mRoomManager?.bindRespDelegate(delegate)
    }

    fun unbindRespDelegate(delegate: AUIRoomManagerRespDelegate) {
        mRoomManager?.unbindRespDelegate(delegate)
    }

}