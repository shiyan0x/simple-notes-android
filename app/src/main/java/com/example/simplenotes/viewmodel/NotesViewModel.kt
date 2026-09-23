package com.example.simplenotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

    suspend fun getNoteById(id: Long): Note? {
        return repository.getNoteById(id)
    }

    suspend fun saveNote(id: Long, titleInput: String, contentInput: String): Long? {
        val trimmedTitle = titleInput.trim()
        val trimmedContent = contentInput.trim()

        // If both title and content are completely empty, don't save an empty note
        if (trimmedTitle.isEmpty() && trimmedContent.isEmpty()) {
            if (id != 0L) {
                // If it was an existing note and user wiped all text, delete it
                repository.deleteNoteById(id)
            }
            return null
        }

        // Rule 11: If user does not enter a title, automatically use "Untitled Note"
        val finalTitle = if (trimmedTitle.isEmpty()) "Untitled Note" else trimmedTitle

        val note = Note(
            id = id,
            title = finalTitle,
            content = contentInput, // Preserve formatting and whitespace in content
            updatedAt = System.currentTimeMillis()
        )

        return repository.saveNote(note)
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun deleteNoteById(id: Long) {
        viewModelScope.launch {
            repository.deleteNoteById(id)
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
