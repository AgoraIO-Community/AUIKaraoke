package io.agora.asceneskit.karaoke

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.asceneskit.databinding.AkaraokeRoomViewBinding
import io.agora.asceneskit.karaoke.binder.AUIChatBottomBarBinder
import io.agora.asceneskit.karaoke.binder.AUIGiftBarrageBinder
import io.agora.asceneskit.karaoke.binder.AUIJukeboxBinder
import io.agora.asceneskit.karaoke.binder.AUIMicSeatsBinder
import io.agora.asceneskit.karaoke.binder.AUIMusicPlayerBinder
import io.agora.asceneskit.karaoke.binder.IAUIBindable
import io.agora.asceneskit.voice.binder.AUIChatListBinder
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIUserInfo
import io.agora.auikit.model.AUIUserThumbnailInfo
import io.agora.auikit.service.IAUIMicSeatService
import io.agora.auikit.service.IAUIRoomManager.AUIRoomManagerRespObserver
import io.agora.auikit.service.IAUIUserService
import io.agora.auikit.ui.basic.AUIBottomDialog
import io.agora.auikit.ui.chatBottomBar.impl.AUIKeyboardStatusWatcher
import io.agora.auikit.ui.chatBottomBar.listener.AUISoftKeyboardHeightChangeListener
import io.agora.auikit.ui.jukebox.impl.AUIJukeboxView
import io.agora.auikit.ui.member.MemberInfo
import io.agora.auikit.ui.member.impl.AUIRoomMemberListView
import io.agora.auikit.ui.musicplayer.listener.IMusicPlayerActionListener

class KaraokeRoomView : FrameLayout,
    IAUIUserService.AUIUserRespObserver,
    IAUIMicSeatService.AUIMicSeatRespObserver,
    AUIRoomManagerRespObserver {

    private var mOnClickShutDown: (() -> Unit)? = null

    private val mRoomViewBinding = AkaraokeRoomViewBinding.inflate(LayoutInflater.from(context))
    private var mRoomService: AUIKaraokeRoomService? = null

    private var mMemberMap = mutableMapOf<String, AUIUserInfo>()

    private var mSeatMap = mutableMapOf<Int, String>()

    private val mBinders = mutableListOf<IAUIBindable>()

    private var activity: FragmentActivity?= null
    private var mLocalMute = true
    private var listener: AUISoftKeyboardHeightChangeListener? = null
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        addView(mRoomViewBinding.root)

        // 调整底部栏样式
        mRoomViewBinding.chatBottomBar.removeMenu(io.agora.auikit.ui.R.id.voice_extend_item_more)
        mRoomViewBinding.chatBottomBar.removeMenu(io.agora.auikit.ui.R.id.voice_extend_item_like)
        mRoomViewBinding.chatBottomBar.updateMenuGravity(io.agora.auikit.ui.R.id.voice_extend_item_mic, Gravity.START)
    }

    fun setFragmentActivity(activity: FragmentActivity){
        this.activity = activity
        getSoftKeyboardHeight()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        mRoomService?.getUserService()?.unRegisterRespObserver(this)
        mRoomService?.getMicSeatsService()?.unRegisterRespObserver(this)
        mRoomService?.getRoomManager()?.unRegisterRespObserver(this)

        mBinders.forEach {
            it.unBind()
        }
        if(isRoomOwner()){
            mRoomService?.getIMManagerService()?.userDestroyedChatroom()
        }else {
            mRoomService?.getIMManagerService()?.userQuitRoom()
        }
    }

    private fun isRoomOwner() = AUIRoomContext.shared().isRoomOwner(mRoomService?.getRoomInfo()?.roomId)

    fun bindService(service: AUIKaraokeRoomService) {
        mRoomService = service
        service.getRoomManager().registerRespObserver(this)

        // 加入房间
        service.enterRoom({
            // success
            post {
                Toast.makeText(context, "Enter room successfully", Toast.LENGTH_SHORT).show()
            }
        }, {
            post {
                Toast.makeText(context, "Enter room failed : ${it.code}", Toast.LENGTH_SHORT).show()
            }
        })

        // 顶部用户信息
        service.getUserService().registerRespObserver(this)
        service.getMicSeatsService().registerRespObserver(this)
        mRoomViewBinding.topUserLayout.tvRoomName.text = service.getRoomInfo().roomName
        mRoomViewBinding.topUserLayout.tvRoomId.text = "房间ID: ${service.getRoomInfo().roomId}"
        Glide.with(mRoomViewBinding.topUserLayout.ivRoomCover)
            .load(service.getRoomInfo().roomOwner?.userAvatar)
            .apply(RequestOptions.circleCropTransform())
            .into(mRoomViewBinding.topUserLayout.ivRoomCover)
        mRoomViewBinding.topUserLayout.btnShutDown.setOnClickListener {
            mOnClickShutDown?.invoke()
        }
        mRoomViewBinding.topUserLayout.btnUserMore.setOnClickListener {
            showUserListDialog()
        }
        service.getRoomInfo().roomOwner?.let {
            mMemberMap[it.userId] = AUIUserInfo().apply {
                userId = it.userId
                userAvatar = it.userAvatar
                userName = it.userName
            }
            updateUserPreview()
        }

        // 礼物
        val auiGiftBarrageBinder = AUIGiftBarrageBinder(
            activity!!,
            mRoomViewBinding.giftView,
            service.getGiftService(),
            service.getChatManager()
        )
        auiGiftBarrageBinder.bind()
        mBinders.add(auiGiftBarrageBinder)

        // 底部栏
        val chatBottomBarBinder = AUIChatBottomBarBinder(service,
            mRoomViewBinding.chatBottomBar,
            mRoomViewBinding.micSeatsView,
            mRoomViewBinding.chatListView,
            object : AUIChatBottomBarBinder.AUIChatBottomBarEventDelegate {
                override fun onClickGift(view: View?) {
                    auiGiftBarrageBinder.showBottomGiftDialog()
                }
            })
        chatBottomBarBinder.bind()
        listener = chatBottomBarBinder.getSoftKeyboardHeightChangeListener()
        mBinders.add(chatBottomBarBinder)

        // 麦位
        val micSeatsBinder = AUIMicSeatsBinder(
            mRoomViewBinding.micSeatsView,
            service.getUserService(),
            service.getMicSeatsService(),
            service.getJukeboxService(),
            service.getChorusService()
        )
        micSeatsBinder.bind()
        mBinders.add(micSeatsBinder)

        // 聊天列表
        val chatListBinder = AUIChatListBinder(
            mRoomViewBinding.chatListView,
            mRoomViewBinding.chatBottomBar,
            service.getIMManagerService(),
            service.getChatManager(),
            service.getRoomInfo()
        )
        chatListBinder.bind()
        mBinders.add(chatListBinder)

        // 播放器
        val musicPlayerBinder = AUIMusicPlayerBinder(
                mRoomViewBinding.musicPlayerView,
                service.getMusicPlayerService(),
                service.getJukeboxService(),
                service.getChorusService(),
                service.getMicSeatsService())
        musicPlayerBinder.bind()
        mBinders.add(musicPlayerBinder)
        mRoomViewBinding.musicPlayerView.setMusicPlayerActionListener(object : IMusicPlayerActionListener {
            override fun onChooseSongClick() {
                super.onChooseSongClick()
                showJukeboxDialog()
            }
        })
    }
    fun setOnShutDownClick(action: () -> Unit) {
        mOnClickShutDown = action
    }

    private fun showUserListDialog() {
        val membersView = AUIRoomMemberListView(context)
        membersView.setMembers(mMemberMap.values.map { model ->
            MemberInfo(
                model.userId,
                model.userName,
                model.userAvatar
            )
        }, mSeatMap)
        AUIBottomDialog(context).apply {
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

    private fun getSoftKeyboardHeight(){
        activity?.let {
            AUIKeyboardStatusWatcher(
                it, activity!!
            ) { isKeyboardShowed: Boolean, keyboardHeight: Int? ->
                Log.e("apex", "isKeyboardShowed: $isKeyboardShowed $keyboardHeight")
                listener?.onSoftKeyboardHeightChanged(isKeyboardShowed,keyboardHeight)
            }
        }
    }

    /** IAUIUserService.AUIRoomRespDelegate */
    override fun onRoomDestroy(roomId: String) {

    }

    /** IAUIUserService.AUIUserRespDelegate */

    override fun onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
        mMemberMap.remove(userInfo.userId)
        updateUserPreview()
    }

    override fun onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        mMemberMap[userInfo.userId] = userInfo
        updateUserPreview()
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
    }

    override fun onAnchorLeaveSeat(seatIndex: Int, userInfo: AUIUserThumbnailInfo) {
        if (mSeatMap[seatIndex].equals(userInfo.userId)) {
            mSeatMap.remove(seatIndex)
        }
    }

}