package dora.trade

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.util.Log
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.client.models.request.SentRequestResult
import dora.lifecycle.walletconnect.R
import dora.trade.activity.WalletConnectActivity
import dora.trade.callback.OnErrorCallback
import dora.trade.callback.OnSuccessCallback
import dora.util.IntentUtils
import dora.util.ToastUtils
import dora.widget.DoraAlertDialog


/**
 * https://dorafund.com
 */
object DoraTrade {

    private var payListener: PayListener? = null
    private lateinit var appMetaData: Core.Model.AppMetaData

    /**
     * 朵拉支付初始化应用元信息。
     */
    @JvmOverloads
    fun init(
        application: Application,
        appName: String,
        appDesc: String,
        domainUrl: String,
        supportChains: Array<Modal.Model.Chain>,
        listener: PayListener? = null
    ) {
        System.loadLibrary("pay-core")
        initWalletConnect(application, appName, appDesc, domainUrl, supportChains)
        listener?.let { setPayListener(it) }
    }

    fun setPayListener(listener: PayListener) {
        // 保存 listener 引用
        payListener = listener
        initPayListener()
    }

    private external fun nativeInitWalletConnect(
        application: Application,
        appName: String,
        appDesc: String,
        domainUrl: String,
        supportChains: Array<Modal.Model.Chain>
    ): Array<String>

    /**
     * 初始化钱包连接配置。
     */
    private fun initWalletConnect(
        application: Application,
        appName: String,
        appDesc: String,
        domainUrl: String,
        supportChains: Array<Modal.Model.Chain>
    ){
        val (relayUrl, serverUri, icon, redirect) = nativeInitWalletConnect(application, appName, appDesc, domainUrl, supportChains)
        appMetaData = Core.Model.AppMetaData(
            name = appName,
            description = appDesc,
            url = domainUrl,
            icons = listOf(icon),
            redirect = redirect
        )
        CoreClient.initialize(
            relayServerUrl = serverUri,
            connectionType = ConnectionType.AUTOMATIC,
            application = application,
            metaData = appMetaData,
        ) {
        }
        Web3Modal.initialize(
            Modal.Params.Init(core = CoreClient,
                recommendedWalletsIds = listOf(
                    "c03dfee351b6fcc421b4494ea33b9d4b92a984f87aa76d1663bb28705e95034a",//Uniswap
                    "1ae92b26df02f0abca6304df07debccd18262fdf5fe82daa81593582dac9a369",//Rainbow
                    "20459438007b75f4f4acb98bf29aa3b800550309646d375da5fd4aac6c2a2c66",//TokenPocket
                    "4622a2b2d6af1c9844944291e5e7351a6aa24cd7b23099efac1b2fd875da31a0",//TrustWallet
                    "ef333840daf915aafdc4a004525502d6d49d77bd9c65e0642dbaefb3c2893bef",//ImToken
                    "c57ca95b47569778a828d19178114f4db188b89b763c899ba0be274e97267d96",//MetaMask
                    "19177a98252e07ddfc9af2083ba8e07ef627cb6103467ffebb3f8f4205fd7927",//Ledger
                )
            ),
            onSuccess = {},
            onError = { error -> }
        )
        Web3Modal.setChains(supportChains.asList())
    }

    /**
     * 初始化支付监听器。
     */
    private fun initPayListener() {
        val delegate = ModalDelegateProxy(payListener)
        Web3Modal.setDelegate(delegate)
    }

    private external fun nativeGetGasParameters(): Array<String>

    /**
     * 与冷钱包建立连接。
     */
    fun connectWallet(context: Context) {
        IntentUtils.startActivity(context, WalletConnectActivity::class.java)
    }

    /**
     * 开始支付，使用默认的gasLimit和gasPrice。
     */
    fun pay(
        context: Context,
        accessKey: String,
        secretKey: String,
        orderTitle: String,
        goodsDesc: String,
        account: String,
        tokenValue: Double
    ) {
        val (gasLimit, gasPrice) = nativeGetGasParameters()
        pay(
            context,
            accessKey,
            secretKey,
            orderTitle,
            goodsDesc,
            account,
            tokenValue,
            gasLimit,
            gasPrice
        )
    }

    private val onSuccessCallback: OnSuccessCallback = object : OnSuccessCallback {
        override fun onSuccess(result: SentRequestResult) {
            ToastUtils.showLong(R.string.payment_successful)
            Log.d("sendTransactionRequest", result.toString())
        }
    }

    private val onErrorCallback: OnErrorCallback = object : OnErrorCallback {
        override fun onError(error: Throwable) {
            ToastUtils.showLong(R.string.payment_failed)
            Log.d("sendTransactionRequest", error.toString())
        }
    }

    /**
     * 开始支付。
     */
    fun pay(
        context: Context,
        accessKey: String,
        secretKey: String,
        orderTitle: String,
        goodsDesc: String,
        account: String,
        tokenValue: Double,
        gasLimit: String,
        gasPrice: String
    ) {
        DoraAlertDialog(context).show(goodsDesc+"\nPayment request is from ${appMetaData.name}(${appMetaData.url}).Not related to https://dorafund.com.") {
            title(orderTitle)
            themeColor(Color.BLACK)
            positiveButton(context.getString(R.string.pay))
            positiveListener {
                Web3Modal.getAccount()?.let { session ->
                    sendTransactionRequest(context, accessKey, secretKey, session.address, account,
                        PayUtils.convertToHexWei(tokenValue), gasLimit, gasPrice,
                        onSuccess = onSuccessCallback,
                        onError = onErrorCallback
                    )
                }
            }
            negativeListener {
                ToastUtils.showShort(R.string.cancel_pay)
            }
        }
    }

    /**
     * 发送交易请求。
     */
    private fun sendTransactionRequest(
        context: Context,
        accessKey: String,
        secretKey: String,
        from: String,
        to: String,
        value: String,
        gasLimit: String,
        gasPrice: String,
        onSuccess: OnSuccessCallback,
        onError: OnErrorCallback
    ) {
        try {
            if (accessKey == "") {
                ToastUtils.showShort("The access key is null")
                return
            }
            if (secretKey == "") {
                ToastUtils.showShort("The secret key is null")
                return
            }
            if (to == "") {
                ToastUtils.showShort("Account is null")
                return
            }
            val status = nativeSendTransactionRequest(context, accessKey, secretKey, from, to, value, gasLimit, gasPrice, onSuccess, onError)
            when (status) {
                0 -> {
                    Log.e("sendTransactionRequest", "OK.")
                }
                -1 -> {
                    Log.e("sendTransactionRequest", "The access key is invalid.")
                }
                -2 -> {
                    Log.e("sendTransactionRequest", "Payment error, please try again.")
                }
            }
        } catch (e: Exception) {
            onError.onError(e)
        }
    }

    private external fun nativeSendTransactionRequest(
        context: Context,
        accessKey: String,
        secretKey: String,
        from: String,
        to: String,
        value: String,
        gasLimit: String,
        gasPrice: String,
        onSuccess: OnSuccessCallback,
        onError: OnErrorCallback
    ): Int

    fun onPaySuccess() {
        payListener?.onPaySuccess()
    }

    fun onPayFailure() {
        payListener?.onPayFailure()
    }

    interface PayListener {

        /**
         * 订单支付成功的回调。
         */
        fun onPaySuccess()

        /**
         * 订单支付失败的回调。
         */
        fun onPayFailure()
    }
}