package com.example.gradebook.ui.releve

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleveScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReleveViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var etudiantExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Relevé de Notes") },
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
            if (uiState.isEnseignant) {
                ExposedDropdownMenuBox(
                    expanded = etudiantExpanded,
                    onExpandedChange = { etudiantExpanded = !etudiantExpanded }
                ) {
                    OutlinedTextField(
                        value = uiState.selectedEtudiant?.let { "${it.prenom} ${it.nom} (${it.classe})" } ?: "Sélectionner un étudiant",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sélectionner un étudiant") },
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
            }

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.selectedEtudiant != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.mention == "Admis") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Relevé de : ${uiState.selectedEtudiant?.prenom} ${uiState.selectedEtudiant?.nom}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = uiState.moyenneGenerale?.let { "$it / 20" } ?: "-- / 20",
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.mention == "Admis") Color(0xFF2E7D32) else Color(0xFFC62828)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (uiState.mention == "Admis") Color(0xFF2E7D32) else Color(0xFFC62828)
                        ) {
                            Text(
                                text = uiState.mention,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Moy. Classe", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = uiState.statsClasse.moyenneClasse?.let { "$it/20" } ?: "-",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Meilleure Note", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = uiState.statsClasse.meilleureNote?.let { "$it/20" } ?: "-",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Plus Faible Note", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = uiState.statsClasse.plusFaibleNote?.let { "$it/20" } ?: "-",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                        }
                    }
                }

                Text(
                    text = "Détail par Matière",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.matieresMoyennes) { mm ->
                        MatiereReleveCard(matMoy = mm)
                    }
                }
            }
        }
    }
}

@Composable
fun MatiereReleveCard(matMoy: MatiereMoyenneUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${matMoy.matiere.nom} (Coeff ${matMoy.matiere.coefficient})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = matMoy.moyenne?.let { "$it / 20" } ?: "Pas de note",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (matMoy.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    matMoy.notes.forEach { note ->
                        AssistChip(
                            onClick = { },
                            label = { Text("${note.type.name}: ${note.valeur}") }
                        )
                    }
                }
            }
        }
    }
}
