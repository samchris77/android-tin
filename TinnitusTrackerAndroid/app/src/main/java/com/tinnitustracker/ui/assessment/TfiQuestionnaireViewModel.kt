package com.tinnitustracker.ui.assessment

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tinnitustracker.data.database.entities.TFIAssessment
import com.tinnitustracker.data.repository.TfiRepository
import kotlinx.coroutines.launch

class TfiQuestionnaireViewModel(
    private val repo: TfiRepository
) : ViewModel() {

    /** Per-item responses, key = item # (1..25), value = 0..10. */
    val responses = mutableStateMapOf<Int, Int>()

    /** Last submitted assessment — drives the Results screen. Null until submitted. */
    val lastSubmitted = mutableStateOf<TFIAssessment?>(null)

    fun setResponse(itemNumber: Int, value: Int) {
        responses[itemNumber] = value
    }

    fun isPageComplete(page: Int): Boolean {
        val pageItems = TfiContent.Pages.getOrNull(page)?.items ?: return false
        return pageItems.all { responses[it.number] != null }
    }

    fun isAllComplete(): Boolean = TfiContent.Items.all { responses[it.number] != null }

    fun submit(onDone: () -> Unit) {
        if (!isAllComplete()) return
        viewModelScope.launch {
            val saved = repo.submit(
                items = responses.toMap(),
                takenAtEpochMs = System.currentTimeMillis()
            )
            lastSubmitted.value = saved
            onDone()
        }
    }

    fun reset() {
        responses.clear()
        lastSubmitted.value = null
    }
}

class TfiQuestionnaireViewModelFactory(
    private val repo: TfiRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(TfiQuestionnaireViewModel::class.java))
        return TfiQuestionnaireViewModel(repo) as T
    }
}
