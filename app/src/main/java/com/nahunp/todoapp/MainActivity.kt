package com.nahunp.todoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.nahunp.todoapp.presentation.navigation.AppEntryViewModel
import com.nahunp.todoapp.presentation.navigation.TodoNavHost
import com.nahunp.todoapp.presentation.theme.TodoAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val entryViewModel: AppEntryViewModel = hiltViewModel()
                    val startDestination by entryViewModel.startDestination.collectAsState()

                    // AppEntryViewModel resolves this once (checking
                    // whether a token already exists) before NavHost can
                    // be composed at all — Compose Navigation needs a
                    // concrete startDestination up front, there's no
                    // "navigate once ready" for the very first screen.
                    // Brief on a warm token-store read; not worth a
                    // dedicated splash *screen* for that.
                    val destination = startDestination
                    if (destination == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        TodoNavHost(startDestination = destination)
                    }
                }
            }
        }
    }
}
