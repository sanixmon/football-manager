package com.footballmanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.footballmanager.app.ui.theme.FmAccentBlue
import com.footballmanager.app.ui.theme.FmAccentCyan
import com.footballmanager.app.ui.theme.FmBorder
import com.footballmanager.app.ui.theme.FmCardBg
import com.footballmanager.app.ui.theme.FmContinueGreen
import com.footballmanager.app.ui.theme.FmDarkBg
import com.footballmanager.app.ui.theme.FmSurface
import com.footballmanager.app.ui.theme.FmSurfaceSelected
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.app.ui.theme.FmTextSecondary
import com.footballmanager.model.Club

@Composable
fun NewGameWizardScreen(
    clubs: List<Club>,
    onBack: () -> Unit,
    onStartCareer: (managerName: String, nationality: String, clubId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var managerName by remember { mutableStateOf("Coach Alex") }
    var nationality by remember { mutableStateOf("ID") }
    var selectedClubId by remember { mutableLongStateOf(clubs.firstOrNull()?.id ?: 1L) }

    val selectedClub = clubs.firstOrNull { it.id == selectedClubId } ?: clubs.first()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FmDarkBg),
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FmSurface)
                .border(1.dp, FmBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = FmTextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CREATE NEW CAREER",
                style = MaterialTheme.typography.titleMedium,
                color = FmTextPrimary,
                fontWeight = FontWeight.Bold,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Left: Manager Setup
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(FmCardBg, RoundedCornerShape(8.dp))
                    .border(1.dp, FmBorder, RoundedCornerShape(8.dp))
                    .padding(16.dp),
            ) {
                Text("1. MANAGER PROFILE", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = managerName,
                    onValueChange = { managerName = it },
                    label = { Text("Manager Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FmAccentBlue,
                        unfocusedBorderColor = FmBorder,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nationality,
                    onValueChange = { nationality = it.take(3).uppercase() },
                    label = { Text("Nationality (Code)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FmAccentBlue,
                        unfocusedBorderColor = FmBorder,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = FmBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Text("SELECTED CLUB DETAILS", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(selectedClub.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = FmTextPrimary)
                Text("Transfer Budget: $${"%,d".format(selectedClub.finance.transferBudget)}", fontSize = 12.sp, color = FmContinueGreen)
                Text("Wage Budget: $${"%,d".format(selectedClub.finance.weeklyWageBudget)}/w", fontSize = 12.sp, color = FmTextSecondary)
                Text("Squad Size: ${selectedClub.squad.size} players", fontSize = 12.sp, color = FmTextMuted)

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { onStartCareer(managerName.ifBlank { "Coach Alex" }, nationality, selectedClubId) },
                    colors = ButtonDefaults.buttonColors(containerColor = FmContinueGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text("START CAREER >", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            // Right: Club Selection
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .background(FmCardBg, RoundedCornerShape(8.dp))
                    .border(1.dp, FmBorder, RoundedCornerShape(8.dp))
                    .padding(16.dp),
            ) {
                Text("2. CHOOSE YOUR CLUB", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(clubs, key = { it.id }) { club ->
                        val isSelected = club.id == selectedClubId

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSelected) FmSurfaceSelected else FmSurface, RoundedCornerShape(6.dp))
                                .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) FmAccentCyan else FmBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedClubId = club.id }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.Shield, contentDescription = null, tint = if (isSelected) FmAccentCyan else FmTextSecondary, modifier = Modifier.size(20.dp))
                                Column {
                                    Text(club.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else FmTextPrimary)
                                    Text("${club.squad.size} players · Budget: $${"%,d".format(club.finance.transferBudget)}", fontSize = 11.sp, color = FmTextMuted)
                                }
                            }
                            if (isSelected) {
                                Text("SELECTED", fontSize = 10.sp, color = FmContinueGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
