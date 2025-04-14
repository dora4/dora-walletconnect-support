package dora.lifecycle.config

import android.app.Application
import android.content.Context
import androidx.fragment.app.FragmentManager
import dora.lifecycle.application.ApplicationLifecycleCallbacks
import dora.lifecycle.application.WalletConnectAppLifecycle

class WalletConnectGlobalConfig : GlobalConfig {

    override fun injectApplicationLifecycle(
        context: Context,
        lifecycles: MutableList<ApplicationLifecycleCallbacks>
    ) {
        lifecycles.add(WalletConnectAppLifecycle())
    }

    override fun injectActivityLifecycle(
        context: Context,
        lifecycles: MutableList<Application.ActivityLifecycleCallbacks>
    ) {
    }

    override fun injectFragmentLifecycle(
        context: Context,
        lifecycles: MutableList<FragmentManager.FragmentLifecycleCallbacks>
    ) {
    }
}