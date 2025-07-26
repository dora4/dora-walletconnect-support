package dora.trade

/**
 * 支付异常。
 * @since 1.82
 */
class PaymentException(override val message: String) : RuntimeException(message) {
}