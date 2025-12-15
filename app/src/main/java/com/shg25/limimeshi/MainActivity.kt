package com.shg25.limimeshi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.shg25.limimeshi.core.ui.theme.LimimeshiTheme
import com.shg25.limimeshi.navigation.LimimeshiNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LimimeshiTheme {
                LimimeshiNavHost()
            }
        }
    }
}
