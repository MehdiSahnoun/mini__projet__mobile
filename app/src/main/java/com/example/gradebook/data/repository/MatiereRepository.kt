package com.example.gradebook.data.repository

import com.example.gradebook.data.firebase.FirebaseRefs
import com.example.gradebook.data.model.Matiere
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MatiereRepository {

    private val matieresRef = FirebaseRefs.matieresRef
    private val notesRef = FirebaseRefs.notesRef

    fun getMatieresFlow(): Flow<List<Matiere>> = callbackFlow {
        val listener = matieresRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                Matiere(
                    id = doc.id,
                    nom = doc.getString("nom") ?: "",
                    coefficient = doc.getDouble("coefficient") ?: 1.0
                )
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addMatiere(matiere: Matiere): Result<String> {
        return try {
            val docRef = matieresRef.add(matiere.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMatiere(matiere: Matiere): Result<Unit> {
        return try {
            matieresRef.document(matiere.id).set(matiere.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMatiere(matiereId: String): Result<Unit> {
        return try {
            val notesSnapshot = notesRef.whereEqualTo("matiereId", matiereId).get().await()
            val batch = FirebaseRefs.db.batch()
            notesSnapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.delete(matieresRef.document(matiereId))
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
