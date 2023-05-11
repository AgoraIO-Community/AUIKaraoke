package io.agora.asceneskit.karaoke

import io.agora.auikit.model.AUiRoomInfo
import io.agora.auikit.model.AUiUserInfo
import io.agora.auikit.service.IAUiChorusService
import io.agora.auikit.service.IAUiCommonService
import io.agora.auikit.service.IAUiJukeboxService
import io.agora.auikit.service.IAUiMicSeatService
import io.agora.auikit.service.IAUiMusicPlayerService
import io.agora.auikit.service.IAUiRoomManager
import io.agora.auikit.service.IAUiUserService
import io.agora.auikit.service.callback.AUiException

interface IKaraokeRoomService :
    IAUiCommonService<IKaraokeRoomService.KaraokeRoomRoomRespDelegate> {

    fun getRoomManager(): IAUiRoomManager

    fun getUserService(): IAUiUserService

    fun getMicSeatsService(): IAUiMicSeatService

    fun getMusicPlayerService(): IAUiMusicPlayerService

    fun getJukeboxService(): IAUiJukeboxService

    fun getChorusService(): IAUiChorusService

    fun enterRoom(success: (AUiRoomInfo) -> Unit, failure: (AUiException)->Unit)

    fun exitRoom()

    fun destroyRoom()

    fun getRoomInfo(): AUiRoomInfo

    fun getUserList(callback: (List<AUiUserInfo>) -> Unit)
    fun setupLocalStreamOn(isOn: Boolean)
    fun setupLocalAudioMute(isMute: Boolean)
    fun setupRemoteAudioMute(userId: String, isMute: Boolean)
    interface KaraokeRoomRoomRespDelegate {
        fun onRoomExitedOrDestroyed() {}
    }

}