package com.fikirkulucka.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fikirkulucka.app.data.AppDatabase
import com.fikirkulucka.app.data.IdeaEntity
import com.fikirkulucka.app.data.IdeaStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class IdeaViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).ideaDao()

    val activeIdeas: StateFlow<List<IdeaEntity>> = dao.observeActiveIdeas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvedIdeas: StateFlow<List<IdeaEntity>> = dao.observeApprovedIdeas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedIdeas: StateFlow<List<IdeaEntity>> = dao.observeArchivedIdeas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
                    status = IdeaStatus.INCUBATING
                )
            )
        }
    }

    fun approveIdea(idea: IdeaEntity) {
        viewModelScope.launch {
            dao.update(idea.copy(status = IdeaStatus.APPROVED))
        }
    }

    fun archiveIdea(idea: IdeaEntity) {
        viewModelScope.launch {
            dao.update(idea.copy(status = IdeaStatus.ARCHIVED))
        }
    }

    fun deleteIdea(idea: IdeaEntity) {
        viewModelScope.launch {
            dao.delete(idea)
        }
    }
}
