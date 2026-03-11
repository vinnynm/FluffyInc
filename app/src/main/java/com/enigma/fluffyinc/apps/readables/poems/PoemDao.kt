package com.enigma.fluffyinc.apps.readables.poems

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface PoemDao {
    @Query("SELECT * FROM poems ORDER BY title ASC")
    fun getAll(): Flow<List<Poem>>

    @Query("SELECT * FROM poems WHERE id = :id")
    fun getById(id: Int): Flow<Poem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(poem: Poem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(poems: List<Poem>)

    @Update
    suspend fun update(poem: Poem)

    @Query("SELECT COUNT(*) FROM poems")
    suspend fun getCount(): Int
}