package io.agora.auikit.service.imp

import android.util.Log
import io.agora.auikit.model.AUiMicSeatInfo
import io.agora.auikit.model.AUiMicSeatStatus
import io.agora.auikit.model.AUiRoomContext
import io.agora.auikit.service.IAUiMicSeatService
import io.agora.auikit.service.callback.AUiCallback
import io.agora.auikit.service.callback.AUiException
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.Utils
import io.agora.auikit.service.http.seat.SeatEnterReq
import io.agora.auikit.service.http.seat.SeatInfoReq
import io.agora.auikit.service.http.seat.SeatInterface
import io.agora.auikit.service.http.seat.SeatLeaveReq
import io.agora.auikit.service.http.seat.SeatPickReq
import io.agora.auikit.service.rtm.AUiRtmManager
import io.agora.auikit.service.rtm.AUiRtmMsgProxyDelegate
import io.agora.auikit.utils.DelegateHelper
import io.agora.auikit.utils.GsonTools
import retrofit2.Call
import retrofit2.Response

private const val kSeatAttrKey = "micSeat"
class AUiMicSeatServiceImpl(
    private val channelName: String,
    private val rtmManager: AUiRtmManager
) : IAUiMicSeatService, AUiRtmMsgProxyDelegate {

    private val delegateHelper = DelegateHelper<IAUiMicSeatService.AUiMicSeatRespDelegate>()

    private var micSeats = mutableMapOf<Int, AUiMicSeatInfo>()

    init {
        rtmManager.subscribeMsg(channelName, kSeatAttrKey, this)
    }

    override fun bindRespDelegate(delegate: IAUiMicSeatService.AUiMicSeatRespDelegate?) {
        delegateHelper.bindDelegate(delegate)
    }

    override fun unbindRespDelegate(delegate: IAUiMicSeatService.AUiMicSeatRespDelegate?) {
        delegateHelper.unBindDelegate(delegate)
    }

    override fun enterSeat(seatIndex: Int, callback: AUiCallback?) {
        val param = SeatEnterReq(
            channelName,
            roomContext.currentUserInfo.userId,
            roomContext.currentUserInfo.userName,
            roomContext.currentUserInfo.userAvatar,
            seatIndex)
        HttpManager.getService(SeatInterface::class.java)
            .seatEnter(param)
            .enqueue(object : retrofit2.Callback<CommonResp<Any>> {
                override fun onResponse(call: Call<CommonResp<Any>>, response: Response<CommonResp<Any>>) {
                    if (response.body()?.code == 0) {
                        callback?.onResult(null)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response))
                    }
                }
                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback?.onResult(AUiException(-1, t.message))
                }
            })
    }

    override fun autoEnterSeat(callback: AUiCallback?) {
        var toIndex: Int? = null
        for ((key, value) in micSeats) {
            if (value.seatStatus == AUiMicSeatStatus.idle) {
                toIndex = key
                break
            }
        }
        if (toIndex != null) {
            enterSeat(toIndex, callback)
        } else {
            callback?.onResult(AUiException(-1, "can not find empty mic seat"))
        }
    }

    override fun leaveSeat(callback: AUiCallback?) {
        HttpManager.getService(SeatInterface::class.java)
            .seatLeave(SeatLeaveReq(channelName, roomContext.currentUserInfo.userId))
            .enqueue(object : retrofit2.Callback<CommonResp<Any>> {
                override fun onResponse(call: Call<CommonResp<Any>>, response: Response<CommonResp<Any>>) {
                    if (response.body()?.code == 0) {
                        callback?.onResult(null)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response))
                    }
                }
                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback?.onResult(AUiException(-1, t.message))
                }
            })
    }

    override fun pickSeat(seatIndex: Int, userId: String, callback: AUiCallback?) {
        HttpManager.getService(SeatInterface::class.java)
            .seatPick(SeatPickReq(channelName, userId, seatIndex))
            .enqueue(object : retrofit2.Callback<CommonResp<Any>> {
                override fun onResponse(call: Call<CommonResp<Any>>, response: Response<CommonResp<Any>>) {
                    if (response.body()?.code == 0) {
                        callback?.onResult(null)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response))
                    }
                }
                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback?.onResult(AUiException(-1, t.message))
                }
            })
    }

    override fun kickSeat(seatIndex: Int, callback: AUiCallback?) {
        HttpManager.getService(SeatInterface::class.java)
            .seatKick(SeatInfoReq(channelName, roomContext.currentUserInfo.userId, seatIndex))
            .enqueue(object : retrofit2.Callback<CommonResp<Any>> {
                override fun onResponse(call: Call<CommonResp<Any>>, response: Response<CommonResp<Any>>) {
                    if (response.body()?.code == 0) {
                        callback?.onResult(null)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response))
                    }
                }
                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback?.onResult(AUiException(-1, t.message))
                }
            })
    }

    override fun muteAudioSeat(seatIndex: Int, isMute: Boolean, callback: AUiCallback?) {
        val param = SeatInfoReq(channelName, roomContext.currentUserInfo.userId, seatIndex)
        val service = HttpManager.getService(SeatInterface::class.java)
        val req = if (isMute) {
            service.seatAudioMute(param)
        } else {
            service.seatAudioUnMute(param)
        }
        req.enqueue(object : retrofit2.Callback<CommonResp<Any>> {
            override fun onResponse(call: Call<CommonResp<Any>>, response: Response<CommonResp<Any>>) {
                if (response.body()?.code == 0) {
                    callback?.onResult(null)
                } else {
                    callback?.onResult(Utils.errorFromResponse(response))
                }
            }
            override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                callback?.onResult(AUiException(-1, t.message))
            }
        })
    }

    override fun muteVideoSeat(seatIndex: Int, isMute: Boolean, callback: AUiCallback?) {
        val param = SeatInfoReq(channelName, roomContext.currentUserInfo.userId, seatIndex)
        val service = HttpManager.getService(SeatInterface::class.java)
        val req = if (isMute) {
            service.seatVideoMute(param)
        } else {
            service.seatVideoUnMute(param)
        }
        req.enqueue(object : retrofit2.Callback<CommonResp<Any>> {
            override fun onResponse(call: Call<CommonResp<Any>>, response: Response<CommonResp<Any>>) {
                if (response.body()?.code == 0) {
                    callback?.onResult(null)
                } else {
                    callback?.onResult(Utils.errorFromResponse(response))
                }
            }
            override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                callback?.onResult(AUiException(-1, t.message))
            }
        })
    }

    override fun closeSeat(seatIndex: Int, isClose: Boolean, callback: AUiCallback?) {
        val param = SeatInfoReq(channelName, roomContext.currentUserInfo.userId, seatIndex)
        val service = HttpManager.getService(SeatInterface::class.java)
        val req = if (isClose) {
            service.seatLock(param)
        } else {
            service.seatUnLock(param)
        }
        req.enqueue(object : retrofit2.Callback<CommonResp<Any>> {
            override fun onResponse(call: Call<CommonResp<Any>>, response: Response<CommonResp<Any>>) {
                if (response.body()?.code == 0) {
                    callback?.onResult(null)
                } else {
                    callback?.onResult(Utils.errorFromResponse(response))
                }
            }
            override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                callback?.onResult(AUiException(-1, t.message))
            }
        })
    }

    override fun getMicSeatInfo(seatIndex: Int): AUiMicSeatInfo? {
        return micSeats[seatIndex]
    }
    override fun getChannelName() = channelName

    /** AUiRtmMsgProxyDelegate */
    override fun onMsgDidChanged(channelName: String, key: String, value: Any) {
        if (key != kSeatAttrKey) {
            return
        }
        Log.d("mic_seat_update", "class: ${value.javaClass}")
        val map: Map<String, Any> = HashMap()
        val seats = GsonTools.toBean(value as String, map.javaClass)
        Log.d("mic_seat_update", "seats: $seats")
        seats?.values?.forEach {
            val micSeat = GsonTools.toBean(GsonTools.beanToString(it), AUiMicSeatInfo::class.java) ?: return
            val index = micSeat.seatIndex
            val originMicSeat = micSeats[index]
            micSeats[index] = micSeat
            delegateHelper.notifyDelegate { delegate ->
                val newUser = micSeat.user
                if ((originMicSeat?.user == null || originMicSeat.user?.userId?.length == 0)
                    && newUser != null) {
                    Log.d("mic_seat_update", "onAnchorEnterSeat: $it")
                    delegate.onAnchorEnterSeat(index, newUser)
                }
                val originUser = originMicSeat?.user
                if ((micSeat.user == null || micSeat.user?.userId?.length == 0)
                    && originUser != null) {
                    Log.d("mic_seat_update", "onAnchorLeaveSeat: $it")
                    delegate.onAnchorLeaveSeat(index, originUser)
                }
                if ((originMicSeat?.seatStatus ?: AUiMicSeatStatus.idle) != micSeat.seatStatus) {
                    Log.d("mic_seat_update", "onSeatClose: $it")
                    delegate.onSeatClose(index, (micSeat.seatStatus == AUiMicSeatStatus.locked))
                }
                if ((originMicSeat?.muteAudio ?: 0) != micSeat.muteAudio) {
                    Log.d("mic_seat_update", "onSeatAudioMute: $it")
                    delegate.onSeatAudioMute(index, (micSeat.muteAudio != 0))
                }
                if ((originMicSeat?.muteVideo ?: 0) != micSeat.muteVideo) {
                    Log.d("mic_seat_update", "onSeatVideoMute: $it")
                    delegate.onSeatVideoMute(index, (micSeat.muteVideo != 0))
                }
            }
        }
    }
}