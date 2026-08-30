package com.footballmanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.footballmanager.app.ui.theme.FmAccentBlue
import com.footballmanager.app.ui.theme.FmBorder
import com.footballmanager.app.ui.theme.FmCardBg
import com.footballmanager.app.ui.theme.FmPrimaryGreen
import com.footballmanager.app.ui.theme.FmSurface
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.calculator.TransferValuationCalculator
import com.footballmanager.model.Player

@Composable
fun TransferBidDialog(
    player: Player,
    availableBudget: Long,
    onDismiss: () -> Unit,
    onSubmitBid: (fee: Long) -> Unit,
) {
    val estimatedValue = TransferValuationCalculator.calculateMarketValue(player)
    var bidFee by remember { mutableLongStateOf(estimatedValue.coerceAtMost(availableBudget.coerceAtLeast(100_000L))) }
    val maxSelectable = availableBudget.coerceAtLeast(estimatedValue * 2)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = FmCardBg,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, FmBorder, RoundedCornerShape(8.dp)),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "MAKE TRANSFER BID",
                    style = MaterialTheme.typography.labelSmall,
                    color = FmAccentBlue,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${player.name} (${player.bestPosition().name} · OVR ${player.bestOverall()})",
                    style = MaterialTheme.typography.titleMedium,
                    color = FmTextPrimary,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text("Est. Valuation", fontSize = 11.sp, color = FmTextMuted)
                        Text("$${"%,d".format(estimatedValue)}", fontSize = 14.sp, color = FmTextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Transfer Budget", fontSize = 11.sp, color = FmTextMuted)
                        Text("$${"%,d".format(availableBudget)}", fontSize = 14.sp, color = FmPrimaryGreen, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Offered Fee: $${"%,d".format(bidFee)}", fontSize = 13.sp, color = FmTextPrimary, fontWeight = FontWeight.Bold)

                Slider(
                    value = bidFee.toFloat(),
                    onValueChange = { bidFee = (it.toLong() / 50_000L) * 50_000L },
                    valueRange = 0f..maxSelectable.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = FmPrimaryGreen,
                        activeTrackColor = FmPrimaryGreen,
                        inactiveTrackColor = FmSurface,
                    ),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { bidFee = (bidFee + 250_000L).coerceAtMost(availableBudget) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("+$250k", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { bidFee = estimatedValue.coerceAtMost(availableBudget) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Est. Value", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSubmitBid(bidFee) },
                        enabled = bidFee in 1L..availableBudget,
                        colors = ButtonDefaults.buttonColors(containerColor = FmPrimaryGreen),
                    ) {
                        Text("Submit Offer", fontSize = 12.sp, color = FmTextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
