package com.example.gradebook.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradebook.data.model.Etudiant
import com.example.gradebook.data.model.Matiere
import com.example.gradebook.data.model.Note
import com.example.gradebook.data.model.TypeNote
import com.example.gradebook.data.repository.EtudiantRepository
import com.example.gradebook.data.repository.MatiereRepository
import com.example.gradebook.data.repository.NoteRepository
import com.example.gradebook.util.Validators
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotesUiState(
    val isLoading: Boolean = true,
    val etudiants: List<Etudiant> = emptyList(),
    val matieres: List<Matiere> = emptyList(),
    val notes: List<Note> = emptyList(),
    val selectedEtudiant: Etudiant? = null,
    val selectedMatiere: Matiere? = null,
    val selectedType: TypeNote = TypeNote.DS,
    val valeurText: String = "",
    val editingNoteId: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class NoteViewModel(
    private val noteRepository: NoteRepository = NoteRepository(),
    private val etudiantRepository: EtudiantRepository = EtudiantRepository(),
    private val matiereRepository: MatiereRepository = MatiereRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            etudiantRepository.getEtudiantsFlow().collect { etudiants ->
                val currentSelected = _uiState.value.selectedEtudiant ?: etudiants.firstOrNull()
                _uiState.value = _uiState.value.copy(
                    etudiants = etudiants,
                    selectedEtudiant = currentSelected
                )
                if (currentSelected != null) {
                    observeNotesForEtudiant(currentSelected.id)
                }
            }
        }

        viewModelScope.launch {
            matiereRepository.getMatieresFlow().collect { matieres ->
                _uiState.value = _uiState.value.copy(
                    matieres = matieres,
                    selectedMatiere = _uiState.value.selectedMatiere ?: matieres.firstOrNull()
                )
            }
        }
    }

    fun selectEtudiant(etudiant: Etudiant) {
        _uiState.value = _uiState.value.copy(selectedEtudiant = etudiant)
        observeNotesForEtudiant(etudiant.id)
    }

    fun selectMatiere(matiere: Matiere) {
        _uiState.value = _uiState.value.copy(selectedMatiere = matiere)
    }

    fun selectType(type: TypeNote) {
        _uiState.value = _uiState.value.copy(selectedType = type)
    }

    fun updateValeurText(text: String) {
        _uiState.value = _uiState.value.copy(valeurText = text)
    }

    private fun observeNotesForEtudiant(etudiantId: String) {
        viewModelScope.launch {
            noteRepository.getNotesForEtudiantFlow(etudiantId).collect { notes ->
                _uiState.value = _uiState.value.copy(isLoading = false, notes = notes)
            }
        }
    }

    fun saveNote() {
        val state = _uiState.value
        val etudiant = state.selectedEtudiant
        val matiere = state.selectedMatiere

        if (etudiant == null || matiere == null) {
            _uiState.value = state.copy(errorMessage = "Veuillez sélectionner un étudiant et une matière.")
            return
        }

        val (isValid, valeur) = Validators.isValidNote(state.valeurText)
        if (!isValid || valeur == null) {
            _uiState.value = state.copy(errorMessage = "La note doit être une valeur numérique entre 0 et 20.")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            val note = Note(
                id = state.editingNoteId ?: "",
                etudiantId = etudiant.id,
                matiereId = matiere.id,
                type = state.selectedType,
                valeur = valeur,
                date = Timestamp.now()
            )

            val result = if (state.editingNoteId.isNullOrEmpty()) {
                noteRepository.addNote(note)
            } else {
                noteRepository.updateNote(note)
            }

            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    valeurText = "",
                    editingNoteId = null,
                    successMessage = "Note enregistrée avec succès !"
                )
            }.onFailure { ex ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = ex.localizedMessage ?: "Erreur d'enregistrement"
                )
            }
        }
    }

    fun editNote(note: Note) {
        val matiere = _uiState.value.matieres.find { it.id == note.matiereId }
        _uiState.value = _uiState.value.copy(
            editingNoteId = note.id,
            selectedMatiere = matiere ?: _uiState.value.selectedMatiere,
            selectedType = note.type,
            valeurText = note.valeur.toString()
        )
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            noteRepository.deleteNote(noteId)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun cancelEdit() {
        _uiState.value = _uiState.value.copy(
            editingNoteId = null,
            valeurText = ""
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
