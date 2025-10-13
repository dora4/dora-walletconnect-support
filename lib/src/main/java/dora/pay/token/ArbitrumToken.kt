package dora.pay.token

import dora.pay.EVMChains

/**
 * ERC20 Token, from Arbitrum.
 * @since 2.1
 */
object ArbitrumToken {

    /**
     * @since 2.1
     */
    @JvmField
    val USDT = Token(EVMChains.ARBITRUM, "USDT", "0xFd086bC7CD5C481DCC9C85ebE478A1C0b69FCbb9", 6)

    /**
     * @since 2.1
     */
    @JvmField
    val USDC = Token(EVMChains.ARBITRUM, "USDC", "0xFF970A61A04b1cA14834A43f5dE4533eBDDB5CC8", 6)

    /**
     * @since 2.1
     */
    @JvmField
    val DAI = Token(EVMChains.ARBITRUM, "DAI", "0xda10009cbd5d07dd0cecc66161fc93d7c9000da1", 18)
}
