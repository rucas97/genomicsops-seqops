package com.genomicsops.seqops

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genomicsops.seqops.ui.*

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
    if (showSplash) {
        SplashScreen { showSplash = false }
    } else {
        var input by remember { mutableStateOf("") }
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
            Text("SeqOps Analyzer", color = GenomicsCyan, fontSize = 28.sp)
            Spacer(Modifier.height(16.dp))
            
            OutlinedTextField(
                value = input, onValueChange = { input = it },
                label = { Text("Paste DNA Sequence") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GenomicsCyan)
            )
            
            Spacer(Modifier.height(24.dp))
            Text("Length: ${cleanSeq.length} bp", color = GenomicsText, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Text("GC Content: ${String.format("%.1f", gcPercent)}%", color = GenomicsTeal, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            
            Spacer(Modifier.height(24.dp))
            Text("Reverse Complement:", color = GenomicsMuted)
            Text(reverseComp, color = GenomicsCyan, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 8.dp).background(GenomicsSurface, RoundedCornerShape(4.dp)).padding(8.dp).fillMaxWidth())
            
            Spacer(Modifier.height(24.dp))
            Text("Protein Translation (Frame 1):", color = GenomicsMuted)
            Text(protein, color = Color(0xFFFFAA00), fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 8.dp).background(GenomicsSurface, RoundedCornerShape(4.dp)).padding(8.dp).fillMaxWidth())
            Spacer(Modifier.height(32.dp))
        }
    }
}
