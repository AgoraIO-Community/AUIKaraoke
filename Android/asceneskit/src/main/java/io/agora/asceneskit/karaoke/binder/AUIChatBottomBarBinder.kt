package io.agora.asceneskit.karaoke.binder

import android.util.Log
import android.view.View
import io.agora.asceneskit.karaoke.AUIKaraokeRoomService
import io.agora.auikit.model.AUIMicSeatInfo
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIUserThumbnailInfo
import io.agora.auikit.service.IAUIInvitationService
import io.agora.auikit.service.IAUIMicSeatService
import io.agora.auikit.service.IAUIUserService
import io.agora.auikit.ui.R
import io.agora.auikit.ui.chatBottomBar.IAUIChatBottomBarView
import io.agora.auikit.ui.chatBottomBar.listener.AUIMenuItemClickListener
import io.agora.auikit.ui.chatBottomBar.listener.AUISoftKeyboardHeightChangeListener
import io.agora.auikit.ui.chatList.IAUIChatListView

class AUIChatBottomBarBinder constructor(
    private val roomService: AUIKaraokeRoomService,
    private val chatBottomBarView: IAUIChatBottomBarView,
    private val chatList: IAUIChatListView,
    private val event: AUIChatBottomBarEventDelegate?
) : IAUIBindable,
    AUIMenuItemClickListener,
    IAUIUserService.AUIUserRespDelegate,
    IAUIMicSeatService.AUIMicSeatRespDelegate,
    IAUIInvitationService.AUIInvitationRespDelegate {

    private var listener:AUISoftKeyboardHeightChangeListener?=null
    private val userService = roomService.getUserService()
    private val chatManager = roomService.getChatManager()
    private val imManagerService = roomService.getIMManagerService()
    private val micSeatService = roomService.getMicSeatsService()
    private var roomContext = AUIRoomContext.shared()
    private val isRoomOwner = roomContext.isRoomOwner(roomService.getRoomInfo().roomId)
    private var mLocalMute = true

    override fun bind() {
        userService.bindRespDelegate(this)
        micSeatService.bindRespDelegate(this)
        chatBottomBarView.setMenuItemClickListener(this)
        chatBottomBarView.setSoftKeyListener()
    }

    override fun unBind() {
        chatBottomBarView.setMenuItemClickListener(null)
        userService.unbindRespDelegate(this)
        micSeatService.unbindRespDelegate(this)
    }


    override fun setSoftKeyBoardHeightChangedListener(listener: AUISoftKeyboardHeightChangeListener) {
        this.listener = listener
    }

    fun getSoftKeyboardHeightChangeListener(): AUISoftKeyboardHeightChangeListener?{
        return listener
    }

    override fun onChatExtendMenuItemClick(itemId: Int, view: View?) {
        when (itemId) {
            R.id.voice_extend_item_more -> {
                //自定义预留
                Log.e("apex","more")
                chatBottomBarView.setShowMoreStatus(isRoomOwner,false)
                event?.onClickMore(view)
            }
            R.id.voice_extend_item_mic -> {
                //点击下方麦克风
                Log.e("apex","mic")
                mLocalMute = !mLocalMute
                setLocalMute(mLocalMute)
                event?.onClickMic(view)
            }
            R.id.voice_extend_item_gift -> {
                //点击下方礼物按钮 弹出送礼菜单
                event?.onClickGift(view)
            }
            R.id.voice_extend_item_like -> {
                //点击下方点赞按钮
                event?.onClickLike(view)
                Log.e("apex","like")
            }
        }
    }

    override fun onSendMessage(content: String?) {
        imManagerService.sendMessage(roomService.getRoomInfo().roomId,
            content ?: ""){ _, error ->
            if(error == null){
                chatList.refreshSelectLast(chatManager.getMsgList())
            }
        }
    }

    interface AUIChatBottomBarEventDelegate{
        fun onClickGift(view: View?){}
        fun onClickLike(view: View?){}
        fun onClickMore(view: View?){}
        fun onClickMic(view: View?){}
    }

    override fun onUserAudioMute(userId: String, mute: Boolean) {
        val localUserId = roomContext?.currentUserInfo?.userId ?: ""
        if (localUserId != userId) {
            return
        }
        var localUserSeat: AUIMicSeatInfo? = null
        for (i in 0..7) {
            val seatInfo = roomService.getMicSeatsService().getMicSeatInfo(i)
            if (seatInfo?.user != null && seatInfo.user?.userId == userId) {
                localUserSeat = seatInfo
                break
            }
        }
        if (localUserSeat != null) {
            val userInfo = userService.getUserInfo(userId)
            val mute = (localUserSeat.muteAudio == 1) || (userInfo?.muteAudio == 1)
            setLocalMute(mute)
        }
    }

    /** IAUiMicSeatService.AUiMicSeatRespDelegate */
    override fun onAnchorEnterSeat(seatIndex: Int, userInfo: AUIUserThumbnailInfo) {
        val localUserId = roomContext?.currentUserInfo?.userId ?: ""
        if (userInfo.userId == localUserId) { // 本地用户上麦
            roomService.setupLocalStreamOn(true)
            val micSeatInfo = roomService.getMicSeatsService().getMicSeatInfo(seatIndex)
            val userInfo = roomService.getUserService().getUserInfo(localUserId)
            val isMute = (micSeatInfo?.muteAudio == 1) || (userInfo?.muteAudio == 1)
            setLocalMute(isMute)
        }
    }

    override fun onAnchorLeaveSeat(seatIndex: Int, userInfo: AUIUserThumbnailInfo) {
        val localUserId = roomContext?.currentUserInfo?.userId ?: ""
        if (userInfo.userId == localUserId) { // 本地用户下麦
            setLocalMute(true)
            roomService.setupLocalStreamOn(false)
        }
    }

    override fun onSeatAudioMute(seatIndex: Int, isMute: Boolean) {
        // 麦位被禁用麦克风
        // 远端用户：关闭对该麦位的音频流订阅
        // 本地用户：保险起见关闭本地用户的麦克风音量
        val micSeatInfo = roomService.getMicSeatsService().getMicSeatInfo(seatIndex)
        val seatUserId = micSeatInfo?.user?.userId
        if (seatUserId == null || seatUserId.isEmpty()) {
            return
        }
        val userInfo = roomService.getUserService().getUserInfo(seatUserId) ?: return
        val localUserId = roomContext?.currentUserInfo?.userId ?: ""
        val mute = isMute || (userInfo.muteAudio == 1)
        if (seatUserId == localUserId) {
            setLocalMute(mute)
        } else {
            roomService.setupRemoteAudioMute(seatUserId, mute)
        }
    }

    private fun setLocalMute(isMute: Boolean) {
        Log.d("local_mic","update rtc mute state: $isMute")
        mLocalMute = isMute
        roomService.setupLocalAudioMute(isMute)
        chatBottomBarView.setEnableMic(isMute)
    }


}