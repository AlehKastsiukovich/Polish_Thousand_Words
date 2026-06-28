package com.polish.thousand.payments

internal const val FullUnlockProductId = "polish_1000_full_unlock"
internal const val FullUnlockEntitlementId = "full_unlock"
internal const val RecommendedFullUnlockPrice = "\$4.99"

internal interface PaymentClient {
    suspend fun loadFullUnlockProduct(): PaymentProduct
    suspend fun isFullUnlockActive(): Boolean
    suspend fun purchaseFullUnlock(): PaymentResult
    suspend fun restoreFullUnlock(): PaymentResult
}

internal data class PaymentProduct(
    val productId: String,
    val displayPrice: String,
    val isStoreConfigured: Boolean
)

internal sealed interface PaymentResult {
    data object Purchased : PaymentResult
    data object Restored : PaymentResult
    data object Cancelled : PaymentResult
    data class Unavailable(val reason: String) : PaymentResult
    data class Failed(val reason: String) : PaymentResult
}

internal expect fun providePaymentClient(): PaymentClient
