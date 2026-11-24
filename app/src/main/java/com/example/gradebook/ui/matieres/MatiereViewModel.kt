package com.example.gradebook.ui.matieres

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradebook.data.model.Matiere
import com.example.gradebook.data.repository.MatiereRepository
import com.example.gradebook.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MatieresUiState(
    val isLoading: Boolean = true,
    val matieres: List<Matiere> = emptyList(),
    val errorMessage: String? = null,
    val operationSuccess: Boolean = false
)

class MatiereViewModel(
    private val matiereRepository: MatiereRepository = MatiereRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatieresUiState())
    val uiState: StateFlow<MatieresUiState> = _uiState.asStateFlow()

    init {
        loadMatieres()
    }

    private fun loadMatieres() {
        viewModelScope.launch {
            matiereRepository.getMatieresFlow().collect { list ->
                _uiState.value = _uiState.value.copy(isLoading = false, matieres = list)
            }
        }
    }

    fun saveMatiere(id: String?, nom: String, coeffText: String) {
        if (!Validators.isNotEmpty(nom, coeffText)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Veuillez remplir le nom et le coefficient.")
            return
        }

        val coeff = coeffText.toDoubleOrNull()
        if (coeff == null || coeff <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Le coefficient doit être un nombre supérieur à 0.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val matiere = Matiere(id = id ?: "", nom = nom.trim(), coefficient = coeff)
            val result = if (id.isNullOrEmpty()) {
                matiereRepository.addMatiere(matiere)
            } else {
                matiereRepository.updateMatiere(matiere)
            }

            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, operationSuccess = true)
            }.onFailure { ex ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = ex.localizedMessage ?: "Erreur d'enregistrement")
            }
        }
    }

    fun deleteMatiere(matiereId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = matiereRepository.deleteMatiere(matiereId)
            result.onFailure { ex ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = ex.localizedMessage ?: "Erreur de suppression")
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetOperationSuccess() {
        _uiState.value = _uiState.value.copy(operationSuccess = false)
    }
}
