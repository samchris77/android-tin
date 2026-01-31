package com.tinnitustracker.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tinnitustracker.data.DiaryDao
import com.tinnitustracker.data.DiaryEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class DiaryViewModel(
    private val diaryDao: DiaryDao
) : ViewModel() {

    val allEntries: StateFlow<List<DiaryEntry>> = diaryDao.getAllEntries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addEntry(loudness: Int, comfort: Int, stress: Int, notes: String?) {
        viewModelScope.launch {
            val entry = DiaryEntry(
                date = Date(),
                loudnessLevel = loudness,
                comfortLevel = comfort,
                stressLevel = stress,
                notes = notes
            )
            diaryDao.insert(entry)
        }
    }

    fun deleteEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            diaryDao.delete(entry)
        }
    }
}

class DiaryViewModelFactory(private val diaryDao: DiaryDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiaryViewModel(diaryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
