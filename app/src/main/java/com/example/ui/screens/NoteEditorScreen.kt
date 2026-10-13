package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.core.audio.AudioRecorderHelper
import com.example.core.pdf.PdfRendererHelper
import com.example.data.local.entity.AttachmentEntity
import com.example.data.local.entity.ChecklistItemEntity
import com.example.data.local.entity.NoteEntity
import com.example.domain.repository.NotesRepository
import com.example.ui.components.GlassBox
import com.example.ui.components.WaveformBars
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.BodySubtle
import com.example.ui.theme.BodyText
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.notesGradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun NoteEditorScreen(
    initialNote: NoteEntity,
    initialChecklists: List<ChecklistItemEntity>,
    initialAttachments: List<AttachmentEntity>,
    repository: NotesRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var noteTitle by remember { mutableStateOf(initialNote.title) }
    var noteBody by remember { mutableStateOf(initialNote.plainText.ifBlank { initialNote.body }) }
    var isPinned by remember { mutableStateOf(initialNote.isPinned) }
    var isImportant by remember { mutableStateOf(initialNote.isImportant) }
    var isFeatured by remember { mutableStateOf(initialNote.isFeatured) }
    var isLocked by remember { mutableStateOf(initialNote.isLocked) }

    val checklists = remember { mutableStateListOf<ChecklistItemEntity>().apply { addAll(initialChecklists) } }
    val attachments = remember { mutableStateListOf<AttachmentEntity>().apply { addAll(initialAttachments) } }

    val recorder = remember { AudioRecorderHelper(context) }
    val isRecording by recorder.isRecording.collectAsState()
    val recordingAmplitudes by recorder.amplitudes.collectAsState()
    val recordingElapsed by recorder.elapsedMillis.collectAsState()

    var newChecklistText by remember { mutableStateOf("") }

    // Debounced autosave
    LaunchedEffect(noteTitle, noteBody, isPinned, isImportant, isFeatured, isLocked) {
        delay(350)
        val updated = initialNote.copy(
            title = noteTitle,
            body = noteBody,
            plainText = noteBody,
            isPinned = isPinned,
            isImportant = isImportant,
            isFeatured = isFeatured,
            isLocked = isLocked,
            updatedAt = System.currentTimeMillis()
        )
        repository.saveNote(updated)
    }

    // Save on back
    val saveAndExit = {
        scope.launch {
            val updated = initialNote.copy(
                title = noteTitle,
                body = noteBody,
                plainText = noteBody,
                isPinned = isPinned,
                isImportant = isImportant,
                isFeatured = isFeatured,
                isLocked = isLocked,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveNote(updated)
            repository.saveChecklistItems(initialNote.id, checklists)
            onBackClick()
        }
    }

    BackHandler {
        saveAndExit()
    }

    // Photo picker (PickVisualMedia - Zero permissions!)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val targetDir = File(File(context.filesDir, "attachments"), initialNote.id)
                targetDir.mkdirs()
                val targetFile = File(targetDir, "img_${System.currentTimeMillis()}.jpg")
                if (PdfRendererHelper.copyUriToFile(context, uri, targetFile)) {
                    val att = AttachmentEntity(
                        id = UUID.randomUUID().toString(),
                        noteId = initialNote.id,
                        type = "IMAGE",
                        uri = targetFile.absolutePath,
                        displayName = targetFile.name,
                        mimeType = "image/jpeg",
                        sizeBytes = targetFile.length()
                    )
                    repository.addAttachment(att)
                    attachments.add(att)
                }
            }
        }
    }

    // Document Picker (PDF / files)
    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val targetDir = File(File(context.filesDir, "attachments"), initialNote.id)
                targetDir.mkdirs()
                val isPdf = uri.toString().endsWith(".pdf", ignoreCase = true) ||
                        context.contentResolver.getType(uri)?.contains("pdf") == true
                val fileName = if (isPdf) "doc_${System.currentTimeMillis()}.pdf" else "file_${System.currentTimeMillis()}"
                val targetFile = File(targetDir, fileName)
                if (PdfRendererHelper.copyUriToFile(context, uri, targetFile)) {
                    val att = AttachmentEntity(
                        id = UUID.randomUUID().toString(),
                        noteId = initialNote.id,
                        type = if (isPdf) "PDF" else "FILE",
                        uri = targetFile.absolutePath,
                        displayName = targetFile.name,
                        mimeType = if (isPdf) "application/pdf" else "application/octet-stream",
                        sizeBytes = targetFile.length()
                    )
                    repository.addAttachment(att)
                    attachments.add(att)
                }
            }
        }
    }

    // Camera capture
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraFile by remember { mutableStateOf<File?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraFile != null && tempCameraFile!!.exists()) {
            scope.launch {
                val att = AttachmentEntity(
                    id = UUID.randomUUID().toString(),
                    noteId = initialNote.id,
                    type = "IMAGE",
                    uri = tempCameraFile!!.absolutePath,
                    displayName = tempCameraFile!!.name,
                    mimeType = "image/jpeg",
                    sizeBytes = tempCameraFile!!.length()
                )
                repository.addAttachment(att)
                attachments.add(att)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(notesGradient())
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassBox(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    onClick = { saveAndExit() }
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

                // Action icons: Pin, Star, Lock, Share
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EditorTopAction(
                        icon = Icons.Default.PushPin,
                        isActive = isPinned,
                        onClick = { isPinned = !isPinned }
                    )
                    EditorTopAction(
                        icon = Icons.Default.Star,
                        isActive = isImportant,
                        onClick = { isImportant = !isImportant }
                    )
                    EditorTopAction(
                        icon = Icons.Default.Lock,
                        isActive = isLocked,
                        onClick = { isLocked = !isLocked }
                    )
                    EditorTopAction(
                        icon = Icons.Default.Share,
                        isActive = false,
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, noteTitle)
                                putExtra(Intent.EXTRA_TEXT, "$noteTitle\n\n$noteBody")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Note"))
                        }
                    )
                }
            }

            // Note Content Sheet (Frosted Glass Container)
            GlassBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(28.dp),
                backgroundColor = Color(0xF2FFFFFF),
                elevation = 6.dp
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    // Note Title
                    item {
                        BasicTextField(
                            value = noteTitle,
                            onValueChange = { noteTitle = it },
                            textStyle = TextStyle(
                                fontFamily = OutfitFontFamily,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue
                            ),
                            decorationBox = { innerTextField ->
                                if (noteTitle.isEmpty()) {
                                    Text(
                                        text = "Title",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB0BDCF)
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("note_title_input")
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Recording waveform banner if active
                    if (isRecording) {
                        item {
                            GlassBox(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(20.dp),
                                backgroundColor = Color(0xFFF3EDF7)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFD32F2F))
                                            .clickable {
                                                val (file, samples) = recorder.stopRecording()
                                                if (file != null) {
                                                    val att = AttachmentEntity(
                                                        id = UUID.randomUUID().toString(),
                                                        noteId = initialNote.id,
                                                        type = "AUDIO",
                                                        uri = file.absolutePath,
                                                        displayName = file.name,
                                                        waveform = samples.joinToString(","),
                                                        durationMs = recordingElapsed
                                                    )
                                                    scope.launch {
                                                        repository.addAttachment(att)
                                                        attachments.add(att)
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Stop,
                                            contentDescription = "Stop",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    WaveformBars(
                                        amplitudes = recordingAmplitudes,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(26.dp),
                                        barColor = AccentBlue
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    val secs = (recordingElapsed / 1000) % 60
                                    val mins = (recordingElapsed / 60000)
                                    Text(
                                        text = String.format("%02d:%02d", mins, secs),
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 13.sp,
                                        color = AccentBlue,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // Attachments row (photos, audios, pdfs)
                    if (attachments.isNotEmpty()) {
                        item {
                            Text(
                                text = "Attachments",
                                fontFamily = OutfitFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BodySubtle
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                attachments.forEach { att ->
                                    Box(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color(0xFFE9EEF4))
                                    ) {
                                        if (att.type == "IMAGE") {
                                            AsyncImage(
                                                model = File(att.uri),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else if (att.type == "PDF") {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PictureAsPdf,
                                                    contentDescription = null,
                                                    tint = Color(0xFFD32F2F),
                                                    modifier = Modifier.size(28.dp)
                                                )
                                                Text(
                                                    text = "PDF",
                                                    fontFamily = OutfitFontFamily,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BodyText
                                                )
                                            }
                                        } else {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Mic,
                                                    contentDescription = null,
                                                    tint = AccentBlue,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                                Text(
                                                    text = "Audio",
                                                    fontFamily = OutfitFontFamily,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BodyText
                                                )
                                            }
                                        }

                                        // Delete attachment icon
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.5f))
                                                .clickable {
                                                    scope.launch {
                                                        repository.removeAttachment(att)
                                                        attachments.remove(att)
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    // Checklist items if any
                    if (checklists.isNotEmpty()) {
                        item {
                            Text(
                                text = "Checklist",
                                fontFamily = OutfitFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BodySubtle
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        itemsIndexed(checklists) { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (item.isChecked) Icons.Default.CheckCircle else Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (item.isChecked) Color(0xFF2E7D32) else Color.Gray,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable {
                                            checklists[index] = item.copy(isChecked = !item.isChecked)
                                            scope.launch { repository.saveChecklistItems(initialNote.id, checklists) }
                                        }
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = item.text,
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 15.sp,
                                    color = if (item.isChecked) Color.Gray else BodyText,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        checklists.removeAt(index)
                                        scope.launch { repository.saveChecklistItems(initialNote.id, checklists) }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Add checklist item inline field
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            BasicTextField(
                                value = newChecklistText,
                                onValueChange = { newChecklistText = it },
                                textStyle = TextStyle(
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 15.sp,
                                    color = BodyText
                                ),
                                decorationBox = { inner ->
                                    if (newChecklistText.isEmpty()) {
                                        Text(
                                            text = "Add checklist task...",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 15.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    inner()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            if (newChecklistText.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        val newItem = ChecklistItemEntity(
                                            noteId = initialNote.id,
                                            text = newChecklistText.trim(),
                                            position = checklists.size
                                        )
                                        checklists.add(newItem)
                                        newChecklistText = ""
                                        scope.launch { repository.saveChecklistItems(initialNote.id, checklists) }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Add",
                                        tint = AccentBlue
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Note Body
                    item {
                        BasicTextField(
                            value = noteBody,
                            onValueChange = { noteBody = it },
                            textStyle = TextStyle(
                                fontFamily = OutfitFontFamily,
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                color = BodyText
                            ),
                            cursorBrush = SolidColor(AccentBlue),
                            decorationBox = { innerTextField ->
                                if (noteBody.isEmpty()) {
                                    Text(
                                        text = "Write your thoughts...",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 16.sp,
                                        color = Color(0xFFA0AAB8)
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp)
                                .testTag("note_body_input")
                        )
                    }
                }
            }

            // Bottom Format & Attachment Bar
            GlassBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = CircleShape,
                backgroundColor = Color(0xEBFFFFFF),
                elevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Photo Picker
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = "Add Photo", tint = AccentBlue)
                    }

                    // Camera
                    IconButton(
                        onClick = {
                            val targetDir = File(File(context.filesDir, "attachments"), initialNote.id)
                            targetDir.mkdirs()
                            val photoFile = File(targetDir, "cam_${System.currentTimeMillis()}.jpg")
                            tempCameraFile = photoFile
                            val photoUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                photoFile
                            )
                            tempCameraUri = photoUri
                            cameraLauncher.launch(photoUri)
                        }
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Take Photo", tint = AccentBlue)
                    }

                    // Voice Recorder
                    IconButton(
                        onClick = {
                            if (!isRecording) {
                                val targetDir = File(File(context.filesDir, "attachments"), initialNote.id)
                                targetDir.mkdirs()
                                val audioFile = File(targetDir, "voice_${System.currentTimeMillis()}.m4a")
                                recorder.startRecording(audioFile)
                            } else {
                                val (file, samples) = recorder.stopRecording()
                                if (file != null) {
                                    val att = AttachmentEntity(
                                        id = UUID.randomUUID().toString(),
                                        noteId = initialNote.id,
                                        type = "AUDIO",
                                        uri = file.absolutePath,
                                        displayName = file.name,
                                        waveform = samples.joinToString(","),
                                        durationMs = recordingElapsed
                                    )
                                    scope.launch {
                                        repository.addAttachment(att)
                                        attachments.add(att)
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Voice Note",
                            tint = if (isRecording) Color.Red else AccentBlue
                        )
                    }

                    // PDF & Document Picker
                    IconButton(
                        onClick = {
                            docPickerLauncher.launch(arrayOf("application/pdf", "*/*"))
                        }
                    ) {
                        Icon(imageVector = Icons.Default.AttachFile, contentDescription = "Attach Document", tint = AccentBlue)
                    }

                    // Reminder Picker
                    IconButton(
                        onClick = {
                            showDateTimePicker(context) { timestamp ->
                                scope.launch {
                                    repository.addReminder(initialNote.id, timestamp)
                                }
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Alarm, contentDescription = "Set Reminder", tint = AccentBlue)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorTopAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    GlassBox(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        backgroundColor = if (isActive) Color.White else GlassBackground,
        onClick = onClick
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) AccentBlue else Color.White,
            modifier = Modifier
                .align(Alignment.Center)
                .size(18.dp)
        )
    }
}

private fun showDateTimePicker(context: Context, onTimeSelected: (Long) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    onTimeSelected(calendar.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}
