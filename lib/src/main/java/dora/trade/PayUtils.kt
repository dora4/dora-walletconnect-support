package dora.trade

import androidx.annotation.WorkerThread
import dora.util.TextUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import java.math.BigDecimal

object PayUtils {

    /**
     * 转换double代币数量为十六进制字符串。
     */
    @JvmStatic
    fun convertToHexWei(amount: Double): String {
        val weiValue = BigDecimal(amount).multiply(BigDecimal.TEN.pow(18)).toBigInteger()
        return "0x" + weiValue.toString(16) // 转换为十六进制字符串
    }

    /**
     * 查询区块链链上数据，该笔订单是否被区块链成功确认。
     */
    @JvmStatic
    @JvmOverloads
    @WorkerThread
    fun queryTransaction(transactionHash: String, jsonRpcUrl: String? = null) : Boolean {
        val url = if (TextUtils.isEmpty(jsonRpcUrl)) "https://eth-mainnet.token.im" else jsonRpcUrl
        val web3j: Web3j = Web3j.build(HttpService(url))
        val receipt: TransactionReceipt? =
            web3j.ethGetTransactionReceipt(transactionHash).send().result
        return receipt != null && receipt.status.equals("0x1")
    }
}
