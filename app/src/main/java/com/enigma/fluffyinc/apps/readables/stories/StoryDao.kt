package com.enigma.fluffyinc.readables.stories

import androidx.room.*
import com.enigma.fluffyinc.apps.readables.stories.Story
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories")
    fun getAll(): Flow<List<Story>>

    @Query("SELECT * FROM stories WHERE id = :id")
    fun getById(id: Int): Flow<Story>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(story: Story)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stories: List<Story>)

    @Update
    suspend fun update(story: Story)

    @Query("SELECT COUNT(*) FROM stories")
    suspend fun getCount(): Int
}
