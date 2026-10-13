package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.backup.BackupHelper
import com.example.data.local.SalimDataStore
import com.example.domain.repository.NotesRepository
import com.example.ui.components.GlassBox
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.BodySubtle
import com.example.ui.theme.BodyText
import com.example.ui.theme.ChipSelectedFill
import com.example.ui.theme.ChipSelectedText
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.notesGradient
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@Composable
fun SettingsScreen(
    currentName: String,
    currentTheme: String,
    isAppLockEnabled: Boolean,
    totalStorageBytes: Long?,
    dataStore: SalimDataStore,
    repository: NotesRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showNameDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(currentName) }
    var showEmptyTrashDialog by remember { mutableStateOf(false) }

    // Backup export launcher
    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val tempBackup = File(context.cacheDir, "salim_temp.salimbackup")
                val success = BackupHelper.exportBackup(context, tempBackup)
                if (success) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        tempBackup.inputStream().use { input -> input.copyTo(out) }
                    }
                    tempBackup.delete()
                    Toast.makeText(context, "Backup exported successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Markdown zip export launcher
    val exportMarkdownLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val tempZip = File(context.cacheDir, "salim_notes.zip")
                val success = BackupHelper.exportMarkdownZip(context, tempZip)
                if (success) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        tempZip.inputStream().use { input -> input.copyTo(out) }
                    }
                    tempZip.delete()
                    Toast.makeText(context, "Markdown notes exported", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(notesGradient())
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassBox(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    onClick = onBackClick
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Settings",
                    fontFamily = OutfitFontFamily,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Section
                item {
                    SettingsGroupCard(title = "Profile") {
                        SettingsItem(
                            icon = Icons.Default.Person,
                            label = "Your Name",
                            value = currentName.ifBlank { "Not set" },
                            onClick = {
                                editedName = currentName
                                showNameDialog = true
                            }
                        )
                    }
                }

                // Appearance Section
                item {
                    SettingsGroupCard(title = "Appearance") {
                        SettingsItem(
                            icon = Icons.Default.Palette,
                            label = "Theme",
                            value = currentTheme.replaceFirstChar { it.uppercase() },
                            onClick = {
                                val next = when (currentTheme) {
                                    "system" -> "light"
                                    "light" -> "dark"
                                    else -> "system"
                                }
                                scope.launch { dataStore.setAppTheme(next) }
                            }
                        )
                    }
                }

                // Security Section
                item {
                    SettingsGroupCard(title = "Security") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = AccentBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = "Biometric Lock",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BodyText
                                )
                            }
                            Switch(
                                checked = isAppLockEnabled,
                                onCheckedChange = { checked ->
                                    scope.launch { dataStore.setAppLockEnabled(checked) }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AccentBlue
                                )
                            )
                        }
                    }
                }

                // Data & Storage Section
                item {
                    val storageMb = ((totalStorageBytes ?: 0L) / (1024.0 * 1024.0))
                    val storageStr = String.format(Locale.getDefault(), "%.1f MB", storageMb)

                    SettingsGroupCard(title = "Data & Storage") {
                        SettingsItem(
                            icon = Icons.Default.Storage,
                            label = "Attachment Storage",
                            value = storageStr,
                            onClick = {}
                        )
                        SettingsItem(
                            icon = Icons.Default.Backup,
                            label = "Backup All Data",
                            value = ".salimbackup",
                            onClick = { exportBackupLauncher.launch("salim_backup_${System.currentTimeMillis()}.salimbackup") }
                        )
                        SettingsItem(
                            icon = Icons.Default.FolderZip,
                            label = "Export as Markdown",
                            value = "ZIP archive",
                            onClick = { exportMarkdownLauncher.launch("salim_notes_${System.currentTimeMillis()}.zip") }
                        )
                        SettingsItem(
                            icon = Icons.Default.Delete,
                            label = "Empty Trash",
                            value = "Clear permanently",
                            onClick = { showEmptyTrashDialog = true }
                        )
                    }
                }

                // About Section
                item {
                    SettingsGroupCard(title = "About Salim") {
                        SettingsItem(
                            icon = Icons.Default.Info,
                            label = "Version",
                            value = "1.0.0",
                            onClick = {}
                        )
                        Text(
                            text = "100% Offline & Private. No tracking, no external servers.",
                            fontFamily = OutfitFontFamily,
                            fontSize = 12.sp,
                            color = BodySubtle,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }

        // Edit Name Dialog
        if (showNameDialog) {
            AlertDialog(
                onDismissRequest = { showNameDialog = false },
                title = { Text("Your Name", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch { dataStore.setUserName(editedName) }
                            showNameDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNameDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Empty Trash Dialog
        if (showEmptyTrashDialog) {
            AlertDialog(
                onDismissRequest = { showEmptyTrashDialog = false },
                title = { Text("Empty Trash", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to permanently delete all notes in the trash?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                repository.emptyTrash()
                                Toast.makeText(context, "Trash cleared", Toast.LENGTH_SHORT).show()
                            }
                            showEmptyTrashDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("Delete Permanently")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEmptyTrashDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsGroupCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            fontFamily = OutfitFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
        )
        GlassBox(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            backgroundColor = Color(0xF2FFFFFF),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentBlue,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                fontFamily = OutfitFontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = BodyText
            )
        }
        Text(
            text = value,
            fontFamily = OutfitFontFamily,
            fontSize = 14.sp,
            color = BodySubtle
        )
    }
}
