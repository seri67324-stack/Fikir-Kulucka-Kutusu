package com.fikirkulucka.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fikirkulucka.app.data.AppDatabase
import com.fikirkulucka.app.data.ExecutionState
import com.fikirkulucka.app.data.IdeaCatalogEngine
import com.fikirkulucka.app.data.IdeaEntity
import com.fikirkulucka.app.data.IdeaStatus
import com.fikirkulucka.app.data.SuggestedIdea
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class IdeaViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).ideaDao()

    private val _selectedCategory = MutableStateFlow("Tümü")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val totalCatalogCount: Int = IdeaCatalogEngine.getTotalCount()

    val filteredSuggestions: StateFlow<List<SuggestedIdea>> =
        combine(_selectedCategory, _searchQuery) { category, query ->
            IdeaCatalogEngine.queryIdeas(category, query)
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), IdeaCatalogEngine.queryIdeas("Tümü", ""))

    val activeIdeas: StateFlow<List<IdeaEntity>> = dao.observeActiveIdeas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvedIdeas: StateFlow<List<IdeaEntity>> = dao.observeApprovedIdeas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedIdeas: StateFlow<List<IdeaEntity>> = dao.observeArchivedIdeas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addIdea(rawText: String, incubationDays: Int) {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) return
        val now = System.currentTimeMillis()
        val matureAt = now + TimeUnit.DAYS.toMillis(incubationDays.toLong())
        viewModelScope.launch {
            dao.insert(
                IdeaEntity(
                    content = trimmed,
                    createdAt = now,
                    matureAt = matureAt,
                    status = IdeaStatus.INCUBATING,
                    executionState = ExecutionState.PENDING
                )
            )
        }
    }

    fun incubateSuggestion(suggestion: SuggestedIdea, incubationDays: Int = 3) {
        val now = System.currentTimeMillis()
        val matureAt = now + TimeUnit.DAYS.toMillis(incubationDays.toLong())
        viewModelScope.launch {
            dao.insert(
                IdeaEntity(
                    content = "[${suggestion.category}] ${suggestion.title}",
                    createdAt = now,
                    matureAt = matureAt,
                    status = IdeaStatus.INCUBATING,
                    executionState = ExecutionState.PENDING,
                    category = suggestion.category,
                    detailText = suggestion.description,
                    stepsSerialized = suggestion.howToDoIt.joinToString("||")
                )
            )
        }
    }

    fun approveIdeaWithState(idea: IdeaEntity, state: ExecutionState) {
        viewModelScope.launch {
            dao.update(
                idea.copy(
                    status = IdeaStatus.APPROVED,
                    executionState = state
                )
            )
        }
    }

    fun updateExecutionState(idea: IdeaEntity, state: ExecutionState) {
        viewModelScope.launch {
            dao.update(idea.copy(executionState = state))
        }
    }

    fun archiveIdea(idea: IdeaEntity) {
        viewModelScope.launch {
            dao.update(
                idea.copy(
                    status = IdeaStatus.ARCHIVED,
                    executionState = ExecutionState.CANCELLED
                )
            )
        }
    }

    fun deleteIdea(idea: IdeaEntity) {
        viewModelScope.launch {
            dao.delete(idea)
        }
    }
}
