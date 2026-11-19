package com.fikirkulucka.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface IdeaDao {
    @Query("SELECT * FROM ideas WHERE status IN ('INCUBATING', 'READY') ORDER BY matureAt ASC")
    fun observeActiveIdeas(): Flow<List<IdeaEntity>>

    @Query("SELECT * FROM ideas WHERE status = 'APPROVED' ORDER BY createdAt DESC")
    fun observeApprovedIdeas(): Flow<List<IdeaEntity>>

    @Query("SELECT * FROM ideas WHERE status = 'ARCHIVED' ORDER BY createdAt DESC")
    fun observeArchivedIdeas(): Flow<List<IdeaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(idea: IdeaEntity)

    @Update
    suspend fun update(idea: IdeaEntity)

    @Delete
    suspend fun delete(idea: IdeaEntity)
}
