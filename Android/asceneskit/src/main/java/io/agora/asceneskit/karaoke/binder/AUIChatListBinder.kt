package io.agora.asceneskit.voice.binder

import android.util.Log
import io.agora.asceneskit.R
import io.agora.asceneskit.karaoke.binder.IAUIBindable
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.service.IAUIIMManagerService
import io.agora.auikit.service.im.AUIChatManager
import io.agora.auikit.ui.chatBottomBar.IAUIChatBottomBarView
import io.agora.auikit.ui.chatList.AUIChatInfo
import io.agora.auikit.ui.chatList.IAUIChatListView
import io.agora.auikit.ui.chatList.listener.AUIChatListItemClickListener

class AUIChatListBinder constructor(
    private val chatListView:IAUIChatListView,
    private val chatBottomBarView:IAUIChatBottomBarView,
    private val imManagerService:IAUIIMManagerService,
    private val chatManager: AUIChatManager,
    private val roomInfo:AUIRoomInfo
): IAUIBindable,
    AUIChatListItemClickListener,
    IAUIIMManagerService.AUIIMManagerRespDelegate {

    init {
        chatListView.initView(roomInfo.roomOwner?.userId ?: "")
    }

    override fun bind() {
        chatListView.setChatListItemClickListener(this)
        imManagerService.bindRespDelegate(this)
        chatManager.saveWelcomeMsg(AUIRoomContext.shared().commonConfig.context.getString(R.string.voice_room_welcome))
        chatListView.refreshSelectLast(chatManager.getMsgList().map { entity ->
            AUIChatInfo(entity.user?.userId ?: "", entity.user?.userName?: "", entity.content, entity.joined)
        })
    }

    override fun unBind() {
        chatListView.setChatListItemClickListener(null)
        imManagerService.unbindRespDelegate(this)
    }

    override fun onChatListViewClickListener() {
        chatBottomBarView.hideKeyboard()
    }

    override fun onItemClickListener(message: AUIChatInfo?) {
        Log.d("AUIChatListBinder","onItemClickListener")
    }

    override fun messageDidReceive(
        roomId: String,
        message: IAUIIMManagerService.AgoraChatTextMessage
    ) {
        chatListView.refreshSelectLast(chatManager.getMsgList().map { entity ->
            AUIChatInfo(entity.user?.userId ?: "", entity.user?.userName?: "", entity.content, entity.joined)
        })
    }

    override fun onUserDidJoinRoom(
        roomId: String,
        message: IAUIIMManagerService.AgoraChatTextMessage
    ) {
        chatListView.refreshSelectLast(chatManager.getMsgList().map { entity ->
            AUIChatInfo(entity.user?.userId ?: "", entity.user?.userName?: "", entity.content, entity.joined)
        })
    }
}