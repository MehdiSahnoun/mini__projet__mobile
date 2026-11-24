package com.example.gradebook.ui.releve

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradebook.data.model.Etudiant
import com.example.gradebook.data.model.Matiere
import com.example.gradebook.data.model.Note
import com.example.gradebook.data.model.Role
import com.example.gradebook.data.model.User
import com.example.gradebook.data.repository.AuthRepository
import com.example.gradebook.data.repository.EtudiantRepository
import com.example.gradebook.data.repository.MatiereRepository
import com.example.gradebook.data.repository.NoteRepository
import com.example.gradebook.util.MoyenneCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MatiereMoyenneUiState(
    val matiere: Matiere,
    val notes: List<Note>,
    val moyenne: Double?
)

data class StatsClasseUiState(
    val moyenneClasse: Double?,
    val meilleureNote: Double?,
    val plusFaibleNote: Double?
)

data class ReleveUiState(
    val isLoading: Boolean = true,
    val currentUser: User? = null,
    val isEnseignant: Boolean = false,
    val etudiants: List<Etudiant> = emptyList(),
    val selectedEtudiant: Etudiant? = null,
    val matieres: List<Matiere> = emptyList(),
    val notesEtudiant: List<Note> = emptyList(),
    val matieresMoyennes: List<MatiereMoyenneUiState> = emptyList(),
    val moyenneGenerale: Double? = null,
    val mention: String = "Non évalué",
    val statsClasse: StatsClasseUiState = StatsClasseUiState(null, null, null),
    val errorMessage: String? = null
)

class ReleveViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val etudiantRepository: EtudiantRepository = EtudiantRepository(),
    private val matiereRepository: MatiereRepository = MatiereRepository(),
    private val noteRepository: NoteRepository = NoteRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReleveUiState())
    val uiState: StateFlow<ReleveUiState> = _uiState.asStateFlow()

    private var allNotes: List<Note> = emptyList()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val userResult = authRepository.getCurrentUserProfile()
            val user = userResult.getOrNull()
            val isEnseignant = user?.role == Role.ENSEIGNANT

            _uiState.value = _uiState.value.copy(
                currentUser = user,
                isEnseignant = isEnseignant
            )

            launch {
                noteRepository.getNotesFlow().collect { notes ->
                    allNotes = notes
                    calculateClassStats()
                    recalculateReleve()
                }
            }

            launch {
                matiereRepository.getMatieresFlow().collect { matieres ->
                    _uiState.value = _uiState.value.copy(matieres = matieres)
                    recalculateReleve()
                }
            }

            if (isEnseignant) {
                etudiantRepository.getEtudiantsFlow().collect { etudiants ->
                    val selected = _uiState.value.selectedEtudiant ?: etudiants.firstOrNull()
                    _uiState.value = _uiState.value.copy(
                        etudiants = etudiants,
                        selectedEtudiant = selected,
                        isLoading = false
                    )
                    if (selected != null) {
                        loadNotesForSelectedEtudiant(selected.id)
                    }
                }
            } else {
                if (user != null) {
                    val etudiantResult = etudiantRepository.getEtudiantByUid(user.uid)
                    val etudiant = etudiantResult.getOrNull()
                    _uiState.value = _uiState.value.copy(
                        selectedEtudiant = etudiant,
                        isLoading = false
                    )
                    if (etudiant != null) {
                        loadNotesForSelectedEtudiant(etudiant.id)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Aucun profil étudiant associé à ce compte."
                        )
                    }
                }
            }
        }
    }

    fun selectEtudiant(etudiant: Etudiant) {
        _uiState.value = _uiState.value.copy(selectedEtudiant = etudiant)
        loadNotesForSelectedEtudiant(etudiant.id)
    }

    private fun loadNotesForSelectedEtudiant(etudiantId: String) {
        viewModelScope.launch {
            noteRepository.getNotesForEtudiantFlow(etudiantId).collect { notes ->
                _uiState.value = _uiState.value.copy(notesEtudiant = notes)
                recalculateReleve()
            }
        }
    }

    private fun recalculateReleve() {
        val state = _uiState.value
        val matieres = state.matieres
        val notes = state.notesEtudiant

        val notesParMatiere = notes.groupBy { it.matiereId }

        val matieresMoyennesList = matieres.map { matiere ->
            val listNotesMatiere = notesParMatiere[matiere.id] ?: emptyList()
            val moy = MoyenneCalculator.calculerMoyenneMatiere(listNotesMatiere)
            MatiereMoyenneUiState(
                matiere = matiere,
                notes = listNotesMatiere,
                moyenne = moy
            )
        }

        val moyGenerale = MoyenneCalculator.calculerMoyenneGenerale(notes, matieres)
        val mention = MoyenneCalculator.obtenirMention(moyGenerale)

        _uiState.value = state.copy(
            matieresMoyennes = matieresMoyennesList,
            moyenneGenerale = moyGenerale,
            mention = mention
        )
    }

    private fun calculateClassStats() {
        if (allNotes.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                statsClasse = StatsClasseUiState(null, null, null)
            )
            return
        }

        val maxVal = allNotes.maxOf { it.valeur }
        val minVal = allNotes.minOf { it.valeur }

        val etudiantIds = allNotes.map { it.etudiantId }.distinct()
        val matieres = _uiState.value.matieres

        val moyennesEtudiants = etudiantIds.mapNotNull { etId ->
            val notesE = allNotes.filter { it.etudiantId == etId }
            MoyenneCalculator.calculerMoyenneGenerale(notesE, matieres)
        }

        val moyClasse = if (moyennesEtudiants.isNotEmpty()) {
            MoyenneCalculator.roundToTwoDecimals(moyennesEtudiants.average())
        } else null

        _uiState.value = _uiState.value.copy(
            statsClasse = StatsClasseUiState(
                moyenneClasse = moyClasse,
                meilleureNote = maxVal,
                plusFaibleNote = minVal
            )
        )
    }
}
