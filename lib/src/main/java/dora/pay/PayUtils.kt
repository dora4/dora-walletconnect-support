package dora.pay

import androidx.annotation.WorkerThread
import com.walletconnect.web3.modal.client.Web3Modal
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.Transaction
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
     * Convert a double token amount to a hexadecimal string.
     * @since 2.0
     */
    @JvmStatic
    fun convertToHexWei(amount: Double): String {
        val weiValue = BigDecimal(amount).multiply(BigDecimal.TEN.pow(18)).toBigInteger()
        return "0x" + weiValue.toString(16) // convert to hexadecimal string
    }

    /**
     * Query blockchain data to check whether the transaction
     * has been successfully confirmed on-chain.
     * Uses the currently selected chain's JSON-RPC URL,
     * defaults to Ethereum if not available.
     * @since 2.0
     */
    @JvmStatic
    @WorkerThread
    fun queryTransaction(transactionHash: String) : Boolean {
        return queryTransaction(transactionHash, Web3Modal.getAccount()?.chain?.rpcUrl ?: DEFAULT_RPC_ETHEREUM)
    }

    /**
     * Query blockchain data to check whether the transaction
     * has been successfully confirmed on-chain,
     * using a custom JSON-RPC URL.
     * @since 2.0
     */
    @JvmStatic
    @WorkerThread
    fun queryTransaction(transactionHash: String, jsonRpcUrl: String) : Boolean {
        val web3j: Web3j = Web3j.build(HttpService(jsonRpcUrl))
        val receipt: TransactionReceipt? =
            web3j.ethGetTransactionReceipt(transactionHash).send().result
        return receipt != null && receipt.blockNumber != null
    }

    /**
     * Query blockchain data to get transaction details
     * for the given transaction hash.
     * Uses the currently selected chain's JSON-RPC URL,
     * defaults to Ethereum if not available.
     * @since 2.0
     */
    @JvmStatic
    @WorkerThread
    fun queryTransactionDetail(transactionHash: String): Transaction? {
        return queryTransactionDetail(transactionHash, Web3Modal.getAccount()?.chain?.rpcUrl ?: DEFAULT_RPC_ETHEREUM)
    }

    /**
     * Query blockchain data to get transaction details
     * for the given transaction hash,
     * using a custom JSON-RPC URL.
     * @since 2.0
     */
    @JvmStatic
    @WorkerThread
    fun queryTransactionDetail(transactionHash: String, jsonRpcUrl: String) : Transaction? {
        val web3j: Web3j = Web3j.build(HttpService(jsonRpcUrl))
        return web3j.ethGetTransactionByHash(transactionHash).send().transaction.orElse(null)
    }
}
