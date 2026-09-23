package com.example.simplenotes.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplenotes.data.ChecklistItem
import com.example.simplenotes.data.ChecklistSerializer
import com.example.simplenotes.ui.components.ColorPickerDialog
import com.example.simplenotes.ui.components.DeleteConfirmationDialog
import com.example.simplenotes.ui.theme.NoteColors
import com.example.simplenotes.viewmodel.NotesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: Long,
    viewModel: NotesViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val defaultColor by viewModel.defaultNoteColor.collectAsState()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }
    var colorHex by remember { mutableStateOf(defaultColor) }
    var isChecklistMode by remember { mutableStateOf(false) }
    val checklistItems = remember { mutableStateListOf<ChecklistItem>() }

    var currentNoteId by remember { mutableStateOf(if (noteId == -1L) 0L else noteId) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var isLoaded by remember { mutableStateOf(noteId == -1L) }

    val coroutineScope = rememberCoroutineScope()

    val isDark = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    // Load note if editing existing note
    LaunchedEffect(noteId) {
        if (noteId != -1L) {
            val existingNote = viewModel.getNoteById(noteId)
            if (existingNote != null) {
                title = existingNote.title.let { if (it == "Untitled Note") "" else it }
                content = existingNote.content
                isPinned = existingNote.isPinned
                colorHex = existingNote.colorHex
                isChecklistMode = existingNote.isChecklist
                checklistItems.clear()
                checklistItems.addAll(ChecklistSerializer.fromJson(existingNote.checklistJson))
                currentNoteId = existingNote.id
            }
            isLoaded = true
        } else {
            colorHex = defaultColor
        }
    }

    // Save action helper
    val saveNoteAction = {
        if (isLoaded) {
            coroutineScope.launch {
                val json = if (isChecklistMode) ChecklistSerializer.toJson(checklistItems) else ""
                val savedId = viewModel.saveNote(
                    id = currentNoteId,
                    titleInput = title,
                    contentInput = content,
                    isPinned = isPinned,
                    colorHex = colorHex,
                    isChecklist = isChecklistMode,
                    checklistJson = json
                )
                if (savedId != null) {
                    currentNoteId = savedId
                }
            }
        }
    }

    val handleBackPress = {
        saveNoteAction()
        onNavigateBack()
    }

    BackHandler {
        handleBackPress()
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isLoaded) {
                coroutineScope.launch {
                    val json = if (isChecklistMode) ChecklistSerializer.toJson(checklistItems) else ""
                    viewModel.saveNote(
                        id = currentNoteId,
                        titleInput = title,
                        contentInput = content,
                        isPinned = isPinned,
                        colorHex = colorHex,
                        isChecklist = isChecklistMode,
                        checklistJson = json
                    )
                }
            }
        }
    }

    val editorBg = NoteColors.getBackgroundColor(colorHex, isDark)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = editorBg,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = handleBackPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Pin / Unpin Button
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = if (isPinned) "Unpin" else "Pin",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Change Color Button
                    IconButton(onClick = { showColorPicker = true }) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Change color"
                        )
                    }

                    // Delete button if existing note
                    if (currentNoteId != 0L) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete note",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Save Button
                    IconButton(onClick = handleBackPress) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save note",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = editorBg
                )
            )
        },
        bottomBar = {
            // Counter Footer: Word/Char count OR Checklist Progress
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = editorBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val counterText = if (isChecklistMode) {
                        NotesViewModel.calculateChecklistProgress(checklistItems)
                    } else {
                        NotesViewModel.calculateWordAndCharCount(content)
                    }

                    Text(
                        text = counterText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            // Mode Segmented Switcher: Text Note vs Checklist
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                SegmentedButton(
                    selected = !isChecklistMode,
                    onClick = { isChecklistMode = false },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp)) }
                ) {
                    Text("Text Note")
                }

                SegmentedButton(
                    selected = isChecklistMode,
                    onClick = {
                        isChecklistMode = true
                        if (checklistItems.isEmpty()) {
                            checklistItems.add(ChecklistItem(text = ""))
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = { Icon(Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.size(16.dp)) }
                ) {
                    Text("Checklist")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title Input Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        text = "Title",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Content Editor depending on Mode
            if (isChecklistMode) {
                // Checklist Mode Items List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = checklistItems,
                        key = { _, item -> item.id }
                    ) { index, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isCompleted,
                                onCheckedChange = { checked ->
                                    checklistItems[index] = item.copy(isCompleted = checked)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            OutlinedTextField(
                                value = item.text,
                                onValueChange = { newText ->
                                    checklistItems[index] = item.copy(text = newText)
                                },
                                placeholder = { Text("Task...") },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )

                            IconButton(
                                onClick = { checklistItems.removeAt(index) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete item",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    item {
                        TextButton(
                            onClick = { checklistItems.add(ChecklistItem(text = "")) },
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add task item")
                        }
                    }
                }
            } else {
                // Normal Text Note Mode
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = {
                        Text(
                            text = "Note content...",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                showDeleteDialog = false
                if (currentNoteId != 0L) {
                    viewModel.deleteNoteByIdWithUndo(currentNoteId) { }
                }
                onNavigateBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // Color Selector Dialog
    if (showColorPicker) {
        ColorPickerDialog(
            currentColor = colorHex,
            isDark = isDark,
            onSelectColor = { selected ->
                colorHex = selected
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }
}
