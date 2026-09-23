package com.example.simplenotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.simplenotes.data.ChecklistItem
import com.example.simplenotes.data.Note
import com.example.simplenotes.data.UserPreferencesRepository
import com.example.simplenotes.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(
    private val repository: NoteRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<Note>> = _searchQuery
        .flatMapLatest { query ->
            repository.searchNotes(query.trim())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val themeMode: StateFlow<String> = userPreferencesRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "system"
        )

    val defaultNoteColor: StateFlow<String> = userPreferencesRepository.defaultNoteColor
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "DEFAULT"
        )

    private var lastDeletedNote: Note? = null

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode)
        }
    }

    fun setDefaultNoteColor(colorHex: String) {
        viewModelScope.launch {
            userPreferencesRepository.setDefaultNoteColor(colorHex)
        }
    }

    suspend fun getNoteById(id: Long): Note? {
        return repository.getNoteById(id)
    }

    suspend fun saveNote(
        id: Long,
        titleInput: String,
        contentInput: String,
        isPinned: Boolean = false,
        colorHex: String = "DEFAULT",
        isChecklist: Boolean = false,
        checklistJson: String = ""
    ): Long? {
        val trimmedTitle = titleInput.trim()
        val trimmedContent = contentInput.trim()

        // If both title, content, and checklist items are completely empty
        if (trimmedTitle.isEmpty() && trimmedContent.isEmpty() && checklistJson.isEmpty()) {
            if (id != 0L) {
                repository.deleteNoteById(id)
            }
            return null
        }

        val finalTitle = if (trimmedTitle.isEmpty()) "Untitled Note" else trimmedTitle

        val note = Note(
            id = id,
            title = finalTitle,
            content = contentInput,
            updatedAt = System.currentTimeMillis(),
            isPinned = isPinned,
            colorHex = colorHex,
            isChecklist = isChecklist,
            checklistJson = checklistJson
        )

        return repository.saveNote(note)
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.togglePinNote(note)
        }
    }

    fun updateNoteColor(note: Note, colorHex: String) {
        viewModelScope.launch {
            repository.updateNoteColor(note, colorHex)
        }
    }

    fun deleteNoteWithUndo(note: Note, onDeleted: () -> Unit) {
        lastDeletedNote = note
        viewModelScope.launch {
            repository.deleteNote(note)
            onDeleted()
        }
    }

    fun deleteNoteByIdWithUndo(id: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            val note = repository.getNoteById(id)
            if (note != null) {
                lastDeletedNote = note
                repository.deleteNote(note)
            }
            onDeleted()
        }
    }

    fun restoreLastDeletedNote() {
        val noteToRestore = lastDeletedNote ?: return
        viewModelScope.launch {
            repository.saveNote(noteToRestore)
            lastDeletedNote = null
        }
    }

    companion object {
        fun calculateWordAndCharCount(text: String): String {
            val trimmed = text.trim()
            val charCount = trimmed.length
            val wordCount = if (trimmed.isEmpty()) 0 else trimmed.split("\\s+".toRegex()).size
            return "$wordCount words • $charCount characters"
        }

        fun calculateChecklistProgress(items: List<ChecklistItem>): String {
            if (items.isEmpty()) return "0 completed"
            val completedCount = items.count { it.isCompleted }
            return "$completedCount of ${items.size} completed"
        }
    }
}

class NotesViewModelFactory(
    private val repository: NoteRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            return NotesViewModel(repository, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
