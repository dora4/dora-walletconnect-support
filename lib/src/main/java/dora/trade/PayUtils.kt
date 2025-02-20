package dora.trade

import java.math.BigDecimal

object PayUtils {

    @JvmStatic
    fun convertToHexWei(amount: Double): String {
        val weiValue = BigDecimal(amount).multiply(BigDecimal.TEN.pow(18)).toBigInteger()
        return "0x" + weiValue.toString(16) // 转换为十六进制字符串
    }
}
