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
    val USDT = Token(EVMChains.BSC, "0x55d398326f99059fF775485246999027B3197955")

    /**
     * @since 2.1
     */
    val USDC = Token(EVMChains.BSC, "0x8ac76a51cc950d9822d68b83fe1ad97b32cd580d")

    /**
     * @since 2.1
     */
    val DAI  = Token(EVMChains.BSC, "0x1AF3F329e8BE154074D8769D1FFa4eE058B1DBc3")
}
