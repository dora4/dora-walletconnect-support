package dora.pay.token

import dora.pay.EVMChains

/**
 * ERC20 Token, from Polygon.
 * @since 2.1
 */
object PolygonToken {

    /**
     * @since 2.1
     */
    @JvmField
    val USDT = Token(EVMChains.POLYGON, "0x3813e82e6f7098b9583FC0F33a962D02018B6803")

    /**
     * @since 2.1
     */
    @JvmField
    val USDC = Token(EVMChains.POLYGON, "0x2791Bca1f2de4661ED88A30C99A7a9449Aa84174")

    /**
     * @since 2.1
     */
    @JvmField
    val DAI = Token(EVMChains.POLYGON, "0x8f3Cf7ad23Cd3CaDbD9735AFf958023239c6A063")
}