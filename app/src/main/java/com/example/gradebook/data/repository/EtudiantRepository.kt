package com.example.gradebook.data.repository

import com.example.gradebook.data.firebase.FirebaseRefs
import com.example.gradebook.data.model.Etudiant
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EtudiantRepository {

    private val etudiantsRef = FirebaseRefs.etudiantsRef
    private val usersRef = FirebaseRefs.usersRef
    private val notesRef = FirebaseRefs.notesRef

    /**
     * Écoute en temps réel tous les comptes utilisateurs ayant le rôle ETUDIANT
     * et fusionne les données avec la collection 'etudiants' pour garantir
     * que TOUS les étudiants inscrits apparaissent dans la liste de l'enseignant.
     */
    fun getEtudiantsFlow(): Flow<List<Etudiant>> = callbackFlow {
        val usersListener = usersRef.whereEqualTo("role", "ETUDIANT")
            .addSnapshotListener { usersSnapshot, usersError ->
                if (usersError != null) {
                    close(usersError)
                    return@addSnapshotListener
                }

                etudiantsRef.get().addOnSuccessListener { etudiantsSnapshot ->
                    val etudiantsMap = etudiantsSnapshot.documents.associateBy { doc ->
                        doc.getString("uid") ?: doc.id
                    }

                    val resultList = mutableListOf<Etudiant>()
                    var counter = 1

                    usersSnapshot?.documents?.forEach { userDoc ->
                        val uid = userDoc.id
                        val nomUser = userDoc.getString("nom") ?: ""
                        val prenomUser = userDoc.getString("prenom") ?: ""
                        val classeUser = userDoc.getString("classe") ?: ""

                        val etudDoc = etudiantsMap[uid]
                        val numEtud = etudDoc?.getString("numeroEtudiant")
                            ?: "ET-" + String.format("%04d", counter)

                        val etudiant = Etudiant(
                            id = etudDoc?.id ?: uid,
                            nom = if (nomUser.isNotEmpty()) nomUser else (etudDoc?.getString("nom") ?: ""),
                            prenom = if (prenomUser.isNotEmpty()) prenomUser else (etudDoc?.getString("prenom") ?: ""),
                            numeroEtudiant = numEtud,
                            classe = if (classeUser.isNotEmpty()) classeUser else (etudDoc?.getString("classe") ?: ""),
                            uid = uid
                        )

                        // Si l'étudiant n'avait pas encore de document dans 'etudiants', le créer automatiquement
                        if (etudDoc == null) {
                            etudiantsRef.document(uid).set(etudiant.toMap())
                        }

                        resultList.add(etudiant)
                        counter++
                    }

                    trySend(resultList)
                }.addOnFailureListener {
                    val fallbackList = usersSnapshot?.documents?.mapIndexed { index, userDoc ->
                        Etudiant(
                            id = userDoc.id,
                            nom = userDoc.getString("nom") ?: "",
                            prenom = userDoc.getString("prenom") ?: "",
                            numeroEtudiant = "ET-" + String.format("%04d", index + 1),
                            classe = userDoc.getString("classe") ?: "",
                            uid = userDoc.id
                        )
                    } ?: emptyList()
                    trySend(fallbackList)
                }
            }

        awaitClose { usersListener.remove() }
    }

    suspend fun addEtudiant(etudiant: Etudiant): Result<String> {
        return try {
            val docRef = etudiantsRef.add(etudiant.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEtudiant(etudiant: Etudiant): Result<Unit> {
        return try {
            etudiantsRef.document(etudiant.id).set(etudiant.toMap()).await()
            // Mettre également à jour la classe dans la collection users si nécessaire
            if (!etudiant.uid.isNullOrEmpty()) {
                usersRef.document(etudiant.uid).update(
                    mapOf(
                        "nom" to etudiant.nom,
                        "prenom" to etudiant.prenom,
                        "classe" to etudiant.classe
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEtudiant(etudiantId: String): Result<Unit> {
        return try {
            val notesSnapshot = notesRef.whereEqualTo("etudiantId", etudiantId).get().await()
            val batch = FirebaseRefs.db.batch()
            notesSnapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.delete(etudiantsRef.document(etudiantId))
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEtudiantByUid(uid: String): Result<Etudiant?> {
        return try {
            val snapshot = etudiantsRef.whereEqualTo("uid", uid).get().await()
            val doc = snapshot.documents.firstOrNull()
            if (doc != null) {
                val etudiant = Etudiant(
                    id = doc.id,
                    nom = doc.getString("nom") ?: "",
                    prenom = doc.getString("prenom") ?: "",
                    numeroEtudiant = doc.getString("numeroEtudiant") ?: "",
                    classe = doc.getString("classe") ?: "",
                    uid = doc.getString("uid")
                )
                Result.success(etudiant)
            } else {
                // Tenter de récupérer depuis 'users'
                val userDoc = usersRef.document(uid).get().await()
                if (userDoc.exists() && userDoc.getString("role") == "ETUDIANT") {
                    val etudiant = Etudiant(
                        id = uid,
                        nom = userDoc.getString("nom") ?: "",
                        prenom = userDoc.getString("prenom") ?: "",
                        numeroEtudiant = "ET-0001",
                        classe = userDoc.getString("classe") ?: "",
                        uid = uid
                    )
                    Result.success(etudiant)
                } else {
                    Result.success(null)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
