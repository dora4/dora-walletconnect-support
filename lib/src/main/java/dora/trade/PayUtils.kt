package dora.trade

import androidx.annotation.WorkerThread
import dora.util.TextUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import java.math.BigDecimal

object PayUtils {

    const val DEFAULT_RPC_ETHEREUM = "https://eth-mainnet.token.im"
    const val DEFAULT_RPC_ARBITRUM = "https://arbitrum-mainnet.token.im"
    const val DEFAULT_RPC_POLYGON = "https://polygon-mainnet.token.im"
    const val DEFAULT_RPC_BSC = "https://bsc-mainnet.token.im"
    const val DEFAULT_RPC_AVALANCHE = "https://api.avax.network"
    const val DEFAULT_RPC_OPTIMISM = "https://optimism-mainnet.token.im"
    const val DEFAULT_RPC_BASE = "https://base-mainnet.token.im"
    const val DEFAULT_RPC_LINEA = "https://rpc.linea.build"
    const val DEFAULT_RPC_OKX = "https://exchainrpc.okex.org"

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
        val url = if (TextUtils.isEmpty(jsonRpcUrl)) DEFAULT_RPC_ETHEREUM else jsonRpcUrl
        val web3j: Web3j = Web3j.build(HttpService(url))
        val receipt: TransactionReceipt? =
            web3j.ethGetTransactionReceipt(transactionHash).send().result
        return receipt != null && receipt.status.equals("0x1")
    }
}
