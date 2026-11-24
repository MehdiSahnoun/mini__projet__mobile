package com.example.gradebook.ui.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gradebook.data.model.Note
import com.example.gradebook.data.model.TypeNote

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onNavigateBack: () -> Unit,
    viewModel: NoteViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var etudiantExpanded by remember { mutableStateOf(false) }
    var matiereExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestion des Notes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (uiState.editingNoteId == null) "Saisie d'une Note" else "Modification de la Note",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    ExposedDropdownMenuBox(
                        expanded = etudiantExpanded,
                        onExpandedChange = { etudiantExpanded = !etudiantExpanded }
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedEtudiant?.let { "${it.prenom} ${it.nom} (${it.classe})" } ?: "Sélectionner un étudiant",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Étudiant") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = etudiantExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = etudiantExpanded,
                            onDismissRequest = { etudiantExpanded = false }
                        ) {
                            uiState.etudiants.forEach { etudiant ->
                                DropdownMenuItem(
                                    text = { Text("${etudiant.prenom} ${etudiant.nom} - ${etudiant.classe}") },
                                    onClick = {
                                        viewModel.selectEtudiant(etudiant)
                                        etudiantExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = matiereExpanded,
                        onExpandedChange = { matiereExpanded = !matiereExpanded }
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedMatiere?.nom ?: "Sélectionner une matière",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Matière") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = matiereExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = matiereExpanded,
                            onDismissRequest = { matiereExpanded = false }
                        ) {
                            uiState.matieres.forEach { matiere ->
                                DropdownMenuItem(
                                    text = { Text("${matiere.nom} (Coeff: ${matiere.coefficient})") },
                                    onClick = {
                                        viewModel.selectMatiere(matiere)
                                        matiereExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Text(text = "Type d'évaluation :", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TypeNote.values().forEach { type ->
                            FilterChip(
                                selected = uiState.selectedType == type,
                                onClick = { viewModel.selectType(type) },
                                label = { Text(type.name) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uiState.valeurText,
                        onValueChange = { viewModel.updateValeurText(it) },
                        label = { Text("Note (/20)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    uiState.errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    uiState.successMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (uiState.editingNoteId != null) {
                            OutlinedButton(
                                onClick = { viewModel.cancelEdit() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Annuler")
                            }
                        }

                        Button(
                            onClick = { viewModel.saveNote() },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isLoading
                        ) {
                            Text(if (uiState.editingNoteId == null) "Enregistrer la note" else "Modifier")
                        }
                    }
                }
            }

            Text(
                text = "Notes de l'étudiant sélectionné",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.notes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aucune note enregistrée pour cet étudiant.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.notes, key = { it.id }) { note ->
                        val matiereNom = uiState.matieres.find { it.id == note.matiereId }?.nom ?: "Matière inconnue"
                        NoteItemCard(
                            note = note,
                            matiereNom = matiereNom,
                            onEdit = { viewModel.editNote(note) },
                            onDelete = { viewModel.deleteNote(note.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoteItemCard(
    note: Note,
    matiereNom: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$matiereNom - [${note.type.name}]",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Note: ${note.valeur}/20",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Modifier",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
