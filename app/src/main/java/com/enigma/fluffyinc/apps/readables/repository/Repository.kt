package com.enigma.fluffyinc.apps.readables.repository

import com.enigma.fluffyinc.apps.readables.poems.Poem
import com.enigma.fluffyinc.apps.readables.poems.PoemDao
import com.enigma.fluffyinc.apps.readables.stories.Story
import com.enigma.fluffyinc.readables.stories.StoryDao
import kotlinx.coroutines.flow.Flow

class Repository(private val poemDao: PoemDao, private val storyDao: StoryDao) {
    val allPoems: Flow<List<Poem>> = poemDao.getAll()
    val allStories: Flow<List<Story>> = storyDao.getAll()

    fun getPoemById(id: Int): Flow<Poem> = poemDao.getById(id)
    fun getStoryById(id: Int): Flow<Story> = storyDao.getById(id)

    suspend fun insertPoem(poem: Poem) {
        poemDao.insert(poem)
    }

    suspend fun insertPoems(poems: List<Poem>) {
        poemDao.insertAll(poems)
    }

    suspend fun insertStories(stories: List<Story>) {
        storyDao.insertAll(stories)
    }

    suspend fun updatePoem(poem: Poem) {
        poemDao.update(poem)
    }

    suspend fun updateStory(story: Story) {
        storyDao.update(story)
    }

    suspend fun getPoemsCount(): Int = poemDao.getCount()
    suspend fun getStoriesCount(): Int = storyDao.getCount()
}