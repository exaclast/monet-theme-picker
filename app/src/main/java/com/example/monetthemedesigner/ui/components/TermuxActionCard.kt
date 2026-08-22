package com.example.monetthemedesigner.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.monetthemedesigner.util.TermuxIntegration

@Composable
fun TermuxActionCard(
    command: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Shell Command", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = command,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { TermuxIntegration.copyToClipboard(context, command) }) {
                    Text("Copy")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { TermuxIntegration.runTermuxCommand(context, command) }) {
                    Text("Run in Termux")
                }
            }
        }
    }
}
