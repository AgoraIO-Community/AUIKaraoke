package io.agora.auikit.service.http.room

import io.agora.auikit.model.AUiRoomInfo

data class CreateRoomReq(
    val roomName: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val micSeatCount: Int
)
data class CreateRoomResp(
    val roomId: String,
    val roomName: String
)
data class RoomUserReq(
    val roomId: String,
    val userId: String
)
data class DestroyRoomResp(
    val roomId: String
)
data class RoomReq(
    val channelName: String
)
data class RoomListReq(
    val pageSize: Int,
    val lastCreateTime: Long?
)
data class RoomListResp(
    val pageSize: Int,
    val count: Int,
    val list: List<AUiRoomInfo>
)