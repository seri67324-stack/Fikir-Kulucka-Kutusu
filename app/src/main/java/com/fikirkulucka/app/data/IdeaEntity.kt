package com.fikirkulucka.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class IdeaStatus {
    INCUBATING,
    READY,
    APPROVED,
    ARCHIVED
}

@Entity(tableName = "ideas")
data class IdeaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val matureAt: Long,
    val status: IdeaStatus = IdeaStatus.INCUBATING
) {
    fun getProgress(now: Long = System.currentTimeMillis()): Float {
        val total = (matureAt - createdAt).coerceAtLeast(1L)
        val elapsed = (now - createdAt).coerceIn(0L, total)
        return (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    }
}
