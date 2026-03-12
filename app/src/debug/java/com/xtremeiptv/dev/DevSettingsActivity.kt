package com.xtremeiptv.dev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xtremeiptv.core.designsystem.theme.XtremeIPTVTheme

class DevSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            XtremeIPTVTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DevSettingsScreen()
                }
            }
        }
    }
}

@Composable
fun DevSettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Developer Settings",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Button(
            onClick = { /* Load sample M3U */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Load Sample M3U Playlist")
        }
        
        Button(
            onClick = { /* Load sample Xtream */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Load Sample Xtream Codes")
        }
        
        Button(
            onClick = { /* Clear database */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear Database")
        }
        
        Button(
            onClick = { /* Toggle debug logs */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Toggle Debug Logs")
        }
    }
}
