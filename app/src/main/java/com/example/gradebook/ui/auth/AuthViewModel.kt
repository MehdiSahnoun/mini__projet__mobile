package com.example.gradebook.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradebook.data.model.Role
import com.example.gradebook.data.model.User
import com.example.gradebook.data.repository.AuthRepository
import com.example.gradebook.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, pass: String) {
        if (!Validators.isNotEmpty(email, pass)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Veuillez remplir tous les champs.")
            return
        }
        if (!Validators.isValidEmail(email)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Adresse email invalide.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.login(email.trim(), pass)
            result.onSuccess { user ->
                _uiState.value = AuthUiState(user = user, isSuccess = true)
            }.onFailure { ex ->
                _uiState.value = AuthUiState(errorMessage = ex.localizedMessage ?: "Échec de connexion")
            }
        }
    }

    fun register(
        nom: String,
        prenom: String,
        email: String,
        pass: String,
        passConfirm: String,
        role: Role,
        classe: String
    ) {
        if (!Validators.isNotEmpty(nom, prenom, email, pass, passConfirm)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Veuillez remplir tous les champs obligatoires.")
            return
        }
        if (role == Role.ETUDIANT && classe.trim().isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Veuillez saisir votre classe.")
            return
        }
        if (!Validators.isValidEmail(email)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Adresse email invalide.")
            return
        }
        if (pass != passConfirm) {
            _uiState.value = _uiState.value.copy(errorMessage = "Les mots de passe ne correspondent pas.")
            return
        }
        if (pass.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Le mot de passe doit faire au moins 6 caractères.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.register(nom.trim(), prenom.trim(), email.trim(), pass, role, classe.trim())
            result.onSuccess { user ->
                _uiState.value = AuthUiState(user = user, isSuccess = true)
            }.onFailure { ex ->
                _uiState.value = AuthUiState(errorMessage = ex.localizedMessage ?: "Échec d'inscription")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }
}
