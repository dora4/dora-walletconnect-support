package dora.pay.wallet

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import dora.pay.activity.WalletConnectActivity
import dora.util.IntentUtils

/**
 * @since 2.1
 */
class WalletContract : ActivityResultContract<Unit, WalletResult?>() {

    companion object {

        /**
         * @since 2.1
         */
        const val EXTRA_CHAIN_ID = "chainId"

        /**
         * @since 2.1
         */
        const val EXTRA_CHAIN_NAME = "chainName"

        /**
         * @since 2.1
         */
        const val EXTRA_ERC20_ADDRESS = "erc20Address"
    }

    override fun createIntent(
        context: Context,
        input: Unit
    ): Intent {
        return Intent(context, WalletConnectActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): WalletResult? {
        return if (resultCode == Activity.RESULT_OK && intent != null) {
            val chainId = IntentUtils.getStringExtra(intent, EXTRA_CHAIN_ID)
            val chainName = IntentUtils.getStringExtra(intent, EXTRA_CHAIN_NAME)
            val erc20Address = IntentUtils.getStringExtra(intent, EXTRA_ERC20_ADDRESS)
            WalletResult(chainId, chainName, erc20Address)
        } else {
            null
        }
    }
}