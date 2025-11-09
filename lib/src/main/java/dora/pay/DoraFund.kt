package dora.pay

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.ColorInt
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.walletconnect.android.Core
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.client.models.request.SentRequestResult
import de.blinkt.openvpn.core.OpenVPNService
import dora.lifecycle.walletconnect.R
import dora.pay.activity.WalletConnectActivity
import dora.pay.token.Token
import dora.pay.wallet.WalletContract
import dora.pay.wallet.WalletResult
import dora.util.IntentUtils
import dora.util.ToastUtils
import dora.widget.DoraAlertDialog
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

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
    const val ERC20_ADDRESS = "0xcBa852Ef29a43a7542B88F60C999eD9cB66f6000"

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
     * Unsupported ERC20 token.
     * @since 2.1
     */
    private const val STATUS_CODE_UNSUPPORTED_ERC20_TOKEN = -8

    /**
     * ERC20 token symbol does not match the expected value during validation.
     * @since 2.1
     */
    private const val STATUS_CODE_TOKEN_SYMBOL_MISMATCH = -9

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
        appMetaData =
            nativeInitWalletConnect(application, appName, appDesc, domainUrl, supportChains, {
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
    ): Core.Model.AppMetaData

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
        replaceWith = ReplaceWith("nativeGetGasParametersEIP1559(context,transferType)"),
        level = DeprecationLevel.WARNING
    )
    private external fun nativeGetGasParameters(): Array<String>

    /**
     * Get recommended Gas parameters from native layer.
     * @property transferType 0=Native，1=ERC20
     * @since 2.1
     */
    private external fun nativeGetGasParametersEIP1559(
        context: Context,
        transferType: Int
    ): Array<String>

    /**
     * Check if the wallet is connected.
     * @since 2.0
     */
    fun isWalletConnected(): Boolean {
        return Web3Modal.getAccount() != null
    }

    /**
     * Get the address of the currently selected wallet.
     * @since 2.0
     */
    fun getCurrentAddress(): String {
        if (!isWalletConnected()) {
            return ""
        }
        return Web3Modal.getAccount()?.address!!
    }

    /**
     * Get the currently selected chain.
     * @since 2.0
     */
    fun getCurrentChain(): Modal.Model.Chain? {
        if (!isWalletConnected()) {
            return null
        }
        return Web3Modal.getAccount()?.chain
    }

    /**
     * Connect to a cold wallet.
     * @since 2.0
     */
    fun connectWallet(context: Context) {
        IntentUtils.startActivity(context, WalletConnectActivity::class.java)
    }

    /**
     * Connect to a cold wallet and pay immediately after connection.
     * @since 2.0
     */
    fun connectWallet(activity: Activity, requestCode: Int) {
        IntentUtils.startActivityForResult(activity, WalletConnectActivity::class.java, requestCode)
    }

    /**
     * Connect to a cold wallet from a Fragment.
     * @since 2.1
     */
    fun connectWallet(fragment: Fragment, onResult: (WalletResult?) -> Unit) {
        val connectWalletLauncher =
            fragment.registerForActivityResult(WalletContract()) { result ->
                onResult(result)
            }
        connectWalletLauncher.launch(Unit)
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
        replaceWith = ReplaceWith(
            "payProxy(context,accessKey,secretKey,orderTitle," +
                    "goodsDesc,value,token,orderListener)"
        ),
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
            val (gasLimit, gasPrice, maxFeePerGas, maxPriorityFeePerGas) = nativeGetGasParametersEIP1559(
                context,
                TRANSFER_TYPE_NATIVE
            )
            val chain = getCurrentChain()
            if (chain == null) {
                ToastUtils.showShort(R.string.please_connect_wallet_first)
                return
            }
            if (chain.rpcUrl == null) {
                ToastUtils.showShort(R.string.rpc_url_is_missing)
                return
            }
            Observable.fromCallable {
                val isEIP1559Chain = when (chain) {
                    EVMChains.ETHEREUM,
                    EVMChains.POLYGON,
                    EVMChains.AVALANCHE,
                    EVMChains.ARBITRUM,
                    EVMChains.OPTIMISM -> true
                    else -> false
                }
                if (isEIP1559Chain && PayUtils.isEIP1559Supported(chain.rpcUrl!!)) {
                    "EIP1559"
                } else {
                    "LEGACY"
                }
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ type ->
                    when (type) {
                        "EIP1559" -> {
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
                        }
                        "LEGACY" -> {
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
                    }
                }, { error ->
                    error.printStackTrace()
                    ToastUtils.showShort(R.string.payment_failed)
                })

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
        replaceWith = ReplaceWith(
            "pay(context,accessKey,secretKey,orderTitle,goodsDesc," +
                    "account,value,token,orderListener)"
        ),
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
            val (gasLimit, gasPrice, maxFeePerGas, maxPriorityFeePerGas) = nativeGetGasParametersEIP1559(
                context,
                TRANSFER_TYPE_NATIVE
            )
            val chain = getCurrentChain()
            if (chain == null) {
                ToastUtils.showShort(R.string.please_connect_wallet_first)
                return
            }
            if (chain.rpcUrl == null) {
                ToastUtils.showShort(R.string.rpc_url_is_missing)
                return
            }
            Observable.fromCallable {
                val isEIP1559Chain = when (chain) {
                    EVMChains.ETHEREUM,
                    EVMChains.POLYGON,
                    EVMChains.AVALANCHE,
                    EVMChains.ARBITRUM,
                    EVMChains.OPTIMISM -> true
                    else -> false
                }
                if (isEIP1559Chain && PayUtils.isEIP1559Supported(chain.rpcUrl!!)) {
                    "EIP1559"
                } else {
                    "LEGACY"
                }
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ type ->
                    when (type) {
                        "EIP1559" -> {
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
                        }
                        "LEGACY" -> {
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
                    }
                }, { error ->
                    error.printStackTrace()
                    ToastUtils.showShort(R.string.payment_failed)
                })

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
        replaceWith = ReplaceWith(
            "pay(context,accessKey,secretKey,orderTitle,goodsDesc," +
                    "account,value,token,orderListener)"
        ),
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
        DoraAlertDialog(context).show(
            "$goodsDesc\n\n${
                ContextCompat.getString(
                    context,
                    R.string.dorafund_provides_technical_support
                )
            }"
        ) {
            title(orderTitle)
            themeColor(themeColor)
            positiveButton(ContextCompat.getString(context, R.string.pay))
            positiveListener {
                Web3Modal.getAccount()?.let { session ->
                    sendTransactionRequest(context, accessKey, secretKey, session.address, account,
                        PayUtils.convertToHexWei(value), gasLimit, gasPrice,
                        onSuccess = {
                            if (it is SentRequestResult.WalletConnect) {
                                ToastUtils.showShort(R.string.please_complete_the_payment_in_the_wallet)
                                orderListener.onPrintOrder(
                                    "order${it.requestId}",
                                    session.chain,
                                    value
                                )
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
        DoraAlertDialog(context).show(
            "$goodsDesc\n\n${
                ContextCompat.getString(
                    context,
                    R.string.dorafund_provides_technical_support
                )
            }"
        ) {
            title(orderTitle)
            themeColor(themeColor)
            positiveButton(ContextCompat.getString(context, R.string.pay))
            positiveListener {
                Web3Modal.getAccount()?.let { session ->
                    sendNativeTransactionRequest(context,
                        accessKey,
                        secretKey,
                        session.address,
                        account,
                        PayUtils.convertToHexWei(value),
                        false,
                        gasLimit,
                        gasPrice,
                        "",
                        "",
                        onSuccess = {
                            if (it is SentRequestResult.WalletConnect) {
                                ToastUtils.showShort(R.string.please_complete_the_payment_in_the_wallet)
                                orderListener.onPrintOrder(
                                    "order${it.requestId}",
                                    session.chain,
                                    value
                                )
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
        DoraAlertDialog(context).show(
            "$goodsDesc\n\n${
                ContextCompat.getString(
                    context,
                    R.string.dorafund_provides_technical_support
                )
            }"
        ) {
            title(orderTitle)
            themeColor(themeColor)
            positiveButton(ContextCompat.getString(context, R.string.pay))
            positiveListener {
                Web3Modal.getAccount()?.let { session ->
                    sendNativeTransactionRequest(context,
                        accessKey,
                        secretKey,
                        session.address,
                        account,
                        PayUtils.convertToHexWei(value),
                        true,
                        gasLimit,
                        "",
                        maxFeePerGas,
                        maxPriorityFeePerGas,
                        onSuccess = {
                            if (it is SentRequestResult.WalletConnect) {
                                ToastUtils.showShort(R.string.please_complete_the_payment_in_the_wallet)
                                orderListener.onPrintOrder(
                                    "order${it.requestId}",
                                    session.chain,
                                    value
                                )
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
        val (gasLimit, gasPrice, maxFeePerGas, maxPriorityFeePerGas) = nativeGetGasParametersEIP1559(
            context,
            TRANSFER_TYPE_ERC20
        )
        val chain = getCurrentChain()
        if (chain == null) {
            ToastUtils.showShort(R.string.please_connect_wallet_first)
            return
        }
        if (chain.rpcUrl == null) {
            ToastUtils.showShort(R.string.rpc_url_is_missing)
            return
        }
        if (chain.id != token.chain.id) {
            ToastUtils.showShort(R.string.please_switch_correct_chain_to_pay_with_tokens)
            return
        }

        val isEIP1559Chain = when (chain) {
            EVMChains.ETHEREUM,
            EVMChains.POLYGON,
            EVMChains.AVALANCHE,
            EVMChains.ARBITRUM,
            EVMChains.OPTIMISM -> true
            else -> false
        }

        if (payListener == null) throw PaymentException("No PayListener is set.")
        DoraAlertDialog(context).show(
            "$goodsDesc\n\n${
                ContextCompat.getString(
                    context,
                    R.string.dorafund_provides_technical_support
                )
            }"
        ) {
            title(orderTitle)
            themeColor(themeColor)
            positiveButton(ContextCompat.getString(context, R.string.pay))
            positiveListener {
                Web3Modal.getAccount()?.let { session ->
                    Observable.fromCallable {
                        if (isEIP1559Chain && PayUtils.isEIP1559Supported(chain.rpcUrl!!)) {
                            nativeSendERC20TransactionRequest(
                                context,
                                accessKey,
                                secretKey,
                                session.address,
                                toAddress,
                                PayUtils.convertToHexWeiABI(amount, token.decimals),
                                token.symbol,
                                token.contractAddress,
                                true,
                                gasLimit,
                                "",
                                maxFeePerGas,
                                maxPriorityFeePerGas,
                                onSuccess = { result ->
                                    if (result is SentRequestResult.WalletConnect) {
                                        ToastUtils.showShort(R.string.please_complete_the_payment_in_the_wallet)
                                        orderListener.onPrintOrder(
                                            "order${result.requestId}",
                                            session.chain,
                                            amount
                                        )
                                    }
                                },
                                onError = { error ->
                                    ToastUtils.showShort(R.string.payment_failed)
                                    Log.e("ERC20Payment", error.toString())
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
                                false,
                                gasLimit,
                                gasPrice,
                                "", "",
                                onSuccess = { result ->
                                    if (result is SentRequestResult.WalletConnect) {
                                        ToastUtils.showShort(R.string.please_complete_the_payment_in_the_wallet)
                                        orderListener.onPrintOrder(
                                            "order${result.requestId}",
                                            session.chain,
                                            amount
                                        )
                                    }
                                },
                                onError = { error ->
                                    ToastUtils.showShort(R.string.payment_failed)
                                    Log.e("ERC20Payment", error.toString())
                                }
                            )
                        }
                    }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ status ->
                            when (status) {
                                STATUS_CODE_OK -> Log.i("sendERC20TransactionRequest", "OK.")
                                STATUS_CODE_ACCESS_KEY_IS_INVALID -> Log.e("sendERC20TransactionRequest", "The access key is invalid.")
                                STATUS_CODE_PAYMENT_ERROR -> Log.e("sendERC20TransactionRequest", "Payment error, please try again.")
                                STATUS_CODE_SINGLE_TRANSACTION_LIMIT -> Log.e("sendERC20TransactionRequest", "Single transaction limit exceeded.")
                                STATUS_CODE_MONTHLY_LIMIT -> Log.e("sendERC20TransactionRequest", "Monthly limit exceeded.")
                                STATUS_CODE_UNSUPPORTED_CHAIN_ID -> Log.e("sendERC20TransactionRequest", "Unsupported chainId.")
                                STATUS_CODE_FAILED_TO_FETCH_TOKEN_PRICE -> Log.e("sendERC20TransactionRequest", "Failed to fetch token price.")
                                STATUS_CODE_ACCESS_KEY_IS_EXPIRED -> Log.e("sendERC20TransactionRequest", "The access key is expired.")
                                STATUS_CODE_UNSUPPORTED_ERC20_TOKEN -> Log.e("sendERC20TransactionRequest", "Unsupported ERC20 token.")
                                STATUS_CODE_TOKEN_SYMBOL_MISMATCH -> Log.e("sendERC20TransactionRequest", "ERC20 token symbol does not match.")
                            }
                        }, { error ->
                            error.printStackTrace()
                            ToastUtils.showShort(R.string.payment_failed)
                        })
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
        replaceWith = ReplaceWith(
            "sendNativeTransactionRequest(context,accessKey,secretKey," +
                    "from,to,value,isEIP1559,gasLimit,gasPrice,maxFeePerGas,maxPriorityFeePerGas,onSuccess,onError)"
        ),
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
            val status = nativeSendTransactionRequest(
                context,
                accessKey,
                secretKey,
                from,
                to,
                value,
                gasLimit,
                gasPrice,
                onSuccess,
                onError
            )
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
        isEIP1559: Boolean,
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
            val status = nativeSendNativeTransactionRequest(
                context,
                accessKey,
                secretKey,
                from,
                to,
                value,
                isEIP1559,
                gasLimit,
                gasPrice,
                maxFeePerGas,
                maxPriorityFeePerGas,
                onSuccess,
                onError
            )
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
            var name = ContextCompat.getString(context, R.string.channel_name_background)
            var channel = NotificationChannel(
                OpenVPNService.NOTIFICATION_CHANNEL_BG_ID,
                name,
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description =
                    ContextCompat.getString(context, R.string.channel_description_background)
                enableLights(false)
                lightColor = Color.DKGRAY
            }
            notificationManager.createNotificationChannel(channel)

            name = ContextCompat.getString(context, R.string.channel_name_status)
            channel = NotificationChannel(
                OpenVPNService.NOTIFICATION_CHANNEL_NEWSTATUS_ID,
                name,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = ContextCompat.getString(context, R.string.channel_description_status)
                enableLights(true)
                lightColor = Color.BLUE
            }
            notificationManager.createNotificationChannel(channel)

            name = ContextCompat.getString(context, R.string.channel_name_userreq)
            channel = NotificationChannel(
                OpenVPNService.NOTIFICATION_CHANNEL_USERREQ_ID,
                name,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = ContextCompat.getString(context, R.string.channel_description_userreq)
                enableVibration(true)
                lightColor = Color.CYAN
            }
            notificationManager.createNotificationChannel(channel)
        } else {
            val notification = NotificationCompat.Builder(context)
                .setContentTitle(ContextCompat.getString(context, R.string.channel_name_status))
                .setContentText(
                    ContextCompat.getString(
                        context,
                        R.string.channel_description_status
                    )
                )
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
        replaceWith = ReplaceWith(
            "nativeSendNativeTransactionRequest(context,accessKey," +
                    "secretKey,from,to,value,isEIP1559,gasLimit,gasPrice,maxFeePerGas,maxPriorityFeePerGas," +
                    "onSuccess,onError)"
        ),
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
        isEIP1559: Boolean,
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
        isEIP1559: Boolean,
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
