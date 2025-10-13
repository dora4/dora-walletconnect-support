package dora.pay.token

import dora.pay.EVMChains

/**
 * ERC20 Token, from BNB Smart Chain.
 * @since 2.1
 */
object BscToken {

    /**
     * @since 2.1
     */
    @JvmField
    val USDT = Token(EVMChains.BSC, "USDT", "0x55d398326f99059fF775485246999027B3197955", 6)

    /**
     * @since 2.1
     */
    @JvmField
    val USDC = Token(EVMChains.BSC, "USDC", "0x8ac76a51cc950d9822d68b83fe1ad97b32cd580d", 6)

    /**
     * @since 2.1
     */
    @JvmField
    val DAI = Token(EVMChains.BSC, "DAI", "0x1AF3F329e8BE154074D8769D1FFa4eE058B1DBc3", 18)
}
