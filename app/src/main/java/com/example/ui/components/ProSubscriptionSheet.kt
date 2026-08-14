package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.billing.BillingState
import com.example.data.billing.ProTier
import com.example.ui.theme.ProGold
import com.example.ui.theme.ProGoldContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProSubscriptionSheet(
    sheetState: SheetState,
    billingState: BillingState,
    onPurchaseTier: (ProTier) -> Unit,
    onRestorePurchase: () -> Unit,
    onCancelSub: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTier by remember { mutableStateOf(ProTier.PRO_ANNUAL) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "Pro Pass",
                        tint = ProGold,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "CleanBite Pro Pass",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = ProGoldContainer
            ) {
                Text(
                    text = "Monetized via Google Play In-App Billing. Unlock full health department intelligence and instant re-inspection alerts.",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp,
                    color = ProGold,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Active Subscription Card if Pro
            if (billingState.isProActive) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Active", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pro Pass Active: ${billingState.activeTier.title}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Renewal Date: ${billingState.expiryDateString ?: "Renews automatically"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        if (billingState.lastPurchaseTxId != null) {
                            Text(
                                text = "Google Play Order: ${billingState.lastPurchaseTxId}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = onCancelSub,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Manage / Cancel Play Subscription")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Pro Features List
            Text(
                text = "Included Premium Features",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            ProFeatureItem(
                icon = Icons.Default.NotificationsActive,
                title = "Real-Time Re-Inspection Alerts",
                description = "Get notified instantly when local favorite spots get inspected or cited."
            )
            ProFeatureItem(
                icon = Icons.Default.Analytics,
                title = "5-Year Health Score Trend Analytics",
                description = "View complete historic violation logs, inspector notes, and score graphs."
            )
            ProFeatureItem(
                icon = Icons.Default.Shield,
                title = "Ad-Free & Promoted Spot Control",
                description = "Browse clean dining map with zero sponsored distraction."
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (!billingState.isProActive) {
                Text(
                    text = "Select Google Play Subscription Tier",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Annual Tier Card
                TierOptionCard(
                    tier = ProTier.PRO_ANNUAL,
                    isSelected = selectedTier == ProTier.PRO_ANNUAL,
                    badgeText = "BEST VALUE (SAVE 45%)",
                    onSelect = { selectedTier = ProTier.PRO_ANNUAL }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Monthly Tier Card
                TierOptionCard(
                    tier = ProTier.PRO_MONTHLY,
                    isSelected = selectedTier == ProTier.PRO_MONTHLY,
                    badgeText = "3-DAY FREE TRIAL",
                    onSelect = { selectedTier = ProTier.PRO_MONTHLY }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onPurchaseTier(selectedTier)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("subscribe_google_play_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ProGold)
                ) {
                    Text(
                        text = "Subscribe with Google Play (${selectedTier.price})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onRestorePurchase,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("restore_purchases_button")
                ) {
                    Text("Restore Existing Google Play Purchase")
                }
            }
        }
    }
}

@Composable
private fun ProFeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(ProGoldContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = ProGold, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TierOptionCard(
    tier: ProTier,
    isSelected: Boolean,
    badgeText: String,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) ProGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ProGoldContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ProGold
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = tier.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = tier.billingCycle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text(
                text = tier.price,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = ProGold
            )
        }
    }
}
