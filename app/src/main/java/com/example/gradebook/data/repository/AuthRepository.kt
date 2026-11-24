package com.example.gradebook.data.repository

import android.util.Log
import com.example.gradebook.data.firebase.FirebaseRefs
import com.example.gradebook.data.model.Etudiant
import com.example.gradebook.data.model.Role
import com.example.gradebook.data.model.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class AuthRepository {

    private val auth = FirebaseRefs.auth
    private val usersRef = FirebaseRefs.usersRef
    private val etudiantsRef = FirebaseRefs.etudiantsRef

    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun login(email: String, pass: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: throw Exception("Utilisateur introuvable")
            
            var user: User? = null
            try {
                val doc = withTimeout(4000) {
                    usersRef.document(uid).get().await()
                }
                if (doc.exists()) {
                    val roleStr = doc.getString("role") ?: Role.ETUDIANT.name
                    user = User(
                        uid = uid,
                        nom = doc.getString("nom") ?: "",
                        prenom = doc.getString("prenom") ?: "",
                        email = doc.getString("email") ?: email,
                        role = try { Role.valueOf(roleStr) } catch (e: Exception) { Role.ETUDIANT },
                        classe = doc.getString("classe") ?: ""
                    )
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Erreur lecture Firestore user profile", e)
            }

            if (user == null) {
                user = User(
                    uid = uid,
                    nom = authResult.user?.displayName ?: "",
                    email = email,
                    role = Role.ETUDIANT
                )
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(formatFirebaseException(e))
        }
    }

    suspend fun register(
        nom: String,
        prenom: String,
        email: String,
        pass: String,
        role: Role,
        classe: String = ""
    ): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: throw Exception("Erreur lors de la création du compte")
            val user = User(
                uid = uid,
                nom = nom,
                prenom = prenom,
                email = email,
                role = role,
                classe = classe
            )
            
            try {
                withTimeout(5000) {
                    usersRef.document(uid).set(user.toMap()).await()

                    if (role == Role.ETUDIANT) {
                        val existingDocs = etudiantsRef.get().await()
                        val nextIndex = existingDocs.size() + 1
                        val autoNumEtud = "ET-" + String.format("%04d", nextIndex)

                        val etudiant = Etudiant(
                            id = uid,
                            nom = nom,
                            prenom = prenom,
                            numeroEtudiant = autoNumEtud,
                            classe = classe,
                            uid = uid
                        )
                        etudiantsRef.document(uid).set(etudiant.toMap()).await()
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Erreur sauvegarde profil Firestore", e)
                val msg = e.localizedMessage ?: ""
                if (msg.contains("The database (default) does not exist", ignoreCase = true) || msg.contains("NOT_FOUND", ignoreCase = true)) {
                    throw Exception("Compte créé dans Auth, mais la base Firestore n'a pas encore été créée dans la Console Firebase (projet gradebook-60c0a). Veuillez cliquer sur 'Créer une base de données' dans la Console Firebase.")
                }
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(formatFirebaseException(e))
        }
    }

    suspend fun getCurrentUserProfile(): Result<User?> {
        val uid = auth.currentUser?.uid ?: return Result.success(null)
        return try {
            val doc = withTimeout(4000) {
                usersRef.document(uid).get().await()
            }
            if (!doc.exists()) {
                return Result.success(User(uid = uid, email = auth.currentUser?.email ?: "", role = Role.ETUDIANT))
            }
            val roleStr = doc.getString("role") ?: Role.ETUDIANT.name
            val user = User(
                uid = uid,
                nom = doc.getString("nom") ?: "",
                prenom = doc.getString("prenom") ?: "",
                email = doc.getString("email") ?: (auth.currentUser?.email ?: ""),
                role = try { Role.valueOf(roleStr) } catch (e: Exception) { Role.ETUDIANT },
                classe = doc.getString("classe") ?: ""
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.success(User(uid = uid, email = auth.currentUser?.email ?: "", role = Role.ETUDIANT))
        }
    }

    fun observeUserProfile(): Flow<User?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = usersRef.document(uid).addSnapshotListener { doc, error ->
            if (error != null || doc == null || !doc.exists()) {
                val fallbackUser = User(uid = uid, email = auth.currentUser?.email ?: "", role = Role.ETUDIANT)
                trySend(fallbackUser)
                return@addSnapshotListener
            }
            val roleStr = doc.getString("role") ?: Role.ETUDIANT.name
            val user = User(
                uid = uid,
                nom = doc.getString("nom") ?: "",
                prenom = doc.getString("prenom") ?: "",
                email = doc.getString("email") ?: "",
                role = try { Role.valueOf(roleStr) } catch (e: Exception) { Role.ETUDIANT },
                classe = doc.getString("classe") ?: ""
            )
            trySend(user)
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateUserProfile(nom: String, prenom: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Non connecté"))
        return try {
            withTimeout(5000) {
                usersRef.document(uid).update(
                    mapOf("nom" to nom, "prenom" to prenom)
                ).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(formatFirebaseException(e))
        }
    }

    suspend fun updatePassword(newPass: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Non connecté"))
        return try {
            user.updatePassword(newPass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(formatFirebaseException(e))
        }
    }

    fun logout() {
        auth.signOut()
    }

    private fun formatFirebaseException(e: Exception): Exception {
        val message = e.localizedMessage ?: e.message ?: ""
        return when {
            message.contains("The database (default) does not exist", ignoreCase = true) || message.contains("NOT_FOUND", ignoreCase = true) -> {
                Exception("La base Cloud Firestore n'existe pas encore pour le projet gradebook-60c0a. Veuillez la créer sur console.firebase.google.com > Build > Firestore Database > Créer une base de données.")
            }
            message.contains("API key not valid", ignoreCase = true) || message.contains("API_KEY_INVALID", ignoreCase = true) -> {
                Exception("Clé API Firebase non valide : vérifiez 'app/google-services.json'.")
            }
            message.contains("Firestore API has not been used", ignoreCase = true) || message.contains("PERMISSION_DENIED", ignoreCase = true) -> {
                Exception("Cloud Firestore n'est pas activé dans votre projet Firebase. Allez sur console.firebase.google.com > Build > Firestore Database > Créer une base de données.")
            }
            message.contains("The email address is already in use", ignoreCase = true) -> {
                Exception("Cette adresse email est déjà utilisée par un autre compte.")
            }
            else -> e
        }
    }
}
