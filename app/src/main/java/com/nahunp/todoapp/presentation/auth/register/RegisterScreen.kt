package com.nahunp.todoapp.presentation.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.registeredSuccessfully) {
        if (state.registeredSuccessfully) onRegisterSuccess()
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Register", style = MaterialTheme.typography.headlineSmall)

        // See RegisterViewModel's doc comment — CAPTCHA isn't wired up yet,
        // this will currently fail server-side. Visible on purpose rather
        // than hidden, so it's obvious this screen isn't done.
        Text(
            "Registration isn't functional yet — see RegisterViewModel's doc comment.",
            color = MaterialTheme.colorScheme.error,
        )

        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Email") },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = viewModel::submit,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.isLoading) "Creating account…" else "Register")
        }
    }
}
