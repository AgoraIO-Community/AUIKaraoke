package io.agora.app.karaoke

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.agora.app.karaoke.databinding.RoomActivityBinding
import io.agora.asceneskit.karaoke.AUIKaraokeRoomServiceRespObserver
import io.agora.asceneskit.karaoke.KaraokeUiKit
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.ui.basic.AUIAlertDialog
import io.agora.auikit.utils.AUILogger
import io.agora.auikit.utils.PermissionHelp

class RoomActivity : AppCompatActivity(),
    AUIKaraokeRoomServiceRespObserver {
    companion object {
        private val EXTRA_IS_CREATE_ROOM = "isCreateRoom"
        private val EXTRA_ROOM_INFO = "roomInfo"
        private val EXTRA_THEME_ID = "themeId"

        fun launch(
            context: Context,
            isCreateRoom: Boolean,
            roomInfo: AUIRoomInfo,
            themeId: Int = View.NO_ID
        ) {
            val intent = Intent(context, RoomActivity::class.java)
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(EXTRA_IS_CREATE_ROOM, isCreateRoom)
            intent.putExtra(EXTRA_ROOM_INFO, roomInfo)
            intent.putExtra(EXTRA_THEME_ID, themeId)
            context.startActivity(intent)
        }
    }

    private val mViewBinding by lazy { RoomActivityBinding.inflate(LayoutInflater.from(this)) }
    private val mPermissionHelp = PermissionHelp(this)
    private val themeId by lazy { intent.getIntExtra(EXTRA_THEME_ID, View.NO_ID) }
    private val isCreateRoom by lazy { intent.getBooleanExtra(EXTRA_IS_CREATE_ROOM, false) }
    private val roomInfo by lazy {
        intent.getSerializableExtra(EXTRA_ROOM_INFO) as AUIRoomInfo
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (themeId != View.NO_ID) {
            setTheme(themeId)
        }
        setContentView(mViewBinding.root)
        mViewBinding.karaokeRoomView.setFragmentActivity(this)
        mViewBinding.karaokeRoomView.setOnShutDownClick {
            onUserLeaveRoom()
        }
        mPermissionHelp.checkMicPerm(
            {
                KaraokeUiKit.generateToken(
                    roomInfo.roomId,
                    onSuccess = { roomConfig ->
                        if (isCreateRoom) {
                            KaraokeUiKit.createRoom(
                                roomInfo,
                                roomConfig,
                                mViewBinding.karaokeRoomView,
                                completion = { error, _ ->
                                    if (error != null) {
                                        shutDownRoom()
                                    }
                                })
                        } else {
                            KaraokeUiKit.enterRoom(
                                roomInfo,
                                roomConfig,
                                mViewBinding.karaokeRoomView,
                                completion = { error, _ ->
                                    if (error != null) {
                                        shutDownRoom()
                                    }
                                })
                        }
                        KaraokeUiKit.registerRoomRespObserver(roomInfo.roomId, this)
                    },
                    onFailure = {
                        shutDownRoom()
                    }
                )

            },
            {
                shutDownRoom()
            },
            true
        )
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
        roomInfo.roomId.let { roomId ->
            KaraokeUiKit.leaveRoom(roomId)
            KaraokeUiKit.unRegisterRoomRespObserver(roomId, this@RoomActivity)
        }
        finish()
    }

    override fun onTokenPrivilegeWillExpire(roomId: String) {
        super.onTokenPrivilegeWillExpire(roomId)
        KaraokeUiKit.generateToken(
            roomInfo.roomId,
            onSuccess = { roomConfig ->
                KaraokeUiKit.renewToken(roomInfo.roomId, roomConfig)
            },
            onFailure = { error ->
                AUILogger.logger()
                    .e("RoomActivity", "onTokenPrivilegeWillExpire generateToken >> error=$error")
            }
        )
    }

    override fun onRoomDestroy(roomId: String) {
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

    override fun onRoomUserBeKicked(roomId: String, userId: String) {
        super.onRoomUserBeKicked(roomId, userId)
        if (roomId == roomInfo.roomId && userId == AUIRoomContext.shared().currentUserInfo.userId) {
            AUIAlertDialog(this).apply {
                setTitle("Tip")
                setMessage("被房主踢出房间")
                setPositiveButton("确认") {
                    dismiss()
                    shutDownRoom()
                }
                show()
            }
        }
    }
}