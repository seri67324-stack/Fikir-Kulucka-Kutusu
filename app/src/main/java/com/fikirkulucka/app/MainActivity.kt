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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
import com.fikirkulucka.app.ui.components.IncubationDashboardCanvas
import com.fikirkulucka.app.ui.components.SwipeReviewCard
import com.fikirkulucka.app.ui.theme.*

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

private val durationOptions = listOf("10DK", "1SA", "1G", "3G", "7G", "14G")

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
    var activeRoadmapIdeaId by remember { mutableStateOf<Long?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val activeRoadmapIdea = remember(activeRoadmapIdeaId, approvedIdeas) {
        approvedIdeas.find { it.id == activeRoadmapIdeaId }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        snackbarMessage = null
    }

    Scaffold(
        containerColor = PearlBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(VibrantCobalt, EmeraldMint))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Lightbulb,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                "Fikir Kuluçka Kutusu",
                                color = InkDark,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                            Text(
                                "${vm.totalCatalogCount} Fikir • ${activeIdeas.size} Kuluçka • ${approvedIdeas.size} Rehber",
                                color = SlateMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PearlBackground)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = VibrantCobalt,
                contentColor = Color.White,
                icon = { Icon(Icons.Rounded.AddCircleOutline, contentDescription = null) },
                text = { Text("Fikir Ekle", fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = PureCard) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Rounded.Explore, contentDescription = null) },
                    label = { Text("Öneriler", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Rounded.Timeline, contentDescription = null) },
                    label = { Text("Kuluçka", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Rounded.Swipe, contentDescription = null) },
                    label = { Text("Karar", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Rounded.RocketLaunch, contentDescription = null) },
                    label = { Text("Rehber", fontSize = 11.sp) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(PearlBackground, SoftTintCard.copy(alpha = 0.45f), PearlBackground)))
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
                        vm.incubateSuggestion(item, "3G")
                        snackbarMessage = "Kuluçkaya alındı: ${item.title}"
                    }
                )
                1 -> IncubatingListTab(
                    ideas = activeIdeas,
                    approvedCount = approvedIdeas.size,
                    onDecideNow = { pendingApprovalIdea = it }
                )
                2 -> ReviewSwipeTab(
                    ideas = activeIdeas,
                    onRequestApprove = { pendingApprovalIdea = it },
                    onDiscard = {
                        vm.archiveIdea(it)
                        snackbarMessage = "Fikir elendi."
                    },
                    onExtendIncubation = {
                        vm.extendIncubation(it, 3)
                        snackbarMessage = "Kuluçka süresi +3 gün uzatıldı."
                    }
                )
                3 -> ResultsTab(
                    approved = approvedIdeas,
                    archived = archivedIdeas,
                    onOpenRoadmap = { activeRoadmapIdeaId = it.id },
                    onChangeState = { idea, state ->
                        vm.updateExecutionState(idea, state)
                        snackbarMessage = "Durum: ${state.label}"
                    },
                    onDelete = vm::deleteIdea
                )
            }
        }

        pendingApprovalIdea?.let { idea ->
            ApprovalDecisionDialog(
                idea = idea,
                onDismiss = { pendingApprovalIdea = null },
                onConfirmState = { chosenState ->
                    vm.approveIdeaWithState(idea, chosenState)
                    pendingApprovalIdea = null
                    selectedTab = 3
                    snackbarMessage = "${chosenState.label} seçildi — Detaylı rehber açılıyor"
                }
            )
        }

        activeRoadmapIdea?.let { roadmapIdea ->
            FullExecutionRoadmapSheet(
                idea = roadmapIdea,
                onDismiss = { activeRoadmapIdeaId = null },
                onStateChange = { newState ->
                    vm.updateExecutionState(roadmapIdea, newState)
                    snackbarMessage = "Durum: ${newState.label}"
                },
                onToggleStep = { idx -> vm.toggleStepCompleted(roadmapIdea, idx) },
                onSaveNotes = { notes ->
                    vm.updateUserNotes(roadmapIdea, notes)
                    snackbarMessage = "Notların kaydedildi."
                }
            )
        }

        inspectedSuggestion?.let { detail ->
            SuggestionDetailSheet(
                suggestion = detail,
                onDismiss = { inspectedSuggestion = null },
                onIncubate = { code ->
                    vm.incubateSuggestion(detail, code)
                    snackbarMessage = "$code süreyle kuluçkaya yatırıldı"
                    inspectedSuggestion = null
                }
            )
        }

        if (showAddSheet) {
            QuickIdeaSheet(
                onDismiss = { showAddSheet = false },
                onSave = { text, code ->
                    vm.addIdea(text, code)
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
        containerColor = PureCard,
        title = {
            Text(
                "Onaylıyor musun? Durumunu Seç",
                color = InkDark,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    idea.content,
                    color = VibrantCobalt,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Nereden başlanmalı, adım adım nasıl yapılır, nasıl devam etmeli, somut örnek ve olası tuzakların bulunduğu tam rehbere geç:",
                    color = SlateMuted,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { onConfirmState(ExecutionState.STARTED) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantCobalt, contentColor = Color.White)
                ) {
                    Text("🚀 Başladım", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onConfirmState(ExecutionState.IN_PROGRESS) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldMint, contentColor = Color.White)
                ) {
                    Text("⚡ Devam Ediyoruz", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { onConfirmState(ExecutionState.CANCELLED) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("✖️ İptal Ettim", color = CoralRose, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Geri Dön", color = SlateMuted)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullExecutionRoadmapSheet(
    idea: IdeaEntity,
    onDismiss: () -> Unit,
    onStateChange: (ExecutionState) -> Unit,
    onToggleStep: (Int) -> Unit,
    onSaveNotes: (String) -> Unit
) {
    val plan = remember(idea.id, idea.content, idea.detailText, idea.stepsSerialized) {
        ActionGuideEngine.buildPlan(idea)
    }
    val completedSet = remember(idea.completedStepIndices) { idea.getCompletedSet() }
    var noteDraft by remember(idea.id, idea.userNotes) { mutableStateOf(idea.userNotes) }

    val totalSteps = plan.stepByStepExecution.size.coerceAtLeast(1)
    val doneSteps = completedSet.size.coerceAtMost(totalSteps)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PearlBackground
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(PureCard)
                        .border(1.dp, BorderSoft, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "DETAYLI UYGULAMA REHBERİ • $doneSteps/$totalSteps ADIM TAMAM",
                        color = VibrantCobalt,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        plan.title,
                        color = InkDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp
                    )
                    LinearProgressIndicator(
                        progress = { doneSteps.toFloat() / totalSteps.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = EmeraldMint,
                        trackColor = BorderSoft
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            ExecutionState.STARTED to "🚀 Başladım",
                            ExecutionState.IN_PROGRESS to "⚡ Devam Ediyoruz",
                            ExecutionState.CANCELLED to "✖️ İptal Ettim"
                        ).forEach { (st, lbl) ->
                            FilterChip(
                                selected = idea.executionState == st,
                                onClick = { onStateChange(st) },
                                label = { Text(lbl, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            item {
                LightGuideSection(
                    heading = "1. Fikir Nasıl Çalışır & Detaylı Açıklama",
                    accent = VibrantCobalt,
                    lines = listOf(plan.howItWorks)
                )
            }

            item {
                LightGuideSection(
                    heading = "2. Nereden Başlanmalı? (İlk 15 Dakika)",
                    accent = SunnyAmber,
                    lines = plan.whereToStart
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(PureCard)
                        .border(1.dp, BorderSoft, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "3. Adım Adım Nasıl Yapılır? (Tiklenebilir Liste)",
                        color = EmeraldMint,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    plan.stepByStepExecution.forEachIndexed { idx, stepText ->
                        val checked = completedSet.contains(idx)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (checked) Color(0xFFECFDF5) else PearlBackground)
                                .clickable { onToggleStep(idx) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (checked) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (checked) EmeraldMint else SlateMuted
                            )
                            Text(
                                text = stepText,
                                color = if (checked) SlateMuted else InkDark,
                                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                LightGuideSection(
                    heading = "4. Nasıl Devam Etmeli? (Sürdürme Stratejisi)",
                    accent = VibrantCobalt,
                    lines = plan.howToContinue
                )
            }

            item {
                LightGuideSection(
                    heading = "5. Somut Uygulama Örneği",
                    accent = EmeraldMint,
                    lines = listOf(plan.concreteExample)
                )
            }

            item {
                LightGuideSection(
                    heading = "6. Dikkat Edilmesi Gereken Tuzaklar",
                    accent = CoralRose,
                    lines = plan.pitfallsAndSolutions
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(PureCard)
                        .border(1.dp, BorderSoft, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "7. Bu Fikre Özel Notların",
                        color = InkDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    OutlinedTextField(
                        value = noteDraft,
                        onValueChange = { noteDraft = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Malzeme, ölçüm, link veya aklına gelen notu yaz...") },
                        minLines = 2
                    )
                    Button(
                        onClick = { onSaveNotes(noteDraft) },
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantCobalt, contentColor = Color.White)
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Notu Kaydet", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LightGuideSection(
    heading: String,
    accent: Color,
    lines: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PureCard)
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = heading,
            color = accent,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        lines.forEach { line ->
            Text(
                text = "• $line",
                color = InkDark.copy(alpha = 0.9f),
                fontSize = 13.sp,
                lineHeight = 19.sp
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
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = VibrantCobalt) },
            placeholder = { Text("Yemek, yazılım, alışkanlık veya anahtar kelime ara...") },
            shape = RoundedCornerShape(16.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(vertical = 8.dp),
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
            text = "${suggestions.size} fikir • Detay ve yapılış için karta dokun",
            color = SlateMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 6.dp)
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
            .background(PureCard)
            .border(1.dp, BorderSoft, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${item.category} • ${item.duration} • ${item.difficulty}",
                color = VibrantCobalt,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "#${item.id}",
                color = SlateMuted,
                fontSize = 11.sp
            )
        }
        Text(
            text = item.title,
            color = InkDark,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = item.description,
            color = SlateMuted,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onIncubate) {
                Icon(Icons.Rounded.Spa, contentDescription = null, tint = EmeraldMint, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Kuluçkaya Al (3G)", color = EmeraldMint, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuggestionDetailSheet(
    suggestion: SuggestedIdea,
    onDismiss: () -> Unit,
    onIncubate: (String) -> Unit
) {
    var selectedCode by remember { mutableStateOf("3G") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PearlBackground
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "${suggestion.category} • ${suggestion.duration} • ${suggestion.difficulty}",
                    color = VibrantCobalt,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    suggestion.title,
                    color = InkDark,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp
                )
            }
            item {
                LightGuideSection(
                    heading = "Açıklama ve Detay",
                    accent = VibrantCobalt,
                    lines = listOf(suggestion.description)
                )
            }
            item {
                LightGuideSection(
                    heading = "Nasıl Yapılır? (Adımlar)",
                    accent = EmeraldMint,
                    lines = suggestion.howToDoIt.mapIndexed { i, s -> "${i + 1}. $s" }
                )
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(PureCard)
                        .border(1.dp, BorderSoft, RoundedCornerShape(18.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Kuluçka Süresi Seç ($selectedCode)", color = InkDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(durationOptions) { code ->
                            FilterChip(
                                selected = selectedCode == code,
                                onClick = { selectedCode = code },
                                label = { Text(code, fontSize = 12.sp) }
                            )
                        }
                    }
                    Button(
                        onClick = { onIncubate(selectedCode) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantCobalt, contentColor = Color.White)
                    ) {
                        Text("Kuluçka Kutusuna Ekle", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun IncubatingListTab(
    ideas: List<IdeaEntity>,
    approvedCount: Int,
    onDecideNow: (IdeaEntity) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            IncubationDashboardCanvas(
                activeIdeas = ideas,
                approvedCount = approvedCount
            )
        }
        if (ideas.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Henüz kuluçkada fikir yok.\nÖneriler sekmesinden seç veya Fikir Ekle ile yeni fikir bırak.",
                        color = SlateMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(ideas, key = { it.id }) { idea ->
                IncubationCard(
                    idea = idea,
                    onDecideNow = onDecideNow
                )
            }
        }
    }
}

@Composable
private fun ReviewSwipeTab(
    ideas: List<IdeaEntity>,
    onRequestApprove: (IdeaEntity) -> Unit,
    onDiscard: (IdeaEntity) -> Unit,
    onExtendIncubation: (IdeaEntity) -> Unit
) {
    val currentIdea = ideas.firstOrNull()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (currentIdea == null) {
            Text(
                "İncelenecek fikir kalmadı.",
                color = SlateMuted
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SwipeReviewCard(
                    idea = currentIdea,
                    onApprove = onRequestApprove,
                    onDiscard = onDiscard,
                    onExtendIncubation = onExtendIncubation
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = { onDiscard(currentIdea) }) {
                        Text("Vazgeç", color = CoralRose, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(onClick = { onExtendIncubation(currentIdea) }) {
                        Text("+3G Uzat", color = SunnyAmber, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onRequestApprove(currentIdea) },
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantCobalt, contentColor = Color.White)
                    ) {
                        Text("Onayla", fontWeight = FontWeight.Bold)
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
                "Onaylanan Projeler & Detaylı Rehberler (${approved.size})",
                color = InkDark,
                fontWeight = FontWeight.ExtraBold,
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
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Elenenler (${archived.size})",
                color = SlateMuted,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        items(archived, key = { "arc_${it.id}" }) { item ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = PureCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(CoralRose, RoundedCornerShape(50))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = item.content, color = SlateMuted, modifier = Modifier.weight(1f))
                    IconButton(onClick = { onDelete(item) }) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Sil", tint = SlateMuted)
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
        ExecutionState.STARTED -> VibrantCobalt
        ExecutionState.IN_PROGRESS -> EmeraldMint
        ExecutionState.CANCELLED -> CoralRose
        ExecutionState.PENDING -> SunnyAmber
    }
    val stepCount = idea.getCompletedSet().size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PureCard)
            .border(1.2.dp, stateColor.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .clickable(onClick = onOpenRoadmap)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${idea.executionState.label.uppercase()} • $stepCount ADIM TIKLI",
                color = stateColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Sil", tint = SlateMuted)
            }
        }
        Text(
            text = idea.content,
            color = InkDark,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Text(
            text = "📖 Nereden başlanır, nasıl yapılır, somut örnek ve notlar için dokun",
            color = VibrantCobalt,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
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
    onSave: (String, String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("10DK") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PureCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Yeni Fikir Ekle", color = InkDark, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Aklındaki ham fikri yaz...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 2
            )
            Text("Kuluçka süresi seç ($code):", color = VibrantCobalt, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(durationOptions) { opt ->
                    FilterChip(
                        selected = code == opt,
                        onClick = { code = opt },
                        label = { Text(opt, fontSize = 12.sp) }
                    )
                }
            }
            Button(
                onClick = { onSave(text, code) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VibrantCobalt, contentColor = Color.White)
            ) {
                Text("Kuluçkaya Yatır", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
