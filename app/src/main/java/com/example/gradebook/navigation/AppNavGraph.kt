package com.example.gradebook.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gradebook.data.firebase.FirebaseRefs
import com.example.gradebook.ui.auth.LoginScreen
import com.example.gradebook.ui.auth.RegisterScreen
import com.example.gradebook.ui.etudiants.AjoutEtudiantScreen
import com.example.gradebook.ui.etudiants.EtudiantsScreen
import com.example.gradebook.ui.main.MainScreen
import com.example.gradebook.ui.matieres.MatieresScreen
import com.example.gradebook.ui.notes.NotesScreen
import com.example.gradebook.ui.profil.ProfilScreen
import com.example.gradebook.ui.releve.ReleveScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = if (FirebaseRefs.auth.currentUser != null) Routes.MAIN else Routes.LOGIN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                onNavigateToEtudiants = { navController.navigate(Routes.ETUDIANTS) },
                onNavigateToMatieres = { navController.navigate(Routes.MATIERES) },
                onNavigateToNotes = { navController.navigate(Routes.NOTES) },
                onNavigateToReleve = { navController.navigate(Routes.RELEVE) },
                onNavigateToProfil = { navController.navigate(Routes.PROFIL) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ETUDIANTS) {
            EtudiantsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddEtudiant = {
                    navController.navigate(Routes.createAjoutEtudiantRoute())
                },
                onNavigateToEditEtudiant = { etudiant ->
                    navController.navigate(
                        Routes.createAjoutEtudiantRoute(
                            etudiantId = etudiant.id,
                            nom = etudiant.nom,
                            prenom = etudiant.prenom,
                            num = etudiant.numeroEtudiant,
                            classe = etudiant.classe,
                            uid = etudiant.uid ?: ""
                        )
                    )
                }
            )
        }

        composable(
            route = Routes.AJOUT_ETUDIANT,
            arguments = listOf(
                navArgument("etudiantId") { type = NavType.StringType; defaultValue = "" },
                navArgument("nom") { type = NavType.StringType; defaultValue = "" },
                navArgument("prenom") { type = NavType.StringType; defaultValue = "" },
                navArgument("num") { type = NavType.StringType; defaultValue = "" },
                navArgument("classe") { type = NavType.StringType; defaultValue = "" },
                navArgument("uid") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val etudiantId = backStackEntry.arguments?.getString("etudiantId")
            val nom = backStackEntry.arguments?.getString("nom") ?: ""
            val prenom = backStackEntry.arguments?.getString("prenom") ?: ""
            val num = backStackEntry.arguments?.getString("num") ?: ""
            val classe = backStackEntry.arguments?.getString("classe") ?: ""
            val uid = backStackEntry.arguments?.getString("uid") ?: ""

            AjoutEtudiantScreen(
                etudiantId = if (etudiantId.isNullOrEmpty()) null else etudiantId,
                initialNom = nom,
                initialPrenom = prenom,
                initialNum = num,
                initialClasse = classe,
                initialUid = uid,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        composable(Routes.MATIERES) {
            MatieresScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.NOTES) {
            NotesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.RELEVE) {
            ReleveScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PROFIL) {
            ProfilScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
