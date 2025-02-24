package dora.trade

import dora.util.TextUtils
import org.json.JSONObject
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import java.math.BigDecimal

object PayUtils {

    @JvmStatic
    fun convertToHexWei(amount: Double): String {
        val weiValue = BigDecimal(amount).multiply(BigDecimal.TEN.pow(18)).toBigInteger()
        return "0x" + weiValue.toString(16) // 转换为十六进制字符串
    }

    /**
     * 查询区块链浏览器该笔订单是否已支付，且已被确认。
     */
    @JvmStatic
    @JvmOverloads
    fun queryTransactionStatus(transactionHash: String, jsonRpcUrl: String? = null) : Boolean {
        val url = if (TextUtils.isEmpty(jsonRpcUrl)) "https://eth-mainnet.token.im" else jsonRpcUrl
        val web3j: Web3j = Web3j.build(HttpService(url))
        val receipt: TransactionReceipt? =
            web3j.ethGetTransactionReceipt(transactionHash).send().result
        return receipt != null && receipt.status.equals("0x1")
    }
}
