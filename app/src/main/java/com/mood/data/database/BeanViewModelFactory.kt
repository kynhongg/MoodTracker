package com.mood.data.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BeanViewModelFactory(private val repository: BeanRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BeanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BeanViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}