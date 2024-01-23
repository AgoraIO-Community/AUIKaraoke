package io.agora.app.karaoke

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import com.tencent.bugly.crashreport.CrashReport
import io.agora.asceneskit.karaoke.AUIAPIConfig
import io.agora.asceneskit.karaoke.KaraokeUiKit
import io.agora.auikit.model.AUICommonConfig
import io.agora.auikit.model.AUIUserThumbnailInfo

class MApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initKaraokeUiKit()
        initBugly()
        listenAppResume()
    }

    private fun initKaraokeUiKit() {
        // Create Common Config
        val config = AUICommonConfig()
        config.context = this
        config.appId = BuildConfig.AGORA_APP_ID
        config.appCert = BuildConfig.AGORA_APP_CERT
        config.host = BuildConfig.SERVER_HOST
        config.imAppKey = BuildConfig.IM_APP_KEY
        config.imClientId = BuildConfig.IM_CLIENT_ID
        config.imClientSecret = BuildConfig.IM_CLIENT_SECRET
        // Randomly generate local user information
        config.owner = AUIUserThumbnailInfo().apply {
            userId = RandomUtils.randomUserId()
            userName = RandomUtils.randomUserName()
            userAvatar = RandomUtils.randomAvatar()
        }
        // Setup karaokeUiKit
        KaraokeUiKit.setup(
            commonConfig = config, // must
            apiConfig = AUIAPIConfig()
        )
    }

    private fun initBugly() {
        CrashReport.UserStrategy(this).let {
            it.isEnableCatchAnrTrace = true
            it.deviceID = Build.DEVICE
            it.deviceModel = Build.MODEL
            CrashReport.initCrashReport(this, "73dcc97653", BuildConfig.DEBUG, it)
        }
    }

    private fun listenAppResume() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (savedInstanceState != null) {
                    packageManager.getLaunchIntentForPackage(packageName)?.let {
                        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(it)
                        Process.killProcess(Process.myPid())
                    }
                }
            }

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityResumed(activity: Activity) {

            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {

            }

        })
    }
}