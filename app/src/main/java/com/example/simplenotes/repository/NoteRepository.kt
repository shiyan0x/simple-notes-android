package com.example.simplenotes.repository

import com.example.simplenotes.data.Note
import com.example.simplenotes.data.NoteDao
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

    fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotesSortedByDate()
    }

    fun searchNotes(query: String): Flow<List<Note>> {
        return if (query.isBlank()) {
            noteDao.getAllNotesSortedByDate()
        } else {
            noteDao.searchNotes(query)
        }
    }

    suspend fun getNoteById(id: Long): Note? {
        return noteDao.getNoteById(id)
    }

    fun getNoteByIdFlow(id: Long): Flow<Note?> {
        return noteDao.getNoteByIdFlow(id)
    }

    suspend fun saveNote(note: Note): Long {
        return if (note.id == 0L) {
            noteDao.insertNote(note)
        } else {
            noteDao.updateNote(note)
            note.id
        }
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

    suspend fun deleteNoteById(id: Long) {
        noteDao.deleteNoteById(id)
    }
}
