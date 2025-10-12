package dora.pay.token

import dora.pay.EVMChains

/**
 * ERC20 Token, from Avalanche C-Chain.
 * @since 2.1
 */
object AvalancheToken {

    /**
     * @since 2.1
     */
    @JvmField
    val USDT = Token(EVMChains.AVALANCHE, "USDT", "0x9702230A8Ea53601f5cD2dc00fDBc13d4dF4A8c7")

    /**
     * @since 2.1
     */
    @JvmField
    val USDC = Token(EVMChains.AVALANCHE, "USDC", "0xB97EF9Ef8734C71904D8002F8b6Bc66Dd9c48a6E")

    /**
     * @since 2.1
     */
    @JvmField
    val DAI = Token(EVMChains.AVALANCHE, "DAI", "0xd586E7F844cEa2F87f50152665BCbc2C279D8d70")
}
