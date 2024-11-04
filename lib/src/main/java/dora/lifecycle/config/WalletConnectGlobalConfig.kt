package dora.lifecycle.config

import android.app.Application
import android.content.Context
import androidx.fragment.app.FragmentManager
import dora.lifecycle.activity.WalletConnectActivityLifecycle
import dora.lifecycle.application.ApplicationLifecycleCallbacks

class WalletConnectGlobalConfig : GlobalConfig {

    override fun injectApplicationLifecycle(
        context: Context,
        lifecycles: MutableList<ApplicationLifecycleCallbacks>
    ) {
    }

    override fun injectActivityLifecycle(
        context: Context,
        lifecycles: MutableList<Application.ActivityLifecycleCallbacks>
    ) {
        lifecycles.add(WalletConnectActivityLifecycle())
    }

    override fun injectFragmentLifecycle(
        context: Context,
        lifecycles: MutableList<FragmentManager.FragmentLifecycleCallbacks>
    ) {
    }
}