package dora.trade

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.util.Log
import com.walletconnect.android.Core
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.client.models.request.SentRequestResult
import dora.lifecycle.walletconnect.R
import dora.trade.activity.WalletConnectActivity
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
        appMetaData = nativeInitWalletConnect(application, appName, appDesc, domainUrl, supportChains, {
             Log.e("initWalletConnect", it.toString())
        }, {
             Log.e("initWalletConnect", it.toString())
        }, {})
        listener?.let { setPayListener(it) }
    }

    /**
     * 设置支付监听器。
     */
    fun setPayListener(listener: PayListener) {
        // 保存 listener 引用
        payListener = listener
        initPayListener()
    }

    /**
     * 初始化钱包连接配置。
     */
    private external fun nativeInitWalletConnect(
        application: Application,
        appName: String,
        appDesc: String,
        domainUrl: String,
        supportChains: Array<Modal.Model.Chain>,
        onErrorCore: (Core.Model.Error) -> Unit,
        onErrorModal: (Modal.Model.Error) -> Unit,
        onSuccessModal: () -> Unit
    ) : Core.Model.AppMetaData

    /**
     * 初始化支付监听器。
     */
    private fun initPayListener() {
        val delegate = ModalDelegateProxy(payListener)
        Web3Modal.setDelegate(delegate)
    }

    /**
     * 获取默认的Gas参数。
     */
    private external fun nativeGetGasParameters(): Array<String>

    /**
     * 与冷钱包建立连接。
     */
    fun connectWallet(context: Context) {
        IntentUtils.startActivity(context, WalletConnectActivity::class.java)
    }

    /**
     * 捐赠，无需支付结果的回调监听。
     */
    fun donate(
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
            gasPrice,
            object : OrderListener {
                override fun onPrintOrder(orderId: String) {
                }
            }
        )
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
        tokenValue: Double,
        orderListener: OrderListener
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
            gasPrice,
            orderListener
        )
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
        gasPrice: String,
        orderListener: OrderListener
    ) {
        DoraAlertDialog(context).show("$goodsDesc\n\n${context.getString(R.string.dorafund_provides_technical_support)}") {
            title(orderTitle)
            themeColor(Color.parseColor("#389CFF"))
            positiveButton(context.getString(R.string.pay))
            positiveListener {
                Web3Modal.getAccount()?.let { session ->
                    sendTransactionRequest(context, accessKey, secretKey, session.address, account,
                        PayUtils.convertToHexWei(tokenValue), gasLimit, gasPrice,
                        onSuccess = {
                            if (it is SentRequestResult.WalletConnect) {
                                ToastUtils.showShort(R.string.please_complete_the_payment_in_the_wallet)
                                orderListener.onPrintOrder("order${it.requestId}")
                            }
                        },
                        onError = {
                            ToastUtils.showShort(R.string.payment_failed)
                        }
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
        onSuccess: (SentRequestResult) -> Unit,
        onError: (Throwable) -> Unit
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
                    Log.i("sendTransactionRequest", "OK.")
                }
                -1 -> {
                    Log.e("sendTransactionRequest", "The access key is invalid.")
                }
                -2 -> {
                    Log.e("sendTransactionRequest", "Payment error, please try again.")
                }
            }
        } catch (e: Exception) {
            onError(e)
        }
    }

    /**
     * 底层处理发送交易请求。
     */
    private external fun nativeSendTransactionRequest(
        context: Context,
        accessKey: String,
        secretKey: String,
        from: String,
        to: String,
        value: String,
        gasLimit: String,
        gasPrice: String,
        onSuccess: (SentRequestResult) -> Unit,
        onError: (Throwable) -> Unit
    ): Int

    interface OrderListener {

        /**
         * 生成该笔订单的交易订单号。
         */
        fun onPrintOrder(orderId: String)
    }

    interface PayListener {

        /**
         * 转账消息上链，确认中。
         */
        fun onSendTransactionToBlockchain(orderId: String, transactionHash: String)

        /**
         * 支付失败。
         */
        fun onPayFailure(orderId: String, transactionHash: String)
    }
}