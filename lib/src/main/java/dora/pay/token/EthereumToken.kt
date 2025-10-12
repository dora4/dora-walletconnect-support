package dora.pay.token

import dora.pay.EVMChains

/**
 * ERC20 Token.
 * @since 2.1
 */
object EthereumToken {

    /**
     * @since 2.1
     */
    @JvmField
    val USDT = Token(EVMChains.ETHEREUM, "0xdAC17F958D2ee523a2206206994597C13D831ec7")

    /**
     * @since 2.1
     */
    @JvmField
    val USDC = Token(EVMChains.ETHEREUM, "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48")

    /**
     * @since 2.1
     */
    @JvmField
    val DAI = Token(EVMChains.ETHEREUM, "0x6B175474E89094C44Da98b954EedeAC495271d0F")
}
