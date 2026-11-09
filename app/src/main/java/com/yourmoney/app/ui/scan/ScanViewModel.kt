package com.yourmoney.app.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourmoney.app.data.sms.HistoricalScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ScanUiState(
    val isScanning: Boolean = false,
    val messagesExamined: Int = 0,
    val candidatesFound: Int = 0,
    val errors: Int = 0,
    val completed: Boolean = false,
    val errorMessage: String? = null,
)

class ScanViewModel(private val historicalScanner: HistoricalScanner) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState

    fun startScan(start: LocalDate, end: LocalDate) {
        if (_uiState.value.isScanning) return
        _uiState.value = ScanUiState(isScanning = true)
        viewModelScope.launch {
            try {
                val result = historicalScanner.scan(start, end) { examined, candidates ->
                    _uiState.update { it.copy(messagesExamined = examined, candidatesFound = candidates) }
                }
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        messagesExamined = result.messagesExamined,
                        candidatesFound = result.candidatesFound,
                        errors = result.errors,
                        completed = true,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isScanning = false, errorMessage = e.message ?: "Scan failed") }
            }
        }
    }

    fun reset() {
        _uiState.value = ScanUiState()
    }
}
