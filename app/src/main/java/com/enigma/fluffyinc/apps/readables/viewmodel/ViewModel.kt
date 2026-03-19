package com.enigma.fluffyinc.apps.readables.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enigma.fluffyinc.apps.readables.poems.Poem
import com.enigma.fluffyinc.apps.readables.repository.Repository
import com.enigma.fluffyinc.apps.readables.stories.Story
import com.enigma.fluffyinc.apps.readables.stories.storyList
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PoemViewModel(private val repository: Repository) : ViewModel() {
    val allPoems: Flow<List<Poem>> = repository.allPoems

    fun loadInitialPoemsIfEmpty(context: Context) = viewModelScope.launch {
        if (repository.getPoemsCount() == 0) {
            try {
                val poemsJson = context.assets.open("Assets/poems.json").bufferedReader().use { it.readText() }
                val poemListType = object : TypeToken<List<Poem>>() {}.type
                val poems: List<Poem> = Gson().fromJson(poemsJson, poemListType)
                repository.insertPoems(poems)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getPoemById(id: Int): Flow<Poem> = repository.getPoemById(id)

    fun addPoem(poem: Poem) = viewModelScope.launch {
        repository.insertPoem(poem)
    }

    fun insertAll(poems: List<Poem>) = viewModelScope.launch {
        repository.insertPoems(poems)
    }

    fun updatePoem(poem: Poem) = viewModelScope.launch {
        repository.updatePoem(poem)
    }
}

class StoryViewModel(private val repository: Repository) : ViewModel() {
    val allStories: Flow<List<Story>> = repository.allStories

    fun loadInitialStoriesIfEmpty() = viewModelScope.launch {
        if (repository.getStoriesCount() == 0) {
            repository.insertStories(storyList)
        }
    }

    fun getStoryById(id: Int): Flow<Story> = repository.getStoryById(id)

    fun addStory(story: Story) = viewModelScope.launch {
        repository.insertStory(story)
    }

    fun insertAll(stories: List<Story>) = viewModelScope.launch {
        repository.insertStories(stories)
    }

    fun updateStory(story: Story) = viewModelScope.launch {
        repository.updateStory(story)
    }
}
