package io.agora.asceneskit.karaoke

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.asceneskit.R
import io.agora.asceneskit.databinding.AkaraokeRoomViewBinding
import io.agora.auikit.binder.AUiJukeboxBinder
import io.agora.auikit.binder.AUiMicSeatsBinder
import io.agora.auikit.binder.AUiMusicPlayerBinder
import io.agora.auikit.binder.IAUiBindable
import io.agora.auikit.model.AUiMicSeatInfo
import io.agora.auikit.model.AUiUserInfo
import io.agora.auikit.model.AUiUserThumbnailInfo
import io.agora.auikit.service.IAUiMicSeatService
import io.agora.auikit.service.IAUiRoomManager
import io.agora.auikit.service.IAUiUserService
import io.agora.auikit.ui.basic.AUiAlertDialog
import io.agora.auikit.ui.basic.AUiBottomDialog
import io.agora.auikit.ui.jukebox.impl.AUiJukeboxView
import io.agora.auikit.ui.member.impl.AUiRoomMemberListView
import io.agora.auikit.ui.musicplayer.listener.IMusicPlayerActionListener

class KaraokeRoomView : FrameLayout, IAUiUserService.AUiUserRespDelegate,
    IAUiMicSeatService.AUiMicSeatRespDelegate, IAUiRoomManager.AUiRoomRespDelegate {

    private val mRoomViewBinding = AkaraokeRoomViewBinding.inflate(LayoutInflater.from(context))
    private var mRoomService: IKaraokeRoomService? = null

    private var mMemberMap = mutableMapOf<String, AUiUserInfo>()

    private var mSeatMap = mutableMapOf<Int, String>()

    private val mBinders = mutableListOf<IAUiBindable>()

    private var mLocalMute = true
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        addView(mRoomViewBinding.root)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        mRoomService?.getUserService()?.unbindRespDelegate(this)
        mRoomService?.getMicSeatsService()?.unbindRespDelegate(this)
        mRoomService?.getRoomManager()?.unbindRespDelegate(this)

        mBinders.forEach {
            it.unBind()
        }
    }

    fun bindService(service: IKaraokeRoomService) {
        mRoomService = service
        service.getUserService().bindRespDelegate(this)
        service.getMicSeatsService().bindRespDelegate(this)
        service.getRoomManager().bindRespDelegate(this)

        // 加入房间
        service.enterRoom({
            // success
            post {
                mRoomViewBinding.topUserLayout.tvRoomName.text = it.roomName
                mRoomViewBinding.topUserLayout.tvRoomId.text = "房间ID: ${it.roomId}"

                Glide.with(mRoomViewBinding.topUserLayout.ivRoomCover)
                    .load(it.roomOwner?.userAvatar)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mRoomViewBinding.topUserLayout.ivRoomCover)

                mRoomViewBinding.topUserLayout.btnShutDown.setOnClickListener {
                    showExitDialog()
                }
                mRoomViewBinding.topUserLayout.btnUserMore.setOnClickListener {
                    showUserListDialog()
                }

                val micSeatsBinder = AUiMicSeatsBinder(
                    mRoomViewBinding.micSeatsView,
                    service.getUserService(),
                    service.getMicSeatsService(),
                    service.getJukeboxService(),
                    service.getChorusService()
                )
                micSeatsBinder.bind()
                mBinders.add(micSeatsBinder)

                mRoomViewBinding.bottomNavLayout.btnGift.setOnClickListener {
//                    showJukeboxDialog()
                }
                mRoomViewBinding.bottomNavLayout.btnMic.setOnClickListener {
                    changeMuteState(!mLocalMute)
                }
                val musicPlayerBinder =
                    AUiMusicPlayerBinder(
                        mRoomViewBinding.musicPlayerView,
                        service.getMusicPlayerService(),
                        service.getJukeboxService(),
                        service.getChorusService(),
                        service.getMicSeatsService()
                    )
                musicPlayerBinder.bind()
                mBinders.add(musicPlayerBinder)

                mRoomViewBinding.musicPlayerView.setMusicPlayerActionListener(object :
                    IMusicPlayerActionListener {
                    override fun onChooseSongClick() {
                        super.onChooseSongClick()
                        showJukeboxDialog()
                    }
                })
            }
        }, {
            post {
                Toast.makeText(context, "Enter room failed : ${it.code}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun setLocalMute(isMute: Boolean) {
        Log.d("local_mic","update rtc mute state: $isMute")
        mLocalMute = isMute
        mRoomService?.setupLocalAudioMute(isMute)
        if (isMute) {
            val drawable = resources.getDrawable(R.mipmap.ic_nav_mic_off)
            mRoomViewBinding.bottomNavLayout.btnMic.setCenterDrawable(drawable)
        } else {
            val drawable = resources.getDrawable(R.mipmap.ic_nav_mic_on)
            mRoomViewBinding.bottomNavLayout.btnMic.setCenterDrawable(drawable)
        }
    }

    private fun changeMuteState(isMute: Boolean) {
        setLocalMute(isMute)
        mRoomService?.getUserService()?.muteUserAudio(isMute) { error ->
            if (error != null) {
                post {
                    setLocalMute(!isMute)
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showUserListDialog() {
        val membersView = AUiRoomMemberListView(context)
        membersView.setMembers(mMemberMap.values.toList(), mSeatMap)
        AUiBottomDialog(context).apply {
            setBackground(null)
            setCustomView(membersView)
            show()
        }
    }

    private fun showJukeboxDialog() {
        val service = mRoomService ?: return
        val jukeboxView = AUiJukeboxView(context)
        val binder = AUiJukeboxBinder(
            jukeboxView,
            service.getJukeboxService()
        )
        AUiBottomDialog(context).apply {
            setOnShowListener { binder.bind() }
            setOnDismissListener { binder.unBind() }
            setBackground(null)
            setCustomView(jukeboxView)
            show()
        }
    }

    fun showExitDialog() {
        AUiAlertDialog(context).apply {
            setTitle("Tip")
            if (isRoomOwner()) {
                setMessage("是否离开并销毁房间？")
            } else {
                setMessage("是否离开房间？")
            }
            setPositiveButton("确认") {
                if (isRoomOwner()) {
                    mRoomService?.destroyRoom()
                } else {
                    mRoomService?.exitRoom()
                }
                dismiss()
            }
            setNegativeButton("取消") {
                dismiss()
            }
            show()
        }
    }

    private fun isRoomOwner() =
        mRoomService?.context?.isRoomOwner(mRoomService?.channelName) ?: false

    private fun updateUserPreview() {
        val members = mMemberMap.values.toList()
        when (members.size) {
            0 -> {
                mRoomViewBinding.topUserLayout.ivUserAvatar1.visibility = View.GONE
                mRoomViewBinding.topUserLayout.llContainer2.visibility = View.GONE
            }
            1 -> {
                mRoomViewBinding.topUserLayout.ivUserAvatar1.visibility = View.VISIBLE
                val firstUser = members.first()
                Glide.with(mRoomViewBinding.topUserLayout.ivUserAvatar1)
                    .load(firstUser.userAvatar)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mRoomViewBinding.topUserLayout.ivUserAvatar1)

                mRoomViewBinding.topUserLayout.llContainer2.visibility = View.GONE
            }
            else -> { // >= 2
                mRoomViewBinding.topUserLayout.ivUserAvatar1.visibility = View.VISIBLE
                mRoomViewBinding.topUserLayout.llContainer2.visibility = View.VISIBLE

                val firstUser = mMemberMap.values.first()
                Glide.with(mRoomViewBinding.topUserLayout.ivUserAvatar1)
                    .load(firstUser.userAvatar)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mRoomViewBinding.topUserLayout.ivUserAvatar1)

                val secondUser = members[1]
                Glide.with(mRoomViewBinding.topUserLayout.ivUserAvatar2)
                    .load(secondUser.userAvatar)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mRoomViewBinding.topUserLayout.ivUserAvatar2)
                if (members.size > 2) {
                    mRoomViewBinding.topUserLayout.tvUserCount.visibility = View.VISIBLE
                    mRoomViewBinding.topUserLayout.tvUserCount.text = members.size.toString()
                } else {
                    mRoomViewBinding.topUserLayout.tvUserCount.visibility = View.GONE
                }
            }
        }
    }

    /** IAUiUserService.AUiRoomRespDelegate */
    override fun onRoomDestroy(roomId: String) {
        if (!isRoomOwner()) {
            mRoomService?.exitRoom(false)
        }
    }

    /** IAUiUserService.AUiUserRespDelegate */
    override fun onRoomUserEnter(roomId: String, userInfo: AUiUserInfo) {
    }

    override fun onRoomUserLeave(roomId: String, userInfo: AUiUserInfo) {
        mMemberMap.remove(userInfo.userId)
        updateUserPreview()
    }

    override fun onRoomUserUpdate(roomId: String, userInfo: AUiUserInfo) {
        mMemberMap[userInfo.userId] = userInfo
        updateUserPreview()
    }

    override fun onUserAudioMute(userId: String, mute: Boolean) {
        val localUserId = mRoomService?.context?.currentUserInfo?.userId ?: ""
        if (localUserId != userId) {
            return
        }
        var localUserSeat: AUiMicSeatInfo? = null
        for (i in 0..7) {
            val seatInfo = mRoomService?.getMicSeatsService()?.getMicSeatInfo(i)
            if (seatInfo?.user != null && seatInfo.user?.userId == userId) {
                localUserSeat = seatInfo
                break
            }
        }
        if (localUserSeat != null) {
            val userInfo = mRoomService?.getUserService()?.getUserInfo(userId)
            val mute = (localUserSeat.muteAudio == 1) || (userInfo?.muteAudio == 1)
            setLocalMute(mute)
        }
    }

    override fun onRoomUserSnapshot(roomId: String, userList: MutableList<AUiUserInfo>?) {
        userList?.forEach { userInfo ->
            mMemberMap[userInfo.userId] = userInfo
        }
        updateUserPreview()
    }

    /** IAUiMicSeatService.AUiMicSeatRespDelegate */
    override fun onAnchorEnterSeat(seatIndex: Int, userInfo: AUiUserThumbnailInfo) {
        mSeatMap[seatIndex] = userInfo.userId
        val localUserId = mRoomService?.context?.currentUserInfo?.userId ?: ""
        if (userInfo.userId == localUserId) { // 本地用户上麦
            mRoomViewBinding.bottomNavLayout.btnMic.visibility = View.VISIBLE
            mRoomService?.setupLocalStreamOn(true)
            val micSeatInfo = mRoomService?.getMicSeatsService()?.getMicSeatInfo(seatIndex)
            val userInfo = mRoomService?.getUserService()?.getUserInfo(localUserId)
            val isMute = (micSeatInfo?.muteAudio == 1) || (userInfo?.muteAudio == 1)
            setLocalMute(isMute)
        }
    }

    override fun onAnchorLeaveSeat(seatIndex: Int, userInfo: AUiUserThumbnailInfo) {
        if (mSeatMap[seatIndex].equals(userInfo.userId)) {
            mSeatMap.remove(seatIndex)
        }
        val localUserId = mRoomService?.context?.currentUserInfo?.userId ?: ""
        if (userInfo.userId == localUserId) { // 本地用户下麦
            mRoomViewBinding.bottomNavLayout.btnMic.visibility = View.INVISIBLE
            setLocalMute(true)
            mRoomService?.setupLocalStreamOn(false)
        }
    }
    override fun onSeatAudioMute(seatIndex: Int, isMute: Boolean) {
        // 麦位被禁用麦克风
        // 远端用户：关闭对该麦位的音频流订阅
        // 本地用户：保险起见关闭本地用户的麦克风音量
        val micSeatInfo = mRoomService?.getMicSeatsService()?.getMicSeatInfo(seatIndex)
        val seatUserId = micSeatInfo?.user?.userId
        if (seatUserId == null || seatUserId.isEmpty()) {
            return
        }
        val userInfo = mRoomService?.getUserService()?.getUserInfo(seatUserId) ?: return
        val localUserId = mRoomService?.context?.currentUserInfo?.userId ?: ""
        val mute = isMute || (userInfo.muteAudio == 1)
        if (seatUserId == localUserId) {
            setLocalMute(mute)
        } else {
            mRoomService?.setupRemoteAudioMute(seatUserId, mute)
        }
    }
}