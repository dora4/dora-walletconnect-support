package dora.pay

/**
 * 支付异常。
 * @since 2.0
 */
class PaymentException(override val message: String) : RuntimeException(message) {
}