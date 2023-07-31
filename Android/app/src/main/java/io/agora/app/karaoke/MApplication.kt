package io.agora.app.karaoke

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import com.tencent.bugly.crashreport.CrashReport

class MApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initBugly()
        listenAppResume()
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