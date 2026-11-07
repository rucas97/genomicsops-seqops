package com.genomicsops.seqops

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genomicsops.seqops.ui.GenomicsTheme
import com.genomicsops.seqops.ui.SplashScreen
import com.genomicsops.seqops.ui.GenomicsCyan
import com.genomicsops.seqops.ui.GenomicsSurface
import com.genomicsops.seqops.ui.GenomicsText

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GenomicsTheme { AppRoot() } }
    }
}

@Composable
fun AppRoot() {
    var showSplash by remember { mutableStateOf(true) }
    if (showSplash) SplashScreen { showSplash = false }
    else {
        var input by remember { mutableStateOf("") }
        val cleanSeq = input.uppercase().filter { it == 'A' || it == 'T' || it == 'C' || it == 'G' }
        val gcCount = cleanSeq.count { it == 'G' || it == 'C' }
        val gcPercent = if (cleanSeq.isEmpty()) 0f else (gcCount.toFloat() / cleanSeq.length) * 100f
        val reverseComp = cleanSeq.reversed().map {
            when(it) { 'A' -> 'T'; 'T' -> 'A'; 'C' -> 'G'; 'G' -> 'C'; else -> it }
        }.joinToString("")

        Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("SeqOps Analyzer", color = GenomicsCyan, fontSize = 24.sp)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = input, onValueChange = { input = it },
                label = { Text("Paste DNA Sequence") },
                modifier = Modifier.fillMaxWidth().height(150.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text("Length: ${cleanSeq.length} bp", color = GenomicsText)
            Text("GC Content: ${String.format("%.1f", gcPercent)}%", color = GenomicsText)
            Spacer(Modifier.height(16.dp))
            Text("Reverse Complement:", color = GenomicsText)
            Text(reverseComp, color = GenomicsCyan, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
