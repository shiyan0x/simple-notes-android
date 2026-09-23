package com.example.simplenotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.simplenotes.data.NotesDatabase
import com.example.simplenotes.data.UserPreferencesRepository
import com.example.simplenotes.repository.NoteRepository
import com.example.simplenotes.ui.navigation.NavGraph
import com.example.simplenotes.ui.theme.SimpleNotesTheme
import com.example.simplenotes.viewmodel.NotesViewModel
import com.example.simplenotes.viewmodel.NotesViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: NotesViewModel by viewModels {
        val database = NotesDatabase.getDatabase(applicationContext)
        val noteRepository = NoteRepository(database.noteDao())
        val userPreferencesRepository = UserPreferencesRepository(applicationContext)
        NotesViewModelFactory(noteRepository, userPreferencesRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()

            SimpleNotesTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
