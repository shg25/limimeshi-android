package com.shg25.limimeshi.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shg25.limimeshi.R

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToChainList: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val webClientId = context.getString(R.string.default_web_client_id)

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LoginContent(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            onGoogleSignIn = { viewModel.signInWithGoogle(context, webClientId) },
            onSignOut = viewModel::signOut,
            onNavigateToChainList = onNavigateToChainList
        )
    }
}

@Composable
private fun LoginContent(
    modifier: Modifier = Modifier,
    uiState: LoginUiState,
    onGoogleSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateToChainList: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LoginHeader()
        Spacer(modifier = Modifier.height(48.dp))

        if (uiState.isLoggedIn) {
            LoggedInSection(
                userName = uiState.userName,
                onNavigateToChainList = onNavigateToChainList,
                onSignOut = onSignOut
            )
        } else {
            LoggedOutSection(
                isLoading = uiState.isLoading,
                onGoogleSignIn = onGoogleSignIn,
                onNavigateToChainList = onNavigateToChainList
            )
        }
    }
}

@Composable
private fun LoginHeader() {
    Text(
        text = "リミメシ",
        style = MaterialTheme.typography.headlineLarge
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "期間限定メニュー情報",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun LoggedInSection(
    userName: String?,
    onNavigateToChainList: () -> Unit,
    onSignOut: () -> Unit
) {
    Text(
        text = "ようこそ、${userName ?: "ユーザー"}さん",
        style = MaterialTheme.typography.titleMedium
    )
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = onNavigateToChainList,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("チェーン店一覧へ")
    }
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedButton(
        onClick = onSignOut,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("ログアウト")
    }
}

@Composable
private fun LoggedOutSection(
    isLoading: Boolean,
    onGoogleSignIn: () -> Unit,
    onNavigateToChainList: () -> Unit
) {
    Text(
        text = "お気に入り機能を使うには\nログインが必要です",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = onGoogleSignIn,
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text("Googleでログイン")
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedButton(
        onClick = onNavigateToChainList,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("ログインせずに続ける")
    }
}
