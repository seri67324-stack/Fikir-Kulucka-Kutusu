package com.fikirkulucka.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EggAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fikirkulucka.app.data.ActionGuideEngine
import com.fikirkulucka.app.data.ExecutionState
import com.fikirkulucka.app.data.IdeaCatalogEngine
import com.fikirkulucka.app.data.IdeaEntity
import com.fikirkulucka.app.data.SuggestedIdea
import com.fikirkulucka.app.ui.components.IncubationCard
import com.fikirkulucka.app.ui.components.SwipeReviewCard
import com.fikirkulucka.app.ui.theme.FikirKuluckaTheme
import com.fikirkulucka.app.ui.theme.NeonCyan
import com.fikirkulucka.app.ui.theme.RoseDiscard
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
    val suggestions by vm.filteredSuggestions.collectAsState()
    val selectedCategory by vm.selectedCategory.collectAsState()
    val searchQuery by vm.searchQuery.collectAsState()
    val activeIdeas by vm.activeIdeas.collectAsState()
    val approvedIdeas by vm.approvedIdeas.collectAsState()
    val archivedIdeas by vm.archivedIdeas.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddSheet by remember { mutableStateOf(false) }
    var inspectedSuggestion by remember { mutableStateOf<SuggestedIdea?>(null) }
    var pendingApprovalIdea by remember { mutableStateOf<IdeaEntity?>(null) }
    var activeRoadmapIdea by remember { mutableStateOf<IdeaEntity?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        snackbarMessage = null
    }

    Scaffold(
        containerColor = VoidBlack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                            "Öneri: ${vm.totalCatalogCount} • Kuluçka: ${activeIdeas.size} • Aktif Proje: ${approvedIdeas.size}",
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
                Icon(Icons.Rounded.Add, contentDescription = "Yeni Fikir Ekle")
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF0C1220)) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null) },
                    label = { Text("10.5B Öneri") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Rounded.EggAlt, contentDescription = null) },
                    label = { Text("Kuluçka") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Rounded.Style, contentDescription = null) },
                    label = { Text("Karar") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Rounded.CheckCircle, contentDescription = null) },
                    label = { Text("Yol Haritası") }
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
                0 -> ExploreSuggestionsTab(
                    suggestions = suggestions,
                    selectedCategory = selectedCategory,
                    searchQuery = searchQuery,
                    onCategorySelected = vm::selectCategory,
                    onSearchChanged = vm::updateSearchQuery,
                    onInspect = { inspectedSuggestion = it },
                    onQuickIncubate = { item ->
                        vm.incubateSuggestion(item, 3)
                        snackbarMessage = "Kuluçkaya alındı: ${item.title}"
                    }
                )
                1 -> IncubatingListTab(ideas = activeIdeas)
                2 -> ReviewSwipeTab(
                    ideas = activeIdeas,
                    onRequestApprove = { idea -> pendingApprovalIdea = idea },
                    onDiscard = { idea ->
                        vm.archiveIdea(idea)
                        snackbarMessage = "Fikir elendi."
                    }
                )
                3 -> ResultsTab(
                    approved = approvedIdeas,
                    archived = archivedIdeas,
                    onOpenRoadmap = { activeRoadmapIdea = it },
                    onChangeState = { idea, state ->
                        vm.updateExecutionState(idea, state)
                        snackbarMessage = "Durum güncellendi: ${state.label}"
                    },
                    onDelete = vm::deleteIdea
                )
            }
        }

        // Onaylarken "Başladım / Devam Ediyoruz / İptal Ettim" seçim penceresi + detay önizleme
        pendingApprovalIdea?.let { idea ->
            ApprovalDecisionDialog(
                idea = idea,
                onDismiss = { pendingApprovalIdea = null },
                onConfirmState = { chosenState ->
                    val updated = idea.copy(executionState = chosenState)
                    vm.approveIdeaWithState(idea, chosenState)
                    pendingApprovalIdea = null
                    selectedTab = 3
                    activeRoadmapIdea = updated
                }
            )
        }

        // Onaylanan fikrin Aşırı Detaylı Uygulama Rehberi
        activeRoadmapIdea?.let { roadmapIdea ->
            FullExecutionRoadmapSheet(
                idea = roadmapIdea,
                onDismiss = { activeRoadmapIdea = null },
                onStateChange = { newState ->
                    vm.updateExecutionState(roadmapIdea, newState)
                    activeRoadmapIdea = roadmapIdea.copy(executionState = newState)
                    snackbarMessage = "Durum: ${newState.label}"
                }
            )
        }

        inspectedSuggestion?.let { detail ->
            SuggestionDetailSheet(
                suggestion = detail,
                onDismiss = { inspectedSuggestion = null },
                onIncubate = { days ->
                    vm.incubateSuggestion(detail, days)
                    snackbarMessage = "$days gün kuluçkaya yatırıldı"
                    inspectedSuggestion = null
                }
            )
        }

        if (showAddSheet) {
            QuickIdeaSheet(
                onDismiss = { showAddSheet = false },
                onSave = { text, days ->
                    vm.addIdea(text, days)
                    snackbarMessage = "Kendi fikrin kuluçkaya yatırıldı"
                    showAddSheet = false
                }
            )
        }
    }
}

@Composable
private fun ApprovalDecisionDialog(
    idea: IdeaEntity,
    onDismiss: () -> Unit,
    onConfirmState: (ExecutionState) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131B2E),
        title = {
            Text(
                "Onaylıyor musun? Durumunu Seç",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    idea.content,
                    color = NeonCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Seçim yaptığın anda bu fikir için 'Nereden Başlanmalı, Nasıl Yapılır, Nasıl Devam Etmeli ve Somut Örnek' içeren tam uygulama rehberi açılacak:",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { onConfirmState(ExecutionState.STARTED) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = VoidBlack)
                ) {
                    Text("🚀 Başladım (Hemen Uygula & Rehberi Aç)", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onConfirmState(ExecutionState.IN_PROGRESS) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = WarmAmber, contentColor = VoidBlack)
                ) {
                    Text("⚡ Devam Ediyoruz (Süreci Sürdür & Rehberi Aç)", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { onConfirmState(ExecutionState.CANCELLED) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("✖️ İptal Ettim (Kaydet ve Notlara Al)", color = RoseDiscard)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Geri Dön", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullExecutionRoadmapSheet(
    idea: IdeaEntity,
    onDismiss: () -> Unit,
    onStateChange: (ExecutionState) -> Unit
) {
    val plan = remember(idea.id, idea.content, idea.detailText, idea.stepsSerialized) {
        ActionGuideEngine.buildPlan(idea)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0E1525)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    "TAM UYGULAMA & YOL HARİTASI REHBERİ",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    plan.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Mevcut Durumun:", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    listOf(
                        ExecutionState.STARTED to "🚀 Başladım",
                        ExecutionState.IN_PROGRESS to "⚡ Devam Ediyoruz",
                        ExecutionState.CANCELLED to "✖️ İptal Ettim"
                    ).forEach { (st, label) ->
                        FilterChip(
                            selected = idea.executionState == st,
                            onClick = { onStateChange(st) },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }
            }

            item {
                RoadmapSectionCard(
                    heading = "1. Detaylı Açıklama ve Nasıl Çalışacağı",
                    accent = NeonCyan,
                    lines = listOf(plan.howItWorks)
                )
            }

            item {
                RoadmapSectionCard(
                    heading = "2. Nereden Başlanmalı? (İlk 15 Dakika)",
                    accent = WarmAmber,
                    lines = plan.whereToStart
                )
            }

            item {
                RoadmapSectionCard(
                    heading = "3. Adım Adım Nasıl Yapılır?",
                    accent = NeonCyan,
                    lines = plan.stepByStepExecution
                )
            }

            item {
                RoadmapSectionCard(
                    heading = "4. Nasıl Devam Etmeli? (Sürdürme Planı)",
                    accent = WarmAmber,
                    lines = plan.howToContinue
                )
            }

            item {
                RoadmapSectionCard(
                    heading = "5. Somut Uygulama Örneği",
                    accent = NeonCyan,
                    lines = listOf(plan.concreteExample)
                )
            }

            item {
                RoadmapSectionCard(
                    heading = "6. Olası Tuzaklar ve Çözümleri",
                    accent = RoseDiscard,
                    lines = plan.pitfallsAndSolutions
                )
            }
        }
    }
}

@Composable
private fun RoadmapSectionCard(
    heading: String,
    accent: Color,
    lines: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF151F34))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = heading,
            color = accent,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        lines.forEach { line ->
            Text(
                text = "• $line",
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun ExploreSuggestionsTab(
    suggestions: List<SuggestedIdea>,
    selectedCategory: String,
    searchQuery: String,
    onCategorySelected: (String) -> Unit,
    onSearchChanged: (String) -> Unit,
    onInspect: (SuggestedIdea) -> Unit,
    onQuickIncubate: (SuggestedIdea) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            singleLine = true,
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = NeonCyan) },
            placeholder = { Text("Yemek, yazılım, alışkanlık veya anahtar kelime ara...") },
            shape = RoundedCornerShape(16.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(IdeaCatalogEngine.categories, key = { it }) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { onCategorySelected(cat) },
                    label = { Text(cat, fontSize = 12.sp) }
                )
            }
        }

        Text(
            text = "${suggestions.size} fikir listeleniyor • Detay ve yapılış için karta dokun",
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(suggestions, key = { it.id }, contentType = { "suggestion" }) { item ->
                SuggestionCard(
                    item = item,
                    onClick = { onInspect(item) },
                    onIncubate = { onQuickIncubate(item) }
                )
            }
        }
    }
}

@Composable
private fun SuggestionCard(
    item: SuggestedIdea,
    onClick: () -> Unit,
    onIncubate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF121A2C))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${item.category}  •  ${item.duration}  •  ${item.difficulty}",
                color = NeonCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "#${item.id}",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp
            )
        }
        Text(
            text = item.title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = item.description,
            color = Color.White.copy(alpha = 0.78f),
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onIncubate) {
                Text("Kuluçkaya Al (3G)", color = WarmAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuggestionDetailSheet(
    suggestion: SuggestedIdea,
    onDismiss: () -> Unit,
    onIncubate: (Int) -> Unit
) {
    var days by remember { mutableIntStateOf(3) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF111827)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "${suggestion.category} • ${suggestion.duration} • ${suggestion.difficulty}",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    suggestion.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                )
            }
            item {
                Text("Açıklama ve Detay", color = WarmAmber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    suggestion.description,
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
            item {
                Text("Nasıl Yapılır? (Adımlar)", color = WarmAmber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            items(suggestion.howToDoIt.mapIndexed { idx, step -> (idx + 1) to step }) { (num, step) ->
                Text(
                    "$num. $step",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Kuluçka Süresi Seç ($days gün)", color = NeonCyan, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(1, 3, 7, 14).forEach { preset ->
                        FilterChip(
                            selected = days == preset,
                            onClick = { days = preset },
                            label = { Text("${preset}G") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onIncubate(days) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = VoidBlack)
                ) {
                    Text("Kuluçka Kutusuna Ekle", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun IncubatingListTab(ideas: List<IdeaEntity>) {
    if (ideas.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Henüz kuluçkada fikir yok.\n10.5B Öneri sekmesinden seç veya + ile kendi fikrini ekle.",
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
    onRequestApprove: (IdeaEntity) -> Unit,
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
                    onApprove = onRequestApprove,
                    onDiscard = onDiscard
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = { onDiscard(currentIdea) }) {
                        Text("Vazgeç", color = RoseDiscard)
                    }
                    Button(
                        onClick = { onRequestApprove(currentIdea) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = VoidBlack)
                    ) {
                        Text("Onayla & Detaylı Rehberi Aç", fontWeight = FontWeight.Bold)
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
    onOpenRoadmap: (IdeaEntity) -> Unit,
    onChangeState: (IdeaEntity, ExecutionState) -> Unit,
    onDelete: (IdeaEntity) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "Onaylanan Projeler & Yol Haritaları (${approved.size})",
                color = WarmAmber,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        items(approved, key = { "app_${it.id}" }) { item ->
            ApprovedRoadmapRow(
                idea = item,
                onOpenRoadmap = { onOpenRoadmap(item) },
                onChangeState = { st -> onChangeState(item, st) },
                onDelete = { onDelete(item) }
            )
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
                            .background(RoseDiscard, RoundedCornerShape(50))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = item.content, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.weight(1f))
                    IconButton(onClick = { onDelete(item) }) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Sil", tint = Color.White.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ApprovedRoadmapRow(
    idea: IdeaEntity,
    onOpenRoadmap: () -> Unit,
    onChangeState: (ExecutionState) -> Unit,
    onDelete: () -> Unit
) {
    val stateColor = when (idea.executionState) {
        ExecutionState.STARTED -> NeonCyan
        ExecutionState.IN_PROGRESS -> WarmAmber
        ExecutionState.CANCELLED -> RoseDiscard
        ExecutionState.PENDING -> Color.White
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF131B2E))
            .border(1.dp, stateColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .clickable(onClick = onOpenRoadmap)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "DURUM: ${idea.executionState.label.uppercase()}",
                color = stateColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Sil", tint = Color.White.copy(alpha = 0.6f))
            }
        }
        Text(
            text = idea.content,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
        Text(
            text = "📖 Detaylı nasıl yapılır, nereden başlanır ve somut örnek rehberi için dokun",
            color = NeonCyan.copy(alpha = 0.9f),
            fontSize = 12.sp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                ExecutionState.STARTED to "Başladım",
                ExecutionState.IN_PROGRESS to "Devam Ediyoruz",
                ExecutionState.CANCELLED to "İptal Ettim"
            ).forEach { (st, lbl) ->
                FilterChip(
                    selected = idea.executionState == st,
                    onClick = { onChangeState(st) },
                    label = { Text(lbl, fontSize = 11.sp) }
                )
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
            Text("Yeni Fikir Ekle", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
