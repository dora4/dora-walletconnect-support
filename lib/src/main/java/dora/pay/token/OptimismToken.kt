package dora.pay.token

import dora.pay.EVMChains

/**
 * ERC20 Token, from Optimism.
 * @since 2.1
 */
object OptimismToken {

    /**
     * @since 2.1
     */
    @JvmField
    val USDT = Token(EVMChains.OPTIMISM, "USDT", "0x94b008aA00579c1307B0EF2c499aD98a8ce58e58")

    /**
     * @since 2.1
     */
    @JvmField
    val USDC = Token(EVMChains.OPTIMISM, "USDC", "0x7F5c764cBc14f9669B88837ca1490cCa17c31607")

    /**
     * @since 2.1
     */
    @JvmField
    val DAI = Token(EVMChains.OPTIMISM, "DAI", "0xDA10009cBd5D07dd0CeCc66161FC93D7c9000da1")
}
