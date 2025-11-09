package dora.pay.wallet

/**
 * @see WalletContract
 * @since 2.1
 */
data class WalletResult(
    val chainId: String,
    val chainName: String,
    val address: String
)