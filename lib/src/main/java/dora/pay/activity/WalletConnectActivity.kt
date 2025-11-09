package dora.pay.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.walletconnect.web3.modal.ui.Web3ModalView
import dora.lifecycle.walletconnect.R
import dora.pay.DoraFund
import dora.pay.wallet.WalletContract
import dora.util.StatusBarUtils

/**
 * @since 2.0
 */
class WalletConnectActivity : AppCompatActivity() {

    @SuppressLint("UnsafeIntentLaunch")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_connect)
        StatusBarUtils.setTransparencyStatusBar(this)
        findViewById<Web3ModalView>(R.id.web3Modal).setOnCloseModal {
            if (DoraFund.isWalletConnected()) {
                intent.putExtra(WalletContract.EXTRA_CHAIN_ID, DoraFund.getCurrentChain()?.id)
                intent.putExtra(WalletContract.EXTRA_CHAIN_NAME, DoraFund.getCurrentChain()?.chainName)
                intent.putExtra(WalletContract.EXTRA_ERC20_ADDRESS, DoraFund.getCurrentAddress())
                setResult(RESULT_OK, intent)
            } else {
                setResult(RESULT_CANCELED)
            }
            finish()
        }
    }
}