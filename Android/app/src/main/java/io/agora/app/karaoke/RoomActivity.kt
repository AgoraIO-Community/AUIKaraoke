package io.agora.app.karaoke

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.agora.app.karaoke.databinding.RoomActivityBinding
import io.agora.asceneskit.karaoke.KaraokeUiKit
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.service.IAUIRoomManager.AUIRoomManagerRespObserver
import io.agora.auikit.ui.basic.AUIAlertDialog
import io.agora.auikit.utils.PermissionHelp

class RoomActivity : AppCompatActivity(),
    AUIRoomManagerRespObserver {
    companion object {
        private var roomInfo: AUIRoomInfo? = null
        private var themeId: Int = View.NO_ID
        private var isCreateRoom = false

        fun launch(context: Context, isCreateRoom: Boolean, roomInfo: AUIRoomInfo, themeId: Int = View.NO_ID) {
            Companion.roomInfo = roomInfo
            Companion.isCreateRoom = isCreateRoom
            Companion.themeId = themeId

            val intent = Intent(context, RoomActivity::class.java)
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private val mViewBinding by lazy { RoomActivityBinding.inflate(LayoutInflater.from(this)) }
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
                KaraokeUiKit.launchRoom(
                    roomInfo,
                    mViewBinding.karaokeRoomView,
                    failure = { finish() })
                KaraokeUiKit.registerRoomRespObserver(this)
            },
            {
                finish()
            },
            true
        )
    }

    private var mRoomDestroyAlert = false
    override fun onRoomDestroy(roomId: String) {
        if (mRoomDestroyAlert) {
            return
        }
        mRoomDestroyAlert = true
//        AUIAlertDialog(this).apply {
//            setTitle("Tip")
//            setMessage("房间已销毁")
//            setPositiveButton("确认") {
//                dismiss()
//                shutDownRoom()
//            }
//            show()
//        }
    }


    override fun onBackPressed() {
        onUserLeaveRoom()
    }

    private fun onUserLeaveRoom() {
        val owner = (roomInfo?.owner?.userId == AUIRoomContext.shared().currentUserInfo.userId)
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
            KaraokeUiKit.unRegisterRoomRespObserver(this@RoomActivity)
        }
        finish()
    }
}