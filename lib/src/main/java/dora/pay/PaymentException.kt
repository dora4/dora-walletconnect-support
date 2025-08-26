package dora.pay

/**
 * Exception thrown during the payment process.
 * @since 2.0
 */
class PaymentException(override val message: String) : RuntimeException(message) {
}