package com.footballmanager.app.ui.components

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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.footballmanager.calculator.WageExpectationCalculator
import com.footballmanager.model.Player
import com.footballmanager.model.SquadStatus

@Composable
fun ContractOfferDialog(
    player: Player,
    availableWeeklyWageBudget: Long,
    onDismiss: () -> Unit,
    onSubmitOffer: (weeklyWage: Long, contractYears: Int, squadStatus: SquadStatus) -> Unit,
) {
    var squadStatus by remember { mutableStateOf(SquadStatus.FIRST_TEAM) }
    val expectedWage = WageExpectationCalculator.calculateExpectedWage(player, squadStatus)
    var weeklyWage by remember { mutableLongStateOf(expectedWage) }
    var contractYears by remember { mutableIntStateOf(3) }

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
                    text = "OFFER CONTRACT TERMS",
                    style = MaterialTheme.typography.labelSmall,
                    color = FmAccentBlue,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = player.name,
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
                        Text("Expected Wage", fontSize = 11.sp, color = FmTextMuted)
                        Text("$${"%,d".format(expectedWage)}/wk", fontSize = 14.sp, color = FmTextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Wage Budget", fontSize = 11.sp, color = FmTextMuted)
                        Text("$${"%,d".format(availableWeeklyWageBudget)}/wk", fontSize = 14.sp, color = FmPrimaryGreen, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Offered Weekly Wage: $${"%,d".format(weeklyWage)}/wk", fontSize = 13.sp, color = FmTextPrimary, fontWeight = FontWeight.Bold)

                Slider(
                    value = weeklyWage.toFloat(),
                    onValueChange = { weeklyWage = (it.toLong() / 250L) * 250L },
                    valueRange = 500f..(expectedWage * 2).toFloat().coerceAtLeast(1000f),
                    colors = SliderDefaults.colors(
                        thumbColor = FmPrimaryGreen,
                        activeTrackColor = FmPrimaryGreen,
                        inactiveTrackColor = FmSurface,
                    ),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Contract Duration: $contractYears Year(s)", fontSize = 12.sp, color = FmTextMuted)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    (1..5).forEach { years ->
                        OutlinedButton(
                            onClick = { contractYears = years },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (contractYears == years) FmSurface else androidx.compose.ui.graphics.Color.Transparent
                            ),
                        ) {
                            Text("${years}y", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSubmitOffer(weeklyWage, contractYears, squadStatus) },
                        colors = ButtonDefaults.buttonColors(containerColor = FmPrimaryGreen),
                    ) {
                        Text("Send Contract Terms", fontSize = 12.sp, color = FmTextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
