package com.footballmanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.footballmanager.app.ui.theme.FmAccentBlue
import com.footballmanager.app.ui.theme.FmBorder
import com.footballmanager.app.ui.theme.FmCardBg
import com.footballmanager.app.ui.theme.FmDarkBg
import com.footballmanager.app.ui.theme.FmPrimaryGreen
import com.footballmanager.app.ui.theme.FmSurface
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.app.ui.theme.FmTextSecondary
import com.footballmanager.app.ui.viewmodel.GameUiState
import com.footballmanager.model.SquadStatus

@Composable
fun FinancesScreen(
    state: GameUiState,
    modifier: Modifier = Modifier,
) {
    val finance = state.humanFinance
    val squad = state.humanSquad
    val currentWeeklyWage = squad.sumOf { it.contract.weeklyWage }
    val wageBudget = finance.weeklyWageBudget.coerceAtLeast(1L)
    val wageRatio = (currentWeeklyWage.toFloat() / wageBudget.toFloat()).coerceIn(0f, 1f)

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FmDarkBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Summary Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FinanceStatCard(
                title = "TOTAL BALANCE",
                value = "$${"%,d".format(finance.balance)}",
                color = FmPrimaryGreen,
                modifier = Modifier.weight(1f),
            )
            FinanceStatCard(
                title = "TRANSFER BUDGET",
                value = "$${"%,d".format(finance.transferBudget)}",
                color = FmAccentBlue,
                modifier = Modifier.weight(1f),
            )
            FinanceStatCard(
                title = "WEEKLY WAGE BUDGET",
                value = "$${"%,d".format(finance.weeklyWageBudget)}/w",
                color = FmTextPrimary,
                modifier = Modifier.weight(1f),
            )
        }

        // Wage Budget Utilization Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FmCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, FmBorder, RoundedCornerShape(8.dp))
                .padding(16.dp),
        ) {
            Text("PAYROLL UTILIZATION", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Current Spend: $${"%,d".format(currentWeeklyWage)}/w",
                    fontSize = 13.sp,
                    color = FmTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${(wageRatio * 100).toInt()}% of budget",
                    fontSize = 13.sp,
                    color = if (wageRatio > 0.90f) Color(0xFFEF4444) else FmPrimaryGreen,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { wageRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (wageRatio > 0.90f) Color(0xFFEF4444) else FmPrimaryGreen,
                trackColor = FmSurface,
            )
        }

        // Payroll by Squad Role Breakdown
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FmCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, FmBorder, RoundedCornerShape(8.dp))
                .padding(16.dp),
        ) {
            Text("WAGE EXPENDITURE BY SQUAD ROLE", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            SquadStatus.entries.forEach { status ->
                val playersInRole = squad.filter { it.contract.squadStatus == status }
                val totalRoleWage = playersInRole.sumOf { it.contract.weeklyWage }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${status.name.replace('_', ' ')} (${playersInRole.size})",
                        fontSize = 12.sp,
                        color = FmTextSecondary,
                    )
                    Text(
                        text = "$${"%,d".format(totalRoleWage)}/w",
                        fontSize = 12.sp,
                        color = FmTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                HorizontalDivider(color = FmBorder, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun FinanceStatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(FmCardBg, RoundedCornerShape(8.dp))
            .border(1.dp, FmBorder, RoundedCornerShape(8.dp))
            .padding(14.dp),
    ) {
        Text(title, fontSize = 10.sp, color = FmTextMuted, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text(value, fontSize = 14.sp, color = color, fontWeight = FontWeight.Bold)
    }
}
