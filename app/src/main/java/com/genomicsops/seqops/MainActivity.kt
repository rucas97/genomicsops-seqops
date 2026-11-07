package com.genomicsops.seqops

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genomicsops.seqops.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GenomicsTheme { AppRoot() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    var showSplash by remember { mutableStateOf(true) }
    if (showSplash) SplashScreen { showSplash = false }
    else {
        val context = LocalContext.current
        val clipboardManager = LocalClipboardManager.current
        var input by remember { mutableStateOf("") }

        val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                try {
                    val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                    input = text?.lines()?.filter { !it.startsWith(">") }?.joinToString("") ?: ""
                } catch (e: Exception) { }
            }
        }

        val cleanSeq = input.uppercase().filter { it == 'A' || it == 'T' || it == 'C' || it == 'G' }
        val gcCount = cleanSeq.count { it == 'G' || it == 'C' }
        val gcPercent = if (cleanSeq.isEmpty()) 0f else (gcCount.toFloat() / cleanSeq.length) * 100f
        
        val reverseComp = cleanSeq.reversed().map {
            when(it) { 'A' -> 'T'; 'T' -> 'A'; 'C' -> 'G'; 'G' -> 'C'; else -> it }
        }.joinToString("")

        val protein = cleanSeq.windowed(3, 3).map { codon ->
            when(codon) {
                "ATG" -> "M"; "TAA", "TAG", "TGA" -> "*"
                "GCT", "GCC", "GCA", "GCG" -> "A"; "CGT", "CGC", "CGA", "CGG", "AGA", "AGG" -> "R"
                "AAT", "AAC" -> "N"; "GAT", "GAC" -> "D"; "TGT", "TGC" -> "C"
                "CAA", "CAG" -> "Q"; "GAA", "GAG" -> "E"; "GGT", "GGC", "GGA", "GGG" -> "G"
                "CAT", "CAC" -> "H"; "ATT", "ATC", "ATA" -> "I"; "TTA", "TTG", "CTT", "CTC", "CTA", "CTG" -> "L"
                "AAA", "AAG" -> "K"; "TTT", "TTC" -> "F"; "CCT", "CCC", "CCA", "CCG" -> "P"
                "TCT", "TCC", "TCA", "TCG", "AGT", "AGC" -> "S"; "ACT", "ACC", "ACA", "ACG" -> "T"
                "TGG" -> "W"; "TAT", "TAC" -> "Y"; "GTT", "GTC", "GTA", "GTG" -> "V"
                else -> "X"
            }
        }.joinToString("")
        Column(Modifier.fillMaxSize().background(GenomicsDark).padding(16.dp).verticalScroll(rememberScrollState())) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("SeqOps Analyzer", color = GenomicsCyan, fontSize = 28.sp)
                IconButton(onClick = { filePicker.launch("*/*") }) {
                    Icon(Icons.Default.FolderOpen, contentDescription = "Load FASTA", tint = GenomicsTeal)
                }
            }
            Spacer(Modifier.height(16.dp))
            
            OutlinedTextField(
                value = input, onValueChange = { input = it },
                label = { Text("Paste DNA Sequence or Load FASTA") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GenomicsCyan)
            )
            
            Spacer(Modifier.height(24.dp))
            Text("Length: ${cleanSeq.length} bp", color = GenomicsText, fontWeight = FontWeight.Bold)
            Text("GC Content: ${String.format("%.1f", gcPercent)}%", color = GenomicsTeal, fontWeight = FontWeight.Bold)
            
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Reverse Complement:", color = GenomicsMuted)
                IconButton(onClick = { clipboardManager.setText(AnnotatedString(reverseComp)); Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show() }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = GenomicsCyan, modifier = Modifier.size(18.dp))
                }
            }
            Text(reverseComp, color = GenomicsCyan, fontFamily = FontFamily.Monospace, modifier = Modifier.background(GenomicsSurface, RoundedCornerShape(4.dp)).padding(8.dp).fillMaxWidth())
            
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Protein Translation (Frame 1):", color = GenomicsMuted)
                IconButton(onClick = { clipboardManager.setText(AnnotatedString(protein)); Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show() }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFFFFAA00), modifier = Modifier.size(18.dp))
                }
            }
            Text(protein, color = Color(0xFFFFAA00), fontFamily = FontFamily.Monospace, modifier = Modifier.background(GenomicsSurface, RoundedCornerShape(4.dp)).padding(8.dp).fillMaxWidth())
            Spacer(Modifier.height(32.dp))
        }
    }
}
