package io.agora.asceneskit.karaoke.binder

import android.util.Log
import android.view.View
import android.widget.Toast
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
import io.agora.auikit.ui.chatList.AUIChatInfo
import io.agora.auikit.ui.chatList.IAUIChatListView
import io.agora.auikit.ui.micseats.IMicSeatsView

class AUIChatBottomBarBinder constructor(
    private val roomService: AUIKaraokeRoomService,
    private val chatBottomBarView: IAUIChatBottomBarView,
    private val micSeatsView: IMicSeatsView,
    private val chatList: IAUIChatListView,
    private val event: AUIChatBottomBarEventDelegate?
) : IAUIBindable,
    AUIMenuItemClickListener,
    IAUIUserService.AUIUserRespObserver,
    IAUIMicSeatService.AUIMicSeatRespObserver,
    IAUIInvitationService.AUIInvitationRespObserver {

    private var listener:AUISoftKeyboardHeightChangeListener?=null
    private val userService = roomService.userService
    private val chatManager = roomService.chatManager
    private val imManagerService = roomService.imManagerService
    private val micSeatService = roomService.micSeatService
    private var roomContext = AUIRoomContext.shared()
    private val isRoomOwner = roomContext.isRoomOwner(roomService.roomInfo?.roomId)
    private var mLocalMute = true

    override fun bind() {
        userService.registerRespObserver(this)
        micSeatService.registerRespObserver(this)
        chatBottomBarView.setShowMic(AUIRoomContext.shared().isRoomOwner(userService.channelName))
        chatBottomBarView.setMenuItemClickListener(this)
        chatBottomBarView.setSoftKeyListener()
    }

    override fun unBind() {
        chatBottomBarView.setMenuItemClickListener(null)
        userService.unRegisterRespObserver(this)
        micSeatService.unRegisterRespObserver(this)
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
                if(isRoomOwner){
                    mLocalMute = !mLocalMute
                    micSeatService.muteAudioSeat(0, mLocalMute, null)
                }else{
                    if(mLocalMute){
                        // 检查是否被房主禁麦
                        for (seatIndex in 0 until micSeatService.micSeatSize){
                            val seatInfo = micSeatService.getMicSeatInfo(seatIndex)
                            if (seatInfo?.user?.userId == roomContext.currentUserInfo.userId) {
                                val isMuteByHost = seatInfo.muteAudio != 0
                                if(isMuteByHost){
                                    Toast.makeText(roomContext.requireCommonConfig().context, "当前麦位已被房主禁麦", Toast.LENGTH_SHORT).show()
                                    return
                                }
                            }
                        }
                    }
                    //点击下方麦克风
                    Log.e("apex","mic")
                    mLocalMute = !mLocalMute
                    userService.muteUserAudio(mLocalMute, null)
                }

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
        imManagerService.sendMessage(roomService.channelName,
            content ?: ""){ _, error ->
            if(error == null){
                chatList.refreshSelectLast(chatManager.getMsgList().map { entity ->
                    AUIChatInfo(entity.user?.userId ?: "", entity.user?.userName?: "", entity.content, entity.joined)
                })
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
        var seatIndex = 0
        for (i in 0..7) {
            val seatInfo = roomService.micSeatService.getMicSeatInfo(i)
            if (seatInfo?.user != null && seatInfo.user?.userId == userId) {
                localUserSeat = seatInfo
                seatIndex = i
                break
            }
        }
        if (localUserSeat != null) {
            setLocalMute(seatIndex, mute)
        }
    }

    /** IAUiMicSeatService.AUiMicSeatRespDelegate */
    override fun onAnchorEnterSeat(seatIndex: Int, userInfo: AUIUserThumbnailInfo) {
        val localUserId = roomContext?.currentUserInfo?.userId ?: ""
        if (userInfo.userId == localUserId) { // 本地用户上麦
            chatBottomBarView.setShowMic(true)
            roomService.setupLocalStreamOn(true)
            val micSeatInfo = roomService.micSeatService.getMicSeatInfo(seatIndex)
            val isMute = (micSeatInfo?.muteAudio == 1) ||
                    (roomService.userService.getUserInfo(localUserId)?.muteAudio == 1)
            setLocalMute(seatIndex, isMute)
        }
    }

    override fun onAnchorLeaveSeat(seatIndex: Int, userInfo: AUIUserThumbnailInfo) {
        val localUserId = roomContext?.currentUserInfo?.userId ?: ""
        if (userInfo.userId == localUserId) { // 本地用户下麦
            chatBottomBarView.setShowMic(false)
            roomService.setupLocalStreamOn(false)
        }
    }

    override fun onSeatAudioMute(seatIndex: Int, isMute: Boolean) {
        // 麦位被禁用麦克风
        // 远端用户：关闭对该麦位的音频流订阅
        // 本地用户：保险起见关闭本地用户的麦克风音量
        val micSeatInfo = roomService.micSeatService.getMicSeatInfo(seatIndex)
        val seatUserId = micSeatInfo?.user?.userId
        if (seatUserId.isNullOrEmpty()) {
            return
        }
        val userInfo = roomService.userService.getUserInfo(seatUserId) ?: return
        val localUserId = roomContext?.currentUserInfo?.userId ?: ""
        val mute = isMute || (userInfo.muteAudio == 1)
        if (seatUserId == localUserId) {
            setLocalMute(seatIndex, mute)
        } else {
            roomService.setupRemoteAudioMute(seatUserId, mute)
        }
    }

    private fun setLocalMute(seatIndex: Int, isMute: Boolean) {
        Log.d("local_mic","update rtc mute state: $isMute")
        mLocalMute = isMute
        roomService.setupLocalAudioMute(isMute)
        chatBottomBarView.setEnableMic(isMute)

        val seatView = micSeatsView.micSeatItemViewList[seatIndex]
        seatView?.let {
            seatView.setAudioMuteVisibility(if (isMute) View.VISIBLE else View.GONE)
        }
    }


}