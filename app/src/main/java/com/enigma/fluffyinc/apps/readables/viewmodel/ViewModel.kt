package com.enigma.fluffyinc.apps.readables.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enigma.fluffyinc.apps.readables.poems.Poem
import com.enigma.fluffyinc.apps.readables.repository.Repository
import com.enigma.fluffyinc.apps.readables.stories.Story
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PoemViewModel(private val repository: Repository) : ViewModel() {
    val allPoems: Flow<List<Poem>> = repository.allPoems

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

    fun getStoryById(id: Int): Flow<Story> = repository.getStoryById(id)

    fun insertAll(stories: List<Story>) = viewModelScope.launch {
        repository.insertStories(stories)
    }

    fun updateStory(story: Story) = viewModelScope.launch {
        repository.updateStory(story)
    }
}
