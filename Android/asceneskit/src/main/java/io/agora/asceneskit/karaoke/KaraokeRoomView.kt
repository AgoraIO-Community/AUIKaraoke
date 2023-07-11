package io.agora.asceneskit.karaoke

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.asceneskit.R
import io.agora.asceneskit.databinding.AkaraokeRoomViewBinding
import io.agora.asceneskit.karaoke.binder.*
import io.agora.asceneskit.voice.binder.AUIChatListBinder
import io.agora.asceneskit.karaoke.binder.AUIGiftBarrageBinder
import io.agora.auikit.model.AUIGiftTabEntity
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIUserInfo
import io.agora.auikit.model.AUIUserThumbnailInfo
import io.agora.auikit.service.IAUIMicSeatService
import io.agora.auikit.service.IAUIRoomManager.AUIRoomManagerRespDelegate
import io.agora.auikit.service.IAUIUserService
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.callback.AUIGiftListCallback
import io.agora.auikit.ui.basic.AUIBottomDialog
import io.agora.auikit.ui.chatBottomBar.listener.AUISoftKeyboardHeightChangeListener
import io.agora.auikit.ui.jukebox.impl.AUIJukeboxView
import io.agora.auikit.ui.member.impl.AUIRoomMemberListView
import io.agora.auikit.ui.musicplayer.listener.IMusicPlayerActionListener

class KaraokeRoomView : FrameLayout, IAUIUserService.AUIUserRespDelegate,
    IAUIMicSeatService.AUIMicSeatRespDelegate, AUIRoomManagerRespDelegate {

    private var mOnClickShutDown: (() -> Unit)? = null

    private val mRoomViewBinding = AkaraokeRoomViewBinding.inflate(LayoutInflater.from(context))
    private var mRoomService: AUIKaraokeRoomService? = null

    private var mMemberMap = mutableMapOf<String, AUIUserInfo>()

    private var mSeatMap = mutableMapOf<Int, String>()

    private val mBinders = mutableListOf<IAUIBindable>()

    private var activity: FragmentActivity?= null
    private var mLocalMute = true
    private var auiGiftBarrageBinder: AUIGiftBarrageBinder? = null
    private var listener: AUISoftKeyboardHeightChangeListener? = null
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        addView(mRoomViewBinding.root)
    }

    fun setFragmentActivity(activity: FragmentActivity){
        this.activity = activity
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

    fun bindService(service: AUIKaraokeRoomService) {
        mRoomService = service
        service.getUserService().bindRespDelegate(this)
        service.getMicSeatsService().bindRespDelegate(this)
        service.getRoomManager().bindRespDelegate(this)
        val giftService = service.getGiftService()

        // 加入房间
        service.enterRoom({
            /** 获取礼物列表 初始化礼物Binder */
            giftService.getGiftsFromService(object : AUIGiftListCallback {
                override fun onResult(error: AUIException?, giftList: List<AUIGiftTabEntity>) {
                    auiGiftBarrageBinder = AUIGiftBarrageBinder(
                        activity,
                        mRoomViewBinding.giftView,
                        giftList,
                        giftService,
                        service.getChatService()
                    )
                    auiGiftBarrageBinder?.let {
                        it.bind()
                        mBinders.add(it)
                    }
                }
            })
            // success
            post {
                mRoomViewBinding.topUserLayout.tvRoomName.text = it.roomName
                mRoomViewBinding.topUserLayout.tvRoomId.text = "房间ID: ${it.roomId}"

                Glide.with(mRoomViewBinding.topUserLayout.ivRoomCover)
                    .load(it.roomOwner?.userAvatar)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mRoomViewBinding.topUserLayout.ivRoomCover)

                mRoomViewBinding.topUserLayout.btnShutDown.setOnClickListener {
                    mOnClickShutDown?.invoke()
                }
                mRoomViewBinding.topUserLayout.btnUserMore.setOnClickListener {
                    showUserListDialog()
                }

                val micSeatsBinder = AUIMicSeatsBinder(
                    mRoomViewBinding.micSeatsView,
                    service.getUserService(),
                    service.getMicSeatsService(),
                    service.getJukeboxService(),
                    service.getChorusService()
                )
                micSeatsBinder.bind()
                mBinders.add(micSeatsBinder)

                val chatListBinder = AUIChatListBinder(
                    mRoomViewBinding.chatListView,
                    mRoomViewBinding.chatBottomBar,
                    service.getChatService(),
                    service.getRoomInfo()
                )
                chatListBinder.let {
                    it.bind()
                    mBinders.add(it)
                }

                val chatBottomBarBinder = AUIChatBottomBarBinder(
                    service,
                    mRoomViewBinding.chatBottomBar,
                    mRoomViewBinding.chatListView,
                    object : AUIChatBottomBarBinder.AUIChatBottomBarEventDelegate{
                        override fun onClickGift(view: View?) {
                            auiGiftBarrageBinder?.showBottomGiftDialog()
                        }

                        override fun onClickLike(view: View?) {
                            mRoomViewBinding.likeView.addFavor()
                        }

                        override fun onClickMore(view: View?) {}
                        override fun onClickMic(view: View?) {}
                    })

                chatBottomBarBinder.let {
                    it.bind()
                    mBinders.add(it)
                    listener = it.getSoftKeyboardHeightChangeListener()
                }

                mRoomViewBinding.bottomNavLayout.btnGift.setOnClickListener {
//                    showJukeboxDialog()
                }
                mRoomViewBinding.bottomNavLayout.btnMic.setOnClickListener {
                    changeMuteState(!mLocalMute)
                }
                val musicPlayerBinder =
                    AUIMusicPlayerBinder(
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
    fun setOnShutDownClick(action: () -> Unit) {
        mOnClickShutDown = action
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
        val membersView = AUIRoomMemberListView(context)
        membersView.setMembers(mMemberMap.values.toList(), mSeatMap)
        AUIBottomDialog(context).apply {
            setBackground(null)
            setCustomView(membersView)
            show()
        }
    }

    private fun showJukeboxDialog() {
        val service = mRoomService ?: return
        val jukeboxView = AUIJukeboxView(context)
        val binder = AUIJukeboxBinder(
            jukeboxView,
            service.getJukeboxService()
        )
        AUIBottomDialog(context).apply {
            setOnShowListener { binder.bind() }
            setOnDismissListener { binder.unBind() }
            setBackground(null)
            setCustomView(jukeboxView)
            show()
        }
    }

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

    /** IAUIUserService.AUIRoomRespDelegate */
    override fun onRoomDestroy(roomId: String) {

    }

    /** IAUIUserService.AUIUserRespDelegate */
    override fun onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
    }

    override fun onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
        mMemberMap.remove(userInfo.userId)
        updateUserPreview()
    }

    override fun onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        mMemberMap[userInfo.userId] = userInfo
        updateUserPreview()
    }

    override fun onUserAudioMute(userId: String, mute: Boolean) {

    }

    override fun onRoomUserSnapshot(roomId: String, userList: MutableList<AUIUserInfo>?) {
        userList?.forEach { userInfo ->
            mMemberMap[userInfo.userId] = userInfo
        }
        updateUserPreview()
    }

    /** IAUIMicSeatService.AUIMicSeatRespDelegate */
    override fun onAnchorEnterSeat(seatIndex: Int, userInfo: AUIUserThumbnailInfo) {
        mSeatMap[seatIndex] = userInfo.userId
        val localUserId = AUIRoomContext.shared().currentUserInfo.userId
        if (userInfo.userId == localUserId) { // 本地用户上麦
            mRoomViewBinding.bottomNavLayout.btnMic.visibility = View.VISIBLE
            mRoomService?.setupLocalStreamOn(true)
            val userInfo = mRoomService?.getUserService()?.getUserInfo(localUserId)
            val isMute = (userInfo?.muteAudio == 1)
            setLocalMute(isMute)
        }
    }

    override fun onAnchorLeaveSeat(seatIndex: Int, userInfo: AUIUserThumbnailInfo) {
        if (mSeatMap[seatIndex].equals(userInfo.userId)) {
            mSeatMap.remove(seatIndex)
        }
        val localUserId = AUIRoomContext.shared().currentUserInfo.userId
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
        val localUserId = AUIRoomContext.shared().currentUserInfo.userId
        val mute = isMute || (userInfo.muteAudio == 1)
        if (seatUserId != localUserId) {
            mRoomService?.setupRemoteAudioMute(seatUserId, mute)
        }
    }
}