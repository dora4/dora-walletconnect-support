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
import androidx.core.app.NotificationCompat
import com.walletconnect.android.Core
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.client.models.request.SentRequestResult
import de.blinkt.openvpn.core.OpenVPNService
import dora.lifecycle.walletconnect.R
import dora.pay.activity.WalletConnectActivity
import dora.pay.token.Token
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
     * VPN notification ID.
     * @since 2.0
     */
    private const val NOTIFICATION_ID = 1001

    /**
     * Transfer.
     * @since 2.1
     */
    private const val TRANSFER_TYPE_NATIVE = 0

    /**
     * ERC20 Transfer.
     * @since 2.1
     */
    private const val TRANSFER_TYPE_ERC20 = 1

    /**
     * Initialize DoraFund with application metadata.
     *
     * @see EVMChains
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
    @Deprecated(
        message = "This method is deprecated, use the nativeGetGasParametersEIP1559() instead",
        replaceWith = ReplaceWith("nativeGetGasParametersEIP1559(transferType)"),
        level = DeprecationLevel.WARNING
    )
    private external fun nativeGetGasParameters(): Array<String>

    /**
     * Get recommended Gas parameters from native layer.
     * @property transferType 0=Native，1=ERC20
     * @since 2.1
     */
    private external fun nativeGetGasParametersEIP1559(context: Context, transferType: Int): Array<String>

    /**
     * Check if the wallet is connected.
     * @since 2.0
     */
    fun isWalletConnected() : Boolean {
        return Web3Modal.getAccount() != null
    }

    /**
     * Get the address of the currently selected wallet.
     * @since 2.0
     */
    fun getCurrentAddress() : String {
        if (!isWalletConnected()) {
            return ""
        }
        return Web3Modal.getAccount()?.address!!
    }

    /**
     * Get the currently selected chain.
     * @since 2.0
     */
    fun getCurrentChain() : Modal.Model.Chain? {
        if (!isWalletConnected()) {
            return null
        }
        return Web3Modal.getAccount()?.chain
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
    @Deprecated(
        message = "This method is deprecated.",
        level = DeprecationLevel.WARNING
    )
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
    @Deprecated(
        message = "This method is deprecated.",
        level = DeprecationLevel.WARNING
    )
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
    @Deprecated(
        message = "This method is deprecated, use the payProxy() instead",
        replaceWith = ReplaceWith("payProxy(context,accessKey,secretKey,orderTitle," +
                "goodsDesc,value,token,orderListener)"),
        level = DeprecationLevel.WARNING
    )
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
     * Proxy payment method for basic access keys.
     *
     * If [token] is not specified, performs a native transfer.
     * If [token] is specified, performs an ERC20 token transfer.
     * Funds are first sent to the official wallet.
     *
     * @param context The context used to show payment dialogs.
     * @param accessKey Basic access key for authorization.
     * @param secretKey Secret key paired with [accessKey].
     * @param orderTitle Title of the payment order.
     * @param goodsDesc Description of the goods or service.
     * @param value Amount to be transferred.
     * @param token Optional ERC20 token to transfer. If null, a native transfer is used.
     * @param orderListener Callback for generating transaction order IDs.
     * @since 2.1
     */
    fun payProxy(
        context: Context,
        accessKey: String,
        secretKey: String,
        orderTitle: String,
        goodsDesc: String,
        value: Double,
        token: Token? = null,
        orderListener: OrderListener
    ) {
        if (token == null) {
            val (gasLimit, gasPrice, maxFeePerGas, maxPriorityFeePerGas) = nativeGetGasParametersEIP1559(context, TRANSFER_TYPE_NATIVE)
            val chain = getCurrentChain()
            if (chain == null) {
                ToastUtils.showShort(context.getString(R.string.please_connect_wallet_first))
                return
            }
            // supports EIP-1559
            val isEIP1559Chain = when (chain) {
                EVMChains.ETHEREUM,
                EVMChains.POLYGON,
                EVMChains.ARBITRUM,
                EVMChains.OPTIMISM -> true
                else -> false
            }
            if (isEIP1559Chain) {
                // EIP-1559：use baseFee + priorityFee
                payEIP1559(
                    context,
                    accessKey,
                    secretKey,
                    orderTitle,
                    goodsDesc,
                    ERC20_ADDRESS,
                    value,
                    gasLimit,
                    maxFeePerGas,
                    maxPriorityFeePerGas,
                    orderListener
                )
            } else {
                payLegacy(
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
        } else {
            payERC20(
                context,
                accessKey,
                secretKey,
                orderTitle,
                goodsDesc,
                ERC20_ADDRESS,
                value,
                token,
                orderListener
            )
        }
    }

    /**
     * Start payment with default gasLimit and gasPrice.
     * @since 2.0
     */
    @Deprecated(
        message = "This method is deprecated, use the pay() instead",
        replaceWith = ReplaceWith("pay(context,accessKey,secretKey,orderTitle,goodsDesc," +
                "account,value,token,orderListener)"),
        level = DeprecationLevel.WARNING
    )
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
     * Direct payment method. This method is intended for Pro and Enterprise access keys only.
     *
     * - If [token] is not specified, a native transfer is performed.
     * - If [token] is specified, an ERC20 token transfer is performed.
     *
     * @param context The context used to show payment dialogs.
     * @param accessKey Pro/Enterprise access key for authorization.
     * @param secretKey Secret key paired with [accessKey].
     * @param orderTitle Title of the payment order.
     * @param goodsDesc Description of the goods or service.
     * @param account Recipient account address.
     * @param value Amount to be transferred.
     * @param token Optional ERC20 token to transfer. If null, a native transfer is used.
     * @param orderListener Callback for generating transaction order IDs.
     * @since 2.1
     */
    fun pay(
        context: Context,
        accessKey: String,
        secretKey: String,
        orderTitle: String,
        goodsDesc: String,
        account: String,
        value: Double,
        token: Token? = null,
        orderListener: OrderListener
    ) {
        if (token == null) {
            val (gasLimit, gasPrice, maxFeePerGas, maxPriorityFeePerGas) = nativeGetGasParametersEIP1559(context, TRANSFER_TYPE_NATIVE)
            val chain = getCurrentChain()
            if (chain == null) {
                ToastUtils.showShort(context.getString(R.string.please_connect_wallet_first))
                return
            }
            // supports EIP-1559
            val isEIP1559Chain = when (chain) {
                EVMChains.ETHEREUM,
                EVMChains.POLYGON,
                EVMChains.ARBITRUM,
                EVMChains.OPTIMISM -> true
                else -> false
            }
            if (isEIP1559Chain) {
                // EIP-1559：use baseFee + priorityFee
                payEIP1559(
                    context,
                    accessKey,
                    secretKey,
                    orderTitle,
                    goodsDesc,
                    account,
                    value,
                    gasLimit,
                    maxFeePerGas,
                    maxPriorityFeePerGas,
                    orderListener
                )
            } else {
                payLegacy(
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
        } else {
            payERC20(
                context,
                accessKey,
                secretKey,
                orderTitle,
                goodsDesc,
                account,
                value,
                token,
                orderListener
            )
        }
    }

    /**
     * Start payment.
     * @since 2.0
     */
    @Deprecated(
        message = "This method is deprecated, use the pay() instead",
        replaceWith = ReplaceWith("pay(context,accessKey,secretKey,orderTitle,goodsDesc," +
                "account,value,token,orderListener)"),
        level = DeprecationLevel.WARNING
    )
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
     * Performs a native transfer payment.
     * For chains that do not support EIP-1559, this method falls back to the legacy transaction format.
     * @since 2.1
     */
    private fun payLegacy(
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
                    sendNativeTransactionRequest(context, accessKey, secretKey, session.address, account,
                        PayUtils.convertToHexWei(value), gasLimit, gasPrice, "", "",
                        onSuccess = {
                            if (it is SentRequestResult.WalletConnect) {
                                ToastUtils.showShort(R.string.please_complete_the_payment_in_the_wallet)
                                orderListener.onPrintOrder("order${it.requestId}", session.chain, value)
                            }
                        },
                        onError = {
                            ToastUtils.showShort(R.string.payment_failed)
                            Log.e("sendNativeTransactionRequest", it.toString())
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
     * Performs a native transfer payment using the EIP-1559 transaction format.
     * @since 2.1
     */
    private fun payEIP1559(
        context: Context,
        accessKey: String,
        secretKey: String,
        orderTitle: String,
        goodsDesc: String,
        account: String,
        value: Double,
        gasLimit: String,
        maxFeePerGas: String,
        maxPriorityFeePerGas: String,
        orderListener: OrderListener
    ) {
        if (payListener == null) throw PaymentException("No PayListener is set.")
        DoraAlertDialog(context).show("$goodsDesc\n\n${context.getString(R.string.dorafund_provides_technical_support)}") {
            title(orderTitle)
            themeColor(themeColor)
            positiveButton(context.getString(R.string.pay))
            positiveListener {
                Web3Modal.getAccount()?.let { session ->
                    sendNativeTransactionRequest(context, accessKey, secretKey, session.address, account,
                        PayUtils.convertToHexWei(value), gasLimit, "", maxFeePerGas, maxPriorityFeePerGas,
                        onSuccess = {
                            if (it is SentRequestResult.WalletConnect) {
                                ToastUtils.showShort(R.string.please_complete_the_payment_in_the_wallet)
                                orderListener.onPrintOrder("order${it.requestId}", session.chain, value)
                            }
                        },
                        onError = {
                            ToastUtils.showShort(R.string.payment_failed)
                            Log.e("sendNativeTransactionRequest", it.toString())
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
     * ERC20 transfer payment.
     * @since 2.1
     */
    private fun payERC20(
        context: Context,
        accessKey: String,
        secretKey: String,
        orderTitle: String,
        goodsDesc: String,
        toAddress: String,
        amount: Double,
        token: Token,
        orderListener: OrderListener
    ) {
        val (gasLimit, gasPrice, maxFeePerGas, maxPriorityFeePerGas) = nativeGetGasParametersEIP1559(context, TRANSFER_TYPE_ERC20)
        val chain = getCurrentChain()
        if (chain == null) {
            ToastUtils.showShort(context.getString(R.string.please_connect_wallet_first))
            return
        }
        if (chain.id != token.chain.id) {
            ToastUtils.showShort(context.getString(R.string.please_switch_correct_chain_to_pay_with_tokens))
            return
        }
        val isEIP1559Chain = when (token.chain) {
            EVMChains.ETHEREUM,
            EVMChains.POLYGON,
            EVMChains.ARBITRUM,
            EVMChains.OPTIMISM -> true
            else -> false
        }
        if (payListener == null) throw PaymentException("No PayListener is set.")
        DoraAlertDialog(context).show(
            "$goodsDesc\n\n${context.getString(R.string.dorafund_provides_technical_support)}"
        ) {
            title(orderTitle)
            themeColor(themeColor)
            positiveButton(context.getString(R.string.pay))
            positiveListener {
                Web3Modal.getAccount()?.let { session ->
                    val status = if (isEIP1559Chain) {
                        nativeSendERC20TransactionRequest(
                            context,
                            accessKey,
                            secretKey,
                            session.address,
                            toAddress,
                            PayUtils.convertToHexWeiABI(amount, token.decimals),
                            token.symbol,
                            token.contractAddress,
                            gasLimit,
                            "",
                            maxFeePerGas,
                            maxPriorityFeePerGas,
                            onSuccess = {
                                if (it is SentRequestResult.WalletConnect) {
                                    ToastUtils.showShort(R.string.please_complete_the_payment_in_the_wallet)
                                    orderListener.onPrintOrder("order${it.requestId}", session.chain, amount)
                                }
                            },
                            onError = {
                                ToastUtils.showShort(R.string.payment_failed)
                                Log.e("ERC20Payment", it.toString())
                            }
                        )
                    } else {
                        nativeSendERC20TransactionRequest(
                            context,
                            accessKey,
                            secretKey,
                            session.address,
                            toAddress,
                            PayUtils.convertToHexWeiABI(amount, token.decimals),
                            token.symbol,
                            token.contractAddress,
                            gasLimit,
                            gasPrice,
                            "", "",
                            onSuccess = {
                                if (it is SentRequestResult.WalletConnect) {
                                    ToastUtils.showShort(R.string.please_complete_the_payment_in_the_wallet)
                                    orderListener.onPrintOrder("order${it.requestId}", session.chain, amount)
                                }
                            },
                            onError = {
                                ToastUtils.showShort(R.string.payment_failed)
                                Log.e("ERC20Payment", it.toString())
                            }
                        )
                    }
                    when (status) {
                        STATUS_CODE_OK -> {
                            Log.i("sendNativeTransactionRequest", "OK.")
                        }
                        STATUS_CODE_ACCESS_KEY_IS_INVALID -> {
                            Log.e("sendNativeTransactionRequest", "The access key is invalid.")
                        }
                        STATUS_CODE_PAYMENT_ERROR -> {
                            Log.e("sendNativeTransactionRequest", "Payment error, please try again.")
                        }
                        STATUS_CODE_SINGLE_TRANSACTION_LIMIT -> {
                            Log.e("sendNativeTransactionRequest", "Single transaction limit exceeded.")
                        }
                        STATUS_CODE_MONTHLY_LIMIT -> {
                            Log.e("sendNativeTransactionRequest", "Monthly limit exceeded.")
                        }
                        STATUS_CODE_UNSUPPORTED_CHAIN_ID -> {
                            Log.e("sendNativeTransactionRequest", "Unsupported chainId.")
                        }
                        STATUS_CODE_FAILED_TO_FETCH_TOKEN_PRICE -> {
                            Log.e("sendNativeTransactionRequest", "Failed to fetch token price.")
                        }
                        STATUS_CODE_ACCESS_KEY_IS_EXPIRED -> {
                            Log.e("sendNativeTransactionRequest", "The access key is expired.")
                        }
                    }
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
    @Deprecated(
        message = "This method is deprecated, use the sendNativeTransactionRequest() instead",
        replaceWith = ReplaceWith("sendNativeTransactionRequest(context,accessKey,secretKey," +
                "from,to,value,gasLimit,gasPrice,maxFeePerGas,maxPriorityFeePerGas,onSuccess,onError)"),
        level = DeprecationLevel.WARNING
    )
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
                STATUS_CODE_OK -> {
                    Log.i("sendTransactionRequest", "OK.")
                }
                STATUS_CODE_ACCESS_KEY_IS_INVALID -> {
                    Log.e("sendTransactionRequest", "The access key is invalid.")
                }
                STATUS_CODE_PAYMENT_ERROR -> {
                    Log.e("sendTransactionRequest", "Payment error, please try again.")
                }
                STATUS_CODE_SINGLE_TRANSACTION_LIMIT -> {
                    Log.e("sendTransactionRequest", "Single transaction limit exceeded.")
                }
                STATUS_CODE_MONTHLY_LIMIT -> {
                    Log.e("sendTransactionRequest", "Monthly limit exceeded.")
                }
                STATUS_CODE_UNSUPPORTED_CHAIN_ID -> {
                    Log.e("sendTransactionRequest", "Unsupported chainId.")
                }
                STATUS_CODE_FAILED_TO_FETCH_TOKEN_PRICE -> {
                    Log.e("sendTransactionRequest", "Failed to fetch token price.")
                }
                STATUS_CODE_ACCESS_KEY_IS_EXPIRED -> {
                    Log.e("sendTransactionRequest", "The access key is expired.")
                }
            }
        } catch (e: Exception) {
            onError(e)
        }
    }

    /**
     * Send native transaction request.
     * @since 2.1
     */
    private fun sendNativeTransactionRequest(
        context: Context,
        accessKey: String,
        secretKey: String,
        from: String,
        to: String,
        value: String,
        gasLimit: String,
        gasPrice: String,
        maxFeePerGas: String,
        maxPriorityFeePerGas: String,
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
            val status = nativeSendNativeTransactionRequest(context, accessKey, secretKey, from, to,
                value, gasLimit, gasPrice, maxFeePerGas, maxPriorityFeePerGas, onSuccess, onError)
            when (status) {
                STATUS_CODE_OK -> {
                    Log.i("sendNativeTransactionRequest", "OK.")
                }
                STATUS_CODE_ACCESS_KEY_IS_INVALID -> {
                    Log.e("sendNativeTransactionRequest", "The access key is invalid.")
                }
                STATUS_CODE_PAYMENT_ERROR -> {
                    Log.e("sendNativeTransactionRequest", "Payment error, please try again.")
                }
                STATUS_CODE_SINGLE_TRANSACTION_LIMIT -> {
                    Log.e("sendNativeTransactionRequest", "Single transaction limit exceeded.")
                }
                STATUS_CODE_MONTHLY_LIMIT -> {
                    Log.e("sendNativeTransactionRequest", "Monthly limit exceeded.")
                }
                STATUS_CODE_UNSUPPORTED_CHAIN_ID -> {
                    Log.e("sendNativeTransactionRequest", "Unsupported chainId.")
                }
                STATUS_CODE_FAILED_TO_FETCH_TOKEN_PRICE -> {
                    Log.e("sendNativeTransactionRequest", "Failed to fetch token price.")
                }
                STATUS_CODE_ACCESS_KEY_IS_EXPIRED -> {
                    Log.e("sendNativeTransactionRequest", "The access key is expired.")
                }
            }
        } catch (e: Exception) {
            onError(e)
        }
    }

    /**
     * Create VPN notification channels, compatible with all API levels.
     * @since 2.0
     */
    fun createNotificationChannels(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        } else {
            val notification = NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.channel_name_status))
                .setContentText(context.getString(R.string.channel_description_status))
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
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
    @Deprecated(
        message = "This method is deprecated, use the nativeSendNativeTransactionRequest() instead",
        replaceWith = ReplaceWith("nativeSendNativeTransactionRequest(context,accessKey," +
                "secretKey,from,to,value,gasLimit,gasPrice,maxFeePerGas,maxPriorityFeePerGas," +
                "onSuccess,onError)"),
        level = DeprecationLevel.WARNING
    )
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
     * Native layer handles sending native transaction requests.
     * @since 2.1
     */
    private external fun nativeSendNativeTransactionRequest(
        context: Context,
        accessKey: String,
        secretKey: String,
        from: String,
        to: String,
        value: String,
        gasLimit: String,
        gasPrice: String,
        maxFeePerGas: String,
        maxPriorityFeePerGas: String,
        onSuccess: (SentRequestResult) -> Unit,
        onError: (Throwable) -> Unit
    ): Int

    /**
     * Native layer handles sending ERC20 transaction requests.
     * @since 2.1
     */
    private external fun nativeSendERC20TransactionRequest(
        context: Context,
        accessKey: String,
        secretKey: String,
        from: String,
        to: String,
        value: String,
        tokenSymbol: String,
        contractAddress: String,
        gasLimit: String,
        gasPrice: String,
        maxFeePerGas: String,
        maxPriorityFeePerGas: String,
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
