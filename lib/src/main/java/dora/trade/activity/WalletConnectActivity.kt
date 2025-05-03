package dora.trade.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.walletconnect.web3.modal.ui.Web3ModalView
import dora.lifecycle.walletconnect.R
import dora.util.StatusBarUtils

class WalletConnectActivity : AppCompatActivity() {

    @SuppressLint("UnsafeIntentLaunch")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_connect)
        StatusBarUtils.setTransparencyStatusBar(this)
        findViewById<Web3ModalView>(R.id.web3Modal).setOnCloseModal {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}