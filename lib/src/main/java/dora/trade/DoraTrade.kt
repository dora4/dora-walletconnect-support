package dora.trade

import android.content.Context
import com.walletconnect.web3.modal.client.Modal

/**
 * http://doratrade.com
 */
object DoraTrade {

    private var payListener: PayListener? = null

    /**
     * 朵拉支付初始化应用元信息。
     */
    @JvmStatic
    @JvmOverloads
    fun init(context: Context, appName: String, appDesc: String, domainUrl: String, supportChains: Array<Modal.Model.Chain>, listener: PayListener? = null) {
        System.loadLibrary("pay-core")
        initWalletConnect(context, appName, appDesc, domainUrl, supportChains)
        listener?.let { setPayListener(it) }
    }

    @JvmStatic
    fun setPayListener(listener: PayListener) {
        // 保存 listener 引用
        payListener = listener
        initPayListener()
    }

    /**
     * 初始化钱包连接配置。
     */
    @JvmStatic
    private external fun initWalletConnect(context: Context, appName: String, appDesc: String, domainUrl: String, supportChains: Array<Modal.Model.Chain>)

    /**
     * 初始化支付监听器。
     */
    @JvmStatic
    private external fun initPayListener()

    /**
     * 开始支付，使用默认的gasLimit和gasPrice。
     */
    @JvmStatic
    fun pay(accessKey: String, orderTitle: String, description: String, fromAccount: String, toAccount: String, tokenValue: String) {
        return pay(accessKey, orderTitle, description, fromAccount, toAccount, tokenValue, "0x27000", "0x17d7840000")
    }

    /**
     * 开始支付。
     */
    @JvmStatic
    external fun pay(accessKey: String, orderTitle: String, description: String, fromAccount: String, toAccount: String, tokenValue: String, gasLimit: String, gasPrice: String)

    @JvmStatic
    fun onPaySuccess() {
        payListener?.onPaySuccess()
    }

    @JvmStatic
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