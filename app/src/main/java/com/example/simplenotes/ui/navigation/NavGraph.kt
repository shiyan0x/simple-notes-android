package com.example.simplenotes.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.simplenotes.ui.screens.NoteEditorScreen
import com.example.simplenotes.ui.screens.NotesScreen
import com.example.simplenotes.ui.screens.SettingsScreen
import com.example.simplenotes.viewmodel.NotesViewModel

object Routes {
    const val HOME = "home"
    const val EDITOR = "editor/{noteId}"
    const val SETTINGS = "settings"

    fun editorRoute(noteId: Long): String {
        return "editor/$noteId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: NotesViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            NotesScreen(
                viewModel = viewModel,
                onNavigateToEditor = { noteId ->
                    navController.navigate(Routes.editorRoute(noteId))
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(
            route = Routes.EDITOR,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
            NoteEditorScreen(
                noteId = noteId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
