package com.polish.thousand.payments

internal actual fun providePaymentClient(): PaymentClient = StoreSetupRequiredPaymentClient(
    storeName = "App Store StoreKit"
)

private class StoreSetupRequiredPaymentClient(
    private val storeName: String
) : PaymentClient {
    // Real StoreKit/RevenueCat integration needs App Store Connect products first.
    // Until then, never grant premium locally from iOS code.
    override suspend fun loadFullUnlockProduct(): PaymentProduct = PaymentProduct(
        productId = FullUnlockProductId,
        displayPrice = RecommendedFullUnlockPrice,
        isStoreConfigured = false
    )

    override suspend fun isFullUnlockActive(): Boolean = false

    override suspend fun purchaseFullUnlock(): PaymentResult = PaymentResult.Unavailable(
        reason = "$storeName product is not configured yet."
    )

    override suspend fun restoreFullUnlock(): PaymentResult = PaymentResult.Unavailable(
        reason = "$storeName product is not configured yet."
    )
}
