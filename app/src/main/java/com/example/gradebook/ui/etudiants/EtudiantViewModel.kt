package com.example.gradebook.ui.etudiants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradebook.data.model.Etudiant
import com.example.gradebook.data.repository.EtudiantRepository
import com.example.gradebook.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EtudiantsUiState(
    val isLoading: Boolean = true,
    val etudiants: List<Etudiant> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val operationSuccess: Boolean = false
)

class EtudiantViewModel(
    private val etudiantRepository: EtudiantRepository = EtudiantRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EtudiantsUiState())
    val uiState: StateFlow<EtudiantsUiState> = _uiState.asStateFlow()

    init {
        loadEtudiants()
    }

    private fun loadEtudiants() {
        viewModelScope.launch {
            etudiantRepository.getEtudiantsFlow().collect { list ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    etudiants = list
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun saveEtudiant(id: String?, nom: String, prenom: String, numeroEtudiant: String, classe: String, uid: String?) {
        if (!Validators.isNotEmpty(nom, prenom, numeroEtudiant, classe)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Tous les champs obligatoires doivent être remplis.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val cleanUid = if (uid.isNullOrBlank()) null else uid.trim()
            val etudiant = Etudiant(
                id = id ?: "",
                nom = nom.trim(),
                prenom = prenom.trim(),
                numeroEtudiant = numeroEtudiant.trim(),
                classe = classe.trim(),
                uid = cleanUid
            )

            val result = if (id.isNullOrEmpty()) {
                etudiantRepository.addEtudiant(etudiant)
            } else {
                etudiantRepository.updateEtudiant(etudiant)
            }

            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, operationSuccess = true)
            }.onFailure { ex ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = ex.localizedMessage ?: "Erreur d'enregistrement")
            }
        }
    }

    fun deleteEtudiant(etudiantId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = etudiantRepository.deleteEtudiant(etudiantId)
            result.onFailure { ex ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = ex.localizedMessage ?: "Erreur lors de la suppression")
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
