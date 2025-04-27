package dora.trade

/**
 * @since 1.82
 */
class PaymentException(override val message: String) : RuntimeException(message) {
}