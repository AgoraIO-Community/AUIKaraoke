package io.agora.asceneskit.karaoke.binder

import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import io.agora.auikit.model.AUIGiftEntity
import io.agora.auikit.model.AUIGiftTabEntity
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.service.IAUIGiftsService
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.callback.AUIGiftListCallback
import io.agora.auikit.service.im.AUIChatManager
import io.agora.auikit.ui.gift.AUIGiftInfo
import io.agora.auikit.ui.gift.AUIGiftTabInfo
import io.agora.auikit.ui.gift.IAUIGiftBarrageView
import io.agora.auikit.ui.gift.impl.dialog.AUiGiftListView
import io.agora.auikit.utils.ThreadManager
import org.libpag.PAGFile
import org.libpag.PAGView
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.security.MessageDigest

class AUIGiftBarrageBinder constructor(
    private val activity: FragmentActivity,
    private val giftView: IAUIGiftBarrageView,
    private val giftService: IAUIGiftsService,
    private val chatManager:AUIChatManager
): IAUIBindable,IAUIGiftsService.AUIGiftRespObserver {

    private val TAG = "AUIGift_LOG"
    private var roomContext = AUIRoomContext.shared()
    private var mPAGView: PAGView? = null
    private val giftList = mutableListOf<AUIGiftTabEntity>()

    init {
        /** 获取礼物列表 初始化礼物Binder */
        giftService.getGiftsFromService(object : AUIGiftListCallback {
            override fun onResult(error: AUIException?, giftList: List<AUIGiftTabEntity>) {
                this@AUIGiftBarrageBinder.giftList.addAll(giftList)
                downloadEffectResource(giftList)
            }
        })

    }

    override fun bind() {
        giftService.registerRespObserver(this)
    }

    override fun unBind() {
        giftService.registerRespObserver(null)
    }

    fun showBottomGiftDialog(){
        val dialog = AUiGiftListView(activity, giftList.map { entity ->
            AUIGiftTabInfo(entity.tabId, entity.displayName ?: "", entity.gifts.map { gift ->
                AUIGiftInfo(
                    gift?.giftId ?: "",
                    gift?.giftName ?: "",
                    gift?.giftIcon ?: "",
                    gift?.giftCount ?: 0,
                    gift?.giftPrice ?: "",
                    gift?.giftEffect ?: "",
                    gift?.effectMD5 ?: "",
                    gift?.sendUser?.userId ?: "",
                    gift?.sendUser?.userName ?: "",
                    gift?.sendUser?.userAvatar ?: "",
                )
            })
        })
        dialog.setDialogActionListener(object : AUiGiftListView.ActionListener{
            override fun onGiftSend(bean: AUIGiftInfo?) {
                bean?: return
                val entity = AUIGiftEntity(
                    bean.giftId,
                    bean.giftName,
                    bean.giftPrice,
                    bean.giftIcon,
                    bean.giftEffect,
                    bean.giftEffectMD5,
                    roomContext.currentUserInfo
                )
                giftService.sendGift(entity) { error ->
                    if (error == null) {
                        ThreadManager.getInstance().runOnMainThread{
                            effectAnimation(entity)
                            dialog.dismiss()
                            Log.d("AUIGiftViewBinder", "sendGift suc ${giftService.channelName}")
                            chatManager.addGiftList(entity)
                            this@AUIGiftBarrageBinder.giftView.refresh(chatManager.getGiftList().map { gift ->
                                AUIGiftInfo(
                                    gift.giftId ?: "",
                                    gift.giftName ?: "",
                                    gift.giftIcon ?: "",
                                    gift.giftCount,
                                    gift.giftPrice ?: "",
                                    gift.giftEffect ?: "",
                                    gift.effectMD5 ?: "",
                                    gift.sendUser?.userId ?: "",
                                    gift.sendUser?.userName ?: "",
                                    gift.sendUser?.userAvatar ?: "",
                                )
                            })
                        }
                    } else {
                        Log.e("AUIGiftViewBinder", "sendGift error ${error.code} ${error.message}")
                    }
                }
            }
        })
        dialog.show(activity.supportFragmentManager, "gift_dialog")
    }
    private fun effectAnimation(gift: AUIGiftEntity) {
        val path = filePath(gift.giftEffect ?: "")
        if (path == null || path.isEmpty() || !File(path).exists()) {
            return
        }
        Log.d(TAG, "effectAnimation $path")
        if (mPAGView == null) {
            val pagView = PAGView(activity)
            pagView.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
            pagView.elevation = 3F
            val contentView = activity.window?.decorView as ViewGroup
            contentView.addView(pagView)
            pagView.addListener(object: PAGView.PAGViewListener {
                override fun onAnimationStart(p0: PAGView?) {
                    pagView.visibility = View.VISIBLE
                    Log.d(TAG, "gift pag: play start")
                }
                override fun onAnimationEnd(p0: PAGView?) {
                    pagView.visibility = View.INVISIBLE
                    Log.d(TAG, "gift pag: play end")
                }
                override fun onAnimationCancel(p0: PAGView?) {
                    pagView.visibility = View.INVISIBLE
                    Log.d(TAG, "gift pag: play cancel")
                }
                override fun onAnimationRepeat(p0: PAGView?) {}
                override fun onAnimationUpdate(p0: PAGView?) {}
            })
            mPAGView = pagView
        }

        val file = PAGFile.Load(path)
        mPAGView?.composition = file
        mPAGView?.setRepeatCount(1)
        mPAGView?.play()
    }

    private fun downloadEffectResource(tabs: List<AUIGiftTabEntity>) {
        tabs.forEach { tab ->
            tab.gifts.forEach { gift ->
                Log.d(TAG, "for each gift: $gift")
                val url = gift?.giftEffect
                val savePath = filePath(gift?.giftEffect ?: "")
                if (url != null && savePath != null) {
                    Log.d(TAG, "down load resource $url to path $savePath")
                    val task = NetworkTask(url, savePath)
                    task.execute()
                }
            }
        }
    }

    private fun filePath(fileName: String): String? {
        return if (fileName.isEmpty()) {
            null
        } else {
            val dir = File(activity.cacheDir,"giftEffects")
            if (!dir.exists()){
                dir.mkdirs()
            }
            val file = dir.resolve("${calculateMD5(fileName)}.pag")
            return file.absoluteFile.toString()
        }
    }

    private fun calculateMD5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    private class NetworkTask constructor(
        val url: String,
        val path: String,
    ) : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String {
            val inputStream = URL(url).openStream()
            val outputStream = FileOutputStream(File(path))
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            return ""
        }
    }

    override fun onReceiveGiftMsg(giftEntity:AUIGiftEntity?) {
        Log.d(TAG, "onReceiveGiftMsg ")
        ThreadManager.getInstance().runOnMainThread{
            giftEntity?.let { effectAnimation(it) }
            this.giftView.refresh(chatManager.getGiftList().map{ gift ->
                AUIGiftInfo(
                    gift.giftId ?: "",
                    gift.giftName ?: "",
                    gift.giftIcon ?: "",
                    gift.giftCount,
                    gift.giftPrice ?: "",
                    gift.giftEffect ?: "",
                    gift.effectMD5 ?: "",
                    gift.sendUser?.userId ?: "",
                    gift.sendUser?.userName ?: "",
                    gift.sendUser?.userAvatar ?: "",
                )
            })
        }
    }

}