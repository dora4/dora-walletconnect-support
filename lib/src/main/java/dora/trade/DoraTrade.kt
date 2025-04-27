package dora.trade

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.annotation.ColorInt
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

    /**
     * 冷钱包支付的回调接口。
     */
    private var payListener: PayListener? = null

    /**
     * 应用的基本信息。
     */
    private lateinit var appMetaData: Core.Model.AppMetaData

    /**
     * SDK默认的主题色，天蓝。
     */
    private const val SDK_THEME_COLOR = "#389CFF"

    /**
     * 主题色，用于弹窗的主色调。
     */
    private var themeColor: Int = Color.parseColor(SDK_THEME_COLOR)

    /**
     * 平台的ERC20钱包地址。
     */
    private const val ERC20_ADDRESS = "0xcBa852Ef29a43a7542B88F60C999eD9cB66f6000"

    /**
     * 成功。
     */
    private const val STATUS_CODE_OK = 0

    /**
     * 访问密钥无效。
     */
    private const val STATUS_CODE_ACCESS_KEY_IS_INVALID = -1

    /**
     * 支付调用失败。
     */
    private const val STATUS_CODE_PAYMENT_ERROR = -2

    /**
     * 单笔额度超额。
     */
    private const val STATUS_CODE_SINGLE_TRANSACTION_LIMIT = -3

    /**
     * 月额度超额。
     */
    private const val STATUS_CODE_MONTHLY_LIMIT = -4

    /**
     * 不支持的chainId。
     */
    private const val STATUS_CODE_UNSUPPORTED_CHAIN_ID = -5

    /**
     * 代币价格获取失败。
     */
    private const val STATUS_CODE_FAILED_TO_FETCH_TOKEN_PRICE = -6

    /**
     * 访问密钥已过期。
     */
    private const val STATUS_CODE_ACCESS_KEY_IS_EXPIRED = -7

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
        @ColorInt themeColor: Int? = Color.parseColor(SDK_THEME_COLOR),
        listener: PayListener? = null
    ) {
        System.loadLibrary("pay-core")
        appMetaData = nativeInitWalletConnect(application, appName, appDesc, domainUrl, supportChains, {
             Log.e("initWalletConnect", it.toString())
        }, {
             Log.e("initWalletConnect", it.toString())
        }, {})
        if (themeColor != null) {
            this.themeColor = themeColor
        }
        listener?.let { setPayListener(it) }
    }

    /**
     * 设置主题色，如用于换肤等场景。
     */
    fun setThemeColor(@ColorInt color: Int) {
        this.themeColor = color
    }

    /**
     * 设置支付监听器。
     */
    fun setPayListener(listener: PayListener) {
        this.payListener = listener
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
     * 断开与冷钱包的连接。
     */
    fun disconnectWallet() {
        disconnectWallet(onSuccess = {}, onError = {})
    }

    /**
     * 断开与冷钱包的连接。
     */
    fun disconnectWallet(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        if (Web3Modal.getAccount() != null) {
            Web3Modal.disconnect(onSuccess, onError)
        }
    }

    /**
     * 捐赠，无需支付结果的回调监听。
     * @since 1.75
     */
    fun donateProxy(
        context: Context,
        accessKey: String,
        secretKey: String,
        orderTitle: String,
        goodsDesc: String,
        value: Double
    ) {
        val (gasLimit, gasPrice) = nativeGetGasParameters()
        pay(
            context,
            accessKey,
            secretKey,
            orderTitle,
            goodsDesc,
            ERC20_ADDRESS,
            value,
            gasLimit,
            gasPrice,
            object : OrderListener {
                override fun onPrintOrder(
                    orderId: String,
                    chain: Modal.Model.Chain,
                    value: Double
                ) {
                }
            }
        )
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
        value: Double
    ) {
        val (gasLimit, gasPrice) = nativeGetGasParameters()
        pay(
            context,
            accessKey,
            secretKey,
            orderTitle,
            goodsDesc,
            account,
            value,
            gasLimit,
            gasPrice,
            object : OrderListener {
                override fun onPrintOrder(
                    orderId: String,
                    chain: Modal.Model.Chain,
                    value: Double
                ) {
                }
            }
        )
    }

    /**
     * 开始支付，使用默认的gasLimit和gasPrice，基础版访问密钥使用它。
     * @since 1.46
     */
    fun payProxy(
        context: Context,
        accessKey: String,
        secretKey: String,
        orderTitle: String,
        goodsDesc: String,
        value: Double,
        orderListener: OrderListener
    ) {
        val (gasLimit, gasPrice) = nativeGetGasParameters()
        pay(
            context,
            accessKey,
            secretKey,
            orderTitle,
            goodsDesc,
            ERC20_ADDRESS,
            value,
            gasLimit,
            gasPrice,
            orderListener
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
        value: Double,
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
            value,
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
        value: Double,
        gasLimit: String,
        gasPrice: String,
        orderListener: OrderListener
    ) {
        if (payListener == null) throw PaymentException("No PayListener is set.")
        DoraAlertDialog(context).show("$goodsDesc\n\n${context.getString(R.string.dorafund_provides_technical_support)}") {
            title(orderTitle)
            themeColor(themeColor)
            positiveButton(context.getString(R.string.pay))
            positiveListener {
                Web3Modal.getAccount()?.let { session ->
                    sendTransactionRequest(context, accessKey, secretKey, session.address, account,
                        PayUtils.convertToHexWei(value), gasLimit, gasPrice,
                        onSuccess = {
                            if (it is SentRequestResult.WalletConnect) {
                                ToastUtils.showShort(R.string.please_complete_the_payment_in_the_wallet)
                                orderListener.onPrintOrder("order${it.requestId}", session.chain, value)
                            }
                        },
                        onError = {
                            ToastUtils.showShort(R.string.payment_failed)
                            Log.e("sendTransactionRequest", it.toString())
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
                STATUS_CODE_OK -> {  // 成功
                    Log.i("sendTransactionRequest", "OK.")
                }
                STATUS_CODE_ACCESS_KEY_IS_INVALID -> { // 访问密钥无效
                    Log.e("sendTransactionRequest", "The access key is invalid.")
                }
                STATUS_CODE_PAYMENT_ERROR -> { // 支付调用失败
                    Log.e("sendTransactionRequest", "Payment error, please try again.")
                }
                STATUS_CODE_SINGLE_TRANSACTION_LIMIT -> { // 单笔额度超额
                    Log.e("sendTransactionRequest", "Single transaction limit exceeded.")
                }
                STATUS_CODE_MONTHLY_LIMIT -> { // 月额度超额
                    Log.e("sendTransactionRequest", "Monthly limit exceeded.")
                }
                STATUS_CODE_UNSUPPORTED_CHAIN_ID -> { // 不支持的chainId
                    Log.e("sendTransactionRequest", "Unsupported chainId.")
                }
                STATUS_CODE_FAILED_TO_FETCH_TOKEN_PRICE -> { // 代币价格获取失败
                    Log.e("sendTransactionRequest", "Failed to fetch token price.")
                }
                STATUS_CODE_ACCESS_KEY_IS_EXPIRED -> { // 访问密钥已过期
                    Log.e("sendTransactionRequest", "The access key is expired.")
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

    /**
     * 调用支付时，用于生成订单。
     */
    interface OrderListener {

        /**
         * 生成该笔订单的交易订单号。
         */
        fun onPrintOrder(orderId: String, chain: Modal.Model.Chain, value: Double)
    }

    /**
     * 冷钱包支付的回调接口。
     */
    interface PayListener {

        /**
         * 转账消息准备上链。
         */
        fun onSendTransactionToBlockchain(orderId: String, transactionHash: String)

        /**
         * 支付失败。
         */
        fun onPayFailure(orderId: String, msg: String)
    }
}
