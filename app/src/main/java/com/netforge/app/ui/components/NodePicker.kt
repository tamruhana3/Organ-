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
import com.netforge.app.domain.model.Node
import com.netforge.app.domain.node.NodeCatalog
import com.netforge.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodePickerSheet(
    selectedNodeId: String,
    onNodeSelected: (Node) -> Unit,
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
                text = "Select Gateway Node",
                style = Typography.titleLarge,
                color = NetForgeChalk
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Encrypted routing endpoints verified by Axiom Collective",
                style = Typography.bodyMedium,
                color = NetForgeSlate
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                items(NodeCatalog.defaultNodes) { node ->
                    val isSelected = node.id == selectedNodeId
                    val borderColor = if (isSelected) NetForgeAccent else NetForgeBorder

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) NetForgePaper2 else NetForgeInk)
                            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                            .clickable {
                                onNodeSelected(node)
                                onDismiss()
                            }
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = node.flagEmoji, fontSize = 26.sp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = node.name,
                                    style = Typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = NetForgeChalk
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${node.host}:${node.port}",
                                    style = MonoTextStyle.copy(fontSize = 11.sp),
                                    color = NetForgeSlate
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${node.latencyMs} ms",
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (node.latencyMs < 50) NetForgeMoss else NetForgeAmber
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isSelected) "ACTIVE" else "READY",
                                    style = Typography.labelSmall.copy(fontSize = 10.sp),
                                    color = if (isSelected) NetForgeAccent else NetForgeSlate
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
