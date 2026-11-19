package com.fikirkulucka.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EggAlt
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fikirkulucka.app.data.IdeaEntity
import com.fikirkulucka.app.ui.components.IncubationCard
import com.fikirkulucka.app.ui.components.SwipeReviewCard
import com.fikirkulucka.app.ui.theme.FikirKuluckaTheme
import com.fikirkulucka.app.ui.theme.NeonCyan
import com.fikirkulucka.app.ui.theme.VoidBlack
import com.fikirkulucka.app.ui.theme.WarmAmber

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FikirKuluckaTheme {
                FikirKuluckaApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FikirKuluckaApp(vm: IdeaViewModel = viewModel()) {
    val activeIdeas by vm.activeIdeas.collectAsState()
    val approvedIdeas by vm.approvedIdeas.collectAsState()
    val archivedIdeas by vm.archivedIdeas.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = VoidBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Fikir Kuluçka Kutusu",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "Kuluçkada: ${activeIdeas.size}  •  Onaylanan: ${approvedIdeas.size}",
                            color = NeonCyan,
                            fontSize = 12.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VoidBlack)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = NeonCyan,
                contentColor = VoidBlack
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Yeni Fikir")
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF0C1220)) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Rounded.EggAlt, contentDescription = null) },
                    label = { Text("Kuluçka") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Rounded.Style, contentDescription = null) },
                    label = { Text("Karar") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Rounded.CheckCircle, contentDescription = null) },
                    label = { Text("Sonuçlar") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(VoidBlack, Color(0xFF0B1221), VoidBlack)
                    )
                )
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            when (selectedTab) {
                0 -> IncubatingListTab(ideas = activeIdeas)
                1 -> ReviewSwipeTab(
                    ideas = activeIdeas,
                    onApprove = vm::approveIdea,
                    onDiscard = vm::archiveIdea
                )
                2 -> ResultsTab(
                    approved = approvedIdeas,
                    archived = archivedIdeas,
                    onDelete = vm::deleteIdea
                )
            }
        }

        if (showAddSheet) {
            QuickIdeaSheet(
                onDismiss = { showAddSheet = false },
                onSave = { text, days ->
                    vm.addIdea(text, days)
                    showAddSheet = false
                }
            )
        }
    }
}

@Composable
private fun IncubatingListTab(ideas: List<IdeaEntity>) {
    if (ideas.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Henüz kuluçkada fikir yok.\n+ butonuyla ham bir fikir bırak.",
                color = Color.White.copy(alpha = 0.65f)
            )
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(ideas, key = { it.id }) { idea ->
            IncubationCard(idea = idea)
        }
    }
}

@Composable
private fun ReviewSwipeTab(
    ideas: List<IdeaEntity>,
    onApprove: (IdeaEntity) -> Unit,
    onDiscard: (IdeaEntity) -> Unit
) {
    val currentIdea = ideas.firstOrNull()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (currentIdea == null) {
            Text(
                "İncelenecek fikir kalmadı.",
                color = Color.White.copy(alpha = 0.65f)
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                SwipeReviewCard(
                    idea = currentIdea,
                    onApprove = onApprove,
                    onDiscard = onDiscard
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = { onDiscard(currentIdea) }) {
                        Text("Vazgeç", color = Color(0xFFFF4D6D))
                    }
                    Button(
                        onClick = { onApprove(currentIdea) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = VoidBlack)
                    ) {
                        Text("Onayla")
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultsTab(
    approved: List<IdeaEntity>,
    archived: List<IdeaEntity>,
    onDelete: (IdeaEntity) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "Yapılmaya Değer (${approved.size})",
                color = WarmAmber,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        items(approved, key = { "app_${it.id}" }) { item ->
            ResultRow(text = item.content, badgeColor = NeonCyan, onDelete = { onDelete(item) })
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Elenenler (${archived.size})",
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        items(archived, key = { "arc_${it.id}" }) { item ->
            ResultRow(text = item.content, badgeColor = Color(0xFFFF4D6D), onDelete = { onDelete(item) })
        }
    }
}

@Composable
private fun ResultRow(text: String, badgeColor: Color, onDelete: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF131B2E),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(badgeColor, RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, color = Color.White, modifier = Modifier.weight(1f))
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Sil", tint = Color.White.copy(alpha = 0.6f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickIdeaSheet(
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var days by remember { mutableIntStateOf(3) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF111827)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Yeni Fikir Bırak", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Aklındaki ham fikri yaz...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 2
            )
            Text("Kuluçka süresi: $days gün", color = NeonCyan, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(1, 3, 7, 14).forEach { preset ->
                    FilterChip(
                        selected = days == preset,
                        onClick = { days = preset },
                        label = { Text("${preset}G") }
                    )
                }
            }
            Button(
                onClick = { onSave(text, days) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = VoidBlack)
            ) {
                Text("Kuluçkaya Yatır", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
