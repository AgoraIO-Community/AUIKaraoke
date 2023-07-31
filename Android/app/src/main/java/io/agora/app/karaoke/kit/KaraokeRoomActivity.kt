package io.agora.app.karaoke.kit

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.agora.app.karaoke.RoomListActivity
import io.agora.app.karaoke.databinding.KaraokeRoomActivityBinding
import io.agora.auikit.model.AUIRoomConfig
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.service.IAUIRoomManager.AUIRoomManagerRespDelegate
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.application.ApplicationInterface
import io.agora.auikit.service.http.application.TokenGenerateReq
import io.agora.auikit.service.http.application.TokenGenerateResp
import io.agora.auikit.service.rtm.AUIRtmErrorProxyDelegate
import io.agora.auikit.ui.basic.AUIAlertDialog
import io.agora.scene.show.utils.PermissionHelp
import retrofit2.Response

class KaraokeRoomActivity : AppCompatActivity(), AUIRoomManagerRespDelegate, AUIRtmErrorProxyDelegate {
    companion object {
        private var roomInfo: AUIRoomInfo? = null
        private var themeId: Int = View.NO_ID
        private var isCreateRoom = false

        fun launch(context: Context, isCreateRoom: Boolean, roomInfo: AUIRoomInfo, themeId: Int = View.NO_ID) {
            Companion.roomInfo = roomInfo
            Companion.isCreateRoom = isCreateRoom
            Companion.themeId = themeId

            val intent = Intent(context, KaraokeRoomActivity::class.java)
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private val mViewBinding by lazy { KaraokeRoomActivityBinding.inflate(LayoutInflater.from(this)) }
    private val mPermissionHelp = PermissionHelp(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (themeId != View.NO_ID) {
            setTheme(themeId)
        }
        setContentView(mViewBinding.root)
        val roomInfo = roomInfo ?: return
        mViewBinding.karaokeRoomView.setFragmentActivity(this)
        mViewBinding.karaokeRoomView.setOnShutDownClick {
            onUserLeaveRoom()
        }
        mPermissionHelp.checkMicPerm(
            {
                generateToken { config ->
                    KaraokeUiKit.launchRoom(roomInfo, config, mViewBinding.karaokeRoomView, KaraokeUiKit.RoomEventHandler {

                    })
                    KaraokeUiKit.subscribeError(roomInfo.roomId, this)
                    KaraokeUiKit.bindRespDelegate(this)
                }
            },
            {
                finish()
            },
            true
        )
    }

    private fun generateToken(onSuccess: (AUIRoomConfig) -> Unit) {
        val config = AUIRoomConfig( roomInfo?.roomId ?: "")
        config.themeId = RoomListActivity.ThemeId
        var response = 3
        val trySuccess = {
            response -= 1;
            if (response == 0) {
                onSuccess.invoke(config)
            }
        }

        val userId = AUIRoomContext.shared().currentUserInfo.userId
        HttpManager
            .getService(ApplicationInterface::class.java)
            .tokenGenerate(TokenGenerateReq(config.channelName, userId))
            .enqueue(object : retrofit2.Callback<CommonResp<TokenGenerateResp>> {
                override fun onResponse(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, response: Response<CommonResp<TokenGenerateResp>>) {
                    val rspObj = response.body()?.data
                    if (rspObj != null) {
                        config.rtcToken007 = rspObj.rtcToken
                        config.rtmToken007 = rspObj.rtmToken
                        AUIRoomContext.shared()?.commonConfig?.appId = rspObj.appId
                    }
                    trySuccess.invoke()
                }
                override fun onFailure(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, t: Throwable) {
                    trySuccess.invoke()
                }
            })
        HttpManager
            .getService(ApplicationInterface::class.java)
            .tokenGenerate(TokenGenerateReq(config.rtcChannelName, userId))
            .enqueue(object : retrofit2.Callback<CommonResp<TokenGenerateResp>> {
                override fun onResponse(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, response: Response<CommonResp<TokenGenerateResp>>) {
                    val rspObj = response.body()?.data
                    if (rspObj != null) {
                        config.rtcRtcToken = rspObj.rtcToken
                        config.rtcRtmToken = rspObj.rtmToken
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

    private var mRoomDestroyAlert = false
    override fun onRoomDestroy(roomId: String) {
        if (mRoomDestroyAlert) {
            return
        }
        mRoomDestroyAlert = true
        AUIAlertDialog(this).apply {
            setTitle("Tip")
            setMessage("房间已销毁")
            setPositiveButton("确认") {
                dismiss()
                shutDownRoom()
            }
            show()
        }
    }

    override fun onRoomInfoChange(roomId: String, roomInfo: AUIRoomInfo) {

    }

    override fun onTokenPrivilegeWillExpire(channelName: String?) {
        TODO("Not yet implemented")
    }
    override fun onBackPressed() {
        onUserLeaveRoom()
    }

    private fun onUserLeaveRoom() {
        val owner = (roomInfo?.roomOwner?.userId == AUIRoomContext.shared().currentUserInfo.userId)
        AUIAlertDialog(this).apply {
            setTitle("Tip")
            if (owner) {
                setMessage("是否离开并销毁房间？")
            } else {
                setMessage("是否离开房间？")
            }
            setPositiveButton("确认") {
                dismiss()
                shutDownRoom()
            }
            setNegativeButton("取消") {
                dismiss()
            }
            show()
        }
    }

    private fun shutDownRoom() {
        roomInfo?.roomId?.let { roomId ->
            KaraokeUiKit.destroyRoom(roomId)
            KaraokeUiKit.unsubscribeError(roomId, this@KaraokeRoomActivity)
            KaraokeUiKit.unbindRespDelegate(this@KaraokeRoomActivity)
        }
        finish()
    }
}