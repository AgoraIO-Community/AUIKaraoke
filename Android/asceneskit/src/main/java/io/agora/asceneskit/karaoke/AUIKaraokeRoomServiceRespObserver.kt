package io.agora.asceneskit.karaoke

import io.agora.auikit.model.AUIRoomInfo

interface AUIKaraokeRoomServiceRespObserver {

    /**
     * 房间Token即将过期时回调
     *
     * @param roomId 房间id
     */
    fun onTokenPrivilegeWillExpire(roomId: String) {}

    /**
     *  房间被销毁的回调
     *
     * @param roomId 房间id
     */
    fun onRoomDestroy(roomId: String) {}

    /**
     * 房间信息变更回调
     *
     * @param roomId 房间id
     * @param roomInfo 房间信息
     */
    fun onRoomInfoChange(roomId: String, roomInfo: AUIRoomInfo) {}

    /**
     * 房间公告发生变更
     *
     * @param roomId 房间id
     * @param announcement 公告变更内容
     */
    fun onRoomAnnouncementChange(roomId: String, announcement: String) {}

    /**
     * 房间用户被踢出房间
     *
     * @param roomId 房间id
     * @param userId 用户id
     */
    fun onRoomUserBeKicked(roomId: String,userId: String) {}
}