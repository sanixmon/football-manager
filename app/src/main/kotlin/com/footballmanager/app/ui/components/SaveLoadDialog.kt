package com.footballmanager.app.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.footballmanager.app.ui.theme.FmAccentBlue
import com.footballmanager.app.ui.theme.FmAccentCyan
import com.footballmanager.app.ui.theme.FmBorder
import com.footballmanager.app.ui.theme.FmContinueGreen
import com.footballmanager.app.ui.theme.FmRatingHigh
import com.footballmanager.app.ui.theme.FmSurface
import com.footballmanager.app.ui.theme.FmSurfaceAlt
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.app.ui.theme.FmTextSecondary
import com.footballmanager.serialization.SaveSlot
import com.footballmanager.serialization.SaveSlotMetadata
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SaveLoadMode {
    SAVE,
    LOAD,
}

@Composable
fun SaveLoadDialog(
    mode: SaveLoadMode,
    slots: List<SaveSlotMetadata>,
    onSelectSlot: (SaveSlot) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = if (mode == SaveLoadMode.SAVE) "SAVE CAREER" else "LOAD CAREER"
    val actionText = if (mode == SaveLoadMode.SAVE) "SAVE" else "LOAD"
    val accentColor = if (mode == SaveLoadMode.SAVE) FmContinueGreen else FmAccentBlue

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(FmSurface)
                .border(1.dp, FmBorder, RoundedCornerShape(12.dp))
                .padding(16.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (mode == SaveLoadMode.SAVE) Icons.Outlined.Save else Icons.Outlined.Folder,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp,
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close", tint = FmTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = FmBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Slots list
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SaveSlot.userSlots.forEach { slot ->
                    val metadata = slots.firstOrNull { it.slot == slot }
                        ?: SaveSlotMetadata(slot = slot, exists = false)

                    val isOccupied = metadata.exists

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(FmSurfaceAlt)
                            .border(1.dp, if (isOccupied) FmBorder else FmBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = slot.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (!isOccupied) {
                                    Text(
                                        text = "[EMPTY]",
                                        fontSize = 10.sp,
                                        color = FmTextMuted,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                            if (isOccupied) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${metadata.clubName ?: "Club"} • ${metadata.managerName ?: "Manager"}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FmAccentCyan,
                                )
                                Text(
                                    text = "${metadata.inGameDate ?: ""} • ${metadata.roundInfo ?: ""}",
                                    fontSize = 10.5.sp,
                                    color = FmTextSecondary,
                                )
                                if (metadata.lastModified > 0) {
                                    val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(metadata.lastModified))
                                    Text(
                                        text = "Saved: $dateStr",
                                        fontSize = 9.5.sp,
                                        color = FmTextMuted,
                                    )
                                }
                            }
                        }

                        // Action button
                        Button(
                            onClick = { onSelectSlot(slot) },
                            enabled = mode == SaveLoadMode.SAVE || isOccupied,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (mode == SaveLoadMode.SAVE) FmContinueGreen else FmAccentBlue,
                                disabledContainerColor = FmSurface,
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(34.dp),
                        ) {
                            Text(
                                text = actionText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (mode == SaveLoadMode.SAVE) Color.Black else Color.White,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = FmSurfaceAlt),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(32.dp),
                ) {
                    Text("CANCEL", fontSize = 11.sp, color = FmTextSecondary)
                }
            }
        }
    }
}
