package dora.pay

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import com.walletconnect.android.Core
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.client.models.request.SentRequestResult
import de.blinkt.openvpn.core.OpenVPNService
import dora.lifecycle.walletconnect.R
import dora.pay.activity.WalletConnectActivity
import dora.util.IntentUtils
import dora.util.ToastUtils
import dora.widget.DoraAlertDialog

/**
 * https://dorafund.com
 */
object DoraFund {

    /**
     * Callback interface for cold wallet payments.
     * @since 2.0
     */
    private var payListener: PayListener? = null

    /**
     * Basic application information.
     * @since 2.0
     */
    private lateinit var appMetaData: Core.Model.AppMetaData

    /**
     * Callback object for wallet events.
     * @since 2.0
     */
    private lateinit var modalDelegate: ModalDelegateProxy

    /**
     * Default SDK theme color, sky blue.
     * @since 2.0
     */
    private const val SDK_THEME_COLOR = "#389CFF"

    /**
     * Theme color, used as the primary color of dialogs/popups.
     * @since 2.0
     */
    private var themeColor: Int = Color.parseColor(SDK_THEME_COLOR)

    /**
     * Platform ERC20 wallet address.
     * @since 2.0
     */
    private const val ERC20_ADDRESS = "0xcBa852Ef29a43a7542B88F60C999eD9cB66f6000"

    /**
     * Success.
     * @since 2.0
     */
    private const val STATUS_CODE_OK = 0

    /**
     * Invalid access key.
     * @since 2.0
     */
    private const val STATUS_CODE_ACCESS_KEY_IS_INVALID = -1

    /**
     * Payment invocation failed.
     * @since 2.0
     */
    private const val STATUS_CODE_PAYMENT_ERROR = -2

    /**
     * Single transaction limit exceeded.
     * @since 2.0
     */
    private const val STATUS_CODE_SINGLE_TRANSACTION_LIMIT = -3

    /**
     * Monthly limit exceeded.
     * @since 2.0
     */
    private const val STATUS_CODE_MONTHLY_LIMIT = -4

    /**
     * Unsupported chainId.
     * @since 2.0
     */
    private const val STATUS_CODE_UNSUPPORTED_CHAIN_ID = -5

    /**
     * Failed to fetch token price.
     * @since 2.0
     */
    private const val STATUS_CODE_FAILED_TO_FETCH_TOKEN_PRICE = -6

    /**
     * Access key has expired.
     * @since 2.0
     */
    private const val STATUS_CODE_ACCESS_KEY_IS_EXPIRED = -7

    /**
     * Initialize DoraFund with application metadata.
     * @since 2.0
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
     * Set theme color, e.g., for skin-switching scenarios.
     * @since 2.0
     */
    fun setThemeColor(@ColorInt color: Int) {
        this.themeColor = color
    }

    /**
     * Get the current theme color.
     * @since 2.0
     */
    fun getThemeColor(): Int {
        return this.themeColor
    }

    /**
     * Set the payment listener.
     * @since 2.0
     */
    fun setPayListener(listener: PayListener) {
        this.payListener = listener
        initPayListener()
    }

    /**
     * Initialize WalletConnect configuration.
     * @since 2.0
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
     * Initialize the payment listener.
     * @since 2.0
     */
    private fun initPayListener() {
        modalDelegate = ModalDelegateProxy(payListener)
        Web3Modal.setDelegate(modalDelegate)
    }

    /**
     * Get recommended Gas parameters from native layer.
     * @since 2.0
     */
    private external fun nativeGetGasParameters(): Array<String>

    /**
     * Check if the wallet is connected.
     * @since 2.0
     */
    fun isWalletConnected() : Boolean {
        return Web3Modal.getAccount() != null
    }

    /**
     * Connect to cold wallet.
     * @since 2.0
     */
    fun connectWallet(context: Context) {
        IntentUtils.startActivity(context, WalletConnectActivity::class.java)
    }

    /**
     * Connect to cold wallet and pay immediately after connection.
     * @since 2.0
     */
    fun connectWallet(activity: Activity, requestCode: Int) {
        IntentUtils.startActivityForResult(activity, WalletConnectActivity::class.java, requestCode)
    }

    /**
     * Disconnect from cold wallet.
     * @since 2.0
     */
    fun disconnectWallet() {
        disconnectWallet(onSuccess = {}, onError = {})
    }

    /**
     * Disconnect from cold wallet.
     * @since 2.0
     */
    fun disconnectWallet(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        if (Web3Modal.getAccount() != null) {
            Web3Modal.disconnect(onSuccess, onError)
        }
    }

    /**
     * Donate, without requiring a payment result callback.
     * @since 2.0
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
     * Donate, without requiring a payment result callback.
     * @since 2.0
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
     * Start payment with default gasLimit and gasPrice.
     * For basic access keys, use this method.
     * @since 2.0
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
     * Start payment with default gasLimit and gasPrice.
     * @since 2.0
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
     * Start payment.
     * @since 2.0
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
     * Send transaction request.
     * @since 2.0
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
     * Create VPN notification channels.
     * @since 2.0
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannels(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var name = context.getString(R.string.channel_name_background)
        var channel = NotificationChannel(
            OpenVPNService.NOTIFICATION_CHANNEL_BG_ID,
            name,
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = context.getString(R.string.channel_description_background)
            enableLights(false)
            lightColor = Color.DKGRAY
        }
        notificationManager.createNotificationChannel(channel)
        name = context.getString(R.string.channel_name_status)
        channel = NotificationChannel(
            OpenVPNService.NOTIFICATION_CHANNEL_NEWSTATUS_ID,
            name,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.channel_description_status)
            enableLights(true)
            lightColor = Color.BLUE
        }
        notificationManager.createNotificationChannel(channel)
        name = context.getString(R.string.channel_name_userreq)
        channel = NotificationChannel(
            OpenVPNService.NOTIFICATION_CHANNEL_USERREQ_ID,
            name,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_description_userreq)
            enableVibration(true)
            lightColor = Color.CYAN
        }
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Connect to VPN.
     * @since 2.0
     */
    external fun connectVPN(context: Context, accessKey: String, secretKey: String)

    /**
     * Disconnect VPN.
     * @since 2.0
     */
    external fun disconnectVPN(context: Context)

    /**
     * Native layer handles sending transaction requests.
     * @since 2.0
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
     * When invoking payment, used to generate an order.
     * @since 2.0
     */
    interface OrderListener {

        /**
         * Generate transaction order ID for this payment.
         * @since 2.0
         */
        fun onPrintOrder(orderId: String, chain: Modal.Model.Chain, value: Double)
    }

    /**
     * Callback interface for cold wallet payments.
     * @since 2.0
     */
    interface PayListener {

        /**
         * Transfer message prepared for blockchain.
         * @since 2.0
         */
        fun onSendTransactionToBlockchain(orderId: String, transactionHash: String)

        /**
         * Payment failure.
         * @since 2.0
         */
        fun onPayFailure(orderId: String, msg: String)
    }
}
