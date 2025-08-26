package dora.lifecycle.application

import android.app.Application
import android.content.Context
import dora.security.AntiDebugMonitor

class WalletConnectAppLifecycle : ApplicationLifecycleCallbacks {

    override fun attachBaseContext(base: Context) {
    }

    override fun onCreate(application: Application) {
        // It is recommended to enable anti-debugging in production environments
        AntiDebugMonitor.startMonitoring()
    }

    override fun onTerminate(application: Application) {
    }
}
