package com.netforge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netforge.app.domain.model.Mode
import com.netforge.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModePickerSheet(
    selectedMode: Mode,
    onModeSelected: (Mode) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = NetForgePaper,
        dragHandle = { BottomSheetDefaults.DragHandle(color = NetForgeBorder) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Tunnel Protocol Mode",
                style = Typography.titleLarge,
                color = NetForgeChalk
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select encapsulation transport mechanism",
                style = Typography.bodyMedium,
                color = NetForgeSlate
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                items(Mode.values()) { mode ->
                    val isSelected = mode == selectedMode
                    val borderColor = if (isSelected) NetForgeAccent else NetForgeBorder

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) NetForgePaper2 else NetForgeInk)
                            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                            .clickable {
                                onModeSelected(mode)
                                onDismiss()
                            }
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = mode.displayName,
                                    style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) NetForgeAccent else NetForgeChalk
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                if (isSelected) {
                                    Text(
                                        text = "SELECTED",
                                        style = Typography.labelSmall.copy(fontSize = 10.sp),
                                        color = NetForgeAccent
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = mode.description,
                                style = Typography.bodySmall,
                                color = NetForgeSlate
                            )
                        }
                    }
                }
            }
        }
    }
}
