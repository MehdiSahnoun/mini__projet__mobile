package com.example.gradebook.ui.profil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradebook.data.model.User
import com.example.gradebook.data.repository.AuthRepository
import com.example.gradebook.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfilUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val nom: String = "",
    val prenom: String = "",
    val email: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ProfilViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfilUiState())
    val uiState: StateFlow<ProfilUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            authRepository.observeUserProfile().collect { user ->
                if (user != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user,
                        nom = user.nom,
                        prenom = user.prenom,
                        email = user.email
                    )
                }
            }
        }
    }

    fun updateNom(nom: String) {
        _uiState.value = _uiState.value.copy(nom = nom)
    }

    fun updatePrenom(prenom: String) {
        _uiState.value = _uiState.value.copy(prenom = prenom)
    }

    fun updateNewPassword(pass: String) {
        _uiState.value = _uiState.value.copy(newPassword = pass)
    }

    fun updateConfirmPassword(pass: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = pass)
    }

    fun saveChanges() {
        val state = _uiState.value
        if (!Validators.isNotEmpty(state.nom, state.prenom)) {
            _uiState.value = state.copy(errorMessage = "Le nom et le prénom ne peuvent pas être vides.")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null, successMessage = null)
            val profileResult = authRepository.updateUserProfile(state.nom.trim(), state.prenom.trim())

            if (profileResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = profileResult.exceptionOrNull()?.localizedMessage ?: "Erreur de mise à jour"
                )
                return@launch
            }

            if (state.newPassword.isNotEmpty()) {
                if (state.newPassword != state.confirmPassword) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Les nouveaux mots de passe ne correspondent pas."
                    )
                    return@launch
                }
                if (state.newPassword.length < 6) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Le mot de passe doit contenir au moins 6 caractères."
                    )
                    return@launch
                }
                val passResult = authRepository.updatePassword(state.newPassword)
                if (passResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = passResult.exceptionOrNull()?.localizedMessage ?: "Erreur de mise à jour du mot de passe"
                    )
                    return@launch
                }
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                newPassword = "",
                confirmPassword = "",
                successMessage = "Profil mis à jour avec succès !"
            )
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
