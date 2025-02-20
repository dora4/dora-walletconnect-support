package dora.trade

object PayUtils {

    @JvmStatic
    fun convertToHexWei(amount: Double): String {
        val weiValue = (amount * 1e18).toLong() // 转换为 Wei（18 位小数）
        return "0x" + weiValue.toString(16) // 转换为十六进制字符串
    }
}
