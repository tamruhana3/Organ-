package com.netforge.app.ui.profiles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.netforge.app.ui.components.EmptyState
import com.netforge.app.ui.components.ProfileCard
import com.netforge.app.ui.theme.*
import com.netforge.app.ui.util.Haptics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileListScreen(
    viewModel: ProfileListViewModel,
    onNavigateBack: () -> Unit,
    onNavigateDetail: (Long) -> Unit,
    onNavigateExport: (Long) -> Unit,
    onNavigateImport: () -> Unit
) {
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val activeId by viewModel.activeProfileId.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var renameDialogTargetId by remember { mutableStateOf<Long?>(null) }
    var renameText by remember { mutableStateOf("") }

    Scaffold(
        containerColor = NetForgeInk,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profiles Vault",
                        style = Typography.titleLarge,
                        color = NetForgeChalk
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NetForgeChalk
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateImport) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Import .nfg",
                            tint = NetForgeAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NetForgeInk)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    Haptics.medium(context)
                    val id = viewModel.createNewProfile()
                    onNavigateDetail(id)
                },
                containerColor = NetForgeAccent,
                contentColor = NetForgeChalk,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Profile", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        if (profiles.isEmpty()) {
            EmptyState(
                icon = Icons.Default.FolderOpen,
                title = "No profiles found",
                message = "Create a custom configuration or import a sealed .nfg vault file to get started.",
                actionLabel = "Import .nfg",
                onAction = onNavigateImport,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(profiles, key = { it.id }) { item ->
                    ProfileCard(
                        profile = item,
                        isSelected = item.id == activeId,
                        onSelect = {
                            Haptics.light(context)
                            viewModel.setActiveProfile(item)
                            onNavigateBack()
                        },
                        onToggleFavorite = {
                            Haptics.light(context)
                            viewModel.toggleFavorite(item)
                        },
                        onEdit = { onNavigateDetail(item.id) },
                        onExport = { onNavigateExport(item.id) },
                        onDelete = {
                            Haptics.medium(context)
                            viewModel.deleteProfile(item.id)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }

        // Rename Dialog
        if (renameDialogTargetId != null) {
            AlertDialog(
                onDismissRequest = { renameDialogTargetId = null },
                containerColor = NetForgePaper,
                title = { Text("Rename Profile", color = NetForgeChalk) },
                text = {
                    OutlinedTextField(
                        value = renameText,
                        onValueChange = { renameText = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NetForgeAccent,
                            unfocusedBorderColor = NetForgeBorder,
                            focusedTextColor = NetForgeChalk,
                            unfocusedTextColor = NetForgeChalk
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            renameDialogTargetId?.let { id ->
                                viewModel.renameProfile(id, renameText.trim())
                            }
                            renameDialogTargetId = null
                        }
                    ) {
                        Text("Save", color = NetForgeAccent)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { renameDialogTargetId = null }) {
                        Text("Cancel", color = NetForgeSlate)
                    }
                }
            )
        }
    }
}
