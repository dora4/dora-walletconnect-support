package dora.lifecycle.application

import android.app.Application
import android.content.Context
import dora.security.AntiDebugMonitor

class WalletConnectAppLifecycle : ApplicationLifecycleCallbacks {

    override fun attachBaseContext(base: Context) {
    }

    override fun onCreate(application: Application) {
        // 建议在正式环境加入防调试
        AntiDebugMonitor.startMonitoring()
    }

    override fun onTerminate(application: Application) {
    }
}