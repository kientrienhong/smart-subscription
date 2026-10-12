package com.smartsubscription.scan

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** The scan screen's steps: read the text, find the details, then the person checks and saves. */
data class ScanState(
    val phase: Phase = Phase.PICKING,
    val lines: Int = 0,
    val result: ScanResult? = null,
    val error: ScanException? = null,
) {
    enum class Phase { PICKING, READING, FINDING, DONE, FAILED }
}

class ScanViewModel(app: Application) : AndroidViewModel(app) {
    private val scanner = InvoiceScanner(app)
    private val _state = MutableStateFlow(ScanState())
    val state: StateFlow<ScanState> = _state.asStateFlow()
    private var job: Job? = null

    init {
        scanner.prefetchModel()
    }

    fun scan(uri: Uri, defaultCurrency: String, today: LocalDate) {
        job?.cancel()
        _state.value = ScanState(phase = ScanState.Phase.READING)
        job = viewModelScope.launch {
            try {
                val text = scanner.readText(uri)
                _state.update { it.copy(phase = ScanState.Phase.FINDING, lines = text.lines().count { l -> l.isNotBlank() }) }
                val result = scanner.parse(text, defaultCurrency, today)
                _state.update { it.copy(phase = ScanState.Phase.DONE, result = result) }
            } catch (e: ScanException) {
                _state.update { it.copy(phase = ScanState.Phase.FAILED, error = e) }
            }
        }
    }

    /** Back to choosing an image, after a failure or a cancelled picker. */
    fun reset() {
        job?.cancel()
        _state.value = ScanState()
    }
}
