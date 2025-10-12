package dora.pay.token

import com.walletconnect.web3.modal.client.Modal

/**
 * Represents an ERC20 token on an EVM-compatible chain.
 *
 * @property chain The chain the token belongs to (e.g., Polygon, Ethereum, BSC, Arbitrum)
 * @property symbol The symbol of the token (e.g., "USDT", "ETH", "DAI").
 * @property contractAddress The token's contract address
 * @since 2.1
 */
open class Token(val chain: Modal.Model.Chain, val symbol: String, val contractAddress: String)