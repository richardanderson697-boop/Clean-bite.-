package com.example.data.billing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BillingState(
    val isProActive: Boolean = false,
    val activeTier: ProTier = ProTier.NONE,
    val activeSku: String? = null,
    val expiryDateString: String? = null,
    val isTrialActive: Boolean = false,
    val lastPurchaseTxId: String? = null
)

enum class ProTier(val title: String, val price: String, val billingCycle: String, val sku: String) {
    NONE("Free Member", "$0", "Forever", "free_tier"),
    PRO_MONTHLY("CleanBite Pro Monthly", "$2.99", "/ month (3-day free trial)", "cleanbite_pro_monthly_299"),
    PRO_ANNUAL("CleanBite Foodie Pass Annual", "$19.99", "/ year (Save 45%)", "cleanbite_pro_annual_1999")
}

class PlayBillingManager {

    private val _billingState = MutableStateFlow(BillingState())
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    fun launchBillingFlow(tier: ProTier, onComplete: (Boolean) -> Unit) {
        if (tier == ProTier.NONE) return
        val txId = "GPA." + (1000..9999).random() + "-" + (1000..9999).random() + "-" + (1000..9999).random()
        val expiry = if (tier == ProTier.PRO_MONTHLY) "Sept 12, 2026" else "Aug 12, 2027"
        
        _billingState.value = BillingState(
            isProActive = true,
            activeTier = tier,
            activeSku = tier.sku,
            expiryDateString = expiry,
            isTrialActive = (tier == ProTier.PRO_MONTHLY),
            lastPurchaseTxId = txId
        )
        onComplete(true)
    }

    fun restorePurchases(onResult: (Boolean, String) -> Unit) {
        if (_billingState.value.isProActive) {
            onResult(true, "Restored active CleanBite Pro subscription!")
        } else {
            // Simulate restoring a previous purchase
            _billingState.value = BillingState(
                isProActive = true,
                activeTier = ProTier.PRO_ANNUAL,
                activeSku = ProTier.PRO_ANNUAL.sku,
                expiryDateString = "Dec 31, 2026",
                isTrialActive = false,
                lastPurchaseTxId = "GPA.8821-4421-9011"
            )
            onResult(true, "Successfully restored Pro Foodie Pass subscription!")
        }
    }

    fun cancelSubscription() {
        _billingState.value = BillingState(
            isProActive = false,
            activeTier = ProTier.NONE,
            activeSku = null,
            expiryDateString = null,
            isTrialActive = false,
            lastPurchaseTxId = null
        )
    }
}
