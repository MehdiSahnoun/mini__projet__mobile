package com.example.gradebook.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gradebook.data.model.Role

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToEtudiants: () -> Unit,
    onNavigateToMatieres: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToReleve: () -> Unit,
    onNavigateToProfil: () -> Unit,
    onLogout: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user
    val isEnseignant = user?.role == Role.ENSEIGNANT

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GradeBook - Tableau de Bord") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Bonjour ${user?.prenom ?: ""} ${user?.nom ?: ""}".trim(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        SuggestionChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = if (isEnseignant) "Enseignant" else "Étudiant",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
                    }
                }

                Text(
                    text = "Accès rapide",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isEnseignant) {
                        item {
                            DashboardCard(
                                title = "Étudiants",
                                icon = Icons.Default.People,
                                onClick = onNavigateToEtudiants
                            )
                        }
                        item {
                            DashboardCard(
                                title = "Matières",
                                icon = Icons.Default.Book,
                                onClick = onNavigateToMatieres
                            )
                        }
                        item {
                            DashboardCard(
                                title = "Notes",
                                icon = Icons.Default.Grade,
                                onClick = onNavigateToNotes
                            )
                        }
                    }

                    item {
                        DashboardCard(
                            title = "Relevé de notes",
                            icon = Icons.Default.Assessment,
                            onClick = onNavigateToReleve
                        )
                    }

                    item {
                        DashboardCard(
                            title = "Mon Profil",
                            icon = Icons.Default.Person,
                            onClick = onNavigateToProfil
                        )
                    }

                    item {
                        DashboardCard(
                            title = "Déconnexion",
                            icon = Icons.Default.ExitToApp,
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            onClick = {
                                viewModel.logout()
                                onLogout()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    icon: ImageVector,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
