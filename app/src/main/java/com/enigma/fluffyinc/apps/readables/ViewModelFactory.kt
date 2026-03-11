package com.enigma.fluffyinc.apps.readables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.enigma.fluffyinc.apps.readables.repository.Repository
import com.enigma.fluffyinc.apps.readables.viewmodel.PoemViewModel
import com.enigma.fluffyinc.apps.readables.viewmodel.StoryViewModel

class ViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PoemViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PoemViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(StoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

