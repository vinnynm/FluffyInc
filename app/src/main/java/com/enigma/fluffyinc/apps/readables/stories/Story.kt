package com.enigma.fluffyinc.apps.readables.stories

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class Story(
    @PrimaryKey val id: Int,
    val title: String,
    val content: String,
    val category: String = "Adult",
    var imageUrl: String? = null,
    var isBookmarked: Boolean = false,
    var lastPosition: Int = 0
)