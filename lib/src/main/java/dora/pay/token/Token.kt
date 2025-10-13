package dora.pay.token

import com.walletconnect.web3.modal.client.Modal

/**
 * Represents an ERC20 token on an EVM-compatible chain.
 *
 * @property chain The blockchain the token belongs to (e.g., Polygon, Ethereum, BSC, Arbitrum).
 * @property symbol The symbol of the token (e.g., "USDT", "USDC", "DAI").
 * @property contractAddress The token's smart contract address on the chain.
 * @property decimals The number of decimal places used by the token (used for converting human-readable amounts to integer units).
 * @since 2.1
 */
open class Token(
    val chain: Modal.Model.Chain,
    val symbol: String,
    val contractAddress: String,
    val decimals: Int
)
