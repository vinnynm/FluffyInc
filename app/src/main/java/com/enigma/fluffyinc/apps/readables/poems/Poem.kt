package com.enigma.fluffyinc.apps.readables.poems

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "poems")
data class Poem(
    @PrimaryKey val id: Int,
    val title: String,
    val content: String,
    val category: String,
    val imageUrl: String? = null,
    var isBookmarked: Boolean = false
)